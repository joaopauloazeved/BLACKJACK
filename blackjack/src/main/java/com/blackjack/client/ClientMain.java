package com.blackjack.client;

import com.blackjack.common.BlackjackService;
import com.blackjack.common.modelos.*;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class ClientMain {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.print("Digite o IP do servidor (Enter para localhost): ");
            String ip = scanner.nextLine().trim();
            if (ip.isEmpty()) ip = "localhost";

            Registry registry = LocateRegistry.getRegistry(ip, 1099);
            BlackjackService servico = (BlackjackService) registry.lookup("BlackjackService");

            System.out.print("Digite seu nome: ");
            String nome = scanner.nextLine();
            JogadorDTO meuJogador = servico.registrarJogador(nome);

            System.out.println("Conectado! Seu ID: " + meuJogador.getId());

            while (true) {
                EstadoJogoDTO estado = servico.obterEstadoJogo();

                switch (estado.getFase()) {
                    case AGUARDANDO_JOGADORES:
                    case RODADA_FINALIZADA:
                        System.out.println("\n--- Status: " + estado.getMensagem() + " ---");
                        System.out.print("Pressione [1] para iniciar nova rodada ou [0] para sair: ");
                        String op = scanner.nextLine();
                        if (op.equals("1")) {
                            servico.iniciarRodada(meuJogador.getId());
                        } else if (op.equals("0")) {
                            System.exit(0);
                        }
                        break;

                    case FAZENDO_APOSTAS:
                        System.out.println("\nFase de Apostas! Seu saldo de fichas: " + meuJogador.getFichas());
                        System.out.print("Digite o valor da sua aposta: ");
                        double aposta = Double.parseDouble(scanner.nextLine());
                        if (servico.fazerApostar(meuJogador.getId(), aposta)) {
                            System.out.println("Aposta confirmada. Aguardando outros jogadores...");
                            esperarTurno(servico, meuJogador.getId());
                        } else {
                            System.out.println("Aposta inválida!");
                        }
                        break;

                    case TURNO_JOGADORES:
                        if (meuJogador.getId().equals(estado.getIdJogadorDaVez())) {
                            exibirMesa(estado, meuJogador.getId());
                            System.out.println("\nSUA VEZ! [1] Pedir Carta (Hit) | [2] Parar (Stand)");
                            String acao = scanner.nextLine();
                            if (acao.equals("1")) {
                                servico.pedirCarta(meuJogador.getId());
                            } else {
                                servico.pararTurno(meuJogador.getId());
                            }
                        } else {
                            System.out.println("Aguardando a jogada de " + estado.getIdJogadorDaVez() + "...");
                            Thread.sleep(1500);
                        }
                        break;

                    case TURNO_DEALER:
                        System.out.println("A Banca está jogando...");
                        Thread.sleep(1500);
                        break;
                }
            }
        } catch (Exception e) {
            System.err.println("Erro no cliente: " + e.getMessage());
        }
    }

    private static void esperarTurno(BlackjackService servico, String meuId) throws Exception {
        while (true) {
            EstadoJogoDTO e = servico.obterEstadoJogo();
            if (e.getFase() == EstadoJogoDTO.FaseJogo.TURNO_JOGADORES) break;
            Thread.sleep(1000);
        }
    }

    private static void exibirMesa(EstadoJogoDTO estado, String meuId) {
        System.out.println("\n=================== MESA DE JOGO ===================");
        System.out.println("Carta visível do Dealer: " + estado.getMaoDealer().getCartas().get(0));
        System.out.println("---------------------------------------------------");
        for (JogadorDTO j : estado.getJogadores()) {
            String marcacao = j.getId().equals(meuId) ? " (VOCÊ)" : "";
            System.out.println("Jogador " + j.getNome() + marcacao + ": " + j.getMao());
        }
        System.out.println("===================================================");
    }
}