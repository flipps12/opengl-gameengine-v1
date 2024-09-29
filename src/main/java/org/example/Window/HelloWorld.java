package org.example.Window;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.entities.Entity;
import org.example.entities.EntityNoMove;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.io.IOException;
import java.nio.*;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL43C.GL_DEBUG_OUTPUT;
import static org.lwjgl.opengl.GL43C.glDebugMessageCallback;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

@RequiredArgsConstructor
public class HelloWorld {

    private final Entity entity;

    Integer W_SCREEN = 1800;
    Integer H_SCREEN = 900;

    // Constante de gravedad y fuerza de salto
    final float GRAVEDAD = -4f; //-9.8f;
    final float FUERZA_SALTO = 10.0f;

    // Variables del objeto
    float posicionY = 0;
    float velocidadVertical = -4;


    float deltaTime = 0.032f; // Supongamos 60 FPS 0.016

    // The window handle
    private long window;
    private List<Entity> entities;
    private List<EntityNoMove> entitiesNo;
    private boolean upPressed, downPressed, leftPressed, rightPressed;

    public void run() throws IOException {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();

    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(W_SCREEN, H_SCREEN, "Hello World!", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            boolean isPressed = action != GLFW_RELEASE;
            switch (key) {
                case GLFW_KEY_W:
                    upPressed = isPressed;
                    break;
                case GLFW_KEY_S:
                    downPressed = isPressed;
                    break;
                case GLFW_KEY_A:
                    leftPressed = isPressed;
                    break;
                case GLFW_KEY_D:
                    rightPressed = isPressed;
                    break;
                case GLFW_KEY_ESCAPE:
                    if (action == GLFW_RELEASE) {
                        glfwSetWindowShouldClose(window, true);
                    }
                    break;
            }
        });

        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(window, pWidth, pHeight);

            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        GL.createCapabilities();

        // Configurar la proyección ortográfica
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, 400, 200, 0, -1, 1); // Ajustar según el tamaño de tu ventana
        glMatrixMode(GL_MODELVIEW);
        glEnable(GL_TEXTURE_2D);

        // Crear entidades
        entities = new ArrayList<>();
        entitiesNo = new ArrayList<>();
        entitiesNo.add(new EntityNoMove(1L, 10, 100, 20, 20, 1.0f, 1.0f, 1.0f)); // Verde

        entitiesNo.add(new EntityNoMove(2L,10, 160, 20, 20, 1.0f, 1.0f, 1.0f)); // Rojo

        entity.setId(0L);
        entity.setR(1.0f);
        entity.setG(1.0f);
        entity.setB(1.0f);
        entity.setX(0.0f);
        entity.setY(0.0f);
        entity.setHeight(20);
        entity.setWidth(20);

        try {
            for (EntityNoMove entityNo : entitiesNo) {
                entityNo.init();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        glEnable(GL_DEBUG_OUTPUT);
        glDebugMessageCallback((source, type, id, severity, length, message, userParam) -> {
            System.err.println("GL DEBUG: " + GLDebugMessageCallback.getMessage(length, message));
        }, 0);
    }


    private void loop() throws IOException {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( !glfwWindowShouldClose(window) ) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            velocidadVertical += GRAVEDAD * deltaTime;
            posicionY -= velocidadVertical * deltaTime;



            float prevX = entity.getX();
            float prevY = entity.getY();

            // Detectar colisiones
            for (EntityNoMove noMove : entitiesNo) {
                int col;

                if (entity.getCollisionBottom() == null) entity.CollisionBottom(false, noMove.getId());

                col = entity.isColliding(noMove);

                //                if (entity.getCollisionTop() != null) {
//                    if (entity.getCollisionTop().getId().equals(noMove.getId())) entity.setCol(entity.isColliding(noMove));
//                }
//                if (entity.getCollisionLeft() != null) {
//                    if (entity.getCollisionLeft().getId().equals(noMove.getId())) entity.setCol(entity.isColliding(noMove));
//                }
//                if (entity.getCollisionRight() != null) {
//                    if (entity.getCollisionRight().getId().equals(noMove.getId())) entity.setCol(entity.isColliding(noMove));
//                }



                switch (col) {
                    case -2: // botton
                        //entity.position(entity.getX(), noMove.getY() - noMove.getHeight());
                        entity.CollisionBottom(true, noMove.getId());
                        break;
                    case 2: // top
                        entity.CollisionTop(true, noMove.getId());
                        entity.position(entity.getX(), noMove.getY());
//                        entity.move(0, -30);
                        velocidadVertical = -20;
                        break;
                    case -1: // left
                        entity.position(noMove.getX() + noMove.getWidth(), entity.getY());
                        entity.CollisionLeft(true, noMove.getId());
                        break;
                    case 1: // right
                        entity.position(noMove.getX() - entity.getWidth(), entity.getY());
                        entity.CollisionRight(true, noMove.getId());
                        break;
                    case 0:
                        if (entity.getCollisionBottom().getId().equals(noMove.getId())) entity.CollisionBottom(false, noMove.getId());
                        break;

                }

                if (entity.getCollisionBottom().isStatus()) {
                    velocidadVertical = 0;
                } else entity.setY(posicionY);
            }

            // Actualizar la posición de la entidad
            if (upPressed) {
                velocidadVertical = FUERZA_SALTO;
                posicionY -= velocidadVertical * deltaTime;
                entity.setY(posicionY);
                entity.CollisionBottom(false, 1L);
            }
            //if (downPressed) entity.move(0, 1);
            if (leftPressed) entity.move(-1, 0); entity.move(0, 0);
            if (rightPressed) entity.move(1, 0); entity.move(0, 0);

            for (EntityNoMove entityNo : entitiesNo) {
                entityNo.render();
            }
            entity.render();
            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();

        }
    }

    public static void main(String[] args) throws IOException {
        new HelloWorld(new Entity(0, 0, 50, 50, 1.0f, 1.0f, 1.0f)).run();
    }

}