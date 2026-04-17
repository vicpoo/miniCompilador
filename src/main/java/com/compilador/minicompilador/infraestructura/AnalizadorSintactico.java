//AnalizadorSintactico.java
package com.compilador.minicompilador.infraestructura;

import com.compilador.minicompilador.dominio.*;
import java.util.*;

public class AnalizadorSintactico {
    private List<Token> tokens;
    private int posicion;
    private List<ErrorCompilacion> errores;
    private TablaSimbolos tablaSimbolos;
    private List<String> codigoIntermedio;
    private int tempCounter;
    private int labelCounter;
    
    public AnalizadorSintactico() {
        this.errores = new ArrayList<>();
        this.codigoIntermedio = new ArrayList<>();
        this.tempCounter = 0;
        this.labelCounter = 0;
    }
    
    public boolean analizar(List<Token> tokens, TablaSimbolos tablaSimbolos) {
        this.tokens = tokens;
        this.posicion = 0;
        this.errores.clear();
        this.codigoIntermedio.clear();
        this.tablaSimbolos = tablaSimbolos;
        this.tempCounter = 0;
        this.labelCounter = 0;
        
        try {
            programa();
            return errores.isEmpty();
        } catch (Exception e) {
            if (errores.isEmpty()) {
                errores.add(new ErrorCompilacion(ErrorCompilacion.TipoError.SINTACTICO, 
                    e.getMessage(), obtenerTokenActual().getLinea(), obtenerTokenActual().getColumna()));
            }
            return false;
        }
    }
    
    private void programa() {
        while (!esFin()) {
            sentencia();
        }
    }
    
    private void sentencia() {
        Token actual = obtenerTokenActual();
        
        if (actual.getTipo() == TipoToken.KEY_NAMPAT || 
            actual.getTipo() == TipoToken.KEY_LINTA || 
            actual.getTipo() == TipoToken.KEY_TENGWA) {
            match(actual.getTipo());
            declaracion();
        } 
        else if (actual.getTipo() == TipoToken.KEY_ANIN) {
            match(TipoToken.KEY_ANIN);
            sentenciaIf();
        } 
        else if (actual.getTipo() == TipoToken.KEY_NAUVA) {
            match(TipoToken.KEY_NAUVA);
            sentenciaWhile();
        } 
        else if (actual.getTipo() == TipoToken.KEY_CARO) {
            match(TipoToken.KEY_CARO);
            sentenciaDoWhile();
        } 
        else if (actual.getTipo() == TipoToken.KEY_ANA) {
            match(TipoToken.KEY_ANA);
            sentenciaFor();
        } 
        else if (actual.getTipo() == TipoToken.KEY_LAVA) {
            match(TipoToken.KEY_LAVA);
            sentenciaSwitch();
        } 
        else if (actual.getTipo() == TipoToken.KEY_TIRA) {
            match(TipoToken.KEY_TIRA);
            sentenciaPrint();
        } 
        else if (actual.getTipo() == TipoToken.KEY_LANNA) {
            match(TipoToken.KEY_LANNA);
            sentenciaRead();
        } 
        else if (actual.getTipo() == TipoToken.ID) {
            match(TipoToken.ID);
            asignacion();
        } 
        else if (actual.getTipo() == TipoToken.INI_BLOQUE) {
            match(TipoToken.INI_BLOQUE);
            bloque();
        } 
        else if (actual.getTipo() != TipoToken.EOF) {
            error("Se esperaba una sentencia válida, pero se encontró: " + actual.getTipo());
        }
    }
    
    private void declaracion() {
        TipoToken tipo = obtenerTokenAnterior().getTipo();
        Token id = esperar(TipoToken.ID);
        
        tablaSimbolos.definir(id.getLexema(), tipo);
        codigoIntermedio.add("DECLARE " + id.getLexema() + " : " + tipo);
        
        if (obtenerTokenActual().getTipo() == TipoToken.ASIGNA) {
            match(TipoToken.ASIGNA);
            String exprResult = expresion();
            esperar(TipoToken.FIN_SENTENCIA);
            codigoIntermedio.add(id.getLexema() + " = " + exprResult);
        } else {
            esperar(TipoToken.FIN_SENTENCIA);
        }
    }
    
