package com.blackjack.server.jogo;

import com.blackjack.common.modelos.Carta;

import java.util.ArrayList;
import java.util.List;


public class Baralho {
    private List<Carta> cartas;

    public Baralho() {
        cartas = new ArrayList<>();
        String[] naipes = {"Paus", "Ouros", "Espadas", "Copas"};
        String[] valores = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};

        for (String naipe : naipes) {
            for (String valor : valores) {
                cartas.add(new Carta(naipe, valor));
            }
        }
    }


    public void embaralhar() {
        java.util.Collections.shuffle(cartas);
    }
    public Carta comprarCarta() {
        if (!cartas.isEmpty()) {
            return cartas.remove(0);
        }
        return null;
    }

    public int quantidade() {
        return cartas.size();
    }
}
