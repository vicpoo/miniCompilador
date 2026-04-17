//TipoToken.java
package com.compilador.minicompilador.dominio;

public enum TipoToken {
    // Palabras reservadas en élfico
    KEY_ANIN("anin"),      // if
    KEY_PENNETH("penneth"), // else
    KEY_CARO("caro"),       // do
    KEY_NAUVA("nauva"),     // while
    KEY_LAVA("lava"),       // switch/leer
    KEY_NORO("noro"),       // break
    KEY_ANA("ana"),         // for
    KEY_TIRNO("tirno"),     // case
    KEY_TIRA("tira"),       // print/escribir
    KEY_LANNA("lanna"),     // read/leer
    
    // Tipos de datos en élfico
    KEY_NAMPAT("nampat"),   // int
    KEY_LINTA("linta"),     // float
    KEY_TENGWA("tengwa"),   // string
    
    // Identificadores
    ID(null),
    
    // Literales
    NUMERO(null),
    NUMERO_DECIMAL(null),
    CADENA(null),
    
    // Operadores
    ASIGNA("="),
    OPERA_SUMA("+"),
    OPERA_RESTA("-"),
    OPERA_MULT("*"),
    OPERA_DIVID("/"),
    OPERA_MENOR("<"),
    OPERA_MAYOR(">"),
    OPERA_MENOR_IGUAL("<="),
    OPERA_MAYOR_IGUAL(">="),
    OPERA_DIFERENTE("!="),
    OPERA_IGUALDAD("=="),
    OPERA_AND("and"),
    OPERA_OR("or"),
    OPERA_NOT("not"),
    
    // Delimitadores
    FIN_SENTENCIA(";"),
    INI_BLOQUE("{"),
    FIN_BLOQUE("}"),
    ABRE_PARENTESIS("("),
    CIERRA_PARENTESIS(")"),
    COMA(","),
    DOS_PUNTOS(":"),
    
    // Especial
    EOF(null),
    ERROR(null);
    
    private final String lexema;
    
    TipoToken(String lexema) {
        this.lexema = lexema;
    }
    
    public String getLexema() {
        return lexema;
    }
}