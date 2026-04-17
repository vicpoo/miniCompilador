//App.java
package com.compilador.minicompilador;

import com.compilador.minicompilador.presentacion.CompiladorCLI;

public class App {
    public static void main(String[] args) {
        CompiladorCLI compiladorCLI = new CompiladorCLI();
        compiladorCLI.iniciar(args);
    }
}