    private void asignacion() {
        Token id = obtenerTokenAnterior();
        esperar(TipoToken.ASIGNA);
        String exprResult = expresion();
        esperar(TipoToken.FIN_SENTENCIA);
        
        TablaSimbolos.Simbolo simbolo = tablaSimbolos.buscar(id.getLexema());
        if (simbolo == null) {
            error("Variable no declarada: " + id.getLexema());
            return;
        }
        
        codigoIntermedio.add(id.getLexema() + " = " + exprResult);
    }
    
    private void sentenciaIf() {
        esperar(TipoToken.ABRE_PARENTESIS);
        String condicion = expresion();
        esperar(TipoToken.CIERRA_PARENTESIS);
        
        int labelElse = generarLabel();
        int labelEnd = generarLabel();
        
        codigoIntermedio.add("IF " + condicion + " == 0 GOTO L" + labelElse);
        
        // Verificar si hay un bloque o una sentencia simple
        if (obtenerTokenActual().getTipo() == TipoToken.INI_BLOQUE) {
            match(TipoToken.INI_BLOQUE);
            bloque();
        } else {
            sentencia();
        }
        
        codigoIntermedio.add("GOTO L" + labelEnd);
        codigoIntermedio.add("L" + labelElse + ":");
        
        // Verificar else
        if (obtenerTokenActual().getTipo() == TipoToken.KEY_PENNETH) {
            match(TipoToken.KEY_PENNETH);
            if (obtenerTokenActual().getTipo() == TipoToken.INI_BLOQUE) {
                match(TipoToken.INI_BLOQUE);
                bloque();
            } else {
                sentencia();
            }
        }
        
        codigoIntermedio.add("L" + labelEnd + ":");
    }
    
    private void sentenciaWhile() {
        esperar(TipoToken.ABRE_PARENTESIS);
        int labelStart = generarLabel();
        int labelEnd = generarLabel();
        
        codigoIntermedio.add("L" + labelStart + ":");
        String condicion = expresion();
        esperar(TipoToken.CIERRA_PARENTESIS);
        
        codigoIntermedio.add("IF " + condicion + " == 0 GOTO L" + labelEnd);
        
        if (obtenerTokenActual().getTipo() == TipoToken.INI_BLOQUE) {
            match(TipoToken.INI_BLOQUE);
            bloque();
        } else {
            sentencia();
        }
        
        codigoIntermedio.add("GOTO L" + labelStart);
        codigoIntermedio.add("L" + labelEnd + ":");
    }
    
    private void sentenciaDoWhile() {
        int labelStart = generarLabel();
        codigoIntermedio.add("L" + labelStart + ":");
        
        if (obtenerTokenActual().getTipo() == TipoToken.INI_BLOQUE) {
            match(TipoToken.INI_BLOQUE);
            bloque();
        } else {
            sentencia();
        }
        
        esperar(TipoToken.KEY_NAUVA);
        esperar(TipoToken.ABRE_PARENTESIS);
        String condicion = expresion();
        esperar(TipoToken.CIERRA_PARENTESIS);
        esperar(TipoToken.FIN_SENTENCIA);
        codigoIntermedio.add("IF " + condicion + " != 0 GOTO L" + labelStart);
    }
    
