module com.academico.core {
    requires javafx.controls;
    requires javafx.fxml;
    requires atlantafx.base;
    requires java.sql;
    requires com.zaxxer.hikari;
    requires org.postgresql.jdbc;
    requires io.github.cdimascio.dotenv.java;
    requires bcrypt;
    requires com.opencsv;

    opens com.academico.core to javafx.fxml;
    opens com.academico.core.ui to javafx.fxml;
    opens com.academico.core.util to javafx.fxml;
    opens com.academico.auth to javafx.fxml;
    opens com.academico.auth.ui to javafx.fxml;
    opens com.academico.inscripciones to javafx.fxml;
    opens com.academico.academia.model to javafx.base;
    opens com.academico.calificaciones.model to javafx.base;

    exports com.academico.core;
}