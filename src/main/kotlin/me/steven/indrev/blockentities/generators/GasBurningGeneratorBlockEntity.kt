package me.steven.indrev.blockentities.generators

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter
import alexiil.mc.lib.attributes.fluid.volume.FluidKey
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.api.machines.TransferMode
import me.steven.indrev.api.sideconfigs.ConfigurationType
import me.steven.indrev.components.FluidComponent
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.inventories.inventory
import me.steven.indrev.registry.IRFluidFuelRegistry
import me.steven.indrev.registry.IRItemRegistry
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.rawId
import me.steven.indrev.utils.use
import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos

class GasBurningGeneratorBlockEntity(pos: BlockPos, state: BlockState) : GeneratorBlockEntity(Tier.MK4, MachineRegistry.GAS_BURNING_GENERATOR_REGISTRY, pos, state) {

    init {
        this.temperatureComponent = TemperatureComponent(this, 0.3, 2200..2400, 2500)
        this.inventoryComponent = inventory(this) {
            coolerSlot = 0
            output {
                slot = 1
            }
        }
        this.fluidComponent = GasBurningGeneratorFluidComponent()
        this.propertiesSize = 9
    }
    private var burnTime = 0
    private var maxBurnTime = 0

    var generatingTicks = 0

    override fun machineTick() {
        super.machineTick()
        if (workingState) {
            generatingTicks++

            if (generatingTicks % 100 == 0 && world!!.random.nextDouble() < 0.6) {
                inventoryComponent!!.inventory.output(ItemStack(IRItemRegistry.SOOT))
            }
        }
    }

    override fun shouldGenerate(): Boolean {
        if (burnTime > 0) burnTime--
        else if (energyCapacity > energy) {
            val invFluid = fluidComponent!!.getInvFluid(0)
            val fluid = invFluid.rawFluid!!
            val key = invFluid.fluidKey
            if (invFluid.isEmpty || !IRFluidFuelRegistry.isFuel(fluid)) return false
            val fuel = IRFluidFuelRegistry.get(fluid)!!
            if (fluidComponent!!.use(key.withAmount(fuel.consumptionRatio))) {
                burnTime = fuel.burnTime
                maxBurnTime = burnTime
            }
        }

        return burnTime > 0 && energy < energyCapacity
    }

    override fun getGenerationRatio(): Double {
        val invFluid = fluidComponent!!.getInvFluid(0)
        val fluid = invFluid.rawFluid!!
        val modifier = if (temperatureComponent!!.isFullEfficiency()) config.temperatureBoost else 1.0
        return (IRFluidFuelRegistry.get(fluid)?.generationRatio?.toDouble() ?: 0.0) * modifier
    }

    override fun get(index: Int): Int {
        return when(index) {
            BURN_TIME_ID -> burnTime
            TOTAL_BURN_TIME_ID -> maxBurnTime
            FLUID_TANK_SIZE_ID -> fluidComponent!!.limit.asInt(1000)
            FLUID_TANK_AMOUNT_ID -> fluidComponent!![0].amount().asInt(1000)
            FLUID_TANK_FLUID_ID -> fluidComponent!![0].rawFluid.rawId
            else -> super.get(index)
        }
    }

    override fun getValidConfigurations(type: ConfigurationType): Array<TransferMode> {
        return when (type) {
            ConfigurationType.ITEM -> arrayOf(TransferMode.OUTPUT, TransferMode.NONE)
            ConfigurationType.FLUID -> arrayOf(TransferMode.INPUT, TransferMode.NONE)
            else -> super.getValidConfigurations(type)
        }
    }

    inner class GasBurningGeneratorFluidComponent : FluidComponent({ this }, FluidAmount.ofWhole(2), 1) {
        override fun isFluidValidForTank(tank: Int, fluid: FluidKey): Boolean {
            return IRFluidFuelRegistry.isFuel(fluid.rawFluid!!)
        }

        override fun getFilterForTank(tank: Int): FluidFilter {
            return FluidFilter { IRFluidFuelRegistry.isFuel(it.rawFluid!!) }
        }
    }

    companion object {
        const val BURN_TIME_ID = 4
        const val TOTAL_BURN_TIME_ID = 5
        const val FLUID_TANK_SIZE_ID = 6
        const val FLUID_TANK_AMOUNT_ID = 7
        const val FLUID_TANK_FLUID_ID = 8
    }
}