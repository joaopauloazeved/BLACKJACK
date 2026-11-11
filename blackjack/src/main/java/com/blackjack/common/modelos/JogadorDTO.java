package com.blackjack.common.modelos;

import java.io.Serializable;

public class JogadorDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String nome;
    private double fichas;
    private double apostaAtual;
    private final Mao mao;
    private boolean parou;
    private boolean pronto;

    public JogadorDTO(String id, String nome, double fichasIniciais) {
        this.id = id;
        this.nome = nome;
        this.fichas = fichasIniciais;
        this.apostaAtual = 0;
        this.mao = new Mao();
        this.parou = false;
        this.pronto = false;
    }
    public String getId() {
        return id;
    }
    public String getNome() {
        return nome;
    }
    public double getFichas() {
        return fichas;
    }
    public void setFichas(double fichas) {
        this.fichas = fichas;
    }
    public double getApostaAtual() {
        return apostaAtual;
    }
    public void setApostaAtual(double apostaAtual) {
        this.apostaAtual = apostaAtual; }
    public Mao getMao() {
        return mao;
    }
    public boolean isParou() {
        return parou;
    }
    public void setParou(boolean parou) {
        this.parou = parou;
    }
    public boolean isPronto() {
        return pronto;
    }
    public void setPronto(boolean pronto) {
        this.pronto = pronto;
    }



}