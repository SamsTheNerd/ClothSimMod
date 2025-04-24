package com.samsthenerd.drapery;

import net.minecraft.util.Pair;
import org.jblas.DoubleMatrix;
import org.jblas.Solve;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;


public class ClothMesh {

    // store our particles and any intrinsic properties they may have
    public final ArrayList<ClothParticle> particles;
    // store the current position of each particle as a squished column vector
    public final DoubleMatrix particlePosMat;
    public final DoubleMatrix particleVelMat;
    public final DoubleMatrix springStiffsMat;
    public final ArrayList<ClothFaceQuad> faces;
    // size of grid -- todo: should this like,, not be locked to squares?
    private final int pwidth;
    private final int pheight;

    private final DoubleMatrix wMat;

    public static final double DTIME = 1.0/60; // ig? idk
    public static final double STRETCH_LIMIT = 1.05; // only 10% allowance
    public static final double PARTICLE_MASS = 1;

    public ClothMesh(int w, int h, Function<Integer, ClothParticle> particleGenerator,
                     BiFunction<Integer, Integer, Double> stiffnessGenerator){
        pwidth = w;
        pheight = h;
        particles = new ArrayList<>(w*h);
        particlePosMat = new DoubleMatrix(w*h*3);
        particleVelMat = new DoubleMatrix(w*h*3);
        // make particles
        for(int j = 0; j < h; j++){
            for(int i = 0; i < w; i++){
                ClothParticle pij = particleGenerator.apply(i+j*w);
                particles.add(pij);
                particlePosMat.put(pij.pIndex*3, pij.origPos.x());
                particlePosMat.put(pij.pIndex*3+1, pij.origPos.y());
                particlePosMat.put(pij.pIndex*3+2, pij.origPos.z());
            }
        }
        // make faces (for easier rendering later)
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
        // gather spring stiffness into matrix TODO: memory inefficient probably
        springStiffsMat = new DoubleMatrix(w*h, w*h);
        for(int p1 = 0; p1 < w*h; p1++){
            for(int p2 = p1; p2 < w*h; p2++){
                if(p1 == p2) continue;
                double k = stiffnessGenerator.apply(p1,p2);
                springStiffsMat.put(p1, p2, k);
                springStiffsMat.put(p2, p1, k);
            }
        }
        // precompute W matrix for later
        DoubleMatrix hishMat = new DoubleMatrix(w*h, w*h);
        hishMat.copy(springStiffsMat);
        double dt2m = DTIME*DTIME/PARTICLE_MASS; // mass can just be 1 idk
        for(int p1 = 0; p1 < w*h; p1++){
            double kp2sum = 0;
            for(int p2 = 0; p2 < w*h; p2++){
                if(p1 == p2) continue;
                kp2sum += springStiffsMat.get(p1, p2);
            }
            hishMat.put(p1,p1, -kp2sum);
        }
        hishMat.mmuli(-dt2m);
        hishMat.addi(DoubleMatrix.eye(w*h));
        wMat = Solve.solveSymmetric(hishMat, DoubleMatrix.eye(w*h));
        hishMat = null;
    }

    public Vector3f getPositionFloat(int pIndex){
        return new Vector3f(
            (float)particlePosMat.get(pIndex *3),
            (float)particlePosMat.get(pIndex *3 + 1),
            (float)particlePosMat.get(pIndex *3 + 2)
        );
    }

    public Vector3d getPosition(int pIndex){
        return new Vector3d(
            particlePosMat.get(pIndex *3),
            particlePosMat.get(pIndex *3 + 1),
            particlePosMat.get(pIndex *3 + 2)
        );
    }

    public Vector3d getVelocity(int pIndex){
        return new Vector3d(
            particleVelMat.get(pIndex *3),
            particleVelMat.get(pIndex *3 + 1),
            particleVelMat.get(pIndex *3 + 2)
        );
    }

    public void setPosition(int pIndex, Vector3d pos){
        particlePosMat.put(pIndex *3, pos.x);
        particlePosMat.put(pIndex *3+1, pos.y);
        particlePosMat.put(pIndex *3+2, pos.z);
    }

    public void setVelocity(int pIndex, Vector3d pos){
        particleVelMat.put(pIndex *3, pos.x);
        particleVelMat.put(pIndex *3+1, pos.y);
        particleVelMat.put(pIndex *3+2, pos.z);
    }

    public Iterable<ClothFaceQuad> getFaces(){
        return faces;
    }

