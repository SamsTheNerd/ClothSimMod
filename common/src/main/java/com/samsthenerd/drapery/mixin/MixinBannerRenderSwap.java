package com.samsthenerd.drapery.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.samsthenerd.drapery.ClothMesh;
import com.samsthenerd.drapery.ClothMesh.ClothParticle;
import com.samsthenerd.drapery.ClothMeshRenderer;
import com.samsthenerd.drapery.RenderWrappersForMixins;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BannerBlockEntityRenderer;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.util.DyeColor;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BannerBlockEntityRenderer.class)
public class MixinBannerRenderSwap {
    @WrapOperation(
        method = "Lnet/minecraft/client/render/block/entity/BannerBlockEntityRenderer;render(Lnet/minecraft/block/entity/BannerBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
        at= @At(value="INVOKE", target="Lnet/minecraft/client/render/block/entity/BannerBlockEntityRenderer;renderCanvas(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/util/SpriteIdentifier;ZLnet/minecraft/util/DyeColor;Lnet/minecraft/component/type/BannerPatternsComponent;)V")
    )
    public void wrapBannerRendererInANiceCozyBlanket(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
         int light, int overlay, ModelPart canvas, SpriteIdentifier baseSprite, boolean isBanner,
         DyeColor color, BannerPatternsComponent patterns, Operation<Void> original, BannerBlockEntity bannerBE){

        if(bannerBE.getWorld() == null){
            original.call(matrices, vertexConsumers, light, overlay, canvas, baseSprite, isBanner, color, patterns);
            return; // item just leave it be?
        }

        RenderWrappersForMixins.wrapBannerButHotswappable(matrices, vertexConsumers, light, overlay, canvas,
            baseSprite, isBanner, color, patterns, original, bannerBE);
    }


}
