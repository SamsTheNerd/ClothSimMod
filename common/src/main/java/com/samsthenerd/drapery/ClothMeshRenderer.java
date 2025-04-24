package com.samsthenerd.drapery;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.samsthenerd.drapery.ClothMesh.ClothFaceQuad;
import com.samsthenerd.drapery.ClothMesh.ClothParticle;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.RenderPhase.Lightmap;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.component.type.BannerPatternsComponent.Layer;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Pair;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class ClothMeshRenderer {

    // mostly just want to get a minimum working Something based around a gridded cloth for now
    public static void renderMesh(ClothMesh cmesh, MatrixStack matStack, VertexConsumerProvider vcp, int light, int overlay){

//        VertexConsumer vc = vcp.getBuffer(RenderLayer.getSolid());
//        VertexConsumer vc = vcp.getBuffer(RenderLayer.getLines());
//        RenderSystem.setShaderTexture(0, );

        RenderSystem.disableCull();
        // solid uses POSITION_COLOR_TEXTURE_LIGHT_NORMAL
        // entity solid uses POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
        // lines is POS_COLOR_NORMAL
        // debug quads is pos color
        boolean wireframe = false;
        for(ClothFaceQuad face : cmesh.getFaces()){

            if(wireframe){
                VertexConsumer vc = vcp.getBuffer(RenderLayer.getLines());
                RenderSystem.disableCull();
                for(var edge : face.getEdges()){
                    Vector3f posa = cmesh.getPositionFloat(edge.getLeft().pIndex()).mulPosition(matStack.peek().getPositionMatrix());
                    Vector3f posb = cmesh.getPositionFloat(edge.getRight().pIndex()).mulPosition(matStack.peek().getPositionMatrix());
                    vc.vertex(posa)
                        .color(0xFF_FFFFFF)
                        .normal(1,0,0);
                    vc.vertex(posb)
                        .color(0xFF_FFFFFF)
                        .normal(1,0,0);
                }
            } else {
                VertexConsumer vc = vcp.getBuffer(RenderLayer.getEntitySolid(DraperyMod.modLoc("textures/entity/white.png")));
                RenderSystem.disableCull();
                Vector3d norm = cmesh.calcFaceNormal(face);
                norm.mul(-1);
                for(ClothParticle p : face){
                    Vector3f pos = cmesh.getPositionFloat(p.pIndex()).mulPosition(matStack.peek().getPositionMatrix());
                    vc.vertex(pos)
                        .color(0xFF_FFFFFF)
                        .texture(p.u(), p.v())
                        .overlay(overlay)
                        .light(light)
                        .normal((float)norm.x, (float)norm.y, (float)norm.z);
                }
            }
        }
        vcp.getBuffer(RenderLayer.LINES);
        RenderSystem.enableCull();
    }



    public static void adHocBannerRenderer(ClothMesh cmesh, MatrixStack matStack, VertexConsumerProvider vcp, int light, int overlay,
                                           SpriteIdentifier baseSprite, DyeColor color, BannerPatternsComponent patterns, BannerBlockEntity bannerBE){

//        VertexConsumer vc = vcp.getBuffer(RenderLayer.getSolid());
//        VertexConsumer vc = vcp.getBuffer(RenderLayer.getLines());
//        RenderSystem.setShaderTexture(0, );

        RenderSystem.disableCull();
        // solid uses POSITION_COLOR_TEXTURE_LIGHT_NORMAL
        // entity solid uses POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
        // lines is POS_COLOR_NORMAL
        // debug quads is pos color

        List<Pair<SpriteIdentifier, DyeColor>> layers = new ArrayList<>();
        layers.add(new Pair<>(baseSprite, color));
        for(Layer lay : patterns.layers()){
            layers.add(new Pair<>(TexturedRenderLayers.getBannerPatternTextureId(lay.pattern()), lay.color()));
        }

        for(var lay : layers){
            Sprite patSprite = lay.getLeft().getSprite();
            VertexConsumer vc = lay.getLeft().getVertexConsumer(vcp, RenderLayer::getEntitySolid);
            for(ClothFaceQuad face : cmesh.getFaces()){
                RenderSystem.disableCull();
                Vector3d normD = cmesh.calcFaceNormal(face);
                Vector3f norm = new Vector3f((float)normD.x, (float)normD.y, (float)normD.z);
                norm.mul(-1);
                for(ClothParticle p : face){
                    Vector3f pos = cmesh.getPositionFloat(p.pIndex()).mulPosition(matStack.peek().getPositionMatrix());
                    pos.add(norm.mul(-0.001f, new Vector3f()));
                    vc.vertex(pos)
                        .color(lay.getRight().getEntityColor())
                        .texture(patSprite.getFrameU(p.u() * (24f/64)+(1f/64)), patSprite.getFrameV(p.v()*(24f/64)+(1f/64)))
//                        .texture(patSprite.getFrameU(p.u()) , patSprite.getFrameV(p.v())*(40f/64)+(1f/64))
//                        .texture(,
//                            patSprite.getMinV() + (patSprite.getMaxV() - patSprite.getMinV()) * p.v())
                        .overlay(overlay)
                        .light(light)
                        .normal((float)norm.x, (float)norm.y, (float)norm.z);
                }
            }
        }
        vcp.getBuffer(RenderLayer.LINES);
        RenderSystem.enableCull();
    }
}
