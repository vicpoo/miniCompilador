//GestorArchivos.java
package com.compilador.minicompilador.infraestructura;

import java.io.*;
import java.nio.file.*;

public class GestorArchivos {
    
    public String leerArchivo(String ruta) throws IOException {
        Path path = Paths.get(ruta);
        if (!Files.exists(path)) {
            throw new IOException("El archivo no existe: " + ruta);
        }
        return new String(Files.readAllBytes(path));
    }
    
    public void escribirArchivo(String ruta, String contenido) throws IOException {
        Path path = Paths.get(ruta);
        Files.write(path, contenido.getBytes());
    }
    
    public boolean archivoExiste(String ruta) {
        if (ruta == null || ruta.trim().isEmpty()) {
            return false;
        }
        try {
            Path path = Paths.get(ruta);
            return Files.exists(path);
        } catch (InvalidPathException e) {
            return false;
        }
    }
    
    public void crearArchivoEjemplo(String ruta) throws IOException {
        String ejemplo = "// Programa Hola Mundo en Élfico\n" +
                        "// Este es un programa de ejemplo\n\n" +
                        "tengwa mensaje = \"Namárië! Bienvenido a la Tierra Media\";\n" +
                        "tira(mensaje);\n\n" +
                        "nampat edad = 25;\n" +
                        "linta altura = 1.75;\n\n" +
                        "tira(\"Edad: \");\n" +
                        "tira(edad);\n" +
                        "tira(\"Altura: \");\n" +
                        "tira(altura);\n\n" +
                        "nampat suma = edad + 10;\n" +
                        "tira(\"Suma: \");\n" +
                        "tira(suma);\n\n" +
                        "anin(edad > 18) {\n" +
                        "    tira(\"Eres mayor de edad\");\n" +
                        "} penneth {\n" +
                        "    tira(\"Eres menor de edad\");\n" +
                        "}\n\n" +
                        "nampat contador = 1;\n" +
                        "nauva(contador <= 3) {\n" +
                        "    tira(\"Cuenta: \");\n" +
                        "    tira(contador);\n" +
                        "    contador = contador + 1;\n" +
                        "}\n\n" +
                        "tira(\"Namárië!\");\n";
        
        escribirArchivo(ruta, ejemplo);
    }
}