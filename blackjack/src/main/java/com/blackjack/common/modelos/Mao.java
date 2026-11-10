package com.blackjack.common.modelos;


import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

public class Mao implements Serializable {
    private List<Carta> cartas;

    public Mao() {
        cartas = new ArrayList<>();
    }
    
public void adicionarCarta(Carta carta) {
        cartas.add(carta);
    }
    public List<Carta> getCartas() {
        return cartas;
    }

    
    }

