/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fakeminceraft;

/**
 *
 * @author SydHo
 */
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.glu.GLU;

public class FakeMinceraft {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }

    /**
     * Calls the appropriate functions to begin the program
     */
    public void start() {
        try {
            createWindow();
            initGL();
            render();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the window to be used in the program
     *
     * @throws Exception
     */
    private void createWindow() throws Exception {
        Display.setFullscreen(false);

        Display.setDisplayMode(new DisplayMode(640, 480));
        Display.setTitle("Minceraft");
        Display.create();
    }
    
    /**
     * Initializes the graphics library
     */
    private void initGL() {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();

        glOrtho(0, 640, 0, 480, 1, -1);
        glMatrixMode(GL_MODELVIEW);
        glHint(GL_PERSPECTIVE_CORRECTION_HINT,
                GL_NICEST);
    }
    
    /**
     * Render function to start drawing
     */
    private void render() {
        while (!Display.isCloseRequested()) {
            try {
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                glLoadIdentity();
                glPointSize(1);
                // checkInput();
                glBegin(GL_POINTS);

                glEnd();
                Display.update();
                Display.sync(144);
            } catch (Exception e) {
            }
        }
        Display.destroy();
    }

}