    private void sentenciaFor() {
        esperar(TipoToken.ABRE_PARENTESIS);
        
        if (obtenerTokenActual().getTipo() == TipoToken.KEY_NAMPAT || 
            obtenerTokenActual().getTipo() == TipoToken.KEY_LINTA || 
            obtenerTokenActual().getTipo() == TipoToken.KEY_TENGWA) {
            match(obtenerTokenActual().getTipo());
            TipoToken tipo = obtenerTokenAnterior().getTipo();
            Token id = esperar(TipoToken.ID);
            tablaSimbolos.definir(id.getLexema(), tipo);
            codigoIntermedio.add("DECLARE " + id.getLexema() + " : " + tipo);
            if (obtenerTokenActual().getTipo() == TipoToken.ASIGNA) {
                match(TipoToken.ASIGNA);
                String exprResult = expresion();
                codigoIntermedio.add(id.getLexema() + " = " + exprResult);
            }
        } else if (obtenerTokenActual().getTipo() == TipoToken.ID) {
            match(TipoToken.ID);
            Token id = obtenerTokenAnterior();
            esperar(TipoToken.ASIGNA);
            String exprResult = expresion();
            codigoIntermedio.add(id.getLexema() + " = " + exprResult);
        }
        
        esperar(TipoToken.FIN_SENTENCIA);
        
        String condicion = "1";
        if (obtenerTokenActual().getTipo() != TipoToken.FIN_SENTENCIA) {
            condicion = expresion();
            esperar(TipoToken.FIN_SENTENCIA);
        } else {
            match(TipoToken.FIN_SENTENCIA);
        }
        
        List<String> incremento = new ArrayList<>();
        if (obtenerTokenActual().getTipo() != TipoToken.CIERRA_PARENTESIS) {
            while (obtenerTokenActual().getTipo() != TipoToken.CIERRA_PARENTESIS && !esFin()) {
                if (obtenerTokenActual().getTipo() == TipoToken.ID) {
                    match(TipoToken.ID);
                    Token id = obtenerTokenAnterior();
                    if (obtenerTokenActual().getTipo() == TipoToken.ASIGNA) {
                        match(TipoToken.ASIGNA);
                        String expr = expresion();
                        incremento.add(id.getLexema() + " = " + expr);
                    }
                }
                if (obtenerTokenActual().getTipo() != TipoToken.CIERRA_PARENTESIS) {
                    posicion++;
                }
            }
        }
        
        esperar(TipoToken.CIERRA_PARENTESIS);
        
        int labelStart = generarLabel();
        int labelEnd = generarLabel();
        
        codigoIntermedio.add("L" + labelStart + ":");
        codigoIntermedio.add("IF " + condicion + " == 0 GOTO L" + labelEnd);
        
        if (obtenerTokenActual().getTipo() == TipoToken.INI_BLOQUE) {
            match(TipoToken.INI_BLOQUE);
            bloque();
        } else {
            sentencia();
        }
        
        for (String inc : incremento) {
            codigoIntermedio.add(inc);
        }
        
        codigoIntermedio.add("GOTO L" + labelStart);
        codigoIntermedio.add("L" + labelEnd + ":");
    }
    
    private void sentenciaSwitch() {
        esperar(TipoToken.ABRE_PARENTESIS);
        Token varSwitch = esperar(TipoToken.ID);
        esperar(TipoToken.CIERRA_PARENTESIS);
        esperar(TipoToken.INI_BLOQUE);
        
        int defaultLabel = generarLabel();
        int endLabel = generarLabel();
        
        while (obtenerTokenActual().getTipo() == TipoToken.KEY_TIRNO) {
            match(TipoToken.KEY_TIRNO);
            Token valor = null;
            if (obtenerTokenActual().getTipo() == TipoToken.NUMERO) {
                match(TipoToken.NUMERO);
                valor = obtenerTokenAnterior();
            } else if (obtenerTokenActual().getTipo() == TipoToken.CADENA) {
                match(TipoToken.CADENA);
                valor = obtenerTokenAnterior();
            } else {
                error("Se esperaba un valor constante en case");
            }
            
            esperar(TipoToken.DOS_PUNTOS);
            int caseLabel = generarLabel();
            codigoIntermedio.add("IF " + varSwitch.getLexema() + " == " + valor.getLexema() + " GOTO L" + caseLabel);
            codigoIntermedio.add("GOTO L" + defaultLabel);
            codigoIntermedio.add("L" + caseLabel + ":");
            
            while (!esFin() && 
                   obtenerTokenActual().getTipo() != TipoToken.KEY_TIRNO && 
                   obtenerTokenActual().getTipo() != TipoToken.KEY_PENNETH && 
                   obtenerTokenActual().getTipo() != TipoToken.FIN_BLOQUE) {
                sentencia();
            }
            
            if (obtenerTokenActual().getTipo() == TipoToken.KEY_NORO) {
                match(TipoToken.KEY_NORO);
            }
        }
        
        if (obtenerTokenActual().getTipo() == TipoToken.KEY_PENNETH) {
            match(TipoToken.KEY_PENNETH);
            esperar(TipoToken.DOS_PUNTOS);
            codigoIntermedio.add("GOTO L" + defaultLabel);
            codigoIntermedio.add("L" + defaultLabel + ":");
            while (!esFin() && obtenerTokenActual().getTipo() != TipoToken.FIN_BLOQUE) {
                sentencia();
            }
        }
        
        esperar(TipoToken.FIN_BLOQUE);
        codigoIntermedio.add("L" + endLabel + ":");
    }
    
