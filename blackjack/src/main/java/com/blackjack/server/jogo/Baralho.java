package com.blackjack.server.jogo;

import com.blackjack.common.modelos.Carta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Baralho {
    private final List<Carta> cartas = new ArrayList<>();

    public Baralho() {
        reiniciar();
    }

    public final void reiniciar() {
        cartas.clear();
        String[] naipes = {"Copas", "Ouros", "Espadas", "Paus"};
        String[] nomes = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};

        for (String naipe : naipes) {
            for (String nome : nomes) {
                int valor;
                if (nome.equals("A")) valor = 11;
                else if (nome.equals("J") || nome.equals("Q") || nome.equals("K")) valor = 10;
                else valor = Integer.parseInt(nome);

                cartas.add(new Carta(naipe, nome, valor));
            }
        }
        Collections.shuffle(cartas);
    }

    public Carta comprar(){
        if (cartas.isEmpty()){
            reiniciar();
        }
        return cartas.remove(0);
    }

}
