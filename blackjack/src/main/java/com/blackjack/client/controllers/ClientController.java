package com.blackjack.client.controllers;

import com.blackjack.common.BlackjackService;
import com.blackjack.common.modelos.Carta;
import com.blackjack.common.modelos.EstadoJogoDTO;
import com.blackjack.common.modelos.JogadorDTO;
import com.blackjack.common.modelos.Mao;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ClientController {
    @FXML private TextField serverIpField;
    @FXML private TextField playerNameField;
    @FXML private TextField betField;
    @FXML private Button connectButton;
    @FXML private Button startRoundButton;
    @FXML private Button betButton;
    @FXML private Button hitButton;
    @FXML private Button standButton;
    @FXML private Label connectionLabel;
    @FXML private Label phaseLabel;
    @FXML private Label messageLabel;
    @FXML private Label dealerScoreLabel;
    @FXML private Label playersCountLabel;
    @FXML private HBox dealerCards;
    @FXML private HBox playerSeats;

    private final ScheduledExecutorService network = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "blackjack-network");
        thread.setDaemon(true);
        return thread;
    });
    private final Map<String, Integer> previousCardCounts = new HashMap<>();
    private volatile BlackjackService service;
    private volatile String playerId;
    private String previousStateSignature = "";

    @FXML
    private void initialize() {
        connectionLabel.setText("OFFLINE  /  informe o servidor para entrar");
        phaseLabel.setText("AGUARDANDO CONEXÃO");
        messageLabel.setText("Conecte-se à mesa para acompanhar a partida.");
        updateControls(null);
    }

    @FXML
    private void onConnect() {
        String host = serverIpField.getText() == null ? "" : serverIpField.getText().trim();
        String name = playerNameField.getText() == null ? "" : playerNameField.getText().trim();
        if (host.isEmpty()) host = "localhost";
        if (name.isEmpty()) {
            connectionLabel.setText("Digite seu nome antes de conectar.");
            return;
        }

        connectButton.setDisable(true);
        connectionLabel.setText("CONECTANDO  /  " + host);
        String serverHost = host;
        network.execute(() -> {
            try {
                Registry registry = LocateRegistry.getRegistry(serverHost, 1099);
                BlackjackService remoteService = (BlackjackService) registry.lookup("BlackjackService");
                JogadorDTO player = remoteService.registrarJogador(name);
                service = remoteService;
                playerId = player.getId();
                Platform.runLater(() -> {
                    connectionLabel.setText("ONLINE  /  " + player.getNome());
                    serverIpField.setDisable(true);
                    playerNameField.setDisable(true);
                    connectButton.setText("CONECTADO");
                });
                network.scheduleAtFixedRate(this::refreshState, 0, 900, TimeUnit.MILLISECONDS);
            } catch (Exception exception) {
                Platform.runLater(() -> {
                    connectionLabel.setText("FALHA NA CONEXÃO  /  " + shortMessage(exception));
                    connectButton.setDisable(false);
                });
            }
        });
    }

    @FXML
    private void onStartRound() {
        runRemoteAction(() -> service.iniciarRodada(playerId));
    }

    @FXML
    private void onBet() {
        double amount;
        try {
            amount = Double.parseDouble(betField.getText().trim().replace(',', '.'));
        } catch (RuntimeException exception) {
            messageLabel.setText("Digite um valor numérico para a aposta.");
            return;
        }
        runRemoteAction(() -> {
            if (!service.fazerApostar(playerId, amount)) {
                throw new IllegalArgumentException("Aposta inválida ou já confirmada.");
            }
        });
    }

    @FXML
    private void onHit() {
        runRemoteAction(() -> service.pedirCarta(playerId));
    }

    @FXML
    private void onStand() {
        runRemoteAction(() -> service.pararTurno(playerId));
    }

    private void runRemoteAction(RemoteAction action) {
        if (service == null || playerId == null) return;
        setActionButtonsDisabled(true);
        network.execute(() -> {
            try {
                action.run();
                refreshState();
            } catch (Exception exception) {
                Platform.runLater(() -> messageLabel.setText(shortMessage(exception)));
                refreshState();
            }
        });
    }

    private void refreshState() {
        BlackjackService currentService = service;
        if (currentService == null) return;
        try {
            EstadoJogoDTO state = currentService.obterEstadoJogo();
            Platform.runLater(() -> render(state));
        } catch (Exception exception) {
            Platform.runLater(() -> {
                connectionLabel.setText("CONEXÃO PERDIDA  /  " + shortMessage(exception));
                updateControls(null);
            });
        }
    }

    private void render(EstadoJogoDTO state) {
        updateControls(state);
        String signature = signatureOf(state);
        if (signature.equals(previousStateSignature)) return;
        previousStateSignature = signature;

        JogadorDTO localPlayer = state.getJogadores().stream()
                .filter(player -> player.getId().equals(playerId))
                .findFirst()
                .orElse(null);
        if (localPlayer != null) {
            connectionLabel.setText("ONLINE  /  " + localPlayer.getNome() + "  /  " + formatChips(localPlayer.getFichas()) + " fichas");
        }

        phaseLabel.setText(phaseText(state.getFase()));
        messageLabel.setText(state.getMensagem());
        playersCountLabel.setText(String.format("MESA  /  %02d JOGADORES", state.getJogadores().size()));
        renderDealer(state);
        renderPlayers(state);
    }

    private void renderDealer(EstadoJogoDTO state) {
        dealerCards.getChildren().clear();
        Mao hand = state.getMaoDealer();
        int oldCount = previousCardCounts.getOrDefault("dealer", 0);
        for (int index = 0; index < hand.getCartas().size(); index++) {
            Node card = createCard(hand.getCartas().get(index));
            dealerCards.getChildren().add(card);
            if (index >= oldCount) animateDeal(card);
        }

        boolean dealerHasHiddenCard = (state.getFase() == EstadoJogoDTO.FaseJogo.TURNO_JOGADORES
                || state.getFase() == EstadoJogoDTO.FaseJogo.TURNO_DEALER)
                && hand.getCartas().size() == 1;
        if (dealerHasHiddenCard) {
            Node hiddenCard = createCardBack();
            dealerCards.getChildren().add(hiddenCard);
            if (oldCount < 2) animateDeal(hiddenCard);
            dealerScoreLabel.setText("PONTOS VISÍVEIS  /  " + hand.getPontuacao() + " + ?");
        } else if (hand.getCartas().isEmpty()) {
            dealerScoreLabel.setText("PONTUAÇÃO  /  --");
        } else {
            dealerScoreLabel.setText("PONTUAÇÃO  /  " + hand.getPontuacao());
        }
        previousCardCounts.put("dealer", hand.getCartas().size());
    }

    private void renderPlayers(EstadoJogoDTO state) {
        playerSeats.getChildren().clear();
        String currentPlayerId = state.getIdJogadorDaVez();
        for (JogadorDTO player : state.getJogadores()) {
            boolean isCurrentPlayer = player.getId().equals(currentPlayerId);
            boolean isLocalPlayer = player.getId().equals(playerId);
            VBox seat = new VBox(10);
            seat.getStyleClass().add("player-seat");
            if (isCurrentPlayer) seat.getStyleClass().add("active-seat");
            if (isLocalPlayer) seat.getStyleClass().add("local-seat");

            HBox heading = new HBox(8);
            heading.getStyleClass().add("seat-heading");
            Label name = new Label(player.getNome());
            name.getStyleClass().add("seat-name");
            heading.getChildren().add(name);
            if (isLocalPlayer) heading.getChildren().add(tag("VOCÊ", "you-tag"));
            if (isCurrentPlayer) heading.getChildren().add(tag("SUA VEZ", "turn-tag"));

            Label score = new Label("PONTOS  /  " + player.getMao().getPontuacao());
            score.getStyleClass().add("score-label");
            HBox cards = new HBox(8);
            cards.getStyleClass().add("card-row");
            String key = "player:" + player.getId();
            int oldCount = previousCardCounts.getOrDefault(key, 0);
            for (int index = 0; index < player.getMao().getCartas().size(); index++) {
                Node card = createCard(player.getMao().getCartas().get(index));
                cards.getChildren().add(card);
                if (index >= oldCount) animateDeal(card);
            }
            previousCardCounts.put(key, player.getMao().getCartas().size());

            HBox details = new HBox(14);
            details.getStyleClass().add("seat-details");
            details.getChildren().add(new Label("FICHAS  " + formatChips(player.getFichas())));
            details.getChildren().add(new Label("APOSTA  " + formatChips(player.getApostaAtual())));
            String playerStatus = state.getFase() == EstadoJogoDTO.FaseJogo.FAZENDO_APOSTAS
                    ? (player.isPronto() ? "APOSTA OK" : "AGUARDANDO APOSTA")
                    : (player.isParou() ? "PAROU" : "NA MESA");
            details.getChildren().add(tag(playerStatus, "status-tag"));

            seat.getChildren().addAll(heading, score, cards, details);
            playerSeats.getChildren().add(seat);
        }
    }

    private void updateControls(EstadoJogoDTO state) {
        boolean connected = service != null && playerId != null;
        if (!connected || state == null) {
            startRoundButton.setDisable(true);
            betButton.setDisable(true);
            betField.setDisable(true);
            hitButton.setDisable(true);
            standButton.setDisable(true);
            return;
        }

        JogadorDTO localPlayer = state.getJogadores().stream()
                .filter(player -> player.getId().equals(playerId))
                .findFirst()
                .orElse(null);
        boolean host = !state.getJogadores().isEmpty()
                && state.getJogadores().get(0).getId().equals(playerId);
        boolean lobbyPhase = state.getFase() == EstadoJogoDTO.FaseJogo.AGUARDANDO_JOGADORES
                || state.getFase() == EstadoJogoDTO.FaseJogo.RODADA_FINALIZADA;
        boolean betting = state.getFase() == EstadoJogoDTO.FaseJogo.FAZENDO_APOSTAS;
        boolean myTurn = state.getFase() == EstadoJogoDTO.FaseJogo.TURNO_JOGADORES
                && playerId.equals(state.getIdJogadorDaVez());

        startRoundButton.setText(state.getFase() == EstadoJogoDTO.FaseJogo.RODADA_FINALIZADA
                ? "NOVA RODADA" : "INICIAR RODADA");
        startRoundButton.setDisable(!host || !lobbyPhase);
        betField.setDisable(!betting || localPlayer == null || localPlayer.isPronto());
        betButton.setDisable(!betting || localPlayer == null || localPlayer.isPronto());
        hitButton.setDisable(!myTurn);
        standButton.setDisable(!myTurn);
    }

    private void setActionButtonsDisabled(boolean disabled) {
        startRoundButton.setDisable(disabled || startRoundButton.isDisable());
        betButton.setDisable(disabled || betButton.isDisable());
        hitButton.setDisable(disabled || hitButton.isDisable());
        standButton.setDisable(disabled || standButton.isDisable());
    }

    private Node createCard(Carta card) {
        VBox face = new VBox(5);
        face.getStyleClass().add("playing-card");
        String suit = suitSymbol(card.getNaipe());
        if (suit.equals("♥") || suit.equals("♦")) face.getStyleClass().add("red-card");
        Label rank = new Label(card.getNome());
        rank.getStyleClass().add("card-rank");
        Label mark = new Label(suit);
        mark.getStyleClass().add("card-suit");
        Label suitName = new Label(card.getNaipe().toUpperCase());
        suitName.getStyleClass().add("card-suit-name");
        face.getChildren().addAll(rank, mark, suitName);
        return face;
    }

    private Node createCardBack() {
        StackPane back = new StackPane(new Label("21"));
        back.getStyleClass().add("playing-card");
        back.getStyleClass().add("card-back");
        return back;
    }

    private Label tag(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().addAll("tag", styleClass);
        return label;
    }

    private void animateDeal(Node card) {
        card.setOpacity(0);
        card.setTranslateY(-22);
        FadeTransition fade = new FadeTransition(Duration.millis(260), card);
        fade.setFromValue(0);
        fade.setToValue(1);
        TranslateTransition move = new TranslateTransition(Duration.millis(260), card);
        move.setFromY(-22);
        move.setToY(0);
        new ParallelTransition(fade, move).play();
    }

    private String signatureOf(EstadoJogoDTO state) {
        StringBuilder signature = new StringBuilder()
                .append(state.getFase()).append('|')
                .append(state.getMensagem()).append('|')
                .append(state.getIdJogadorDaVez()).append('|')
                .append(state.getMaoDealer().getCartas());
        for (JogadorDTO player : state.getJogadores()) {
            signature.append('|').append(player.getId()).append(':')
                    .append(player.getFichas()).append(':')
                    .append(player.getApostaAtual()).append(':')
                    .append(player.isPronto()).append(':')
                    .append(player.isParou()).append(':')
                    .append(player.getMao().getCartas());
        }
        return signature.toString();
    }

    private String phaseText(EstadoJogoDTO.FaseJogo phase) {
        return switch (phase) {
            case AGUARDANDO_JOGADORES -> "SALA ABERTA";
            case FAZENDO_APOSTAS -> "FAÇA SUA APOSTA";
            case TURNO_JOGADORES -> "TURNO DOS JOGADORES";
            case TURNO_DEALER -> "DEALER JOGANDO";
            case RODADA_FINALIZADA -> "RODADA ENCERRADA";
        };
    }

    private String suitSymbol(String suit) {
        return switch (suit.toLowerCase()) {
            case "copas" -> "♥";
            case "ouros" -> "♦";
            case "espadas" -> "♠";
            case "paus" -> "♣";
            default -> "?";
        };
    }

    private String formatChips(double amount) {
        return String.format("%.0f", amount);
    }

    private String shortMessage(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) return exception.getClass().getSimpleName();
        return message.length() > 72 ? message.substring(0, 69) + "..." : message;
    }

    public void shutdown() {
        network.shutdownNow();
    }

    @FunctionalInterface
    private interface RemoteAction {
        void run() throws Exception;
    }
}
