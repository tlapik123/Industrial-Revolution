package me.steven.indrev.blocks.furnace

import me.steven.indrev.blocks.ElectricBlockEntity
import me.steven.indrev.inventories.DefaultSidedInventory
import net.minecraft.block.BlockState
import net.minecraft.block.InventoryProvider
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.container.ArrayPropertyDelegate
import net.minecraft.container.PropertyDelegate
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.recipe.RecipeFinder
import net.minecraft.recipe.RecipeInputProvider
import net.minecraft.util.Tickable
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorld

abstract class ElectricCraftingBlockEntity(type: BlockEntityType<*>) : ElectricBlockEntity(type), Tickable, InventoryProvider, RecipeInputProvider {
    private val inventory = DefaultSidedInventory(2)
    var processingItem: Item? = null
    var output: ItemStack? = null
    var processTime: Int = 0
    var totalProcessTime: Int = 0

    override fun tick() {
        super.tick()
        if (world?.isClient == true) return
        val inputStack = inventory.getInvStack(0)
        val outputStack = inventory.getInvStack(1).copy()
        if (isProcessing()) {
            if (inputStack.item == processingItem) {
                processTime--
                if (!takeEnergy(1.0)) return
                if (processTime <= 0) {
                    inventory.setInvStack(0, inputStack.apply { count-- })
                    if (outputStack.item == output?.item)
                        inventory.setInvStack(1, outputStack.apply { increment(output?.count ?: 0) })
                    else if (outputStack.isEmpty)
                        inventory.setInvStack(1, output?.copy())
                }
            } else
                reset()
        } else if (getEnergy() > 0 && !inputStack.isEmpty && processTime <= 0) {
            reset()
            findRecipe(inventory)
        }
        propertyDelegate[2] = processTime
        propertyDelegate[3] = totalProcessTime
        markDirty()
    }

    abstract fun findRecipe(inventory: Inventory)

    private fun reset() {
        processTime = 0
        totalProcessTime = 0
        processingItem = null
        output = null
    }

    override fun createDelegate(): PropertyDelegate = ArrayPropertyDelegate(4)

    override fun getMaxInput(): Double = 1.0

    override fun getMaxOutput(): Double = 0.0

    private fun isProcessing() = processTime > 0 && getEnergy() > 0 && processingItem != null && output != null

    override fun fromTag(tag: CompoundTag?) {
        super.fromTag(tag)
        processTime = tag?.getInt("ProcessTime") ?: 0
        propertyDelegate[2] = processTime
        totalProcessTime = tag?.getInt("MaxProcessTime") ?: 0
        propertyDelegate[3] = totalProcessTime
        val tagList = tag?.get("Inventory") as ListTag? ?: ListTag()
        tagList.indices.forEach { i ->
            val stackTag = tagList.getCompound(i)
            val slot = stackTag.getInt("Slot")
            inventory.setInvStack(slot, ItemStack.fromTag(stackTag))
        }
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        tag?.putInt("ProcessTime", processTime)
        tag?.putInt("MaxProcessTime", totalProcessTime)
        val tagList = ListTag()
        for (i in 0 until inventory.invSize) {
            val stackTag = CompoundTag()
            stackTag.putInt("Slot", i)
            tagList.add(inventory.getInvStack(i).toTag(stackTag))
        }
        tag?.put("Inventory", tagList)
        return super.toTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag?) {
        super.fromClientTag(tag)
        processTime = tag?.getInt("ProcessTime") ?: 0
        propertyDelegate[2] = processTime
        totalProcessTime = tag?.getInt("MaxProcessTime") ?: 0
        propertyDelegate[3] = totalProcessTime
        val tagList = tag?.get("Inventory") as ListTag? ?: ListTag()
        tagList.indices.forEach { i ->
            val stackTag = tagList.getCompound(i)
            val slot = stackTag.getInt("Slot")
            inventory.setInvStack(slot, ItemStack.fromTag(stackTag))
        }
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        tag?.putInt("ProcessTime", processTime)
        tag?.putInt("MaxProcessTime", totalProcessTime)
        val tagList = ListTag()
        for (i in 0 until inventory.invSize) {
            val stackTag = CompoundTag()
            stackTag.putInt("Slot", i)
            tagList.add(inventory.getInvStack(i).toTag(stackTag))
        }
        tag?.put("Inventory", tagList)
        return super.toClientTag(tag)
    }

    override fun getInventory(state: BlockState?, world: IWorld?, pos: BlockPos?): SidedInventory = inventory

    override fun provideRecipeInputs(recipeFinder: RecipeFinder?) {
        for (i in 0 until inventory.invSize)
            recipeFinder?.addItem(inventory.getInvStack(i))
    }
}