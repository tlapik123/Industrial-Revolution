package me.steven.indrev.blockentities.solarpowerplant

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.crafters.CraftingMachineBlockEntity
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.components.fluid.FluidComponent
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Enhancer
import me.steven.indrev.recipes.machines.DistillerRecipe
import me.steven.indrev.recipes.machines.IRRecipeType
import me.steven.indrev.registry.MachineRegistry
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos

class DistillerBlockEntity(pos: BlockPos, state: BlockState) : CraftingMachineBlockEntity<DistillerRecipe>(Tier.MK4, MachineRegistry.DISTILLER_REGISTRY, pos, state) {

    override val enhancerSlots: IntArray = intArrayOf(3, 4, 5, 6)
    override val availableEnhancers: Array<Enhancer> = Enhancer.DEFAULT

    init {
        this.temperatureComponent = TemperatureComponent(this, 0.01, 70..120, 200)
        this.fluidComponent = FluidComponent(this, FluidAmount.BUCKET)
        this.inventoryComponent = inventory(this) {
            output { slot = 2 }
        }
    }

    override val type: IRRecipeType<DistillerRecipe> = DistillerRecipe.TYPE

    override fun getMaxCount(enhancer: Enhancer): Int {
        return if (enhancer == Enhancer.SPEED) return 2 else super.getMaxCount(enhancer)
    }
}