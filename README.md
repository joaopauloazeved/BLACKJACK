# 🃏 21 — BLACKJACK

## 📖 Descrição do Projeto

O **21**, também conhecido como **Blackjack**, é um jogo de cartas clássico cujo objetivo é alcançar uma pontuação superior à da banca (servidor), sem ultrapassar o limite de 21 pontos.

O jogo utiliza um baralho padrão de 52 cartas, com a seguinte distribuição de valores:

- **Cartas de 2 a 10:** possuem seus respectivos valores nominais.
- **Valete (J), Dama (Q) e Rei (K):** valem 10 pontos cada.
- **Ás (A):** possui valor flexível, podendo valer 1 ou 11 pontos, de acordo com a combinação mais vantajosa para a mão do jogador.

## 🎮 Funcionamento do Jogo

A partida começa com a distribuição de duas cartas iniciais para o jogador. Com base na pontuação obtida, ele deverá decidir qual ação realizar durante seu turno:

- **Hit (Pedir carta):** solicita uma nova carta ao baralho, buscando aproximar sua pontuação de 21.
- **Stand (Parar):** encerra o turno, mantendo a pontuação atual e permitindo que a banca realize sua jogada.

Caso a pontuação do jogador ultrapasse 21 pontos, ocorre o chamado **Bust (estouro)**, resultando em derrota imediata.

Após o encerramento do turno do jogador, a banca realiza sua jogada seguindo uma regra predefinida: deverá solicitar novas cartas enquanto possuir uma pontuação inferior a 17 pontos, permanecendo com sua mão ao atingir 17 ou mais pontos.

## 🏆 Regras de Vitória

Ao final da rodada, as pontuações do jogador e da banca são comparadas para determinar o resultado:

- **Vitória do jogador:** quando sua pontuação é superior à da banca, sem ultrapassar 21 pontos.
- **Vitória da banca:** quando sua pontuação é superior à do jogador, sem ultrapassar 21 pontos, ou quando o jogador ultrapassa 21 pontos.
- **Empate (Push):** quando jogador e banca terminam com a mesma pontuação.
- **Bust da banca:** caso a banca ultrapasse 21 pontos, o jogador vence, desde que não tenha estourado anteriormente.

O vencedor será aquele que conseguir alcançar a pontuação mais próxima de 21, sem ultrapassar esse limite.

## 🎯 Objetivo

O projeto tem como objetivo desenvolver uma aplicação baseada no jogo Blackjack, contemplando suas regras fundamentais, gerenciamento das partidas, distribuição de cartas, cálculo de pontuação e identificação dos resultados.

A aplicação também
