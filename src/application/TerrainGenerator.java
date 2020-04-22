package application;

import java.util.Random;
public class TerrainGenerator {
    private Random rand;
    float r;
    private float[][] map;
    public TerrainGenerator(Random rand, float r, int xDim, int yDim) {
        this.r = r / xDim;
        map = new float[xDim][yDim];
        this.rand = (rand==null) ? new Random() : rand;
    }

    public void init() {
        int xMax = map.length-1;
        int yMax = map[0].length-1;
        map[0][0] = rand.nextFloat()-.5f;
        map[0][yMax] = rand.nextFloat()-.5f;
        map[xMax][0] = rand.nextFloat()-.5f;
        map[xMax][yMax] = rand.nextFloat()-.5f;
        doTerrain(0, 0, xMax, yMax);
    }

    private float spread(float a, int b, int c) {
        return a + r * (float) (rand.nextGaussian()*(c-b));
    }

    private void doTerrain(int xMin, int yMin, int xMax, int yMax) {
    	//huge help from here: https://stackoverflow.com/questions/5531019/perlin-noise-in-java
    	//the terrain generation is not the main focus of the project, it is quite ugly even, but it helps with the genetic portion
    	//by allowing diversity from location and genes that affect those qualities
        int xMid = (xMin + xMax) / 2;
        int yMid = (yMin + yMax) / 2;
        if ((xMin == xMid) && (yMin == yMid)) return;
        map[xMid][yMin] = .5f*(map[xMin][yMin]+map[xMax][yMin]);
        map[xMid][yMax] = .5f*(map[xMin][yMax]+map[xMax][yMax]);
        map[xMin][yMid] = .5f*(map[xMin][yMin]+map[xMin][yMax]);
        map[xMax][yMid] = .5f*(map[xMax][yMin]+map[xMax][yMax]);
        float peak = spread(.5f *(map[xMid][yMin]+map[xMid][yMax]),xMin+yMin,yMax+xMax);
        map[xMid][yMid] = peak;
        map[xMid][yMin] = spread(map[xMid][yMin], xMin, xMax);
        map[xMid][yMax] = spread(map[xMid][yMax], xMin, xMax);
        map[xMin][yMid] = spread(map[xMin][yMid], yMin, yMax);
        map[xMax][yMid] = spread(map[xMax][yMid], yMin, yMax);
        doTerrain(xMin, yMin, xMid, yMid);
        doTerrain(xMid, yMin, xMax, yMid);
        doTerrain(xMin, yMid, xMid, yMax);
        doTerrain(xMid, yMid, xMax, yMax);
    }

    public float getNoise(int x, int y) {
    	return map[x][y];
    }
    
}
