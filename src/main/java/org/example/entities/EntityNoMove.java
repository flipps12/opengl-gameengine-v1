package org.example.entities;

import lombok.Getter;
import lombok.Setter;
import org.example.TextureLoader;

import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.lwjgl.opengl.GL11.*;

@Setter
@Getter
public class EntityNoMove {
    private int id;
    private float x, y;
    private float width, height;
    private float r, g, b;
    private String name;
    private int textureID;

    public EntityNoMove(int id , float x, float y, float width, float height, float r, float g, float b, String name) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.r = r;
        this.g = g;
        this.b = b;
        this.name = name;
    }

    public void init() throws IOException {
        BufferedImage image = TextureLoader.loadImage("/shaders/" + name + ".png");
        if (image == null) {
            throw new IOException("Failed to load image");
        }
//        else {
//            System.out.println("Image loaded successfully: " + image.getWidth() + "x" + image.getHeight());
//        }
        textureID = TextureLoader.loadTexture(image);

        // Configurar parámetros de la textura
        glBindTexture(GL_TEXTURE_2D, textureID);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glBindTexture(GL_TEXTURE_2D, 0);

    }

    public void render() throws IOException {
        glBindTexture(GL_TEXTURE_2D, textureID);

        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex2f(x, y);
        glTexCoord2f(1, 0); glVertex2f(x + width, y);
        glTexCoord2f(1, 1); glVertex2f(x + width, y + height);
        glTexCoord2f(0, 1); glVertex2f(x, y + height);
        glEnd();

        glBindTexture(GL_TEXTURE_2D, 0);
    }



}
