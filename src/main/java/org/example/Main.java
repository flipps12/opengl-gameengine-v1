package org.example;


import org.example.entities.Player;

public class Main {

    public static void main(String[] args) throws Exception {
        new Window(new Player(0, 0, 20, 40, 1.0f, 1.0f, 1.0f)).run();
    }
}