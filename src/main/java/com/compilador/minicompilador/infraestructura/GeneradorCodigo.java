//GeneradorCodigo.java
package com.compilador.minicompilador.infraestructura;

import com.compilador.minicompilador.dominio.*;
import java.util.*;

public class GeneradorCodigo {
    private List<String> codigoObjeto;
    private Map<String, Object> variables;
    
    public GeneradorCodigo() {
        this.codigoObjeto = new ArrayList<>();
        this.variables = new HashMap<>();
    }
    
    public List<String> generarCodigoObjeto(List<String> codigoIntermedio, TablaSimbolos tablaSimbolos) {
        this.codigoObjeto.clear();
        this.variables.clear();
        
        codigoObjeto.add("=== CÓDIGO OBJETO (EJECUTABLE) ===");
        codigoObjeto.add("Lenguaje: Élfico - Traducción a instrucciones de máquina virtual");
        codigoObjeto.add("");
        
        for (String instruccion : codigoIntermedio) {
            String codigoObj = traducirInstruccion(instruccion, tablaSimbolos);
            codigoObjeto.add(codigoObj);
        }
        
        codigoObjeto.add("");
        codigoObjeto.add("=== FIN CÓDIGO OBJETO ===");
        
        return codigoObjeto;
    }
    
    private String traducirInstruccion(String instruccion, TablaSimbolos tablaSimbolos) {
        if (instruccion.startsWith("DECLARE")) {
            String[] partes = instruccion.split(" ");
            String var = partes[1];
            variables.put(var, null);
            return "[DECL] Asignar memoria para '" + var + "'";
        } else if (instruccion.startsWith("PRINT")) {
            String valor = instruccion.substring(6);
            return "[OUT] Imprimir: " + valor;
        } else if (instruccion.startsWith("READ")) {
            String var = instruccion.substring(5);
            return "[IN] Leer valor para '" + var + "'";
        } else if (instruccion.contains("=")) {
            String[] partes = instruccion.split("=");
            String var = partes[0].trim();
            String expr = partes[1].trim();
            
            if (expr.matches("\\d+(\\.\\d+)?")) {
                return "[ASIG] " + var + " = " + expr;
            } else if (expr.startsWith("t")) {
                return "[ASIG] " + var + " = resultado de operación";
            } else if (expr.contains("+") || expr.contains("-") || expr.contains("*") || expr.contains("/")) {
                return "[OP] " + var + " = " + expr;
            } else if (expr.contains("and") || expr.contains("or")) {
                return "[OP_LOG] " + var + " = " + expr;
            } else {
                return "[ASIG] " + var + " = " + expr;
            }
        } else if (instruccion.startsWith("IF")) {
            return "[BR_COND] " + instruccion;
        } else if (instruccion.startsWith("GOTO")) {
            return "[BR] " + instruccion;
        } else if (instruccion.startsWith("L")) {
            return "[LABEL] " + instruccion;
        } else if (instruccion.startsWith("t")) {
            String[] partes = instruccion.split("=");
            String temp = partes[0].trim();
            String op = partes[1].trim();
            return "[TEMP] " + temp + " = " + op;
        } else {
            return "[???] " + instruccion;
        }
    }
    
    public void ejecutarCodigoObjeto(List<String> codigoIntermedio, TablaSimbolos tablaSimbolos) {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║        EJECUTANDO PROGRAMA            ║");
        System.out.println("╚════════════════════════════════════════╝");
        
        for (String instruccion : codigoIntermedio) {
            ejecutarInstruccion(instruccion, tablaSimbolos);
        }
        
        System.out.println("=========================================\n");
    }
    
    private void ejecutarInstruccion(String instruccion, TablaSimbolos tablaSimbolos) {
        if (instruccion.startsWith("PRINT")) {
            String contenido = instruccion.substring(6);
            if (contenido.startsWith("\"") && contenido.endsWith("\"")) {
                System.out.println("  " + contenido.substring(1, contenido.length() - 1));
            } else {
                TablaSimbolos.Simbolo simbolo = tablaSimbolos.buscar(contenido);
                if (simbolo != null && simbolo.getValor() != null) {
                    System.out.println("  " + simbolo.getValor());
                } else {
                    System.out.println("  [variable no inicializada]");
                }
            }
        } else if (instruccion.startsWith("READ")) {
            String var = instruccion.substring(5);
            System.out.println("  [SIMULACIÓN] Leyendo valor para '" + var + "' (asignando 42)");
            tablaSimbolos.asignar(var, 42);
        }
    }
    
    public void imprimirCodigoObjeto() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║         CÓDIGO OBJETO                 ║");
        System.out.println("╚════════════════════════════════════════╝");
        for (String linea : codigoObjeto) {
            System.out.println("  " + linea);
        }
        System.out.println("=========================================\n");
    }
}