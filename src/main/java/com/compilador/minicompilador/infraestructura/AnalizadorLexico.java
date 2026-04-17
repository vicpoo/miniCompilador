//Analizadorlexico.java
package com.compilador.minicompilador.infraestructura;

import com.compilador.minicompilador.dominio.*;
import java.util.*;

public class AnalizadorLexico {
    private String codigo;
    private int posicion;
    private int linea;
    private int columna;
    private List<Token> tokens;
    private List<ErrorCompilacion> errores;
    
    private static final Map<String, TipoToken> palabrasReservadas = new HashMap<>();
    
    static {
        palabrasReservadas.put("anin", TipoToken.KEY_ANIN);
        palabrasReservadas.put("penneth", TipoToken.KEY_PENNETH);
        palabrasReservadas.put("caro", TipoToken.KEY_CARO);
        palabrasReservadas.put("nauva", TipoToken.KEY_NAUVA);
        palabrasReservadas.put("lava", TipoToken.KEY_LAVA);
        palabrasReservadas.put("noro", TipoToken.KEY_NORO);
        palabrasReservadas.put("ana", TipoToken.KEY_ANA);
        palabrasReservadas.put("tirno", TipoToken.KEY_TIRNO);
        palabrasReservadas.put("tira", TipoToken.KEY_TIRA);
        palabrasReservadas.put("lanna", TipoToken.KEY_LANNA);
        palabrasReservadas.put("nampat", TipoToken.KEY_NAMPAT);
        palabrasReservadas.put("linta", TipoToken.KEY_LINTA);
        palabrasReservadas.put("tengwa", TipoToken.KEY_TENGWA);
        palabrasReservadas.put("and", TipoToken.OPERA_AND);
        palabrasReservadas.put("or", TipoToken.OPERA_OR);
        palabrasReservadas.put("not", TipoToken.OPERA_NOT);
    }
    
    public AnalizadorLexico() {
        this.tokens = new ArrayList<>();
        this.errores = new ArrayList<>();
    }
    
    public List<Token> analizar(String codigoFuente) {
        this.codigo = codigoFuente;
        this.posicion = 0;
        this.linea = 1;
        this.columna = 1;
        this.tokens.clear();
        this.errores.clear();
        
        while (posicion < codigo.length()) {
            char actual = caracterActual();
            
            if (Character.isWhitespace(actual)) {
                if (actual == '\n') {
                    linea++;
                    columna = 1;
                } else {
                    columna++;
                }
                posicion++;
                continue;
            }
            
            if (actual == '/' && siguienteCaracter() == '/') {
                ignorarComentario();
                continue;
            }
            
            if (Character.isDigit(actual)) {
                leerNumero();
                continue;
            }
            
            if (Character.isLetter(actual) || actual == '_') {
                leerIdentificadorOPalabraReservada();
                continue;
            }
            
            if (actual == '"') {
                leerCadena();
                continue;
            }
            
            leerOperacionODelimitador();
        }
        
        tokens.add(new Token(TipoToken.EOF, "", linea, columna));
        return tokens;
    }
    
    private char caracterActual() {
        return codigo.charAt(posicion);
    }
    
    private char siguienteCaracter() {
        if (posicion + 1 >= codigo.length()) return '\0';
        return codigo.charAt(posicion + 1);
    }
    
    private void ignorarComentario() {
        while (posicion < codigo.length() && caracterActual() != '\n') {
            posicion++;
        }
    }
    
    private void leerNumero() {
        int inicioCol = columna;
        StringBuilder numero = new StringBuilder();
        boolean esDecimal = false;
        
        while (posicion < codigo.length() && (Character.isDigit(caracterActual()) || caracterActual() == '.')) {
            if (caracterActual() == '.') {
                if (esDecimal) break;
                esDecimal = true;
            }
            numero.append(caracterActual());
            posicion++;
            columna++;
        }
        
        if (esDecimal) {
            tokens.add(new Token(TipoToken.NUMERO_DECIMAL, numero.toString(), linea, inicioCol));
        } else {
            tokens.add(new Token(TipoToken.NUMERO, numero.toString(), linea, inicioCol));
        }
    }
    
    private void leerIdentificadorOPalabraReservada() {
        int inicioCol = columna;
        StringBuilder texto = new StringBuilder();
        
        while (posicion < codigo.length() && (Character.isLetterOrDigit(caracterActual()) || caracterActual() == '_')) {
            texto.append(caracterActual());
            posicion++;
            columna++;
        }
        
        String lexema = texto.toString();
        TipoToken tipo = palabrasReservadas.getOrDefault(lexema, TipoToken.ID);
        
        tokens.add(new Token(tipo, lexema, linea, inicioCol));
    }
    
