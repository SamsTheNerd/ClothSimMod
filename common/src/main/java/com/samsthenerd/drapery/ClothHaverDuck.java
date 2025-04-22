package com.samsthenerd.drapery;

import org.jetbrains.annotations.Nullable;

public interface ClothHaverDuck {

    void setMesh(@Nullable ClothMesh mesh);

    @Nullable
    ClothMesh getMesh();

    boolean isSimTicking();

    void setSimTicking(boolean shouldTick);
}
