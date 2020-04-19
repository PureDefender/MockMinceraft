/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fakeminceraft;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
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
    private FloatBuffer lightPosition;
    private FloatBuffer whiteLight;
    float movementSpeed = .35f;

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
        for (DisplayMode d1 : d) {
            if (d1.getWidth() == 640 && d1.getHeight() == 480 && d1.getBitsPerPixel() == 32) {
                displayMode = d1;
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
        GLU.gluPerspective(100.0f, (float) displayMode.getWidth() / (float) displayMode.getHeight(), 0.1f, 300.0f);
        glMatrixMode(GL_MODELVIEW);
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);

        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);
        glEnable(GL_TEXTURE_2D);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glEnable(GL_DEPTH_TEST);

        initLightArrays();
        glLight(GL_LIGHT0, GL_POSITION, lightPosition);
        glLight(GL_LIGHT0, GL_SPECULAR, whiteLight);
        glLight(GL_LIGHT0, GL_DIFFUSE, whiteLight);
        glLight(GL_LIGHT0, GL_AMBIENT, whiteLight);
        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);
    }

    void gameLoop() {
        float dx = 0.0f;
        float dy = 0.0f;
        float dt = 0.0f;        //length of frame
        float lastTime = 0.0f;  // when the last frame was
        long time;
        float mouseSensitivity = 0.09f;
        Mouse.setGrabbed(true);
        // repeat run until user desires to exit
        while (!Display.isCloseRequested() && !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {

            time = Sys.getTime();
            lastTime = time;

            // Apply orientation (pitch and yaw):
            camera.changeYaw(Mouse.getDX() * mouseSensitivity);
            camera.changePitch(Mouse.getDY() * mouseSensitivity);

            checkInput(camera);

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

    private void checkInput(FirstPersonCameraController camera) {
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
        // Increase movement speed of the camera
        if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
            movementSpeed += .1f;
        }
        // Decrease movement speed of the camera
        if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
            movementSpeed -= .1f;
        }
        // Reset movement speed of the camera
        if (Keyboard.isKeyDown(Keyboard.KEY_R)) {
            movementSpeed = .35f;
        }
        if (movementSpeed <= 0) {
            movementSpeed = .05f;
        }
    }

    private void initLightArrays() {
        lightPosition = BufferUtils.createFloatBuffer(4);
        lightPosition.put(0.0f).put(0.0f).put(0.0f).put(1.0f).flip();
        whiteLight = BufferUtils.createFloatBuffer(4);
        whiteLight.put(1.0f).put(1.0f).put(1.0f).put(0.0f).flip();
    }

}
