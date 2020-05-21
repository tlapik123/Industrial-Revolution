package me.steven.indrev

import me.steven.indrev.blocks.furnace.ElectricCraftingBlock
import me.steven.indrev.blocks.generators.GeneratorBlock
import me.steven.indrev.blocks.generators.GeneratorBlockEntity
import me.steven.indrev.gui.furnace.ElectricFurnaceController
import me.steven.indrev.gui.generators.CoalGeneratorController
import me.steven.indrev.recipes.PulverizerRecipe
import me.steven.indrev.registry.MachineRegistry
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.container.ContainerProviderRegistry
import net.minecraft.container.BlockContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.PacketByteBuf
import net.minecraft.util.registry.Registry
import team.reborn.energy.Energy
import team.reborn.energy.minecraft.EnergyModInitializer

class IndustrialRevolution : EnergyModInitializer() {
    override fun onInitialize() {
        super.onInitialize()
        Energy.registerHolder(GeneratorBlockEntity::class.java) { obj -> obj as GeneratorBlockEntity }
        MachineRegistry().registerAll()
        ContainerProviderRegistry.INSTANCE.registerFactory(GeneratorBlock.COAL_GENERATOR_SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            CoalGeneratorController(
                    syncId,
                    player.inventory,
                    BlockContext.create(player.world, buf.readBlockPos())
            )
        }

        ContainerProviderRegistry.INSTANCE.registerFactory(ElectricCraftingBlock.ELECTRIC_FURNACE_SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            ElectricFurnaceController(
                    syncId,
                    player.inventory,
                    BlockContext.create(player.world, buf.readBlockPos())
            )
        }

        Registry.register(Registry.RECIPE_TYPE, PulverizerRecipe.IDENTIFIER, PulverizerRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, PulverizerRecipe.IDENTIFIER, PulverizerRecipe.SERIALIZER)
    }

    companion object {
        const val MOD_ID = "indrev"

        val MOD_GROUP: ItemGroup = FabricItemGroupBuilder.build(identifier("indrev_group")) { ItemStack(MachineRegistry.COAL_GENERATOR_BLOCK_ITEM) }
    }
}