module com.blackjack {

    requires javafx.controls;
    requires javafx.fxml;
    requires java.rmi;

    opens com.blackjack to javafx.fxml;
    opens com.blackjack.client.controllers to javafx.fxml;

    exports com.blackjack;
    exports com.blackjack.common;
    exports com.blackjack.common.modelos;
    exports com.blackjack.server;
}