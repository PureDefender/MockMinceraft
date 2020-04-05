/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fakeminceraft;

import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.glu.GLU;

/**
 *
 * @author SydHo, Luke Doukakis
 */
public class FakeMinceraft {
    
    
    DisplayMode displayMode;
    FirstPersonCameraController camera;

    public static void main(String[] args) {
        FakeMinceraft main = new FakeMinceraft();
        main.start();
    }

    // create window, initlialize settings, and repeatedly render
    public void start() {
        try {
            createWindow();
            initGL();
            camera = new FirstPersonCameraController(0, 0, 0);
            gameLoop();
            Display.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void createWindow() throws Exception {
        DisplayMode d[] = Display.getAvailableDisplayModes();
        for (int i = 0; i < d.length; i++) {
            if (d[i].getWidth() == 640 && d[i].getHeight() == 480 && d[i].getBitsPerPixel() == 32) {
                displayMode = d[i];
                break;
            }
        }
        Display.setDisplayMode(displayMode);
        Display.setTitle("Fake Minecraft");
        Display.create();  
    }

    void initGL() {
        glClearColor(.0f, .0f, .0f, .0f);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        GLU.gluPerspective(100.0f, (float)displayMode.getWidth() / (float)displayMode.getHeight(), 0.1f, 300.0f);
        glMatrixMode(GL_MODELVIEW);
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
        // -- allow chunks to be rendered
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);
        glEnable(GL_DEPTH_TEST);
        // --
    }
    
    void gameLoop() {
        float dx = 0.0f;
        float dy = 0.0f;
        float dt = 0.0f;        //length of frame
        float lastTime = 0.0f;  // when the last frame was
        long time = 0;
        float mouseSensitivity = 0.09f;
        float movementSpeed = .35f;
        Mouse.setGrabbed(true);
        // repeat run until user desires to exit
        while (!Display.isCloseRequested() && !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            
            time = Sys.getTime();
            lastTime = time;
            
            // Apply orientation (pitch and yaw):
            camera.changeYaw(Mouse.getDX() * mouseSensitivity);
            camera.changePitch(Mouse.getDY() * mouseSensitivity);

            checkInput(camera, movementSpeed);

            // Show scene:
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glLoadIdentity();
            camera.lookThrough();
            
            
            // -- call method to draw a box (from checkpoint 1)
            // drawScene();
            // --
            
            // -- draw chunk
            camera.chunk.render();
            // --
            
            Display.update();
            Display.sync(60);
        }
        Display.destroy();
    }

    private void checkInput(FirstPersonCameraController camera, float movementSpeed) {
        if (Keyboard.isKeyDown(Keyboard.KEY_W) || Keyboard.isKeyDown(Keyboard.KEY_UP)) {
            camera.moveForward(movementSpeed);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_S) || Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
            camera.moveBackward(movementSpeed);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_A) || Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
            camera.moveLeft(movementSpeed);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_D) || Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
            camera.moveRight(movementSpeed);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
            camera.moveUp(movementSpeed);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            camera.moveDown(movementSpeed);
        }
    }

    private void drawScene() {
        

        glBegin(GL_QUADS);
        //Top
        glColor3f(0.0f, 0.0f, 20.0f);
        glVertex3f(20.0f, 20.0f, -20.0f);
        glVertex3f(-20.0f, 20.0f, -20.0f);
        glVertex3f(-20.0f, 20.0f, 20.0f);
        glVertex3f(20.0f, 20.0f, 20.0f);
        //Bottom
        glColor3f(20.0f, 0.0f, 20.0f);
        glVertex3f(20.0f, -20.0f, 20.0f);
        glVertex3f(-20.0f, -20.0f, 20.0f);
        glVertex3f(-20.0f, -20.0f, -20.0f);
        glVertex3f(20.0f, -20.0f, -20.0f);
        //Front
        glColor3f(0.0f, 20.0f, 20.0f);
        glVertex3f(20.0f, 20.0f, 20.0f);
        glVertex3f(-20.0f, 20.0f, 20.0f);
        glVertex3f(-20.0f, -20.0f, 20.0f);
        glVertex3f(20.0f, -20.0f, 20.0f);
        //Back
        glColor3f(0.0f, 20.0f, 0.0f);
        glVertex3f(20.0f, -20.0f, -20.0f);
        glVertex3f(-20.0f, -20.0f, -20.0f);
        glVertex3f(-20.0f, 20.0f, -20.0f);
        glVertex3f(20.0f, 20.0f, -20.0f);
        //Left
        glColor3f(20.0f, 0.0f, 20.0f);
        glVertex3f(-20.0f, 20.0f, 20.0f);
        glVertex3f(-20.0f, 20.0f, -20.0f);
        glVertex3f(-20.0f, -20.0f, -20.0f);
        glVertex3f(-20.0f, -20.0f, 20.0f);
        //Right
        glColor3f(20.0f, 20.0f, 20.0f);
        glVertex3f(20.0f, 20.0f, -20.0f);
        glVertex3f(20.0f, 20.0f, 20.0f);
        glVertex3f(20.0f, -20.0f, 20.0f);
        glVertex3f(20.0f, -20.0f, -20.0f);
        glEnd();
    }

}