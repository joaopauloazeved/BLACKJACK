package com.blackjack.common.modelos;


import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

public class Mao implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<Carta> cartas = new ArrayList<>();

    public void adicionarCarta(Carta carta){
        cartas.add(carta);
    }

    public List<Carta> getCartas(){
        return cartas;
    }

    public int getPontuacao(){
        int total = 0;
        int ases = 0;

        for (Carta c : cartas){
            total +=c.getValorbase();
            if (c.getNome().equals("A")){
                ases++;
            }
        }
        while (total > 21 && ases > 0){
            total -= 10;
            ases--;
        }
        return total;
    }
    public boolean estourou(){
        return getPontuacao() > 21;
    }
    public void limpar(){
        cartas.clear();
    }

    @Override
    public String toString() {
        return "Mao{" +
                "cartas=" + cartas +
                '}' + getPontuacao();
    }
}

