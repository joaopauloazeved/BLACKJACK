package com.blackjack.server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerMain {
    public static void main(String[] args) {
        try {
            BlackjackServiceImpl servico = new BlackjackServiceImpl();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("BlackjackService", servico);
            System.out.println(">>> Servidor RMI de Blackjack rodando na porta 1099!");
        } catch (Exception e) {
            System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}