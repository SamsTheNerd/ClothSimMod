package com.samsthenerd.drapery;

import com.mojang.blaze3d.systems.RenderSystem;
import com.samsthenerd.drapery.ClothMesh.ClothFaceQuad;
import com.samsthenerd.drapery.ClothMesh.ClothParticle;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase.Lightmap;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Vector3d;
import org.joml.Vector3f;

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
}
