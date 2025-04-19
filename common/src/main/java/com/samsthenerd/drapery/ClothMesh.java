package com.samsthenerd.drapery;

import net.minecraft.util.Pair;
import org.jblas.DoubleMatrix;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;


public class ClothMesh {

    // store our particles and any intrinsic properties they may have
    public final ArrayList<ClothParticle> particles;
    // store the current position of each particle as a squished column vector
    public final DoubleMatrix particlePosMat;
    public final ArrayList<ClothFaceQuad> faces;
    // size of grid -- todo: should this like,, not be locked to squares?
    private final int pwidth;
    private final int pheight;


    public ClothMesh(int w, int h, BiFunction<Integer, Integer, ClothParticle> particleGenerator){
        pwidth = w;
        pheight = h;
        particles = new ArrayList<>(w*h);
        particlePosMat = new DoubleMatrix(w*h*3);
        for(int j = 0; j < h; j++){
            for(int i = 0; i < w; i++){
                ClothParticle pij = particleGenerator.apply(i,j);
                particles.add(pij);
                particlePosMat.put(pij.pIndex*3, pij.origPos.x());
                particlePosMat.put(pij.pIndex*3+1, pij.origPos.y());
                particlePosMat.put(pij.pIndex*3+2, pij.origPos.z());
            }
        }
        faces = new ArrayList<>();
        for(int j = 0; j < pheight-1; j++){
            for(int i = 0; i < pwidth-1; i++){
                faces.add(new ClothFaceQuad(
                   particles.get(i + w * j),
                   particles.get(i + w * (j+1)),
                   particles.get((i+1) + w * (j+1)),
                   particles.get((i+1) + w * (j))
                ));
            }
        }
    }

    public Vector3f getPositionFloat(int pIndex){
        return new Vector3f(
            (float)particlePosMat.get(pIndex *3),
            (float)particlePosMat.get(pIndex *3 + 1),
            (float)particlePosMat.get(pIndex *3 + 2)
        );
    }

    public Iterable<ClothFaceQuad> getFaces(){
        return faces;
    }

    // this is probably,, fine. todo: it should maybe include support for animatable locked positions and texture stuffs though
    public record ClothParticle(int pIndex, Vector3d origPos, boolean fixedPos, float u, float v){

    }

    public record ClothFaceQuad(ClothParticle p1, ClothParticle p2, ClothParticle p3, ClothParticle p4) implements Iterable<ClothParticle>{

        @NotNull
        @Override
        public Iterator<ClothParticle> iterator() {
            return List.of(p1,p2,p3,p4).iterator();
        }


        public List<Pair<ClothParticle, ClothParticle>> getEdges(){
            return List.of( new Pair<>(p1, p2), new Pair<>(p2, p3),
                    new Pair<>(p3, p4), new Pair<>(p4, p1));
        }
    }
}
