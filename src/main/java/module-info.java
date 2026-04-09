module com.academico.core {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;

    // AtlantaFX
    requires atlantafx.base;

    // Base de datos
    requires java.sql;
    requires com.zaxxer.hikari;
    requires org.postgresql.jdbc;

    // Utilidades
    requires io.github.cdimascio.dotenv.java;
    requires bcrypt;
    requires com.opencsv;

    // Abre los paquetes que JavaFX necesita acceder por reflexión
    opens com.academico.core to javafx.fxml;
    opens com.academico.auth to javafx.fxml;
    opens com.academico.auth.ui to javafx.fxml;
    opens com.academico.academia to javafx.fxml;
    opens com.academico.academia.ui to javafx.fxml;
    opens com.academico.calificaciones to javafx.fxml;
    opens com.academico.calificaciones.ui to javafx.fxml;
    opens com.academico.inscripciones to javafx.fxml;
    opens com.academico.inscripciones.ui to javafx.fxml;
    opens com.academico.core.ui to javafx.fxml;

    // Expone los modelos a JavaFX para binding de propiedades
    opens com.academico.academia.model to javafx.base;
    opens com.academico.auth.model to javafx.base;
    opens com.academico.calificaciones.model to javafx.base;
    opens com.academico.inscripciones.model to javafx.base;

    // Exporta el paquete principal
    exports com.academico.core;
}