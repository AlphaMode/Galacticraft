/*
 * Copyright (c) 2019 HRZN LTD
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.hrznstudio.galacticraft.blocks.machines.refinery;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.FluidProviderItem;
import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil;
import alexiil.mc.lib.attributes.fluid.filter.ConstantFluidFilter;
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter;
import alexiil.mc.lib.attributes.fluid.impl.SimpleFixedFluidInv;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import alexiil.mc.lib.attributes.item.filter.ItemFilter;
import alexiil.mc.lib.attributes.misc.Ref;
import com.hrznstudio.galacticraft.Galacticraft;
import com.hrznstudio.galacticraft.api.block.entity.ConfigurableElectricMachineBlockEntity;
import com.hrznstudio.galacticraft.energy.GalacticraftEnergy;
import com.hrznstudio.galacticraft.entity.GalacticraftBlockEntities;
import com.hrznstudio.galacticraft.fluids.FuelFluid;
import com.hrznstudio.galacticraft.fluids.GalacticraftFluids;
import com.hrznstudio.galacticraft.tag.GalacticraftFluidTags;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.FishBucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tickable;

/**
 * @author <a href="https://github.com/StellarHorizons">StellarHorizons</a>
 */
public class RefineryBlockEntity extends ConfigurableElectricMachineBlockEntity implements Tickable {

    private static final ItemFilter[] SLOT_FILTERS;

    static {
        SLOT_FILTERS = new ItemFilter[3];
        SLOT_FILTERS[0] = GalacticraftEnergy.ENERGY_HOLDER_ITEM_FILTER;
        SLOT_FILTERS[1] = stack -> stack.getItem() instanceof FluidProviderItem;
        SLOT_FILTERS[2] = stack -> stack.getItem() instanceof FluidProviderItem;
    }

    private final SimpleFixedFluidInv fluidInv = new SimpleFixedFluidInv(2, FluidVolume.BUCKET * 10) {
        @Override
        public FluidFilter getFilterForTank(int tank) {
            if (tank == 0) {
                return fluidKey -> fluidKey.withAmount(FluidVolume.BUCKET).getRawFluid().matches(GalacticraftFluidTags.OIL);
            } else if (tank == 1) {
                return fluidKey -> fluidKey.withAmount(FluidVolume.BUCKET).getRawFluid().matches(GalacticraftFluidTags.FUEL);
            } else {
                return fluidKey -> false;
            }
        }

        @Override
        public boolean isFluidValidForTank(int tank, FluidKey fluid) {
            if (tank == 0) {
                return fluid.withAmount(FluidVolume.BUCKET).getRawFluid().matches(GalacticraftFluidTags.OIL);
            } else if (tank == 1) {
                return fluid.withAmount(FluidVolume.BUCKET).getRawFluid().matches(GalacticraftFluidTags.FUEL);
            } else {
                return false;
            }
        }
    };
    public RefineryStatus status = RefineryStatus.INACTIVE;

    public RefineryBlockEntity() {
        super(GalacticraftBlockEntities.REFINERY_TYPE);
    }

    @Override
    protected int getInvSize() {
        return 3;
    }

    @Override
    protected ItemFilter getFilterForSlot(int slot) {
        return SLOT_FILTERS[slot];
    }

    @Override
    public void tick() {
        if (world.isClient || !enabled()) {
            return;
        }

        attemptChargeFromStack(0);

        if (getInventory().getInvStack(1).getItem() instanceof FluidProviderItem) {
            Ref<ItemStack> ref = new Ref<>(getInventory().getInvStack(1));
            FluidVolume output = ((FluidProviderItem) getInventory().getInvStack(1).getItem()).drain(ref);
            if (output.getRawFluid().matches(GalacticraftFluidTags.OIL)) {
                this.fluidInv.getTank(0).insert(output);
                getInventory().setInvStack(1, ref.obj, Simulation.ACTION);
            }
        }

        if (getEnergyAttribute().getCurrentEnergy() <= 0) {
            status = RefineryStatus.INACTIVE;
            return;
        }

        if (!fluidInv.getInvFluid(0).isEmpty() && !(fluidInv.getInvFluid(1).getAmount() >= fluidInv.getMaxAmount(1))) {
            this.status = RefineryStatus.ACTIVE;
        } else {
            this.status = RefineryStatus.IDLE;
        }

        if (status == RefineryStatus.ACTIVE) {
            this.getEnergyAttribute().extractEnergy(GalacticraftEnergy.GALACTICRAFT_JOULES, 2, Simulation.ACTION);
            FluidVolume extracted = this.fluidInv.getTank(0).extract(10);
            this.fluidInv.getTank(1).insert(FluidVolume.create(GalacticraftFluids.FUEL, extracted.getAmount()));
        }

        if (getInventory().getInvStack(2).getItem() instanceof FluidProviderItem) {
            Ref<ItemStack> stackRef = new Ref<>(getInventory().getInvStack(2));
            Ref<FluidVolume> fluidRef = new Ref<>(fluidInv.getTank(1).attemptExtraction(ConstantFluidFilter.ANYTHING, FluidVolume.BUCKET, Simulation.ACTION));
            ((FluidProviderItem) getInventory().getInvStack(2).getItem()).fill(stackRef, fluidRef);
            if (stackRef.obj != getInventory().getInvStack(2)) {
                getInventory().setInvStack(2, stackRef.obj, Simulation.ACTION);
            }
            fluidInv.getTank(1).insert(fluidRef.obj);
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        tag.put("FluidInventory", fluidInv.toTag());
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        super.fromTag(tag);
        fluidInv.fromTag(tag.getCompound("FluidInventory"));
    }
}