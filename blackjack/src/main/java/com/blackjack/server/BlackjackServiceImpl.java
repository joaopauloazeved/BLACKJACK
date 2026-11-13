package com.blackjack.server;

import com.blackjack.common.BlackjackService;
import com.blackjack.common.modelos.EstadoJogoDTO;
import com.blackjack.common.modelos.JogadorDTO;
import com.blackjack.common.modelos.Mao;
import com.blackjack.server.jogo.Baralho;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class BlackjackServiceImpl extends UnicastRemoteObject implements BlackjackService {

    private final List<JogadorDTO> jogadores = Collections.synchronizedList(new ArrayList<>());
    private final Mao maoDealer = new Mao();
    private final Baralho baralho = new Baralho();
    private EstadoJogoDTO.FaseJogo faseAtual = EstadoJogoDTO.FaseJogo.AGUARDANDO_JOGADORES;
    private int indiceJogadorDaVez = 0;
    private String mensagemLog = "Aguardando jogadores se conectarem ";

    public BlackjackServiceImpl() throws RemoteException{
        super(1100);
    }

    @Override
    public  synchronized JogadorDTO registrarJogador(String nome) throws RemoteException {
        if (faseAtual != EstadoJogoDTO.FaseJogo.AGUARDANDO_JOGADORES && faseAtual != EstadoJogoDTO.FaseJogo.RODADA_FINALIZADA){
              throw new RemoteException("Rodada em andamento. Aguarde o término.");
          }
          if (nome == null || nome.isBlank()) {
                throw new RemoteException("O nome do jogador não pode ficar vazio.");
        }
        String id = UUID.randomUUID().toString();
        JogadorDTO jogador = new JogadorDTO(id, nome, 500.0);
        jogadores.add(jogador);
        mensagemLog = "Jogador " + nome + " entrou na sala.";
        System.out.println(mensagemLog);
        return jogador;
    }

    @Override
    public synchronized boolean fazerApostar(String idJogador, double valor) throws RemoteException {
        if (faseAtual != EstadoJogoDTO.FaseJogo.FAZENDO_APOSTAS) {
            throw new RemoteException("Não está na fase de apostas.");
        }
        JogadorDTO jogador = buscarJogador(idJogador);
        if (jogador.isPronto() || !Double.isFinite(valor) || valor <= 0 || valor > jogador.getFichas()){
            return false;
        }
        jogador.setApostaAtual(valor);
        jogador.setFichas(jogador.getFichas() - valor);
        jogador.setPronto(true);
        mensagemLog = jogador.getNome() + " apostou " + valor + " fichas.";

        boolean todosApostaram = jogadores.stream().allMatch(JogadorDTO::isPronto);

        if (todosApostaram){
            distribuirCartasIniciais();
            faseAtual = EstadoJogoDTO.FaseJogo.TURNO_JOGADORES;
            indiceJogadorDaVez = 0;
            mensagemLog = "Cartas distribuídas. Vez de " + jogadores.get(0).getNome() + ".";
        }
        return true;
    }



    @Override
    public synchronized void iniciarRodada(String idJogador) throws RemoteException{
        if (jogadores.isEmpty()) throw new RemoteException("Nenhum jogador na sala");
        if (faseAtual != EstadoJogoDTO.FaseJogo.AGUARDANDO_JOGADORES
                && faseAtual != EstadoJogoDTO.FaseJogo.RODADA_FINALIZADA) {
            throw new RemoteException("Já existe uma rodada em andamento.");
        }
        buscarJogador(idJogador);
        if (!jogadores.get(0).getId().equals(idJogador)) {
            throw new RemoteException("Somente quem criou a sala pode iniciar a rodada.");
        }

        faseAtual = EstadoJogoDTO.FaseJogo.FAZENDO_APOSTAS;
        baralho.reiniciar();
        maoDealer.limpar();

        for (JogadorDTO j : jogadores){
            j.getMao().limpar();
            j.setParou(false);
            j.setPronto(false);
            j.setApostaAtual(0);
        }
        mensagemLog = "Nova rodada iniciada Façam as apostas";
    }



    public void distribuirCartasIniciais(){
        for (JogadorDTO j : jogadores){
            j.getMao().adicionarCarta(baralho.comprar());
            j.getMao().adicionarCarta(baralho.comprar());
        }
        maoDealer.adicionarCarta(baralho.comprar());
        maoDealer.adicionarCarta(baralho.comprar());
    }

    @Override
    public synchronized EstadoJogoDTO pedirCarta(String idJogador) throws RemoteException{
        validarTurno(idJogador);
        JogadorDTO jogador = jogadores.get(indiceJogadorDaVez);

        jogador.getMao().adicionarCarta(baralho.comprar());
        mensagemLog = jogador.getNome() + "Pediu carta";

        if (jogador.getMao().estourou()){
            jogador.setParou(true);
            mensagemLog = jogador.getNome() + " Estourou os 21 pontos";
            avancarTurno();
        }
        return obterEstadoJogo();
    }

    @Override
    public synchronized EstadoJogoDTO pararTurno(String idJogador) throws RemoteException{
        validarTurno(idJogador);
        JogadorDTO jogador = jogadores.get(indiceJogadorDaVez);
        jogador.setParou(true);
        mensagemLog = jogador.getNome() + " parou a jogada";
        avancarTurno();
        return obterEstadoJogo();
    }

    private void avancarTurno(){
        indiceJogadorDaVez++;
        if (indiceJogadorDaVez >= jogadores.size()){
            executarTurnoDealer();
        }else {
            mensagemLog = " É a vez de " + jogadores.get(indiceJogadorDaVez).getNome();
        }
    }

    private void executarTurnoDealer(){
        faseAtual = EstadoJogoDTO.FaseJogo.TURNO_DEALER;
        mensagemLog = "É a vez da banca ";

        while (maoDealer.getPontuacao() < 17){
            maoDealer.adicionarCarta(baralho.comprar());
        }
        finalizarRodada();
    }

    private void finalizarRodada(){
        faseAtual = EstadoJogoDTO.FaseJogo.RODADA_FINALIZADA;
        int pontosDealer = maoDealer.getPontuacao();
        boolean dealerEstourou = maoDealer.estourou();

        StringBuilder sb = new StringBuilder("Rodada encerrada. ");
        if (dealerEstourou) sb.append("A banca estourou. ");
        for (JogadorDTO j : jogadores){
            int pontosJ = j.getMao().getPontuacao();
            if (j.getMao().estourou()){
                sb.append(j.getNome()).append(" perdeu. ");
            } else if (dealerEstourou || pontosJ > pontosDealer) {
                double premio = j.getApostaAtual() * 2;
                j.setFichas(j.getFichas() + premio);
                sb.append(j.getNome()).append(" venceu e recebeu ").append(premio).append(" fichas. ");
            } else if (pontosJ == pontosDealer) {
                j.setFichas(j.getFichas() + j.getApostaAtual());
                sb.append(j.getNome()).append(" empatou. A aposta foi devolvida. ");
            }else {
                sb.append(j.getNome()).append(" perdeu. ");
            }
        }
        mensagemLog = sb.toString();
    }

    @Override
    public synchronized EstadoJogoDTO obterEstadoJogo()throws RemoteException{
        String idVez = faseAtual == EstadoJogoDTO.FaseJogo.TURNO_JOGADORES
                && indiceJogadorDaVez < jogadores.size()
                ? jogadores.get(indiceJogadorDaVez).getId()
                : null;
        Mao maoDealerVisivel = maoDealer;
        if (faseAtual != EstadoJogoDTO.FaseJogo.RODADA_FINALIZADA
                && maoDealer.getCartas().size() > 1) {
            maoDealerVisivel = new Mao();
            maoDealerVisivel.adicionarCarta(maoDealer.getCartas().get(0));
        }
        return new EstadoJogoDTO(faseAtual, new ArrayList<>(jogadores), maoDealerVisivel, idVez, mensagemLog);
    }

    private void validarTurno(String idJogador) throws RemoteException{
        if (faseAtual != EstadoJogoDTO.FaseJogo.TURNO_JOGADORES
            || indiceJogadorDaVez >= jogadores.size()){
            throw new RemoteException("Não está na fase de jogadas");
        }
        JogadorDTO jogadorDaVez = jogadores.get(indiceJogadorDaVez);
        if (!jogadorDaVez.getId().equals(idJogador)){
            throw new RemoteException(("Não é o seu turno"));
        }
    }

    private JogadorDTO buscarJogador(String id) throws RemoteException{
        return jogadores.stream()
                .filter(j -> j.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RemoteException("Jogadoir não encontrado "));
    }








}