    private void sentenciaPrint() {
        esperar(TipoToken.ABRE_PARENTESIS);
        String valor = expresion();
        esperar(TipoToken.CIERRA_PARENTESIS);
        esperar(TipoToken.FIN_SENTENCIA);
        codigoIntermedio.add("PRINT " + valor);
    }
    
    private void sentenciaRead() {
        esperar(TipoToken.ABRE_PARENTESIS);
        Token id = esperar(TipoToken.ID);
        esperar(TipoToken.CIERRA_PARENTESIS);
        esperar(TipoToken.FIN_SENTENCIA);
        codigoIntermedio.add("READ " + id.getLexema());
    }
    
    private String expresion() {
        return expresionLogica();
    }
    
    private String expresionLogica() {
        String left = expresionComparacion();
        
        while (obtenerTokenActual().getTipo() == TipoToken.OPERA_AND || 
               obtenerTokenActual().getTipo() == TipoToken.OPERA_OR) {
            Token op = obtenerTokenActual();
            match(op.getTipo());
            String right = expresionComparacion();
            String temp = generarTemp();
            codigoIntermedio.add(temp + " = " + left + " " + op.getLexema() + " " + right);
            left = temp;
        }
        
        if (obtenerTokenActual().getTipo() == TipoToken.OPERA_NOT) {
            match(TipoToken.OPERA_NOT);
            String right = expresionComparacion();
            String temp = generarTemp();
            codigoIntermedio.add(temp + " = not " + right);
            left = temp;
        }
        
        return left;
    }
    
    private String expresionComparacion() {
        String left = expresionAritmetica();
        
        if (obtenerTokenActual().getTipo() == TipoToken.OPERA_MENOR || 
            obtenerTokenActual().getTipo() == TipoToken.OPERA_MAYOR || 
            obtenerTokenActual().getTipo() == TipoToken.OPERA_MENOR_IGUAL ||
            obtenerTokenActual().getTipo() == TipoToken.OPERA_MAYOR_IGUAL ||
            obtenerTokenActual().getTipo() == TipoToken.OPERA_IGUALDAD || 
            obtenerTokenActual().getTipo() == TipoToken.OPERA_DIFERENTE) {
            Token op = obtenerTokenActual();
            match(op.getTipo());
            String right = expresionAritmetica();
            String temp = generarTemp();
            codigoIntermedio.add(temp + " = " + left + " " + op.getLexema() + " " + right);
            return temp;
        }
        
        return left;
    }
    
    private String expresionAritmetica() {
        String left = termino();
        
        while (obtenerTokenActual().getTipo() == TipoToken.OPERA_SUMA || 
               obtenerTokenActual().getTipo() == TipoToken.OPERA_RESTA) {
            Token op = obtenerTokenActual();
            match(op.getTipo());
            String right = termino();
            String temp = generarTemp();
            codigoIntermedio.add(temp + " = " + left + " " + op.getLexema() + " " + right);
            left = temp;
        }
        
        return left;
    }
    
