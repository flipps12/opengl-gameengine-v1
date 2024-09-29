package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL20.*;

public class ShaderUtils {
    public static int loadShader(String filePath, int type) throws IOException {
        String shaderSource = new String(Files.readAllBytes(Paths.get(filePath)));
        int shaderID = glCreateShader(type);
        glShaderSource(shaderID, shaderSource);
        glCompileShader(shaderID);

        if (glGetShaderi(shaderID, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Error compiling shader: " + glGetShaderInfoLog(shaderID));
        }

        return shaderID;
    }

    public static int createShaderProgram(String vertexFilePath, String fragmentFilePath) throws IOException {
        int vertexShader = loadShader(vertexFilePath, GL_VERTEX_SHADER);
        int fragmentShader = loadShader(fragmentFilePath, GL_FRAGMENT_SHADER);

        int programID = glCreateProgram();
        glAttachShader(programID, vertexShader);
        glAttachShader(programID, fragmentShader);
        glLinkProgram(programID);

        if (glGetProgrami(programID, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Error linking shader program: " + glGetProgramInfoLog(programID));
        }

        return programID;
    }
}

