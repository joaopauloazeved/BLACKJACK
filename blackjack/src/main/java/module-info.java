module com.blackjack {

    requires javafx.controls;
    requires javafx.fxml;
    requires java.rmi;

    exports com.blackjack.common;
    exports com.blackjack.common.modelos;
    exports com.blackjack.server;
}