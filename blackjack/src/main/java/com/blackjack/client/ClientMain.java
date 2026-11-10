package com.blackjack.client;

import com.blackjack.common.modelos.Carta;
import com.blackjack.common.modelos.Mao;
import com.blackjack.server.jogo.Baralho;

public class ClientMain {

    public static void main(String[] args) {
    
Baralho baralho = new Baralho();
baralho.embaralhar();

System.out.println("Quantidade de cartas no baralho: " + baralho.quantidade());

Carta cartao1 = baralho.comprarCarta();
Carta cartao2 = baralho.comprarCarta();

Mao mao = new Mao();
mao.adicionarCarta(cartao1);
mao.adicionarCarta(cartao2);

System.out.println("Cartas COMPRADAS");
System.out.println(mao);
System.out.println("valor e naipe das cartas compradas: " + mao.getCartas().get(0).getValor() + " de " + mao.getCartas().get(0).getNaipe());

System.out.println("Quantidade de cartas no baralho após comprar: " + baralho.quantidade());

}
}
