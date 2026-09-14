module com.example.lifestylelevellingasianlifestyle {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.net.http;
    requires jbcrypt;
    requires com.fasterxml.jackson.databind;


    opens com.example.lifestylelevellingasianlifestyle to javafx.fxml;
    exports com.example.lifestylelevellingasianlifestyle;
}
