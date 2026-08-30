module com.example.lifestylelevellingasianlifestyle {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;


    opens com.example.lifestylelevellingasianlifestyle to javafx.fxml;
    exports com.example.lifestylelevellingasianlifestyle;
}