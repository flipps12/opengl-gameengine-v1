package org.example;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONArray;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class MapLoader {

    public static JSONObject loadMap(String resourcePath) throws Exception {
        // System.out.println(resourcePath); // arreglar errror
        try (InputStream inputStream = MapLoader.class.getResourceAsStream("/mapas/maps.json")) {
            if (inputStream == null) {
                throw new FileNotFoundException("Resource not found: " + resourcePath);
            }
            byte[] bytes = inputStream.readAllBytes();
            String content = new String(bytes, StandardCharsets.UTF_8);
            return new JSONObject(content);
        }
    }

    public static String[][] convertToArray(JSONArray jsonArray) {
        int rows = jsonArray.length();
        int cols = jsonArray.getJSONArray(0).length();
        String[][] array = new String[rows][cols];

        for (int i = 0; i < rows; i++) {
            JSONArray row = jsonArray.getJSONArray(i);
            for (int j = 0; j < cols; j++) {
                if (j < row.length()) {
                    array[i][j] = row.getString(j);
                } else {
                    array[i][j] = "aire"; // O cualquier valor por defecto
                }
            }
        }

        return array;
    }

    public static void main(String[] args) throws Exception {
        JSONArray mapa = loadMap("").getJSONArray("mapa1");
        String[][] array = convertToArray(mapa);

        // Imprimir el array para verificar
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                System.out.print(array[i][j] + " ");
            }
            System.out.println();
        }
    }
}
