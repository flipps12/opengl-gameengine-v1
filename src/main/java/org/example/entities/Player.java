package org.example.entities;

import lombok.Getter;
import lombok.Setter;
import org.example.TextureLoader;

import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.lwjgl.opengl.GL11.*;

@Setter
@Getter
public class Player {
    private int id = -1;
    private float x, y;
    private float width = 20, height = 40;
    private float r = 1.0f, g = 1.0f, b = 1.0f;

    private CollisionData CollisionRight;
    private CollisionData CollisionLeft;
    private CollisionData CollisionTop;
    private CollisionData CollisionBottom;
    private int[] textureIDs;
    private int currentFrame;
    private long lastFrameTime;
    private long frameDuration; // Duración de cada fotograma en milisegundos
    private boolean flip;


    Integer col = 0;

    public Player(float x, float y, float width, float height, float r, float g, float b) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.r = r;
        this.g = g;
        this.b = b;
    }


    public void initAnimation(String imagePath, int tileWidth, int tileHeight, long frameDuration) throws IOException {
        BufferedImage image = TextureLoader.loadImage(imagePath);
        BufferedImage[] tiles = TextureLoader.splitImage(image, tileWidth, tileHeight);
        this.textureIDs = TextureLoader.loadTextures(tiles);
        this.frameDuration = frameDuration;
        this.currentFrame = 0;
        this.lastFrameTime = System.currentTimeMillis();
    }

    public void update() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameTime >= frameDuration) {
            currentFrame = (currentFrame + 1) % textureIDs.length;
            lastFrameTime = currentTime;
        }
    }

    public void render(float scaleX, float sclaeY) {
        glBindTexture(GL_TEXTURE_2D, textureIDs[currentFrame]);

        float scaledWidth = width * scaleX;
        float scaledHeight = height * sclaeY;

        // Ajustar la posición para que la textura se escale hacia arriba
        float offsetX = (width - scaledWidth) / 2;
        float offsetY = height - scaledHeight;

        glBegin(GL_QUADS);
        glTexCoord2f(flip ? 1 : 0, 0); glVertex2f(x + offsetX, y + offsetY); // 1 0 // 0 0
        glTexCoord2f(flip ? 0 : 1, 0); glVertex2f(x + offsetX + scaledWidth, y + offsetY); // 0 0 // 1 0
        glTexCoord2f(flip ? 0 : 1, 1); glVertex2f(x + offsetX + scaledWidth, y + height); // 0 1 // 1 1
        glTexCoord2f(flip ? 1 : 0, 1); glVertex2f(x + offsetX, y + height); // 1 1 // 0 1
        glEnd();

        glBindTexture(GL_TEXTURE_2D, 0);
    }





    public void render() {
        // Dibujar el relleno de la entidad
        glColor3f(r, g, b);
        glBegin(GL_QUADS);
        glVertex2f(x, y);
        glVertex2f(x + width, y);
        glVertex2f(x + width, y + height);
        glVertex2f(x, y + height);
        glEnd();

    }

    public void move(float dx, float dy) {
        this.x += dx;
        this.y += dy;
    }

    public void position(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public int isColliding(EntityNoMove player) {
        boolean colliding = this.x < player.getX() + player.getWidth() -1 &&
                this.x + this.width > player.getX() - 1 &&
                this.y < player.getY() + player.getHeight() -1 &&
                this.y + this.height > player.getY() -1;

        if (!colliding) {
            return 0; // No hay colisión
        }

        float dx = (player.getX() + player.getWidth() / 2) - (this.x + this.width / 2);
        float dy = (player.getY() + player.getHeight() / 2) - (this.y + this.height / 2);

        float width = (player.getWidth() + this.width) / 2;
        float height = (player.getHeight() + this.height) / 2;

        float crossWidth = width * dy;
        float crossHeight = height * dx;

        if (Math.abs(dx) <= width && Math.abs(dy) <= height) {
            if (crossWidth > crossHeight) {
                return (crossWidth > -crossHeight) ? -2 : -1; // Top or Right
            } else {
                return (crossWidth > -crossHeight) ? 1 : 2; // Left or Bottom
            }
        }
        return 0;
    }

    public void CollisionBottom(boolean b, int id) {
        this.CollisionBottom = new CollisionData(b, id);
    }

    public void CollisionTop(boolean b, int id) {
        this.CollisionTop = new CollisionData(b, id);
    }

    public void CollisionLeft(boolean b, int id) {
        this.CollisionLeft = new CollisionData(b, id);
    }

    public void CollisionRight(boolean b, int id) {
        this.CollisionRight = new CollisionData(b, id);
    }
}

