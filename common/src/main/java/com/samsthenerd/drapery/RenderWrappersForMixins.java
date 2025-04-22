package com.samsthenerd.drapery;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.samsthenerd.drapery.ClothMesh.ClothParticle;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.util.DyeColor;
import org.joml.Vector3d;

// this exists just to let me hotswap mixins. TODO: clean it up later
public class RenderWrappersForMixins {
    public static void wrapBannerButHotswappable(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                                 int light, int overlay, ModelPart canvas, SpriteIdentifier baseSprite, boolean isBanner,
                                                 DyeColor color, BannerPatternsComponent patterns, Operation<Void> original, BannerBlockEntity bannerBE){

        ClothMesh mesh = ((ClothHaverDuck)bannerBE).getMesh();

        int w = 5;
        int h = 10;

        if(mesh == null){
            mesh = new ClothMesh(w, h, (idx) -> {
                var p = ClothMesh.indexToGridCoords(idx, w);
                int i = p.getLeft(); int j = p.getRight();
                return new ClothParticle(i + w * j,
                    new Vector3d(20.0/16*(((float)i)/(w-1)-0.5),40.0/16*(1-((float)j)/(h-1))-0.5, -1.1/16),
                    j==0 && (i ==0 || i == w-1), ((float)i)/w, ((float)j)/w);
            }, ClothMesh.makeSpringGenerator(50, 20, 0, w));
            ((ClothHaverDuck)bannerBE).setMesh(mesh);
        }
        if(((ClothHaverDuck)bannerBE).isSimTicking()){
            mesh.stepClothSim();
        }

        matrices.push();
        matrices.scale(1,-1,1);
        ClothMeshRenderer.renderMesh(mesh, matrices, vertexConsumers, light, overlay);
        matrices.pop();
        // nop?
    }
}