    private void leerCadena() {
        int inicioCol = columna;
        StringBuilder cadena = new StringBuilder();
        posicion++;
        columna++;
        
        while (posicion < codigo.length() && caracterActual() != '"') {
            if (caracterActual() == '\n') {
                errores.add(new ErrorCompilacion(ErrorCompilacion.TipoError.LEXICO, 
                    "Cadena no terminada", linea, inicioCol));
                return;
            }
            cadena.append(caracterActual());
            posicion++;
            columna++;
        }
        
        if (caracterActual() == '"') {
            posicion++;
            columna++;
            tokens.add(new Token(TipoToken.CADENA, cadena.toString(), linea, inicioCol));
        } else {
            errores.add(new ErrorCompilacion(ErrorCompilacion.TipoError.LEXICO, 
                "Cadena no terminada", linea, inicioCol));
        }
    }
    
    private void leerOperacionODelimitador() {
        char actual = caracterActual();
        int inicioCol = columna;
        
        switch (actual) {
            case '=':
                if (siguienteCaracter() == '=') {
                    tokens.add(new Token(TipoToken.OPERA_IGUALDAD, "==", linea, inicioCol));
                    posicion += 2;
                    columna += 2;
                } else {
                    tokens.add(new Token(TipoToken.ASIGNA, "=", linea, inicioCol));
                    posicion++;
                    columna++;
                }
                break;
            case '!':
                if (siguienteCaracter() == '=') {
                    tokens.add(new Token(TipoToken.OPERA_DIFERENTE, "!=", linea, inicioCol));
                    posicion += 2;
                    columna += 2;
                } else {
                    errorLexico("Carácter inesperado: " + actual, inicioCol);
                    posicion++;
                    columna++;
                }
                break;
            case '<':
                if (siguienteCaracter() == '=') {
                    tokens.add(new Token(TipoToken.OPERA_MENOR_IGUAL, "<=", linea, inicioCol));
                    posicion += 2;
                    columna += 2;
                } else {
                    tokens.add(new Token(TipoToken.OPERA_MENOR, "<", linea, inicioCol));
                    posicion++;
                    columna++;
                }
                break;
            case '>':
                if (siguienteCaracter() == '=') {
                    tokens.add(new Token(TipoToken.OPERA_MAYOR_IGUAL, ">=", linea, inicioCol));
                    posicion += 2;
                    columna += 2;
                } else {
                    tokens.add(new Token(TipoToken.OPERA_MAYOR, ">", linea, inicioCol));
                    posicion++;
                    columna++;
                }
                break;
            case '+':
                tokens.add(new Token(TipoToken.OPERA_SUMA, "+", linea, inicioCol));
                posicion++;
                columna++;
                break;
            case '-':
                tokens.add(new Token(TipoToken.OPERA_RESTA, "-", linea, inicioCol));
                posicion++;
                columna++;
                break;
            case '*':
                tokens.add(new Token(TipoToken.OPERA_MULT, "*", linea, inicioCol));
                posicion++;
                columna++;
                break;
            case '/':
                tokens.add(new Token(TipoToken.OPERA_DIVID, "/", linea, inicioCol));
                posicion++;
                columna++;
                break;
            case ';':
                tokens.add(new Token(TipoToken.FIN_SENTENCIA, ";", linea, inicioCol));
                posicion++;
                columna++;
                break;
            case '{':
                tokens.add(new Token(TipoToken.INI_BLOQUE, "{", linea, inicioCol));
                posicion++;
                columna++;
                break;
            case '}':
                tokens.add(new Token(TipoToken.FIN_BLOQUE, "}", linea, inicioCol));
                posicion++;
                columna++;
                break;
            case '(':
                tokens.add(new Token(TipoToken.ABRE_PARENTESIS, "(", linea, inicioCol));
                posicion++;
                columna++;
                break;
            case ')':
                tokens.add(new Token(TipoToken.CIERRA_PARENTESIS, ")", linea, inicioCol));
                posicion++;
                columna++;
                break;
            case ',':
                tokens.add(new Token(TipoToken.COMA, ",", linea, inicioCol));
                posicion++;
                columna++;
                break;
            case ':':
                tokens.add(new Token(TipoToken.DOS_PUNTOS, ":", linea, inicioCol));
                posicion++;
                columna++;
                break;
            default:
                errorLexico("Carácter inesperado: " + actual, inicioCol);
                posicion++;
                columna++;
        }
    }
    
    private void errorLexico(String mensaje, int columna) {
        errores.add(new ErrorCompilacion(ErrorCompilacion.TipoError.LEXICO, 
            mensaje, linea, columna));
    }
    
    public List<ErrorCompilacion> getErrores() {
        return errores;
    }
    
    public void imprimirTokens() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║     ANÁLISIS LÉXICO - TOKENS          ║");
        System.out.println("╚════════════════════════════════════════╝");
        for (Token token : tokens) {
            if (token.getTipo() != TipoToken.EOF) {
                System.out.printf("  %-20s → %s%n", token.getTipo(), 
                    token.getLexema() != null ? token.getLexema() : "");
            }
        }
        System.out.println("=========================================\n");
    }
}