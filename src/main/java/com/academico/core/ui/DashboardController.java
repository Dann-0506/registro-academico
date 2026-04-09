package com.academico.core.ui;

import com.academico.auth.Usuario;
import com.academico.core.MainApp;
import com.academico.core.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class DashboardController {

    @FXML private Label    labelNombreUsuario;
    @FXML private Label    labelRolUsuario;
    @FXML private Label    labelBienvenida;
    @FXML private VBox     menuNavegacion;
    @FXML private StackPane areaPrincipal;

    // Rastrea el botón activo para resaltarlo
    private Button botonActivo;

    @FXML
    public void initialize() {
        Usuario usuario = SessionManager.getUsuarioActual();
        if (usuario == null) {
            MainApp.navegarA("/com/academico/auth/ui/login.fxml", "Iniciar Sesión");
            return;
        }

        labelNombreUsuario.setText(usuario.getNombre());
        labelRolUsuario.setText(formatearRol(usuario.getRol()));
        labelBienvenida.setText("Bienvenido, " + usuario.getNombre().split(" ")[0] + ".");

        construirMenu(usuario.getRol());
    }

    private void construirMenu(String rol) {
        menuNavegacion.getChildren().clear();

        if ("admin".equals(rol)) {
            agregarSeccion("CATÁLOGOS");
            agregarBoton("Alumnos",      "/com/academico/inscripciones/ui/alumnos.fxml");
            agregarBoton("Materias",     "/com/academico/academia/ui/materias.fxml");
            agregarBoton("Maestros",     "/com/academico/academia/ui/maestros.fxml");
            agregarBoton("Grupos",       "/com/academico/academia/ui/grupos.fxml");
            agregarBoton("Inscripciones","/com/academico/inscripciones/ui/inscripciones.fxml");

            agregarSeccion("SISTEMA");
            agregarBoton("Configuración","/com/academico/core/ui/configuracion.fxml");
            agregarBoton("Utilerías",    "/com/academico/core/ui/utileria.fxml");
        }

        if ("maestro".equals(rol)) {
            agregarSeccion("MIS GRUPOS");
            agregarBoton("Actividades",  "/com/academico/calificaciones/ui/actividades.fxml");
            agregarBoton("Calificaciones","/com/academico/calificaciones/ui/calificaciones.fxml");
            agregarBoton("Reportes",     "/com/academico/calificaciones/ui/reportes.fxml");

            agregarSeccion("CUENTA");
            agregarBoton("Mi perfil",    "/com/academico/core/ui/perfil.fxml");
        }
    }

    private void agregarSeccion(String titulo) {
        Label label = new Label(titulo);
        label.getStyleClass().add("sidebar-titulo");
        menuNavegacion.getChildren().add(label);
    }

    private void agregarBoton(String texto, String rutaFxml) {
        Button boton = new Button(texto);
        boton.getStyleClass().add("sidebar-boton");
        boton.setMaxWidth(Double.MAX_VALUE);
        boton.setOnAction(e -> cargarVista(rutaFxml, boton));
        menuNavegacion.getChildren().add(boton);
    }

    private void cargarVista(String rutaFxml, Button botonOrigen) {
        try {
            // Actualiza el estilo del botón activo
            if (botonActivo != null) {
                botonActivo.getStyleClass().remove("sidebar-boton-activo");
            }
            botonActivo = botonOrigen;
            botonActivo.getStyleClass().add("sidebar-boton-activo");

            // Carga el FXML en el área principal
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource(rutaFxml));
            Node vista = loader.load();
            areaPrincipal.getChildren().setAll(vista);

        } catch (IOException e) {
            // Vista aún no construida — muestra mensaje temporal
            Label placeholder = new Label(
                "Vista en construcción: " + rutaFxml);
            placeholder.setStyle("-fx-text-fill: -color-fg-muted;");
            areaPrincipal.getChildren().setAll(placeholder);
        }
    }

    @FXML
    private void handleCerrarSesion() {
        SessionManager.cerrarSesion();
        MainApp.navegarA("/com/academico/auth/ui/login.fxml", "Iniciar Sesión");
    }

    private String formatearRol(String rol) {
        return switch (rol) {
            case "admin"   -> "Administrador";
            case "maestro" -> "Docente";
            case "alumno"  -> "Estudiante";
            default        -> rol;
        };
    }
}