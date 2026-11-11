package com.blackjack.common.modelos;

import java.io.Serializable;

public class Carta implements Serializable {
   private  static final long serialVersionUID = 1L;
    private final String naipe;
    private final String nome;
    private final int valorbase;

    public Carta(String naipe, String nome, int valorbase) {
        this.naipe = naipe;
        this.nome = nome;
        this.valorbase = valorbase;
    }

    public String getNaipe() {
        return naipe;
    }

    public String getNome() {
        return nome;
    }

    public int getValorbase() {
        return valorbase;
    }

    @Override
    public String toString() {
        return "Carta{" +
                "naipe='" + naipe + '\'' +
                ", nome='" + nome + '\'' +
                '}';
    }
}
