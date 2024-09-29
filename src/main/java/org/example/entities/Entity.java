package org.example.entities;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;

@Setter
@Getter
public class Entity {
    private Long id;
    private float x, y;
    private float width, height;
    private float r, g, b;

    private CollisionData CollisionRight;
    private CollisionData CollisionLeft;
    private CollisionData CollisionTop;
    private CollisionData CollisionBottom;

    Integer col = 0;

    public Entity(float x, float y, float width, float height, float r, float g, float b) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.r = r;
        this.g = g;
        this.b = b;
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
        boolean colliding = this.x < player.getX() + player.getWidth() &&
                this.x + this.width > player.getX() &&
                this.y < player.getY() + player.getHeight() &&
                this.y + this.height > player.getY();

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

    public void CollisionBottom(boolean b, Long id) {
        this.CollisionBottom = new CollisionData(b, id);
    }

    public void CollisionTop(boolean b, Long id) {
        this.CollisionTop = new CollisionData(b, id);
    }

    public void CollisionLeft(boolean b, Long id) {
        this.CollisionLeft = new CollisionData(b, id);
    }

    public void CollisionRight(boolean b, Long id) {
        this.CollisionRight = new CollisionData(b, id);
    }
}

