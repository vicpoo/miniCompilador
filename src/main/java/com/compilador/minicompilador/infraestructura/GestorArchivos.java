//GestorArchivos.java
package com.compilador.minicompilador.infraestructura;

import java.io.*;
import java.nio.file.*;

public class GestorArchivos {
    
    public String leerArchivo(String ruta) throws IOException {
        return new String(Files.readAllBytes(Paths.get(ruta)));
    }
    
    public void escribirArchivo(String ruta, String contenido) throws IOException {
        Files.write(Paths.get(ruta), contenido.getBytes());
    }
    
    public boolean archivoExiste(String ruta) {
        return Files.exists(Paths.get(ruta));
    }
    
    public void crearArchivoEjemplo(String ruta) throws IOException {
        String ejemplo = "// Programa de ejemplo en Élfico\n" +
                        "// Hola Mundo y operaciones básicas\n\n" +
                        "nampat edad = 25;\n" +
                        "linta altura = 1.75;\n" +
                        "tengwa mensaje = \"A Elbereth Gilthoniel!\";\n\n" +
                        "tira(mensaje);\n" +
                        "tira(\"Edad: \");\n" +
                        "tira(edad);\n" +
                        "tira(\"Altura: \");\n" +
                        "tira(altura);\n\n" +
                        "// Operaciones matemáticas\n" +
                        "nampat suma = edad + 10;\n" +
                        "nampat producto = edad * 2;\n" +
                        "tira(\"Suma: \");\n" +
                        "tira(suma);\n" +
                        "tira(\"Producto: \");\n" +
                        "tira(producto);\n\n" +
                        "// Estructura condicional\n" +
                        "anin(edad > 18) {\n" +
                        "    tira(\"Eres mayor de edad en la Tierra Media\");\n" +
                        "} penneth {\n" +
                        "    tira(\"Eres un joven hobbit\");\n" +
                        "}\n\n" +
                        "// Ciclo while\n" +
                        "nampat contador = 1;\n" +
                        "nauva(contador <= 5) {\n" +
                        "    tira(\"Cuenta élfica: \");\n" +
                        "    tira(contador);\n" +
                        "    contador = contador + 1;\n" +
                        "}\n\n" +
                        "// Ciclo for\n" +
                        "ana(nampat i = 1; i <= 3; i = i + 1) {\n" +
                        "    tira(\"Valor de i: \");\n" +
                        "    tira(i);\n" +
                        "}\n\n" +
                        "tira(\"Namárië! (Adiós en élfico)\");\n";
        
        escribirArchivo(ruta, ejemplo);
    }
}