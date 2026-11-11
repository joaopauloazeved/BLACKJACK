package com.blackjack.common;


import com.blackjack.common.modelos.EstadoJogoDTO;
import com.blackjack.common.modelos.JogadorDTO;

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface BlackjackService extends Remote {

    JogadorDTO registrarJogador(String nome) throws RemoteException;
    boolean fazerApostar(String idJogador, double valorApostas) throws RemoteException;
    EstadoJogoDTO pedirCarta(String idJogador) throws RemoteException;
    EstadoJogoDTO pararTurno(String idJogador) throws RemoteException;
    EstadoJogoDTO obterEstadoJogo() throws RemoteException;
    void iniciarRodada(String idJogador) throws RemoteException;

}
