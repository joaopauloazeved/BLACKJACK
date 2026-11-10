package com.blackjack.common.modelos;

import java.io.Serializable;

public class Carta implements Serializable {
    private String naipe;
    private String valor;

    public Carta(String naipe, String valor) {
        this.naipe = naipe;
        this.valor = valor;
    }

    public String getNaipe() {
        return naipe;
    }

    public String getValor() {
        return valor;
    }
    
    @Override 
    public String toString() {
        return valor + " de " + naipe;
    }


}
