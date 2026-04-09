package com.academico.core;

import atlantafx.base.theme.PrimerLight;

import com.academico.auth.AuthService;
import com.academico.core.db.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        // Aplica el tema de AtlantaFX globalmente
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        // Configura la ventana principal
        stage.setTitle("Sistema de Registro de Resultados Académicos");
        stage.setMinWidth(1280);
        stage.setMinHeight(720);
        stage.setWidth(1280);
        stage.setHeight(720);

        // Carga la pantalla de login como escena inicial
        navegarA("/com/academico/auth/ui/login.fxml", "Iniciar Sesión");

        stage.show();
    }

    @Override
    public void init() {
        // Inicializa la BD en hilo de fondo
        try {
            DatabaseManager.initialize();
        } catch (Exception e) {
            System.err.println("Error crítico al inicializar la base de datos: " + e.getMessage());
            // Si la BD falla, no tiene sentido abrir la aplicación
            javafx.application.Platform.exit();
        }
    }

    @Override
    public void stop() {
        // Limpia recursos al cerrar la aplicación
        DatabaseManager.close();
    }

    public static void navegarA(String rutaFxml, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource(rutaFxml));
            Scene scene = new Scene(loader.load());
            primaryStage.setTitle("Registro Académico — " + titulo);
            primaryStage.setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar la vista: " + rutaFxml, e);
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}