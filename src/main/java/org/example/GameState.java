package org.example;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameState {
    private double delta;

    // Gravedad y Fisica
    final float GRAVEDAD = -0.03f; //-9.8f;
}

