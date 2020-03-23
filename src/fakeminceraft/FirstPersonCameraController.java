package fakeminceraft;

/**
 *
 * @author Luke
 */

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.Sys;


public class FirstPersonCameraController {
    
    //3d vector to store the camera's position
    Vector3f position = null;
    Vector3f lPosition = null;
    
    //the rotation around the Y axis of the camera
    float yaw = 0.0f;
    
    //the rotation around the X axis of the camera
    float pitch = 0.0f;
    
    Vector3Float me;
    
    // constructor, initializes camera's Vector3f position
    public FirstPersonCameraController(float x, float y, float z){
        
        position = new Vector3f(x, y, z);
        
        lPosition = new Vector3f(x, y, z); 
        lPosition.x = 0f;
        lPosition.y = 15f;
        lPosition.z = 0f;
    }
    
    
    //increment the camera's current yaw rotation
    public void changeYaw(float amount){
        //increment the yaw by the amount param
        yaw += amount;
    }
    
    //increment the camera's current pitch rotation
    public void changePitch(float amount){
        //increment the pitch by the amount param
        pitch -= amount;
    }
    
    
    //moves the camera forward relative to its current rotation (yaw)
    public void moveForward(float distance){
        float xOffset = distance * (float)Math.sin(Math.toRadians(yaw));
        float zOffset = distance * (float)Math.cos(Math.toRadians(yaw));
        position.x -= xOffset;
        position.z += zOffset;
    }
    
    //moves the camera backward relative to its current rotation (yaw)
    public void moveBackward(float distance){
        float xOffset = distance * (float)Math.sin(Math.toRadians(yaw));
        float zOffset = distance * (float)Math.cos(Math.toRadians(yaw));
        position.x += xOffset;
        position.z -= zOffset;
    }
    
    //strafes the camera left relative to its current rotation (yaw)
    public void moveLeft(float distance){
        float xOffset = distance * (float)Math.sin(Math.toRadians(yaw-90));
        float zOffset = distance * (float)Math.cos(Math.toRadians(yaw-90));
        position.x -= xOffset;
        position.z += zOffset;
    }
    
    public void moveRight(float distance){
        float xOffset = distance * (float)Math.sin(Math.toRadians(yaw+90));
        float zOffset = distance * (float)Math.cos(Math.toRadians(yaw+90));
        position.x -= xOffset;
        position.z += zOffset;
    }
    
    //moves the camera up relative to its current rotation (yaw)
    public void moveUp(float distance){
        position.y -= distance;
    }
    
    //moves the camera down
    public void moveDown(float distance){
        position.y += distance;
    }
    
    
    
    // establish look vector based on pitch and yaw
    public void lookThrough(){
        
        //rotate the pitch around the X axis
        glRotatef(pitch, 1.0f, 0.0f, 0.0f);
        
        //rotate the yaw around the Y axis
        glRotatef(yaw, 0.0f, 1.0f, 0.0f);
        
        //translate to the position vector's location
        glTranslatef(position.x, position.y, position.z);
    }
    
    
    
    
}
