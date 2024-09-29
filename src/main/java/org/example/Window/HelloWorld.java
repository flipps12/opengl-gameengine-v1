package org.example.Window;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.MapLoader;
import org.example.entities.Entity;
import org.example.entities.EntityNoMove;
import org.json.JSONArray;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.io.IOException;
import java.lang.reflect.Array;
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
    final float GRAVEDAD = -18f; //-9.8f;
    final float FUERZA_SALTO = 37.0f;

    // Variables del objeto
    float posicionY = 0;
    float velocidadVertical = -10;


    float deltaTime = 0.032f; // Supongamos 60 FPS 0.016

    // pj config
    int saltos = 0;

    // The window handle
    private long window;
    private List<EntityNoMove> entitiesNo;
    private boolean upPressed, downPressed, leftPressed, rightPressed;

    public void run() throws Exception {
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

    private void init() throws Exception {
        // Entidades
        entitiesNo = new ArrayList<>();

        String[][] mapa = MapLoader.convertToArray(MapLoader.loadMap("src/main/resources/mapas/maps.json").getJSONArray("mapa1"));

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
                case GLFW_KEY_SPACE:
                    if (saltos > 2) break;
                    upPressed = isPressed;
                    velocidadVertical = FUERZA_SALTO;
                    posicionY -= velocidadVertical * deltaTime;
                    entity.setY(posicionY);
                    entity.CollisionBottom(false, 1);
                    saltos++;
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

        System.out.println(mapa);

        for (Integer i = 0; i < mapa.length; i++) {
            for (Integer j = 0; j < mapa[i].length; j++) {
                if (!mapa[i][j].equals("aire")) entitiesNo.add(new EntityNoMove(Integer.parseInt(i.toString() + j.toString()), j * 20, i * 20, 20, 20, 1.0f, 1.0f, 1.0f, mapa[i][j]));
            }
        }

        entity.setId(-1);
        entity.setR(1.0f);
        entity.setG(1.0f);
        entity.setB(1.0f);
        entity.setX(0.0f);
        entity.setY(0.0f);
        entity.setHeight(40);
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

                switch (col) {
                    case -2: // botton
                        saltos = 0;
                        entity.position(entity.getX(), noMove.getY() - entity.getHeight());
                        entity.CollisionBottom(true, noMove.getId());
                        break;
                    case 2: // top
                        entity.CollisionTop(true, noMove.getId());
                        entity.position(entity.getX(), noMove.getY() + noMove.getHeight());
//                        entity.move(0, -30);
                        velocidadVertical = -10;
                        break;
                    case -1: // left
                        //saltos = 0;
                        entity.position(noMove.getX() + entity.getWidth() + 1, entity.getY());
                        entity.CollisionLeft(true, noMove.getId());
                        break;
                    case 1: // right
                        //saltos = 0;
                        entity.position(noMove.getX() - entity.getWidth() - 1, entity.getY());
                        entity.CollisionRight(true, noMove.getId());
                        break;
                    case 0:
                        if (entity.getCollisionBottom().getId() == noMove.getId() && entity.isColliding(noMove) != -2) entity.CollisionBottom(false, noMove.getId());
                        break;
                }

                if (entity.getCollisionBottom().isStatus()) {
                    velocidadVertical = 0;
                } else entity.setY(posicionY);
            }

            // Actualizar la posición de la entidad
//            if (upPressed) {
//                velocidadVertical = FUERZA_SALTO;
//                posicionY -= velocidadVertical * deltaTime;
//                entity.setY(posicionY);
//                entity.CollisionBottom(false, 1L);
//            }
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

    public static void main(String[] args) throws Exception {
        new HelloWorld(new Entity(0, 0, 50, 50, 1.0f, 1.0f, 1.0f)).run();
    }

}