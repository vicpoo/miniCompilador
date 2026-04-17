//ErrorCompilacion.java
package com.compilador.minicompilador.dominio;

public class ErrorCompilacion {
    public enum TipoError {
        LEXICO, SINTACTICO, SEMANTICO
    }
    
    private final TipoError tipo;
    private final String mensaje;
    private final int linea;
    private final int columna;
    
    public ErrorCompilacion(TipoError tipo, String mensaje, int linea, int columna) {
        this.tipo = tipo;
        this.mensaje = mensaje;
        this.linea = linea;
        this.columna = columna;
    }
    
    public TipoError getTipo() { return tipo; }
    public String getMensaje() { return mensaje; }
    public int getLinea() { return linea; }
    public int getColumna() { return columna; }
    
    @Override
    public String toString() {
        return String.format("[ERROR %s] Línea %d, Col %d: %s", tipo, linea, columna, mensaje);
    }
}