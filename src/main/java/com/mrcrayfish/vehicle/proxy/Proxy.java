package com.mrcrayfish.vehicle.proxy;

import com.mrcrayfish.vehicle.entity.EntityPoweredVehicle;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;

/**
 * Author: MrCrayfish
 */
public interface Proxy
{
    default void preInit() {}

    default void init() {}

    default void playVehicleSound(EntityPlayer player, EntityPoweredVehicle vehicle) {}

    default void openVehicleEditWindow(int entityId, int windowId) {}

    default void playSound(SoundEvent sound, BlockPos pos, float volume, float pitch) {};
}