    // steps cloth sim forward by DTIME seconds
    // this is an implementation of Desbrun et al's Interactive animation of structured deformable objects
    // https://multires.caltech.edu/pubs/GI99.pdf
    public void stepClothSim(){
        Vector3d centerGrav = new Vector3d();
        // compute forces due to springs and stuff
        List<Vector3d> forces = new ArrayList<>(particles.size());
        for(int i = 0; i < particles.size(); i++){
            Vector3d iForce = new Vector3d();
            Vector3d xi = getPosition(i);
            Vector3d vi = getVelocity(i);
            centerGrav.add(xi);
            // TODO: inefficient to loop through everything but don't currently have a spring lookup thing
            for(int j = 0; j < particles.size(); j++){
                double kij = springStiffsMat.get(i,j);
                if(kij == 0) continue;
                Vector3d xj = getPosition(j);
                // PAPER IS WRONG!!!! This needs to be swapped from how they give it.
                Vector3d ijdiff = xj.sub(xi, new Vector3d());
                double springCoef = kij * (ijdiff.length()-(particles.get(i).origPos.distance(particles.get(j).origPos()))) / ijdiff.length();
                iForce.add(ijdiff.mul(springCoef));
                iForce.add(getVelocity(j).sub(vi).mul(kij*DTIME));
            }
            forces.add(iForce);
        }

        centerGrav.div(particles.size());

        Vector3d globTorque = new Vector3d();
        // integrate the approximation
        List<Vector3d> newPositions = new ArrayList<>(particles.size());
        Vector3d extForce = new Vector3d(0,-10,-0.5);
        for(int i = 0; i < particles.size(); i++) {
            Vector3d iForceFilt = new Vector3d();
            for(int j = 0; j < particles.size(); j++){
                iForceFilt.add(forces.get(j).mul(wMat.get(i,j), new Vector3d()));
            }
            Vector3d xi = getPosition(i);
            globTorque.add(iForceFilt.cross(xi, new Vector3d()));
            Vector3d vi = getVelocity(i);
            Vector3d vinew = vi.add(iForceFilt.add(extForce, new Vector3d()).mul(DTIME/PARTICLE_MASS));
            if(particles.get(i).fixedPos){
                newPositions.add(xi);
            } else {
                newPositions.add(xi.add(vinew.mul(DTIME, new Vector3d()), new Vector3d()));
            }
        }

        // just to test that stuff kinda maybe does something maybe?
//        for(int i = 0; i < particles.size(); i++){
//            Vector3d xi = getPosition(i);
//            Vector3d vi = getVelocity(i);
//            Vector3d vinew = vi.add(forces.get(i).add(new Vector3d(0,-1,0), new Vector3d()).mul(DTIME), new Vector3d());
//            if(particles.get(i).fixedPos){
//                newPositions.add(xi);
//            } else {
//                newPositions.add(xi.add(vinew.mul(DTIME, new Vector3d()), new Vector3d()));
//            }
//        }


        globTorque.mul(DTIME);
        for(int i = 0; i < particles.size(); i++){
            if(particles.get(i).fixedPos) continue;
            Vector3d fCorrec = (centerGrav.sub(getPosition(i), new Vector3d())).cross(globTorque);
            newPositions.get(i).add(fCorrec.mul(DTIME*DTIME/PARTICLE_MASS));
        }

        // overstretch correction
        int nbItr = 0;
        boolean mayNeedCorrection = true;
        while(nbItr < 20 && mayNeedCorrection){
            mayNeedCorrection = false;
            // TODO: looping through all is stupid but whatever
            for(int i = 0; i < particles.size(); i++){
                for(int j = 0; j < particles.size(); j++){
                    boolean iFixed = particles.get(i).fixedPos;
                    boolean jFixed = particles.get(j).fixedPos;
                    if(iFixed && jFixed) continue;
                    double kij = springStiffsMat.get(i,j);
                    if(kij == 0) continue;
                    Vector3d xi = newPositions.get(i);
                    Vector3d xj = newPositions.get(j);
                    Vector3d ijdiff = xi.sub(xj, new Vector3d()); // j to i
                    double origDist = particles.get(i).origPos.distance(particles.get(j).origPos());
                    double idealLength = origDist * STRETCH_LIMIT;
                    if(ijdiff.length() <= idealLength) continue;
                    if(iFixed){
                        // move only j
                        xi.add(ijdiff.mul(-idealLength/ijdiff.length()), xj);
                    } else if(jFixed){
                        // move only i
                        xj.add(ijdiff.mul(idealLength/ijdiff.length()), xi);
                    } else {
                        // move both
                        Vector3d ijMidp = xj.add(ijdiff.mul(0.5, new Vector3d()), new Vector3d());
                        ijMidp.add(ijdiff.mul( idealLength/(2*ijdiff.length()), new Vector3d()), xi);
                        ijMidp.add(ijdiff.mul(-idealLength/(2*ijdiff.length()), new Vector3d()), xj);
                    }
                    mayNeedCorrection = true;
                }
            }
            nbItr++;
        }

        // update real vals
        for(int i = 0; i < particles.size(); i++){
            if(particles.get(i).fixedPos) continue;
            Vector3d xiold = getPosition(i);
            Vector3d xinew = newPositions.get(i);
            Vector3d vinew = (xinew.sub(xiold, new Vector3d()).div(DTIME));
            setPosition(i, xinew);
            setVelocity(i, vinew);
        }
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

    public Vector3d calcFaceNormal(ClothFaceQuad face){
        var pos1 = getPosition(face.p1().pIndex);
        var v12 = getPosition(face.p2().pIndex).sub(pos1, new Vector3d());
        var v14 = getPosition(face.p4().pIndex).sub(pos1, new Vector3d());
        return v12.cross(v14).normalize();
    }

    public static Pair<Integer, Integer> indexToGridCoords(int idx, int w){
        return new Pair<>(idx % w, idx / w);
    }

    public static BiFunction<Integer, Integer, Double> makeSpringGenerator(
        double structStiff, double shearStiff, double flexStiff, int w){
        return (a, b) -> {
            var p1 = indexToGridCoords(a, w);
            var p2 = indexToGridCoords(b, w);
            int xdiff = Math.abs(p1.getLeft() - p2.getLeft());
            int ydiff = Math.abs(p1.getRight() - p2.getRight());
            if(xdiff == 0 && ydiff == 1 || xdiff == 1 && ydiff == 0) return structStiff;
            if(xdiff == 1 && ydiff == 1) return shearStiff;
            if(xdiff == 0 && ydiff == 2 || xdiff == 2 && ydiff == 0) return flexStiff;
            return 0.0;
        };
    }
}
