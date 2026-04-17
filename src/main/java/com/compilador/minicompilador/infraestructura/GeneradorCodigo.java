//GeneradorCodigo.java
package com.compilador.minicompilador.infraestructura;

import com.compilador.minicompilador.dominio.*;
import java.util.*;

public class GeneradorCodigo {
    private List<String> codigoObjeto;
    private Map<String, Object> variables;
    private Scanner teclado;
    
    public GeneradorCodigo() {
        this.codigoObjeto = new ArrayList<>();
        this.variables = new HashMap<>();
        this.teclado = new Scanner(System.in);
    }
    
    public List<String> generarCodigoObjeto(List<String> codigoIntermedio, TablaSimbolos tablaSimbolos) {
        this.codigoObjeto.clear();
        this.variables.clear();
        
        // Optimizar código intermedio antes de generar código objeto
        List<String> codigoOptimizado = optimizarCodigo(codigoIntermedio);
        
        codigoObjeto.add("=== CÓDIGO OBJETO (EJECUTABLE) ===");
        codigoObjeto.add("Lenguaje: Élfico - Traducción a instrucciones de máquina virtual");
        codigoObjeto.add("Optimización: Eliminación de código muerto y propagación de constantes");
        codigoObjeto.add("");
        
        for (String instruccion : codigoOptimizado) {
            String codigoObj = traducirInstruccion(instruccion, tablaSimbolos);
            codigoObjeto.add(codigoObj);
        }
        
        codigoObjeto.add("");
        codigoObjeto.add("=== FIN CÓDIGO OBJETO ===");
        
        return codigoObjeto;
    }
    
