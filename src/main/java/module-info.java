module com.app.superapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.sun.jna;


    opens com.app.superapp to javafx.fxml;
    exports com.app.superapp;
    exports com.app.superapp.Processes;
    opens com.app.superapp.Processes to javafx.fxml;
    exports com.app.superapp.Socket;
    opens com.app.superapp.Socket to javafx.fxml;
}