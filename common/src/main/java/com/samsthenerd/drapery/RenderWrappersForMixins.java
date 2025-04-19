package com.samsthenerd.drapery;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.samsthenerd.drapery.ClothMesh.ClothParticle;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.util.DyeColor;
import org.joml.Vector3d;

public class RenderWrappersForMixins {
    public static void wrapBannerButHotswappable(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                                  int light, int overlay, ModelPart canvas, SpriteIdentifier baseSprite, boolean isBanner,
                                                  DyeColor color, BannerPatternsComponent patterns, Operation<Void> original){

        int w = 4;
        int h = 8;
        ClothMesh cmesh = new ClothMesh(w, h, (i,j) ->
            new ClothParticle(i + w * j, new Vector3d(20.0/16*(((float)i)/(w-1)-0.5),40.0/16*(((float)j)/(h-1))-2, -1.1/16), false, ((float)i)/w, ((float)j)/w));
        ClothMeshRenderer.renderMesh(cmesh, matrices, vertexConsumers, light, overlay);
        // nop?
    }
}
