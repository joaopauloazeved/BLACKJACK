package com.blackjack.server.jogo;

import com.blackjack.common.modelos.Carta;
import com.blackjack.common.modelos.Mao;

public class MotorBlackjack {

    private final Baralho baralho;
    private final Mao mao;

    public MotorBlackjack() {
        this.baralho = new Baralho();
        this.mao = new Mao();
        this.baralho.embaralhar();
    }

    public Carta comprarCarta() {
        Carta carta = baralho.comprarCarta();
        if (carta != null) {
            mao.adicionarCarta(carta);
        }
        return carta;
    }

    public int calcularValorMao(Mao mao) {
        int pontuacao = 0;

for (Carta carta : mao.getCartas()) {
    
    String valor =carta.getValor()
    if (valor.equals("K")) {
        pontuacao += 10;
    } else if (valor.equals("Q")) {
        pontuacao += 10;
    } else if (valor.equals("J")) {
        pontuacao += 10;
    } else {
        pontuacao += Integer.parseInt(valor);
    }
        return pontuacao;
    }

    public boolean estourou (Mao mao) {
        return calcularValorMao(mao) > 21;
    }   
    public boolean chegouEm21(Mao mao) {
        return mao.quantidade() == 2 && calcularValorMao(mao) == 21;
    }

    public Mao getMao() {
        return mao;
    }
}
