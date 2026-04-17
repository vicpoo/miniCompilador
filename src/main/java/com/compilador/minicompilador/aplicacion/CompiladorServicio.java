//CompiladorServicio.java
package com.compilador.minicompilador.aplicacion;

import com.compilador.minicompilador.dominio.*;
import com.compilador.minicompilador.infraestructura.*;
import java.util.*;

public class CompiladorServicio {
    private AnalizadorLexico analizadorLexico;
    private AnalizadorSintactico analizadorSintactico;
    private AnalizadorSemantico analizadorSemantico;
    private GeneradorCodigo generadorCodigo;
    
    public CompiladorServicio() {
        this.analizadorLexico = new AnalizadorLexico();
        this.analizadorSintactico = new AnalizadorSintactico();
        this.analizadorSemantico = new AnalizadorSemantico();
        this.generadorCodigo = new GeneradorCodigo();
    }
    
    private String repetir(String str, int veces) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < veces; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
    
    public boolean compilar(String codigoFuente) {
        System.out.println("\n" + repetir("=", 60));
        System.out.println("     INICIANDO PROCESO DE COMPILACIÓN (LENGUAJE ÉLFICO)");
        System.out.println(repetir("=", 60));
        
        // Fase 1: Análisis Léxico
        System.out.println("\n[FASE 1] ANÁLISIS LÉXICO");
        List<Token> tokens = analizadorLexico.analizar(codigoFuente);
        
        if (!analizadorLexico.getErrores().isEmpty()) {
            mostrarErrores(analizadorLexico.getErrores());
            return false;
        }
        analizadorLexico.imprimirTokens();
        
        // Fase 2: Análisis Sintáctico
        System.out.println("[FASE 2] ANÁLISIS SINTÁCTICO");
        TablaSimbolos tablaSimbolos = new TablaSimbolos();
        
        if (!analizadorSintactico.analizar(tokens, tablaSimbolos)) {
            mostrarErrores(analizadorSintactico.getErrores());
            return false;
        }
        analizadorSintactico.imprimirArbolSintactico();
        analizadorSintactico.imprimirCodigoIntermedio();
        
        // Fase 3: Análisis Semántico
        System.out.println("[FASE 3] ANÁLISIS SEMÁNTICO");
        if (!analizadorSemantico.analizar(tablaSimbolos)) {
            mostrarErrores(analizadorSemantico.getErrores());
            return false;
        }
        analizadorSemantico.imprimirResultados();
        tablaSimbolos.imprimirTabla();
        
        // Fase 4: Generación de Código
        System.out.println("[FASE 4] GENERACIÓN DE CÓDIGO");
        List<String> codigoIntermedio = analizadorSintactico.getCodigoIntermedio();
        List<String> codigoObjeto = generadorCodigo.generarCodigoObjeto(codigoIntermedio, tablaSimbolos);
        generadorCodigo.imprimirCodigoObjeto();
        
        // Ejecutar el código objeto
        generadorCodigo.ejecutarCodigoObjeto(codigoIntermedio, tablaSimbolos);
        
        System.out.println(repetir("=", 60));
        System.out.println("     COMPILACIÓN EXITOSA - ¡Namárië!");
        System.out.println(repetir("=", 60));
        
        return true;
    }
    
    private void mostrarErrores(List<ErrorCompilacion> errores) {
        System.out.println("\n❌ ERRORES DE COMPILACIÓN:");
        for (ErrorCompilacion error : errores) {
            System.out.println("  " + error);
        }
    }
}