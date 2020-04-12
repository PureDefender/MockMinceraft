package fakeminceraft;

/**
 *
 * File: Chunk.java
 *
 * @author Luke Class: CS 4450-01 - Computer Graphics Assignment: Final Program
 * Date Last Modified:
 *
 * Purpose:
 */
import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.Random;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

public class Chunk {

    static final int CHUNK_SIZE = 50;
    static final int CUBE_LENGTH = 2;
    static final float PMIN = 0.04f;
    static final float PMAX = 0.06f;
    private BlockLoader[][][] BlocksArray;
    private int VBOVertexHandle;
    private int VBOColorHandle;
    private int VBOTextureHandle;
    private Texture texture;
    private Random r;

    public Chunk(int startX, int startY, int startZ) {
        try {
            texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("terrain.png"));
            System.out.println("Texture found");
        } catch (Exception e) {
            System.out.println("Could not find terrain.png");
        }
        r = new Random();
        BlocksArray = new BlockLoader[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_SIZE; y++) {
                for (int z = 0; z < CHUNK_SIZE; z++) {
                    if (r.nextFloat() > 0.7f) {
                        BlocksArray[x][y][z] = new BlockLoader(BlockLoader.BlockType.BlockType_Grass);
                    } else if (r.nextFloat() > 0.6f) {
                        BlocksArray[x][y][z] = new BlockLoader(BlockLoader.BlockType.BlockType_Dirt);
                    } else if (r.nextFloat() > 0.5f) {
                        BlocksArray[x][y][z] = new BlockLoader(BlockLoader.BlockType.BlockType_Stone);
                    } else if (r.nextFloat() > 0.4f) {
                        BlocksArray[x][y][z] = new BlockLoader(BlockLoader.BlockType.BlockType_Water);
                    } else if (r.nextFloat() > 0.3f) {
                        BlocksArray[x][y][z] = new BlockLoader(BlockLoader.BlockType.BlockType_Bedrock);
                    } else {
                        BlocksArray[x][y][z] = new BlockLoader(BlockLoader.BlockType.BlockType_Sand);
                    }
                    // TODO: support for all block types in enumeration

                }
            }
        }
        VBOTextureHandle = glGenBuffers();
        VBOColorHandle = glGenBuffers();
        VBOVertexHandle = glGenBuffers();
        rebuildMesh(startX, startY, startZ);
    }

    public void render() {
        glPushMatrix();
        glBindBuffer(GL_ARRAY_BUFFER, VBOVertexHandle);
        glVertexPointer(3, GL_FLOAT, 0, 0L);
        glBindBuffer(GL_ARRAY_BUFFER, VBOColorHandle);
        glColorPointer(3, GL_FLOAT, 0, 0L);
        glBindBuffer(GL_ARRAY_BUFFER, VBOTextureHandle);
        glBindTexture(GL_TEXTURE_2D, 1);
        glTexCoordPointer(2, GL_FLOAT, 0, 0L);
        glDrawArrays(GL_QUADS, 0, CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE * 24);
        glPopMatrix();
    }

    public void rebuildMesh(float startX, float startY, float startZ) {
        VBOColorHandle = glGenBuffers();
        VBOVertexHandle = glGenBuffers();
        VBOTextureHandle = glGenBuffers();

        double persistance = 0;
        while (persistance < PMIN) {
            persistance = PMAX * r.nextDouble();
        }
        int seed = (int) (50 * r.nextFloat());
        SimplexNoise noise = new SimplexNoise((int) CHUNK_SIZE, (double) persistance, (int) seed);

        FloatBuffer VertexPositionData = BufferUtils.createFloatBuffer((CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE) * 6 * 12);
        FloatBuffer VertexColorData = BufferUtils.createFloatBuffer((CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE) * 6 * 12);
        FloatBuffer VertexTextureData = BufferUtils.createFloatBuffer((CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE) * 6 * 12);

        float height;
        for (float x = 0; x < CHUNK_SIZE; x++) {
            for (float z = 0; z < CHUNK_SIZE; z++) {
                // Height randomized
                int i = (int) (startX + x * ((300 - startX) / 640));
                int j = (int) (startZ + z * ((300 - startZ) / 480));
                height = 19 + Math.abs((startY + (int) (190 * noise.getNoise(i, j)) * CUBE_LENGTH / 2));
                persistance = 0;
                for (float y = 0; y < height; y++) {
                    if (height >= CHUNK_SIZE) {
                        break;
                    }
                    BlocksArray[(int) x][(int) y][(int) z].setActive(true);
                    BlocksArray[(int) x][(int) y][(int) z].setType(BlockLoader.BlockType.BlockType_Stone);
                    while (persistance < PMIN) {
                        persistance = (PMAX) * r.nextFloat();
                    }
                }
            }
        }

        renderElements();

        for (float x = 0; x < CHUNK_SIZE; x++) {
            for (float z = 0; z < CHUNK_SIZE; z++) {
                for (float y = 0; y < CHUNK_SIZE; y++) {
                    if (BlocksArray[(int) (x)][(int) (y)][(int) (z)].active() && blockExposed((int) x, (int) y, (int) z)) {
                        VertexPositionData.put(createCube((float) (startX + x * CUBE_LENGTH), (float) (y * CUBE_LENGTH + (float) (CHUNK_SIZE * -1.0)), (float) (startZ + z * CUBE_LENGTH) + (float) (CHUNK_SIZE * 1.5)));
                        VertexColorData.put(createCubeVertexCol(getCubeColor(BlocksArray[(int) x][(int) y][(int) z])));
                        VertexTextureData.put(createTexCube((float) 0, (float) 0, BlocksArray[(int) (x)][(int) (y)][(int) (z)]));
                    }
                }
            }
        }
        VertexTextureData.flip();
        VertexColorData.flip();
        VertexPositionData.flip();
        glBindBuffer(GL_ARRAY_BUFFER, VBOVertexHandle);
        glBufferData(GL_ARRAY_BUFFER, VertexPositionData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ARRAY_BUFFER, VBOColorHandle);
        glBufferData(GL_ARRAY_BUFFER, VertexColorData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ARRAY_BUFFER, VBOTextureHandle);
        glBufferData(GL_ARRAY_BUFFER, VertexTextureData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    private float[] createCubeVertexCol(float[] CubeColorArray) {
        float[] cubeColors = new float[CubeColorArray.length * 4 * 6];
        for (int i = 0; i < cubeColors.length; i++) {
            cubeColors[i] = CubeColorArray[i % CubeColorArray.length];
        }
        return cubeColors;
    }

    public static float[] createCube(float x, float y, float z) {
        int offset = CUBE_LENGTH / 2;
        return new float[]{
            x + offset, y + offset, z,
            x - offset, y + offset, z,
            x - offset, y + offset, z - CUBE_LENGTH,
            x + offset, y + offset, z - CUBE_LENGTH,
            x + offset, y - offset, z - CUBE_LENGTH,
            x - offset, y - offset, z - CUBE_LENGTH,
            x - offset, y - offset, z,
            x + offset, y - offset, z,
            x + offset, y + offset, z - CUBE_LENGTH,
            x - offset, y + offset, z - CUBE_LENGTH,
            x - offset, y - offset, z - CUBE_LENGTH,
            x + offset, y - offset, z - CUBE_LENGTH,
            x + offset, y - offset, z,
            x - offset, y - offset, z,
            x - offset, y + offset, z,
            x + offset, y + offset, z,
            x - offset, y + offset, z - CUBE_LENGTH,
            x - offset, y + offset, z,
            x - offset, y - offset, z,
            x - offset, y - offset, z - CUBE_LENGTH,
            x + offset, y + offset, z,
            x + offset, y + offset, z - CUBE_LENGTH,
            x + offset, y - offset, z - CUBE_LENGTH,
            x + offset, y - offset, z};
    }

    private float[] getCubeColor(BlockLoader block) {
        return new float[]{1, 1, 1};
    }

    private static float[] createTexCube(float x, float y, BlockLoader block) {
        float offset = (1024f / 16) / 1024f;
        switch (block.getID()) {
            case 0:
                return texCubeHelper(x, y, offset, 3, 10, 4, 1, 3, 1);
            case 1:
                return texCubeHelper(x, y, offset, 3, 2, 3, 2, 3, 2);
            case 2:
                return texCubeHelper(x, y, offset, 15, 13, 15, 13, 15, 13);
            case 3:
                return texCubeHelper(x, y, offset, 3, 1, 3, 1, 3, 1);
            case 4:
                return texCubeHelper(x, y, offset, 2, 1, 2, 1, 2, 1);
            case 5:
                return texCubeHelper(x, y, offset, 2, 2, 2, 2, 2, 2);
//            case 6:
//                return texureBlock(x,y,offset,15,15,15,15,15,15);
//            case 7:
//                return texureBlock(x,y,offset,6,2,5,2,6,2);
//            case 8:
//                return texureBlock(x,y,offset,6,4,6,4,6,4);
//            case 9:
//                return texureBlock(x,y,offset,3,3,3,3,3,3);
//            case 10:
//                return texureBlock(x,y,offset,1,11,1,11,1,11);
//            case 11:
//                return texureBlock(x,y,offset,4,4,4,4,4,4);
//            case 12:
//                return texureBlock(x,y,offset,2,3,2,3,2,3);
//            case 13:
//                return texureBlock(x,y,offset,1,3,1,3,1,3);
//            case 14:
//                return texureBlock(x,y,offset,3,4,3,4,3,4);
            default:
                System.out.println("not found");
                return null;
        }
    }

    private static float[] texCubeHelper(float x, float y, float offset, int xTop, int yTop, int xSide, int ySide, int xBottom, int yBottom) {
        return new float[]{
            x + offset * xTop, y + offset * yTop,
            x + offset * (xTop - 1), y + offset * yTop,
            x + offset * (xTop - 1), y + offset * (yTop - 1),
            x + offset * xTop, y + offset * (yTop - 1),
            x + offset * xBottom, y + offset * yBottom,
            x + offset * (xBottom - 1), y + offset * yBottom,
            x + offset * (xBottom - 1), y + offset * (yBottom - 1),
            x + offset * xBottom, y + offset * (yBottom - 1),
            x + offset * xSide, y + offset * (ySide - 1),
            x + offset * (xSide - 1), y + offset * (ySide - 1),
            x + offset * (xSide - 1), y + offset * ySide,
            x + offset * xSide, y + offset * ySide,
            x + offset * xSide, y + offset * ySide,
            x + offset * (xSide - 1), y + offset * ySide,
            x + offset * (xSide - 1), y + offset * (ySide - 1),
            x + offset * xSide, y + offset * (ySide - 1),
            x + offset * xSide, y + offset * (ySide - 1),
            x + offset * (xSide - 1), y + offset * (ySide - 1),
            x + offset * (xSide - 1), y + offset * ySide,
            x + offset * xSide, y + offset * ySide,
            x + offset * xSide, y + offset * (ySide - 1),
            x + offset * (xSide - 1), y + offset * (ySide - 1),
            x + offset * (xSide - 1), y + offset * ySide,
            x + offset * xSide, y + offset * ySide};
    }

    private boolean blockExposed(int x, int y, int z) {
        try {
            if (!BlocksArray[x][y][z].active()) {
                return false;
            }
            if (!BlocksArray[x + 1][y][z].active()) {
                return true;
            }
            if (!BlocksArray[x - 1][y][z].active()) {
                return true;
            }
            if (!BlocksArray[x][y + 1][z].active()) {
                return true;
            }
            if (!BlocksArray[x][y - 1][z].active()) {
                return true;
            }
            if (!BlocksArray[x][y][z + 1].active()) {
                return true;
            }
            if (!BlocksArray[x][y][z - 1].active()) {
                return true;
            }
        } catch (IndexOutOfBoundsException e) {
            return true;
        }
        return false;
    }

    private void renderElements() {
        renderDirtLayer();
        renderSandWater();
        renderBedrockLayer();
        renderBedrock();
    }

    private void renderDirtLayer() {
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                int y = 0;
                while (y < CHUNK_SIZE - 1 && BlocksArray[x][y][z].active()) {
                    y++;
                }
                y--;
                if (y > 2) {
                    BlocksArray[x][y][z].setType(BlockLoader.BlockType.BlockType_Grass);
                    BlocksArray[x][y - 1][z].setType(BlockLoader.BlockType.BlockType_Dirt);
                    BlocksArray[x][y - 2][z].setType(BlockLoader.BlockType.BlockType_Dirt);
                }
            }
        }
    }

    private void renderBedrockLayer() {
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                BlocksArray[x][0][z].setType(BlockLoader.BlockType.BlockType_Bedrock);
            }
        }
    }

    private void renderBedrock() {
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                for (int y = 1; y < 4; y++) {
                    if (Math.random() < 1 - (double) (y) / 4) {
                        BlocksArray[x][y][z].setType(BlockLoader.BlockType.BlockType_Bedrock);
                    }
                }
            }
        }
    }

    public void renderSandWater() {
        Vector3f start = startWater();
        int x = (int) start.x;
        int y = (int) start.y;
        int z = (int) start.z;
        makeWater(x, y, z);
        makeSand(y);
    }

    public void makeSand(int yStart) {
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                try {
                    if (BlocksArray[x][yStart][z].getType() == BlockLoader.BlockType.BlockType_Water) {
                        try {
                            if (BlocksArray[x + 1][yStart][z].getType() != BlockLoader.BlockType.BlockType_Water) {
                                BlocksArray[x + 1][yStart][z].setType(BlockLoader.BlockType.BlockType_Sand);
                            }
                        } catch (IndexOutOfBoundsException e) {
                        }
                        try {
                            if (BlocksArray[x - 1][yStart][z].getType() != BlockLoader.BlockType.BlockType_Water) {
                                BlocksArray[x - 1][yStart][z].setType(BlockLoader.BlockType.BlockType_Sand);
                            }
                        } catch (IndexOutOfBoundsException e) {
                        }
                        try {
                            if (BlocksArray[x][yStart][z + 1].getType() != BlockLoader.BlockType.BlockType_Water) {
                                BlocksArray[x][yStart][z + 1].setType(BlockLoader.BlockType.BlockType_Sand);
                            }
                        } catch (IndexOutOfBoundsException e) {
                        }
                        try {
                            if (BlocksArray[x][yStart][z - 1].getType() != BlockLoader.BlockType.BlockType_Water) {
                                BlocksArray[x][yStart][z - 1].setType(BlockLoader.BlockType.BlockType_Sand);
                            }
                        } catch (IndexOutOfBoundsException e) {
                        }
                        try {
                            if (!BlocksArray[x + 1][yStart + 2][z].active()) {
                                BlocksArray[x + 1][yStart + 1][z].setActive(false);
                            }
                        } catch (IndexOutOfBoundsException e) {
                        }
                        try {
                            if (!BlocksArray[x - 1][yStart + 1][z].active()) {
                                BlocksArray[x - 1][yStart + 1][z].setActive(false);
                            }
                        } catch (IndexOutOfBoundsException e) {
                        }
                        try {
                            if (!BlocksArray[x][yStart + 1][z + 1].active()) {
                                BlocksArray[x][yStart + 1][z + 1].setActive(false);
                            }
                        } catch (IndexOutOfBoundsException e) {
                        }
                        try {
                            if (!BlocksArray[x][yStart + 1][z - 1].active()) {
                                BlocksArray[x][yStart + 1][z - 1].setActive(false);
                            }
                        } catch (IndexOutOfBoundsException e) {
                        }
                        BlocksArray[x][yStart - 1][z].setType(BlockLoader.BlockType.BlockType_Sand);
                    }
                } catch (IndexOutOfBoundsException e) {
                }
            }
        }
    }

    public Vector3f startWater() {
        LinkedList<Vector3f> positions = new LinkedList<>();
        int minY = CHUNK_SIZE - 1;
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                int y = 0;
                while (BlocksArray[x][y][z].getType() != BlockLoader.BlockType.BlockType_Grass && y < CHUNK_SIZE - 1) {
                    y++;
                }
                if (y < minY) {
                    minY = y;
                    positions.clear();
                    positions.add(new Vector3f(x, y, z));
                } else if (y == minY) {
                    positions.add(new Vector3f(x, y, z));
                }
            }
        }
        int rand = (int) (Math.random() * positions.size());
        return positions.get(rand);
    }

    public void makeWater(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 || x >= CHUNK_SIZE || y >= CHUNK_SIZE || z >= CHUNK_SIZE) {
            return;
        }
        if (BlocksArray[x][y][z].getType() == BlockLoader.BlockType.BlockType_Grass) {
            BlocksArray[x][y][z].setType(BlockLoader.BlockType.BlockType_Water);
            makeWater(x + 1, y, z);
            makeWater(x - 1, y, z);
            makeWater(x, y, z + 1);
            makeWater(x, y, z - 1);
        }
    }
}
