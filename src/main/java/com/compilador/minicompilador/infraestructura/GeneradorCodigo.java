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
        
        // Mapa para almacenar valores temporales
        Map<String, Object> temporales = new HashMap<>();
        
        for (String instruccion : codigoIntermedio) {
            ejecutarInstruccion(instruccion, tablaSimbolos, temporales);
        }
        
        System.out.println("=========================================\n");
    }
    
    private void ejecutarInstruccion(String instruccion, TablaSimbolos tablaSimbolos, Map<String, Object> temporales) {
        // Declaración de variable
        if (instruccion.startsWith("DECLARE")) {
            String[] partes = instruccion.split(" ");
            String var = partes[1];
            // No hacer nada, solo registrar que existe
        }
        // Asignación directa: a = 12
        else if (instruccion.matches("^[a-zA-Z][a-zA-Z0-9]*\\s*=\\s*\\d+(\\.\\d+)?$")) {
            String[] partes = instruccion.split("=");
            String var = partes[0].trim();
            String valorStr = partes[1].trim();
            
            Object valor;
            if (valorStr.contains(".")) {
                valor = Double.parseDouble(valorStr);
            } else {
                valor = Integer.parseInt(valorStr);
            }
            
            tablaSimbolos.asignar(var, valor);
        }
        // Asignación de resultado de operación: resultado = t0
        else if (instruccion.matches("^[a-zA-Z][a-zA-Z0-9]*\\s*=\\s*t\\d+$")) {
            String[] partes = instruccion.split("=");
            String var = partes[0].trim();
            String temp = partes[1].trim();
            
            Object valor = temporales.get(temp);
            if (valor != null) {
                tablaSimbolos.asignar(var, valor);
            }
        }
        // Operación temporal: t0 = a + b
        else if (instruccion.matches("^t\\d+\\s*=\\s*.+$")) {
            String[] partes = instruccion.split("=");
            String temp = partes[0].trim();
            String expr = partes[1].trim();
            
            Object resultado = evaluarExpresion(expr, tablaSimbolos, temporales);
            temporales.put(temp, resultado);
        }
        // PRINT
        else if (instruccion.startsWith("PRINT")) {
            String contenido = instruccion.substring(6).trim();
            
            if (contenido.startsWith("\"") && contenido.endsWith("\"")) {
                // Es una cadena literal
                System.out.println("  " + contenido.substring(1, contenido.length() - 1));
            } else {
                // Es una variable
                TablaSimbolos.Simbolo simbolo = tablaSimbolos.buscar(contenido);
                if (simbolo != null && simbolo.getValor() != null) {
                    System.out.println("  " + simbolo.getValor());
                } else {
                    System.out.println("  [variable no inicializada: " + contenido + "]");
                }
            }
        }
        // READ
        else if (instruccion.startsWith("READ")) {
            String var = instruccion.substring(5).trim();
            System.out.println("  [SIMULACIÓN] Leyendo valor para '" + var + "' (asignando 18)");
            tablaSimbolos.asignar(var, 18);
        }
    }
    
    private Object evaluarExpresion(String expr, TablaSimbolos tablaSimbolos, Map<String, Object> temporales) {
        expr = expr.trim();
        
        // Manejar operaciones con temporales: t0 + t1
        if (expr.contains("+")) {
            String[] partes = expr.split("\\+");
            Object izquierda = obtenerValor(partes[0].trim(), tablaSimbolos, temporales);
            Object derecha = obtenerValor(partes[1].trim(), tablaSimbolos, temporales);
            
            if (izquierda instanceof Integer && derecha instanceof Integer) {
                return (Integer) izquierda + (Integer) derecha;
            } else if (izquierda instanceof Double || derecha instanceof Double) {
                double left = (izquierda instanceof Integer) ? ((Integer) izquierda).doubleValue() : (Double) izquierda;
                double right = (derecha instanceof Integer) ? ((Integer) derecha).doubleValue() : (Double) derecha;
                return left + right;
            }
        }
        // Manejar operaciones con temporales: t0 - t1
        else if (expr.contains("-")) {
            String[] partes = expr.split("-");
            Object izquierda = obtenerValor(partes[0].trim(), tablaSimbolos, temporales);
            Object derecha = obtenerValor(partes[1].trim(), tablaSimbolos, temporales);
            
            if (izquierda instanceof Integer && derecha instanceof Integer) {
                return (Integer) izquierda - (Integer) derecha;
            } else if (izquierda instanceof Double || derecha instanceof Double) {
                double left = (izquierda instanceof Integer) ? ((Integer) izquierda).doubleValue() : (Double) izquierda;
                double right = (derecha instanceof Integer) ? ((Integer) derecha).doubleValue() : (Double) derecha;
                return left - right;
            }
        }
        // Manejar operaciones con temporales: t0 * t1
        else if (expr.contains("*")) {
            String[] partes = expr.split("\\*");
            Object izquierda = obtenerValor(partes[0].trim(), tablaSimbolos, temporales);
            Object derecha = obtenerValor(partes[1].trim(), tablaSimbolos, temporales);
            
            if (izquierda instanceof Integer && derecha instanceof Integer) {
                return (Integer) izquierda * (Integer) derecha;
            } else if (izquierda instanceof Double || derecha instanceof Double) {
                double left = (izquierda instanceof Integer) ? ((Integer) izquierda).doubleValue() : (Double) izquierda;
                double right = (derecha instanceof Integer) ? ((Integer) derecha).doubleValue() : (Double) derecha;
                return left * right;
            }
        }
        // Manejar operaciones con temporales: t0 / t1
        else if (expr.contains("/")) {
            String[] partes = expr.split("/");
            Object izquierda = obtenerValor(partes[0].trim(), tablaSimbolos, temporales);
            Object derecha = obtenerValor(partes[1].trim(), tablaSimbolos, temporales);
            
            if (izquierda instanceof Integer && derecha instanceof Integer) {
                return (Integer) izquierda / (Integer) derecha;
            } else if (izquierda instanceof Double || derecha instanceof Double) {
                double left = (izquierda instanceof Integer) ? ((Integer) izquierda).doubleValue() : (Double) izquierda;
                double right = (derecha instanceof Integer) ? ((Integer) derecha).doubleValue() : (Double) derecha;
                return left / right;
            }
        }
        // Número directo
        else if (expr.matches("\\d+")) {
            return Integer.parseInt(expr);
        } else if (expr.matches("\\d+\\.\\d+")) {
            return Double.parseDouble(expr);
        }
        // Variable
        else if (expr.matches("[a-zA-Z][a-zA-Z0-9]*")) {
            TablaSimbolos.Simbolo simbolo = tablaSimbolos.buscar(expr);
            if (simbolo != null && simbolo.getValor() != null) {
                return simbolo.getValor();
            }
        }
        // Temporal
        else if (expr.matches("t\\d+")) {
            if (temporales.containsKey(expr)) {
                return temporales.get(expr);
            }
        }
        
        return 0;
    }
    
    private Object obtenerValor(String token, TablaSimbolos tablaSimbolos, Map<String, Object> temporales) {
        if (token.matches("\\d+")) {
            return Integer.parseInt(token);
        } else if (token.matches("\\d+\\.\\d+")) {
            return Double.parseDouble(token);
        } else if (token.matches("[a-zA-Z][a-zA-Z0-9]*")) {
            TablaSimbolos.Simbolo simbolo = tablaSimbolos.buscar(token);
            if (simbolo != null && simbolo.getValor() != null) {
                return simbolo.getValor();
            }
            return 0;
        } else if (token.matches("t\\d+")) {
            if (temporales.containsKey(token)) {
                return temporales.get(token);
            }
            return 0;
        }
        return 0;
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