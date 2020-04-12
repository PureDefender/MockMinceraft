package fakeminceraft;

/**
 *
 * File: BlockLoader.java
 *
 * @author SydHo 
 * Class: CS 4450-01 - Computer Graphics 
 * Assignment: Final Program
 * Date Last Modified:
 *
 * Purpose:
 */
public class BlockLoader {

    private boolean active;
    private BlockType type;
    
    public float x, y, z;
   
    public enum BlockType {

        BlockType_Grass(0),
        BlockType_Sand(1),
        BlockType_Water(2),
        BlockType_Dirt(3),
        BlockType_Stone(4),
        BlockType_Bedrock(5),
        BlockType_Lava(6),
        BlockType_Wood(7),
        BlockType_Leaves(8),
        BlockType_Coal(9),
        BlockType_Lapiz(10),
        BlockType_Redstone(11),
        BlockType_IronOre(12),
        BlockType_GoldOre(13),
        BlockType_DiamondOre(14),
        BlockType_Default(15);

        private int blockID;

        BlockType(int i) {
            blockID = i;
        }

        public int getID() {
            return blockID;
        }

        public void setID(int i) {
            blockID = i;
        }
    }

    public BlockLoader(BlockType type) {
        this.type = type;
    }
    
    void setCoords(float x, float y, float z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public boolean active() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getID() {
        return type.getID();
    }

    public BlockType getType() {
        return type;
    }

    public void setType(BlockType type) {
        this.type = type;
    }
}