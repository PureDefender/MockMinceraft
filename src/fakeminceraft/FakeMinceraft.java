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

/**
 *
 * @author SydHo, Luke Doukakis
 */
public class FakeMinceraft {

    public static void main(String[] args) {
        FakeMinceraft main = new FakeMinceraft();
        main.start();
    }

    // create window, initlialize settings, and repeatedly render
    public void start() {

        try {

            createWindow();
            initGL();

            render();

            Display.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void createWindow() throws Exception {
        Display.setFullscreen(false);
        Display.setDisplayMode(new DisplayMode(640, 480));
        Display.setTitle("Fake Minecraft");
        Display.create();
    }

    void initGL() {
        glClearColor(.0f, .0f, .0f, .0f);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(-320, 320, -240, 240, 1, -1);   // origin centered on window
        glMatrixMode(GL_MODELVIEW);
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
    }

    // @TODO
    void render() {

        // camera controller
        FirstPersonCameraController camera = new FirstPersonCameraController(0, 0, 0);

        float dx = 0.0f;
        float dy = 0.0f;
        float dt = 0.0f;        //length of frame
        float lastTime = 0.0f;  // when the last frame was
        long time = 0;
        float mouseSensitivity = 0.09f;
        float movementSpeed = .35f;
        Mouse.setGrabbed(true);

        // while loop, to run until user desires to exit
        while (!Display.isCloseRequested() && !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {

            time = Sys.getTime();
            lastTime = time;

            // *************************
            // Apply orientation (pitch and yaw):
            // yaw based on mouse x movement
            camera.changeYaw(Mouse.getDX() * mouseSensitivity);

            // pitch based on mouse y movement
            camera.changePitch(Mouse.getDY() * mouseSensitivity);
            
            checkInput(camera, movementSpeed);
            
            // Show scene:
            glLoadIdentity();
            camera.lookThrough();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // call method to draw the box
            //drawScene();
            Display.update();
            Display.sync(60);

            // *************************
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

}
