package org.example.Window;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.GameState;
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
import java.util.Objects;

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
    private static final GameState gameState = new GameState();

    Integer W_SCREEN = 1800;
    Integer H_SCREEN = 900;


    // Gravedad y Fisica
    final float GRAVEDAD = -0.03f; //-9.8f;
    float posicionY;
    float velocidadVertical = 0;

    final int TARGET_FPS = 60;
    final long OPTIMAL_TIME = 400000000 / TARGET_FPS; // Nanosegundos por frame

    // Jugador config
    float textureScaleX = 4.3f;
    float textureScaleY = 2.0f;
    int saltos = 0;
    final float FUERZA_SALTO = 2.0f;

    // The window handle
    private long window;
    private List<EntityNoMove> entitiesNo;
    private boolean upPressed, downPressed, leftPressed, rightPressed, animationInProcess;

    public void run() throws Exception {
        System.out.println("LWJGL " + Version.getVersion() + "!");

        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free(); // glfwSetErrorCallback(null).free();
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

        glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
            switch (button) {
                case GLFW_MOUSE_BUTTON_LEFT:
                    if (action == GLFW_PRESS) {
                        try {
                            entity.initAnimation("/shaders/_AttackNoMovement.png", 120, 80, 120);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    break;
                case GLFW_MOUSE_BUTTON_RIGHT:
                    if (action == GLFW_PRESS) {
                        try {
                            entity.initAnimation("/shaders/_Attack2.png", 120, 80, 120);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    break;
            }
        });

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            boolean isPressed = action != GLFW_RELEASE;
            double delta = gameState.getDelta();
            switch (key) {
                case GLFW_KEY_SPACE:
                    try {
                        if (saltos < 2 && action == GLFW_PRESS) {
                            velocidadVertical = FUERZA_SALTO;
                            posicionY -= (float) (velocidadVertical * delta);
                            //entity.setY(posicionY);
                            entity.CollisionBottom(false, 1);
                            saltos++;
                            entity.initAnimation("/shaders/_Jump.png", 120, 80, 100);
                            animationInProcess = true;
                        } else if (!isPressed && animationInProcess) {
                            animationInProcess = false;
                        }
                    } catch (IOException e) {throw new RuntimeException(e);}
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
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // Mapa
        for (Integer i = 0; i < mapa.length; i++) {
            for (Integer j = 0; j < mapa[i].length; j++) {
                if (!mapa[i][j].equals("aire")) entitiesNo.add(new EntityNoMove(Integer.parseInt(i.toString() + j.toString()), j * 20, i * 20, 20, 20, 1.0f, 1.0f, 1.0f, mapa[i][j]));
            }
            posicionY = i * 20 - 40;
            entity.setY(posicionY - 20);
        }

        entity.setId(-1);
        entity.setR(1.0f);
        entity.setG(1.0f);
        entity.setB(1.0f);
        entity.setX(190.0f);
        entity.setHeight(40);
        entity.setWidth(20);


        entity.CollisionBottom(false, 1);
        entity.CollisionTop(false, 1);
        entity.CollisionLeft(false, 1);
        entity.CollisionRight(false, 1);

        entity.initAnimation("/shaders/_Idle.png", 120, 80, 100); // 100 ms por fotograma

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

        long lastLoopTime = System.nanoTime();
        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( !glfwWindowShouldClose(window) ) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            float camY = 120 - entity.getY();

            long now = System.nanoTime();
            long updateLength = now - lastLoopTime;

            lastLoopTime = now;

            double delta = updateLength / ((double) OPTIMAL_TIME);
            gameState.setDelta(delta);


            velocidadVertical += (float) (GRAVEDAD * delta);
            posicionY -= (float) (velocidadVertical * delta);

            if (entity.getCollisionBottom().isStatus()) {
                velocidadVertical = 0;
                posicionY = entity.getY();
            } else entity.setY(posicionY);

            if (!entity.getCollisionBottom().isStatus() && !animationInProcess) {
                entity.initAnimation("/shaders/_Fall.png", 120, 80, 100);
            } else if (!animationInProcess) {
                entity.initAnimation("/shaders/_Idle.png", 120, 80, 100);
            }

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
                        velocidadVertical = -1;
                        break;
                    case -1: // left
                        entity.position(noMove.getX() + entity.getWidth() , entity.getY());
                        entity.CollisionLeft(true, noMove.getId());
                        break;
                    case 1: // right
                        entity.position(noMove.getX() - entity.getWidth() , entity.getY());
                        entity.CollisionRight(true, noMove.getId());
                        break;
                    case 0:
                        if (entity.getCollisionBottom().getId() == noMove.getId() && entity.isColliding(noMove) != -2) {
                            entity.CollisionBottom(false, noMove.getId());
                        }
                        break;
                }
            }

            if (entity.getCollisionBottom().isStatus() && !leftPressed && !rightPressed) {
                try {
                    entity.initAnimation("/shaders/_Idle.png", 120, 80, 100);
                    animationInProcess = false;
                } catch (IOException e) {throw new RuntimeException(e);}
            }
            if (leftPressed  && !rightPressed) {
                entity.move(-1, 0);
                entity.setFlip(true);
                try {
                    if (!animationInProcess && !rightPressed && entity.getCollisionBottom().isStatus()) {
                        entity.initAnimation("/shaders/_Run.png", 120, 80, 100);
                        animationInProcess = true;
                    }
                } catch (IOException e) {throw new RuntimeException(e);}
            }
            if (rightPressed) {
                entity.move(1, 0);
                entity.setFlip(false);
                try {
                    if (!animationInProcess && !leftPressed && entity.getCollisionBottom().isStatus()) {
                        entity.initAnimation("/shaders/_Run.png", 120, 80, 100);
                        animationInProcess = true;
                    }
                } catch (IOException e) {throw new RuntimeException(e);}
            }

            for (EntityNoMove entityNo : entitiesNo) {
                entityNo.render();
            }

            glLoadIdentity();
            glTranslatef(0, camY, 0);
            entity.render(entity.getX(), entity.getY(), entity.getWidth(), entity.getHeight(), textureScaleX, textureScaleY);
            entity.update();

            long sleepTime = (lastLoopTime - System.nanoTime() + OPTIMAL_TIME) / 1000000;
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }

}