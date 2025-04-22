package com.samsthenerd.drapery.mixin;

import com.samsthenerd.drapery.ClothHaverDuck;
import com.samsthenerd.drapery.ClothMesh;
import net.minecraft.block.entity.BannerBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BannerBlockEntity.class)
public class MixinBannerAttachCloth implements ClothHaverDuck {
    @Unique
    private ClothMesh mesh;
    @Unique
    private boolean shouldSimTick = true;


    @Override
    public void setMesh(@Nullable ClothMesh mesh) {
        this.mesh = mesh;
    }

    @Override
    public @Nullable ClothMesh getMesh() {
        return this.mesh;
    }

    @Override
    public boolean isSimTicking() {
        return shouldSimTick;
    }

    @Override
    public void setSimTicking(boolean shouldTick) {
        this.shouldSimTick = shouldTick;
    }


}
