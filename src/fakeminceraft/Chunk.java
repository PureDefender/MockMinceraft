package fakeminceraft;

/**
 *
 * File: Chunk.java
 *
 * @author Luke, Sydney
 * Class: CS 4450-01 - Computer Graphics 
 * Assignment: Final Program
 * Date Last Modified: 4/13/2020
 *
 * Purpose: Renders the chunk
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

public final class Chunk {

    static final int CHUNK_SIZE = 50;
    static final int CUBE_LENGTH = 2;
    static final float PMIN = 0.04f;
    static final float PMAX = 0.06f;
    private final BlockLoader[][][] BlocksArray;
    private int VBOVertexHandle;
    private int VBOColorHandle;
    private int VBOTextureHandle;
    private Texture texture;
    private final Random r;

    /**
     * Constructor for Chunk object
     *
     * @param startX x coordinates of starting value for chunk
     * @param startY y-coordinates of starting value for chunk
     * @param startZ z-coordinates of starting value for chunk
     */
    public Chunk(int startX, int startY, int startZ) {
        try {
            // Looks for terrain.png texture file in base directory of project
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
                }
            }
        }
        VBOTextureHandle = glGenBuffers();
        VBOColorHandle = glGenBuffers();
        VBOVertexHandle = glGenBuffers();
        rebuildMesh(startX, startY, startZ);
    }

    /**
     * Renders the chunk
     */
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

    /**
     * Rebuilds the world
     *
     * @param startX starting x coordinate
     * @param startY starting y coordinate
     * @param startZ starting z coordinate
     */
    public void rebuildMesh(float startX, float startY, float startZ) {
        VBOColorHandle = glGenBuffers();
        VBOVertexHandle = glGenBuffers();
        VBOTextureHandle = glGenBuffers();

        // Persistance value randomized for simplex noise generation
        double persistance = 0;
        // Make sure persistance isn't too high or low to have smoother valleys/mountains
        while (persistance < PMIN) {
            persistance = PMAX * r.nextDouble();
        }
        // Using a random number generator for seed
        SimplexNoise noise = new SimplexNoise((int) CHUNK_SIZE, (double) persistance, r.nextInt());

        FloatBuffer VertexPositionData = BufferUtils.createFloatBuffer((CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE) * 6 * 12);
        FloatBuffer VertexColorData = BufferUtils.createFloatBuffer((CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE) * 6 * 12);
        FloatBuffer VertexTextureData = BufferUtils.createFloatBuffer((CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE) * 6 * 12);

        float height;
        for (float x = 0; x < CHUNK_SIZE; x++) {
            for (float z = 0; z < CHUNK_SIZE; z++) {
                // Height randomized
                int i = (int) (startX + x * ((300 - startX) / 640));
                int j = (int) (startZ + z * ((300 - startZ) / 480));
                height = 15 + Math.abs((startY + (int) (130 * noise.getNoise(i, j)) * CUBE_LENGTH / 2));
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

        // Renders all block types in the chunk
        renderElements();

        for (float x = 0; x < CHUNK_SIZE; x++) {
            for (float z = 0; z < CHUNK_SIZE; z++) {
                for (float y = 0; y < CHUNK_SIZE; y++) {
                    if (BlocksArray[(int) (x)][(int) (y)][(int) (z)].active() && blockExposed((int) x, (int) y, (int) z)) {
                        VertexPositionData.put(createCube((float) (startX + x * CUBE_LENGTH) + (float) (CHUNK_SIZE * -1.0), (float) (y * CUBE_LENGTH + (float) (CHUNK_SIZE * -1.0)), (float) (startZ + z * CUBE_LENGTH) - (float) (CHUNK_SIZE * 1.0)));
                        VertexColorData.put(new float[]{1, 1, 1});
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

    /**
     * Deprecated
     * Used to color in the cubes
     * @param CubeColorArray Column array
     * @return Array of cube colors
     */
    private float[] createCubeVertexCol(float[] CubeColorArray) {
        float[] cubeColors = new float[CubeColorArray.length * 4 * 6];
        for (int i = 0; i < cubeColors.length; i++) {
            cubeColors[i] = CubeColorArray[i % CubeColorArray.length];
        }
        return cubeColors;
    }

    /**
     * Creates a cube
     * @param x x coordinate of cube
     * @param y y coordinate of cube
     * @param z z coordinate of cube
     * @return array of cube values
     */
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

    /**
     * Deprecated
     * Colors a cube's faces
     * @param block colored block
     * @return a newly colored block
     */
    private float[] getCubeColor(BlockLoader block) {
        return new float[]{1, 1, 1};
    }

    /**
     * Applies textures to a cube
     * @param x x coordinate of cube
     * @param y y coordinate of cube
     * @param block the block to be textured
     * @return returns an array of the textured cube values
     */
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
            case 6:
                return texCubeHelper(x, y, offset, 15, 15, 15, 15, 15, 15);
            case 7:
                return texCubeHelper(x, y, offset, 6, 2, 5, 2, 6, 2);
            case 8:
                return texCubeHelper(x, y, offset, 6, 4, 6, 4, 6, 4);
            case 9:
                return texCubeHelper(x, y, offset, 3, 3, 3, 3, 3, 3);
            case 10:
                return texCubeHelper(x, y, offset, 1, 11, 1, 11, 1, 11);
            case 11:
                return texCubeHelper(x, y, offset, 4, 4, 4, 4, 4, 4);
            case 12:
                return texCubeHelper(x, y, offset, 2, 3, 2, 3, 2, 3);
            case 13:
                return texCubeHelper(x, y, offset, 1, 3, 1, 3, 1, 3);
            case 14:
                return texCubeHelper(x, y, offset, 3, 4, 3, 4, 3, 4);
            default:
                System.out.println("not found");
                return null;
        }
    }

    /**
     * Helper function for the texture cube creator
     * @param x x coordinate
     * @param y y coordinate
     * @param offset offset needed for the cube
     * @param xTop
     * @param yTop
     * @param xSide
     * @param ySide
     * @param xBottom
     * @param yBottom
     * @return
     */
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

    /**
     * Checks if a block is exposed
     * @param x x coordinate of the block to be checked
     * @param y y coordinate of the block to be checked
     * @param z z coordinate of the block to be checked
     * @return True if the block is exposed, false otherwise
     */
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

    /**
     * Calls each method to check for layers of texture to apply
     */
    private void renderElements() {
        renderDirtLayer();
        renderSandWater();
        renderBedrockLayer();
        renderBedrock();
        renderLava();
        renderCoal();
        renderRedStone();
        renderLapis();
        renderIronOre();
        renderDiamondOre();
        renderGoldOre();
        renderTrees();
    }

    /**
     * Creates the dirt layer on the top of the chunk (grass layer on top, dirt
     * goes down 2 layers under grass)
     */
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

    /**
     * Makes the bottom layer of the chunk solid bedrock
     */
    private void renderBedrockLayer() {
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                BlocksArray[x][0][z].setType(BlockLoader.BlockType.BlockType_Bedrock);
            }
        }
    }

    /**
     * Randomly generates bedrock from layers 1 to 4 (like regular Minecraft)
     */
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

    /**
     * Generates where sand and water should be rendered (in valleys)
     */
    private void renderSandWater() {
        Vector3f start = startWater();
        int x = (int) start.x;
        int y = (int) start.y;
        int z = (int) start.z;
        makeWater(x, y, z);
        makeSand(y);
    }

    /**
     * Method checks for water and extends the sand patch
     * @param yStart Level at which to check for water
     */
    private void makeSand(int yStart) {
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

    /**
     * Finds where water starts (in valley)
     * @return a value where water starts
     */
    private Vector3f startWater() {
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

    /**
     * Creates a water patch to be rendered
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     */
    private void makeWater(int x, int y, int z) {
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

    /**
     * Extra feature 1: Creates an underground layer of lava flowing
     */
    private void renderLava() {
        // Uses simplex noise generation to create areas underground where lava should spawn
        float persistance = 0;
        while (persistance < PMIN) {
            persistance = (PMAX) * r.nextFloat();
        }
        int seed = (int) (50 * r.nextFloat());
        SimplexNoise noise = new SimplexNoise(CHUNK_SIZE, persistance, seed);
        double temp = 0;
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                temp = noise.getNoise(x, z);
                if (Math.abs(temp) < 0.02) {
                    BlocksArray[x][6][z].setType(BlockLoader.BlockType.BlockType_Lava);
                    BlocksArray[x][7][z].setActive(false);
                }
            }
        }
    }

    /**
     * Spawns coal ore chunks around the map
     */
    private void renderCoal() {
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                for (int y = 1; y < 19; y++) {
                    // Spawns very often
                    if (Math.random() < 0.015) {
                        renderOreChunk(x, y, z, BlockLoader.BlockType.BlockType_Coal, 10);
                    }
                }
            }
        }
    }

    /**
     * Spawns Lapis Lazuli ore chunks
     */
    private void renderLapis() {
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                for (int y = 1; y < 19; y++) {
                    // Not as rare as diamond, spawns sparsely
                    if (Math.random() < 0.008) {
                        renderOreChunk(x, y, z, BlockLoader.BlockType.BlockType_Lapis, 6);
                    }
                }
            }
        }
    }

    /**
     * Spawns redstone ore chunks
     */
    private void renderRedStone() {
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                for (int y = 1; y < 19; y++) {
                    // Same spawn rate as gold
                    if (Math.random() < 0.01) {
                        renderOreChunk(x, y, z, BlockLoader.BlockType.BlockType_Redstone, 6);
                    }
                }
            }
        }
    }

    /**
     * Spawns iron ore chunks
     */
    private void renderIronOre() {
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                for (int y = 1; y < 19; y++) {
                    // Spawns often
                    if (Math.random() < 0.014) {
                        renderOreChunk(x, y, z, BlockLoader.BlockType.BlockType_IronOre, 8);
                    }
                }
            }
        }
    }

    /**
     * Renders gold ore randomly underground
     */
    private void renderGoldOre() {
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                for (int y = 1; y < 15; y++) {
                    // Spawns less often than iron
                    if (Math.random() < 0.01) {
                        renderOreChunk(x, y, z, BlockLoader.BlockType.BlockType_GoldOre, 6);
                    }
                }
            }
        }
    }

    /**
     * Renders diamond ore randomly underground (rare)
     */
    private void renderDiamondOre() {
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                for (int y = 1; y < 7; y++) {
                    // Spawns rarely
                    if (Math.random() < 0.003) {
                        renderOreChunk(x, y, z, BlockLoader.BlockType.BlockType_DiamondOre, 4);
                    }
                }
            }
        }
    }

    /**
     * Extra feature 2
     * Renders a small chunk of a given ore type and number of ores to spawn
     * @param x x coordinate of ore chunk to spawn around
     * @param y y coordinate of ore chunk to spawn around
     * @param z z coordinate of ore chunk to spawn around
     * @param type Ore type to be spawned
     * @param numOres Number of ores to be spawned
     */
    private void renderOreChunk(int x, int y, int z, BlockLoader.BlockType type, int numOres) {
        if (numOres == 0 || x < 0 || y < 0 || z < 0 || x >= CHUNK_SIZE || y >= CHUNK_SIZE || z >= CHUNK_SIZE) {
            return;
        }
        if (BlocksArray[x][y][z].getType() == BlockLoader.BlockType.BlockType_Stone) {
            BlocksArray[x][y][z].setType(type);
            double rand = Math.random();
            if (rand < 1.0 / 6) {
                renderOreChunk(x - 1, y, z, type, numOres - 1);
            } else if (rand < 2.0 / 6) {
                renderOreChunk(x + 1, y, z, type, numOres - 1);
            } else if (rand < 3.0 / 6) {
                renderOreChunk(x, y - 1, z, type, numOres - 1);
            } else if (rand < 4.0 / 6) {
                renderOreChunk(x, y + 1, z, type, numOres - 1);
            } else if (rand < 5.0 / 6) {
                renderOreChunk(x, y, z - 1, type, numOres - 1);
            } else {
                renderOreChunk(x, y, z + 1, type, numOres - 1);
            }
        }
    }

    /**
     * Extra Feature 3
     * Creates trees that cannot spawn too close to each other with varying
     * leaf patterns
     */
    public void renderTrees() {
        LinkedList<Vector3f> positions = getTrees();
        for (int i = 0; i < positions.size(); i++) {
            plantTree((int) positions.get(i).x, (int) positions.get(i).y, (int) positions.get(i).z);
        }
    }

    /**
     * Plants a tree with a specific pattern
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     */
    private void plantTree(int x, int y, int z) {
        // Finds a random height for a tree
        int randomHeight = (int) (Math.random() * 5 + 4);
        for (int i = 1; i <= randomHeight; i++) {
            placeLeaf(x, y + i, z, BlockLoader.BlockType.BlockType_Wood);
        }
        int randomTreePatern = (int) (Math.random() * 3);
        // Three tree patterns (for leaves)
        // Each leaf is placed individually
        switch (randomTreePatern) {
            case 0:
                placeLeaf(x + 1, y + randomHeight, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 1, y + randomHeight, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight, z + 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight, z - 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x + 1, y + randomHeight, z + 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 1, y + randomHeight, z + 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x + 1, y + randomHeight, z - 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 1, y + randomHeight, z - 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x + 1, y + randomHeight + 1, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 1, y + randomHeight + 1, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight + 1, z + 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight + 1, z - 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x + 1, y + randomHeight - 1, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 1, y + randomHeight - 1, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight - 1, z + 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight - 1, z - 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x + 2, y + randomHeight, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 2, y + randomHeight, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight, z + 2, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight, z - 2, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight + 1, z, BlockLoader.BlockType.BlockType_Leaves);
                break;
            case 1:
                placeLeaf(x + 1, y + randomHeight, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 1, y + randomHeight, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight, z + 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight, z - 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x + 1, y + randomHeight, z + 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 1, y + randomHeight, z + 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x + 1, y + randomHeight, z - 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 1, y + randomHeight, z - 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x + 1, y + randomHeight + 2, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 1, y + randomHeight + 2, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight + 2, z + 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight + 2, z - 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x + 1, y + randomHeight + 1, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 1, y + randomHeight + 1, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight + 1, z + 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight + 1, z - 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x + 1, y + randomHeight + 1, z + 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 1, y + randomHeight + 1, z + 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x + 1, y + randomHeight + 1, z - 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 1, y + randomHeight + 1, z - 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x + 1, y + randomHeight - 1, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 1, y + randomHeight - 1, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight - 1, z + 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight - 1, z - 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x + 2, y + randomHeight + 1, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 2, y + randomHeight + 1, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight + 1, z + 2, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight + 1, z - 2, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x + 2, y + randomHeight, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 2, y + randomHeight, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight, z + 2, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight, z - 2, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight + 1, z, BlockLoader.BlockType.BlockType_Leaves);
                break;
            case 2:
                placeLeaf(x + 1, y + randomHeight, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 1, y + randomHeight, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight, z + 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight, z - 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x + 1, y + randomHeight, z + 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 1, y + randomHeight, z + 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x + 1, y + randomHeight, z - 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 1, y + randomHeight, z - 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 2, y + randomHeight, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x + 2, y + randomHeight, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 2, y + randomHeight, z - 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x + 2, y + randomHeight, z + 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 2, y + randomHeight, z + 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x + 2, y + randomHeight, z - 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight, z + 2, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight, z - 2, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 1, y + randomHeight, z + 2, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 1, y + randomHeight, z - 2, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x + 1, y + randomHeight, z + 2, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x + 1, y + randomHeight, z - 2, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x + 1, y + randomHeight + 2, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 1, y + randomHeight + 2, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight + 2, z + 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight + 2, z - 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x + 1, y + randomHeight + 2, z + 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 1, y + randomHeight + 2, z + 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x + 1, y + randomHeight + 2, z - 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 1, y + randomHeight + 2, z - 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x + 1, y + randomHeight + 3, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 1, y + randomHeight + 3, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight + 3, z + 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight + 3, z - 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight + 3, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x + 1, y + randomHeight + 1, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 1, y + randomHeight + 1, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight + 1, z + 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight + 1, z - 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x + 1, y + randomHeight + 1, z + 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 1, y + randomHeight + 1, z + 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x + 1, y + randomHeight + 1, z - 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 1, y + randomHeight + 1, z - 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 2, y + randomHeight + 1, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x + 2, y + randomHeight + 1, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 2, y + randomHeight + 1, z - 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x + 2, y + randomHeight + 1, z + 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 2, y + randomHeight + 1, z + 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x + 2, y + randomHeight + 1, z - 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight + 1, z + 2, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight + 1, z - 2, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 1, y + randomHeight + 1, z + 2, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 1, y + randomHeight + 1, z - 2, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x + 1, y + randomHeight + 1, z + 2, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x + 1, y + randomHeight + 1, z - 2, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x + 1, y + randomHeight - 1, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 1, y + randomHeight - 1, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight - 1, z + 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight - 1, z - 1, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x + 2, y + randomHeight + 2, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 2, y + randomHeight + 2, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight + 2, z + 2, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight + 2, z - 2, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x + 2, y + randomHeight + 1, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 2, y + randomHeight + 1, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight + 1, z + 2, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight + 1, z - 2, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x + 2, y + randomHeight, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x - 2, y + randomHeight, z, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight, z + 2, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight, z - 2, BlockLoader.BlockType.BlockType_Leaves);
                placeLeaf(x, y + randomHeight + 1, z, BlockLoader.BlockType.BlockType_Leaves);
                break;
            default:
                break;
        }
    }

    /**
     * Places blocks one at a time (helper for leaves)
     * @param x x coordinate of leaf block
     * @param y y coordinate of leaf block
     * @param z z coordinate of leaf block
     * @param type type of block to be placed (should be leaves)
     */
    private void placeLeaf(int x, int y, int z, BlockLoader.BlockType type) {
        try {
            BlocksArray[x][y][z].setActive(true);
            BlocksArray[x][y][z].setType(type);
        } catch (IndexOutOfBoundsException e) {
        }
    }

    /**
     * Creates a list of random size with location of trees to be planted
     * @return a LinkedList with the location of trees to be planted
     */
    private LinkedList<Vector3f> getTrees() {
        LinkedList<Vector3f> p = new LinkedList<>();
        int numAttempts = (int) (3 + (Math.random() * 30));
        for (int i = 0; i < numAttempts; i++) {
            int x = (int) (Math.random() * CHUNK_SIZE);
            int z = (int) (Math.random() * CHUNK_SIZE);
            // Ensures that the trees are planted on the surface
            int y = getYMax(x, z);
            if (BlocksArray[x][y][z].getType() == BlockLoader.BlockType.BlockType_Grass && checkDistance(x, z, p)) {
                p.add(new Vector3f(x, y, z));
            }

        }
        return p;
    }

    /**
     * Checks for distance between trees to prevent tree collision
     * @param x x coordinate of a tree
     * @param z z coordinate of a tree
     * @param positions Positions of trees
     * @return false if there is collision, true otherwise
     */
    private boolean checkDistance(int x, int z, LinkedList<Vector3f> positions) {
        for (int i = 0; i < positions.size(); i++) {
            if (x < positions.get(i).x + 2 && x > positions.get(i).x - 2) {
                return false;
            }
            if (z < positions.get(i).z + 2 && z > positions.get(i).z - 2) {
                return false;
            }
        }
        return true;
    }

    /**
     * Finds the Y maximum of a place on the grid
     * @param x x coordinate of column
     * @param z z coordinate of column
     * @return the y-max of the column of blocks
     */
    private int getYMax(int x, int z) {
        int y = 0;
        while (y < CHUNK_SIZE - 1 && BlocksArray[x][y][z].active()) {
            y++;
        }
        return y - 1;
    }
}
