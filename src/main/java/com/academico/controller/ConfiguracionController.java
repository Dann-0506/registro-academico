package com.academico.controller;

import com.academico.service.individuals.ConfiguracionService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.Duration;

import java.math.BigDecimal;

public class ConfiguracionController {

    @FXML private TextField campoMaxima;
    @FXML private TextField campoMinima;
    @FXML private Label mensajeGeneral;

    private final ConfiguracionService configService = new ConfiguracionService();

    @FXML
    public void initialize() {
        cargarDatos();
    }

    private void cargarDatos() {
        try {
            BigDecimal minima = configService.obtenerCalificacionMinima();
            BigDecimal maxima = configService.obtenerCalificacionMaxima();

            campoMinima.setText(minima.stripTrailingZeros().toPlainString());
            campoMaxima.setText(maxima.stripTrailingZeros().toPlainString());
        } catch (Exception e) {
            mostrarNotificacion("Error al cargar la configuración actual: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleGuardar() {
        try {
            String textoMinima = campoMinima.getText().trim();
            String textoMaxima = campoMaxima.getText().trim();

            if (textoMinima.isEmpty() || textoMaxima.isEmpty()) {
                throw new IllegalArgumentException("Debes llenar ambos campos numéricos.");
            }

            BigDecimal minima;
            BigDecimal maxima;
            try {
                minima = new BigDecimal(textoMinima);
                maxima = new BigDecimal(textoMaxima);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Por favor ingresa únicamente valores numéricos válidos.");
            }

            configService.actualizarLimites(minima, maxima);
            
            mostrarNotificacion("¡Configuración actualizada con éxito!", false);

            cargarDatos();

        } catch (IllegalArgumentException e) {
            mostrarNotificacion(e.getMessage(), true);
        } catch (Exception e) {
            mostrarNotificacion(e.getMessage(), true);
        }
    }

    // === UTILIDAD ESTÁNDAR DE NOTIFICACIÓN ===
    private void mostrarNotificacion(String msj, boolean error) {
        mensajeGeneral.setText(msj);
        mensajeGeneral.setVisible(true); 
        mensajeGeneral.setManaged(true);
        
        mensajeGeneral.setStyle("-fx-background-color: " + (error ? "#f8d7da" : "#d4edda") + 
                                "; -fx-text-fill: " + (error ? "#721c24" : "#155724") + 
                                "; -fx-padding: 12 25; -fx-background-radius: 30; -fx-font-weight: bold;");
        
        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(Duration.seconds(1), mensajeGeneral);
        fade.setDelay(Duration.seconds(2.5)); 
        fade.setFromValue(1.0); 
        fade.setToValue(0.0);
        fade.setOnFinished(e -> { 
            mensajeGeneral.setVisible(false); 
            mensajeGeneral.setManaged(false); 
        });
        fade.play();
    }
}