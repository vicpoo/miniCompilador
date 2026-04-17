//Token.java
package com.compilador.minicompilador.dominio;

public class Token {
    private final TipoToken tipo;
    private final String lexema;
    private final int linea;
    private final int columna;
    
    public Token(TipoToken tipo, String lexema, int linea, int columna) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.linea = linea;
        this.columna = columna;
    }
    
    public TipoToken getTipo() { return tipo; }
    public String getLexema() { return lexema; }
    public int getLinea() { return linea; }
    public int getColumna() { return columna; }
    
    @Override
    public String toString() {
        return String.format("%s(%s) [línea: %d, col: %d]", 
            tipo, lexema != null ? lexema : "", linea, columna);
    }
}