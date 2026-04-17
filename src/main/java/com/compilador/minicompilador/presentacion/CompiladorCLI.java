//CompiladorCLI.java
package com.compilador.minicompilador.presentacion;

import com.compilador.minicompilador.aplicacion.CompiladorServicio;
import com.compilador.minicompilador.infraestructura.GestorArchivos;
import java.io.*;
import java.util.Scanner;

public class CompiladorCLI {
    private CompiladorServicio compiladorServicio;
    private GestorArchivos gestorArchivos;
    
    public CompiladorCLI() {
        this.compiladorServicio = new CompiladorServicio();
        this.gestorArchivos = new GestorArchivos();
    }
    
    private String repetir(String str, int veces) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < veces; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
    
    private String limpiarRuta(String ruta) {
        if (ruta == null || ruta.trim().isEmpty()) {
            return "";
        }
        
        String limpia = ruta.trim();
        
        // Eliminar prefijos de PowerShell como "& '"
        if (limpia.startsWith("& '")) {
            limpia = limpia.substring(3);
        }
        if (limpia.startsWith("& \"")) {
            limpia = limpia.substring(3);
        }
        
        // Eliminar comillas simples y dobles al inicio y final
        if (limpia.startsWith("'") && limpia.endsWith("'")) {
            limpia = limpia.substring(1, limpia.length() - 1);
        }
        if (limpia.startsWith("\"") && limpia.endsWith("\"")) {
            limpia = limpia.substring(1, limpia.length() - 1);
        }
        
        // Eliminar caracteres de escape y limpiar
        limpia = limpia.replace("\\", "");
        
        // Eliminar cualquier prefijo de comando de PowerShell
        String[] prefijos = {"& ", ".\\", "./"};
        for (String prefijo : prefijos) {
            if (limpia.startsWith(prefijo)) {
                limpia = limpia.substring(prefijo.length());
            }
        }
        
        // Eliminar espacios extras
        limpia = limpia.trim();
        
        return limpia;
    }
    
    public void iniciar(String[] args) {
        System.out.println("\n" + repetir("=", 60));
        System.out.println("   COMPILADOR DEL LENGUAJE ÉLFICO - Tierra Media");
        System.out.println("   Universidad Politécnica de Chiapas");
        System.out.println("   Compiladores e Intérpretes");
        System.out.println(repetir("=", 60));
        
        Scanner scanner = new Scanner(System.in);
        
        String archivoEjemplo = "programa.elf";
        try {
            if (!gestorArchivos.archivoExiste(archivoEjemplo)) {
                System.out.println("\n📝 Creando archivo de ejemplo: " + archivoEjemplo);
                gestorArchivos.crearArchivoEjemplo(archivoEjemplo);
                System.out.println("✓ Archivo creado exitosamente");
            }
        } catch (IOException e) {
            System.out.println("❌ Error al crear archivo de ejemplo: " + e.getMessage());
        }
        
        while (true) {
            System.out.println("\n" + repetir("-", 40));
            System.out.println("OPCIONES:");
            System.out.println("  1. Compilar archivo de ejemplo (programa.elf)");
            System.out.println("  2. Compilar archivo específico (puede arrastrar el archivo)");
            System.out.println("  3. Crear nuevo archivo fuente");
            System.out.println("  4. Salir");
            System.out.print("\nSeleccione una opción: ");
            
            String opcion = scanner.nextLine().trim();
            
            switch (opcion) {
                case "1":
                    compilarArchivo("programa.elf");
                    break;
                case "2":
                    System.out.println("\n📂 Ingrese la ruta del archivo o arrastre el archivo aquí:");
                    System.out.println("💡 Puede arrastrar el archivo directamente desde el explorador");
                    System.out.print(">> ");
                    String rutaOriginal = scanner.nextLine().trim();
                    String ruta = limpiarRuta(rutaOriginal);
                    System.out.println("📁 Ruta procesada: " + ruta);
                    compilarArchivo(ruta);
                    break;
                case "3":
                    crearNuevoArchivo(scanner);
                    break;
                case "4":
                    System.out.println("\nNamárië! (Adiós)");
                    scanner.close();
                    return;
                default:
                    System.out.println("❌ Opción inválida");
            }
        }
    }
    
    private void compilarArchivo(String ruta) {
        try {
            if (!gestorArchivos.archivoExiste(ruta)) {
                System.out.println("❌ El archivo no existe: " + ruta);
                System.out.println("\n💡 SUGERENCIAS:");
                System.out.println("   1. Asegúrese de que el archivo tenga extensión .elf");
                System.out.println("   2. Verifique que el archivo esté en la ubicación correcta");
                System.out.println("   3. Intente escribir solo el nombre si está en la misma carpeta");
                System.out.println("   4. O use la ruta completa sin comillas ni caracteres especiales");
                System.out.println("\n📝 Ejemplo de ruta válida: D:\\carpeta\\archivo.elf");
                return;
            }
            
            System.out.println("\n📖 Leyendo archivo: " + ruta);
            String codigoFuente = gestorArchivos.leerArchivo(ruta);
            
            System.out.println("\n📄 CÓDIGO FUENTE:");
            System.out.println(repetir("-", 40));
            System.out.println(codigoFuente);
            System.out.println(repetir("-", 40));
            
            compiladorServicio.compilar(codigoFuente);
            
        } catch (IOException e) {
            System.out.println("❌ Error al leer el archivo: " + e.getMessage());
        }
    }
    
    private void crearNuevoArchivo(Scanner scanner) {
        System.out.print("\n📝 Ingrese el nombre del archivo (sin extensión): ");
        String nombre = scanner.nextLine().trim();
        String ruta = nombre + ".elf";
        
        System.out.println("\n✍️ Ingrese el código fuente (línea vacía para finalizar):");
        System.out.println("(Puede escribir código en lenguaje Élfico)");
        System.out.println(repetir("-", 40));
        
        StringBuilder codigo = new StringBuilder();
        while (true) {
            String linea = scanner.nextLine();
            if (linea.isEmpty() && codigo.length() == 0) continue;
            if (linea.isEmpty()) break;
            codigo.append(linea).append("\n");
        }
        
        try {
            gestorArchivos.escribirArchivo(ruta, codigo.toString());
            System.out.println("\n✓ Archivo guardado: " + ruta);
            
            System.out.print("\n¿Desea compilarlo ahora? (s/n): ");
            String respuesta = scanner.nextLine().trim().toLowerCase();
            if (respuesta.equals("s") || respuesta.equals("si")) {
                compilarArchivo(ruta);
            }
        } catch (IOException e) {
            System.out.println("❌ Error al guardar el archivo: " + e.getMessage());
        }
    }
}