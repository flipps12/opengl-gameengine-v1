package org.example;


import org.example.Window.HelloWorld;
import org.example.entities.Entity;

public class Main {

    public static void main(String[] args) throws Exception {
        new HelloWorld(new Entity(0, 0, 50, 50, 1.0f, 1.0f, 1.0f)).run();
    }
}