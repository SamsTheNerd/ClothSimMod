package com.samsthenerd.drapery.neoforge;

import net.neoforged.fml.common.Mod;

import com.samsthenerd.drapery.DraperyMod;

@Mod(DraperyMod.MOD_ID)
public final class DraperyModNeoForge {
    public DraperyModNeoForge() {
        // Run our common setup.
        DraperyMod.init();
    }
}