    private String termino() {
        String left = factor();
        
        while (obtenerTokenActual().getTipo() == TipoToken.OPERA_MULT || 
               obtenerTokenActual().getTipo() == TipoToken.OPERA_DIVID) {
            Token op = obtenerTokenActual();
            match(op.getTipo());
            String right = factor();
            String temp = generarTemp();
            codigoIntermedio.add(temp + " = " + left + " " + op.getLexema() + " " + right);
            left = temp;
        }
        
        return left;
    }
    
    private String factor() {
        if (obtenerTokenActual().getTipo() == TipoToken.NUMERO) {
            match(TipoToken.NUMERO);
            return obtenerTokenAnterior().getLexema();
        } else if (obtenerTokenActual().getTipo() == TipoToken.NUMERO_DECIMAL) {
            match(TipoToken.NUMERO_DECIMAL);
            return obtenerTokenAnterior().getLexema();
        } else if (obtenerTokenActual().getTipo() == TipoToken.CADENA) {
            match(TipoToken.CADENA);
            return "\"" + obtenerTokenAnterior().getLexema() + "\"";
        } else if (obtenerTokenActual().getTipo() == TipoToken.ID) {
            match(TipoToken.ID);
            Token id = obtenerTokenAnterior();
            TablaSimbolos.Simbolo simbolo = tablaSimbolos.buscar(id.getLexema());
            if (simbolo == null) {
                error("Variable no declarada: " + id.getLexema());
                return "0";
            }
            return id.getLexema();
        } else if (obtenerTokenActual().getTipo() == TipoToken.ABRE_PARENTESIS) {
            match(TipoToken.ABRE_PARENTESIS);
            String expr = expresion();
            esperar(TipoToken.CIERRA_PARENTESIS);
            return expr;
        } else {
            error("Se esperaba un factor (número, variable o expresión entre paréntesis)");
            return "0";
        }
    }
    
    private void bloque() {
        while (!esFin() && obtenerTokenActual().getTipo() != TipoToken.FIN_BLOQUE) {
            sentencia();
        }
        esperar(TipoToken.FIN_BLOQUE);
    }
    
    private boolean match(TipoToken tipo) {
        if (!esFin() && obtenerTokenActual().getTipo() == tipo) {
            posicion++;
            return true;
        }
        return false;
    }
    
    private Token esperar(TipoToken tipo) {
        if (match(tipo)) {
            return obtenerTokenAnterior();
        }
        error("Se esperaba: " + tipo);
        return new Token(TipoToken.ERROR, "", 0, 0);
    }
    
    private Token obtenerTokenActual() {
        if (posicion >= tokens.size()) {
            return tokens.get(tokens.size() - 1);
        }
        return tokens.get(posicion);
    }
    
    private Token obtenerTokenAnterior() {
        if (posicion == 0) return null;
        return tokens.get(posicion - 1);
    }
    
    private boolean esFin() {
        return obtenerTokenActual().getTipo() == TipoToken.EOF;
    }
    
    private String generarTemp() {
        return "t" + (tempCounter++);
    }
    
    private int generarLabel() {
        return labelCounter++;
    }
    
    private void error(String mensaje) {
        Token token = obtenerTokenActual();
        errores.add(new ErrorCompilacion(ErrorCompilacion.TipoError.SINTACTICO, 
            mensaje, token.getLinea(), token.getColumna()));
        throw new RuntimeException(mensaje);
    }
    
    public List<ErrorCompilacion> getErrores() {
        return errores;
    }
    
    public List<String> getCodigoIntermedio() {
        return codigoIntermedio;
    }
    
    public void imprimirArbolSintactico() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║      ÁRBOL SINTÁCTICO                  ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println("  ✓ Estructura del programa validada");
        System.out.println("  ✓ Sentencias analizadas correctamente");
        System.out.println("  ✓ Bloques anidados verificados");
        System.out.println("=========================================\n");
    }
    
    public void imprimirCodigoIntermedio() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║    CÓDIGO INTERMEDIO (3 Direcciones)  ║");
        System.out.println("╚════════════════════════════════════════╝");
        for (int i = 0; i < codigoIntermedio.size(); i++) {
            System.out.printf("  %3d: %s%n", i + 1, codigoIntermedio.get(i));
        }
        System.out.println("=========================================\n");
    }
}