    private List<String> optimizarCodigo(List<String> codigoIntermedio) {
        List<String> optimizado = new ArrayList<>();
        Set<String> variablesUsadas = new HashSet<>();
        Map<String, String> constantes = new HashMap<>();
        
        // Primera pasada: identificar variables usadas
        for (String instruccion : codigoIntermedio) {
            if (instruccion.contains("=") && !instruccion.startsWith("DECLARE")) {
                String[] partes = instruccion.split("=");
                if (partes.length == 2) {
                    String var = partes[0].trim();
                    variablesUsadas.add(var);
                    
                    // Propagación de constantes
                    String expr = partes[1].trim();
                    if (expr.matches("\\d+(\\.\\d+)?")) {
                        constantes.put(var, expr);
                    }
                }
            }
            
            // Identificar variables en PRINT y READ
            if (instruccion.startsWith("PRINT")) {
                String contenido = instruccion.substring(6).trim();
                if (!contenido.startsWith("\"")) {
                    variablesUsadas.add(contenido);
                }
            }
            if (instruccion.startsWith("READ")) {
                String var = instruccion.substring(5).trim();
                variablesUsadas.add(var);
            }
        }
        
        // Segunda pasada: optimizar y eliminar código muerto
        for (String instruccion : codigoIntermedio) {
            if (instruccion.startsWith("DECLARE")) {
                String[] partes = instruccion.split(" ");
                String var = partes[1];
                if (variablesUsadas.contains(var)) {
                    optimizado.add(instruccion);
                }
            } else if (instruccion.contains("=") && !instruccion.startsWith("DECLARE")) {
                String[] partes = instruccion.split("=");
                if (partes.length == 2) {
                    String var = partes[0].trim();
                    String expr = partes[1].trim();
                    
                    // Optimizar expresiones constantes
                    if (constantes.containsKey(var) && !variablesUsadas.contains(var)) {
                        continue; // Eliminar asignación a variable no usada
                    }
                    
                    // Propagar constantes en expresiones
                    for (Map.Entry<String, String> constante : constantes.entrySet()) {
                        if (expr.contains(constante.getKey())) {
                            expr = expr.replace(constante.getKey(), constante.getValue());
                        }
                    }
                    
                    optimizado.add(var + " = " + expr);
                } else {
                    optimizado.add(instruccion);
                }
            } else {
                optimizado.add(instruccion);
            }
        }
        
        System.out.println("  ✓ Optimización aplicada: " + codigoIntermedio.size() + " → " + optimizado.size() + " instrucciones");
        return optimizado;
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
        
        // Optimizar antes de ejecutar
        List<String> codigoOptimizado = optimizarCodigo(codigoIntermedio);
        
        // Mapa para almacenar valores temporales
        Map<String, Object> temporales = new HashMap<>();
        
        for (String instruccion : codigoOptimizado) {
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
        // Asignación con expresión directa: resultado = a + b
        else if (instruccion.matches("^[a-zA-Z][a-zA-Z0-9]*\\s*=\\s*.+$") && (instruccion.contains("+") || 
                 instruccion.contains("-") || instruccion.contains("*") || instruccion.contains("/"))) {
            String[] partes = instruccion.split("=");
            if (partes.length == 2) {
                String var = partes[0].trim();
                String expr = partes[1].trim();
                Object resultado = evaluarExpresion(expr, tablaSimbolos, temporales);
                tablaSimbolos.asignar(var, resultado);
            }
        }
        // PRINT
        else if (instruccion.startsWith("PRINT")) {
            String contenido = instruccion.substring(6).trim();
            
            if (contenido.startsWith("\"") && contenido.endsWith("\"")) {
                // Es una cadena literal
                System.out.println("  " + contenido.substring(1, contenido.length() - 1));
            } else {
                // Es una variable o expresión
                Object valor = evaluarExpresion(contenido, tablaSimbolos, temporales);
                System.out.println("  " + valor);
            }
        }
        // READ REAL desde teclado
        else if (instruccion.startsWith("READ")) {
            String var = instruccion.substring(5).trim();
            System.out.print("  Ingrese valor para '" + var + "': ");
            String input = teclado.nextLine().trim();
            
            // Determinar tipo de variable
            TablaSimbolos.Simbolo simbolo = tablaSimbolos.buscar(var);
            Object valorConvertido;
            
            if (simbolo != null) {
                if (simbolo.getTipo() == TipoToken.KEY_NAMPAT) {
                    // Entero
                    try {
                        valorConvertido = Integer.parseInt(input);
                    } catch (NumberFormatException e) {
                        valorConvertido = 0;
                        System.out.println("  [ERROR] Se esperaba un número entero, asignando 0");
                    }
                } else if (simbolo.getTipo() == TipoToken.KEY_LINTA) {
                    // Flotante
                    try {
                        valorConvertido = Double.parseDouble(input);
                    } catch (NumberFormatException e) {
                        valorConvertido = 0.0;
                        System.out.println("  [ERROR] Se esperaba un número decimal, asignando 0");
                    }
                } else {
                    // String
                    valorConvertido = input;
                }
                tablaSimbolos.asignar(var, valorConvertido);
            }
        }
        // IF condicional
        else if (instruccion.startsWith("IF")) {
            // Las instrucciones IF se manejan mediante los labels y GOTO
            // Esta ejecución es simplificada para el ejemplo
        }
        // GOTO
        else if (instruccion.startsWith("GOTO")) {
            // Los GOTO se manejan mediante el flujo de control
        }
        // Labels
        else if (instruccion.startsWith("L")) {
            // Los labels son marcadores de posición
        }
    }
    
    private Object evaluarExpresion(String expr, TablaSimbolos tablaSimbolos, Map<String, Object> temporales) {
        expr = expr.trim();
        
        // Manejar paréntesis
        if (expr.startsWith("(") && expr.endsWith(")")) {
            expr = expr.substring(1, expr.length() - 1);
        }
        
        // Manejar operaciones con temporales y variables
        String[] operadores = {"+", "-", "*", "/"};
        for (String op : operadores) {
            if (expr.contains(op) && !expr.startsWith("\"")) {
                String[] partes = separarExpresion(expr, op);
                if (partes.length == 2) {
                    Object izquierda = obtenerValor(partes[0].trim(), tablaSimbolos, temporales);
                    Object derecha = obtenerValor(partes[1].trim(), tablaSimbolos, temporales);
                    
                    // Conversión automática a double si hay operación mixta
                    boolean esMixta = (izquierda instanceof Double && derecha instanceof Integer) ||
                                      (izquierda instanceof Integer && derecha instanceof Double);
                    
                    if (esMixta) {
                        double left = (izquierda instanceof Integer) ? ((Integer) izquierda).doubleValue() : (Double) izquierda;
                        double right = (derecha instanceof Integer) ? ((Integer) derecha).doubleValue() : (Double) derecha;
                        
                        switch (op) {
                            case "+": return left + right;
                            case "-": return left - right;
                            case "*": return left * right;
                            case "/": return left / right;
                        }
                    } else if (izquierda instanceof Integer && derecha instanceof Integer) {
                        switch (op) {
                            case "+": return (Integer) izquierda + (Integer) derecha;
                            case "-": return (Integer) izquierda - (Integer) derecha;
                            case "*": return (Integer) izquierda * (Integer) derecha;
                            case "/": return (Integer) izquierda / (Integer) derecha;
                        }
                    } else if (izquierda instanceof Double || derecha instanceof Double) {
                        double left = (izquierda instanceof Integer) ? ((Integer) izquierda).doubleValue() : (Double) izquierda;
                        double right = (derecha instanceof Integer) ? ((Integer) derecha).doubleValue() : (Double) derecha;
                        
                        switch (op) {
                            case "+": return left + right;
                            case "-": return left - right;
                            case "*": return left * right;
                            case "/": return left / right;
                        }
                    }
                }
            }
        }
        
        // Número directo
        if (expr.matches("\\d+")) {
            return Integer.parseInt(expr);
        } else if (expr.matches("\\d+\\.\\d+")) {
            return Double.parseDouble(expr);
        }
        // Cadena
        else if (expr.startsWith("\"") && expr.endsWith("\"")) {
            return expr.substring(1, expr.length() - 1);
        }
        // Variable
        else if (expr.matches("[a-zA-Z][a-zA-Z0-9]*")) {
            TablaSimbolos.Simbolo simbolo = tablaSimbolos.buscar(expr);
            if (simbolo != null && simbolo.getValor() != null) {
                return simbolo.getValor();
            }
            return 0;
        }
        // Temporal
        else if (expr.matches("t\\d+")) {
            if (temporales.containsKey(expr)) {
                return temporales.get(expr);
            }
            return 0;
        }
        
        return 0;
    }
    
    private String[] separarExpresion(String expr, String operador) {
        int index = expr.lastIndexOf(operador);
        if (index > 0) {
            String izquierda = expr.substring(0, index);
            String derecha = expr.substring(index + 1);
            return new String[]{izquierda, derecha};
        }
        return new String[]{expr};
    }
    
    private Object obtenerValor(String token, TablaSimbolos tablaSimbolos, Map<String, Object> temporales) {
        if (token.matches("\\d+")) {
            return Integer.parseInt(token);
        } else if (token.matches("\\d+\\.\\d+")) {
            return Double.parseDouble(token);
        } else if (token.startsWith("\"") && token.endsWith("\"")) {
            return token.substring(1, token.length() - 1);
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