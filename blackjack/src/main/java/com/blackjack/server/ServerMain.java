package com.blackjack.server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerMain {
    public static void main(String[] args) {
        try {
            if (args.length > 0 && !args[0].isBlank()) {
                System.setProperty("java.rmi.server.hostname", args[0]);
            }
            BlackjackServiceImpl servico = new BlackjackServiceImpl();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("BlackjackService", servico);
            System.out.println(">>> Servidor RMI de Blackjack rodando. Registro: 1099, serviço: 1100.");
            if (args.length > 0 && !args[0].isBlank()) {
                System.out.println(">>> IP anunciado aos clientes: " + args[0]);
            }
        } catch (Exception e) {
            System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}