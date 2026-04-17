//TablaSimbolos.java
package com.compilador.minicompilador.dominio;

import java.util.*;

public class TablaSimbolos {
    private Map<String, Simbolo> simbolos;
    private TablaSimbolos padre;
    
    public static class Simbolo {
        private final String nombre;
        private final TipoToken tipo;
        private Object valor;
        private boolean inicializado;
        
        public Simbolo(String nombre, TipoToken tipo) {
            this.nombre = nombre;
            this.tipo = tipo;
            this.inicializado = false;
        }
        
        public String getNombre() { return nombre; }
        public TipoToken getTipo() { return tipo; }
        public Object getValor() { return valor; }
        public void setValor(Object valor) { 
            this.valor = valor;
            this.inicializado = true;
        }
        public boolean isInicializado() { return inicializado; }
    }
    
    public TablaSimbolos() {
        this.simbolos = new HashMap<>();
        this.padre = null;
    }
    
    public TablaSimbolos(TablaSimbolos padre, String ambito) {
        this.simbolos = new HashMap<>();
        this.padre = padre;
    }
    
    public void definir(String nombre, TipoToken tipo) {
        if (simbolos.containsKey(nombre)) {
            throw new RuntimeException("Símbolo ya definido: " + nombre);
        }
        simbolos.put(nombre, new Simbolo(nombre, tipo));
    }
    
    public Simbolo buscar(String nombre) {
        Simbolo simbolo = simbolos.get(nombre);
        if (simbolo == null && padre != null) {
            return padre.buscar(nombre);
        }
        return simbolo;
    }
    
    public void asignar(String nombre, Object valor) {
        Simbolo simbolo = buscar(nombre);
        if (simbolo == null) {
            throw new RuntimeException("Símbolo no definido: " + nombre);
        }
        simbolo.setValor(valor);
    }
    
    public Simbolo obtener(String nombre) {
        return buscar(nombre);
    }
    
    public void imprimirTabla() {
        System.out.println("\n=== TABLA DE SÍMBOLOS ===");
        System.out.println("Variable     | Tipo     | Valor");
        System.out.println("-------------|----------|--------");
        imprimirSimbolos(this);
        System.out.println("=============================\n");
    }
    
    private void imprimirSimbolos(TablaSimbolos tabla) {
        for (Map.Entry<String, Simbolo> entry : tabla.simbolos.entrySet()) {
            Simbolo s = entry.getValue();
            System.out.printf("%-12s | %-8s | %s%n", 
                s.getNombre(), s.getTipo(), s.getValor() != null ? s.getValor() : "no inicializado");
        }
        if (tabla.padre != null) {
            imprimirSimbolos(tabla.padre);
        }
    }
}