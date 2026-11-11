package com.blackjack.common.modelos;

import java.io.Serializable;
import java.util.List;

public class EstadoJogoDTO implements Serializable {
    private static final long serialVersion = 1L;

    public enum FaseJogo{
        AGUARDANDO_JOGADORES,
        FAZENDO_APOSTAS,
        TURNO_JOGADORES,
        TURNO_DEALER,
        RODADA_FINALIZADA
    }

    private final FaseJogo fase;
    private final List<JogadorDTO> jogadores;
    private final Mao maoDealer;
    private final String idJogadorDaVez;
    private final String mensagem;

    public EstadoJogoDTO(FaseJogo fase, List<JogadorDTO> jogadores, Mao maoDealer, String idJogadorDaVez, String mensagem) {
        this.fase = fase;
        this.jogadores = jogadores;
        this.maoDealer = maoDealer;
        this.idJogadorDaVez = idJogadorDaVez;
        this.mensagem = mensagem;
    }

    public FaseJogo getFase() {
        return fase;
    }

    public List<JogadorDTO> getJogadores() {
        return jogadores;
    }

    public Mao getMaoDealer() {
        return maoDealer;
    }

    public String getIdJogadorDaVez() {
        return idJogadorDaVez;
    }

    public String getMensagem() {
        return mensagem;
    }
}
