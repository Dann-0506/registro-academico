package com.academico.controller;

import com.academico.model.Usuario;
import com.academico.service.individuals.UsuarioService;
import com.academico.util.NavegationUtil;
import com.academico.util.SessionManagerUtil;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class DashboardAdminController {

    // === ELEMENTOS PRINCIPALES (Menú y Cabecera) ===
    @FXML private Label labelNombreUsuario;
    @FXML private Label labelRolUsuario;
    @FXML private Label labelBienvenida;
    @FXML private VBox menuNavegacion;
    @FXML private StackPane areaPrincipal;
    @FXML private Label labelInicialesUsuario;
    @FXML private Label labelInicialesHeader;

    // === ELEMENTOS DE MI PERFIL (Panel Flotante) ===
    @FXML private StackPane panelPerfilFlotante;
    @FXML private TextField campoPerfilNombre;
    @FXML private TextField campoPerfilEmail;
    @FXML private PasswordField campoPerfilPassword;
    @FXML private Label mensajePerfil;

    // === VARIABLES DE ESTADO Y SERVICIOS ===
    private final UsuarioService usuarioService = new UsuarioService();
    private Button botonActivo;

    // ==========================================
    // INICIALIZACIÓN
    // ==========================================

    @FXML
    public void initialize() {
        Usuario usuario = SessionManagerUtil.getUsuarioActual();
        if (usuario == null) {
            NavegationUtil.irA(NavegationUtil.LOGIN);
            return;
        }

        labelNombreUsuario.setText(usuario.getNombre());
        labelRolUsuario.setText(formatearRol(usuario.getRol()));
        labelBienvenida.setText("Bienvenido, " + usuario.getNombre().split(" ")[0] + ".");

        construirMenu(usuario.getRol());

        NavegationUtil.cargarEnArea(areaPrincipal, NavegationUtil.ANALISIS);

        // Verificación de seguridad por contraseña genérica
        if (usuario.isRequiereCambioPassword()) {
            javafx.application.Platform.runLater(() -> {
                abrirPerfilFlotante();
                mostrarNotificacionPerfil("¡Atención! Estás usando la contraseña predeterminada. Por tu seguridad, cámbiala ahora.", true, true);
            });
        }
    }

    // ==========================================
    // LÓGICA DE NAVEGACIÓN Y MENÚ
    // ==========================================

    private void construirMenu(String rol) {
        menuNavegacion.getChildren().clear();

        agregarSeccion("PRINCIPAL");
        agregarBoton("Análisis de Datos y Rendimiento", NavegationUtil.ANALISIS);

        agregarSeccion("CATÁLOGOS");
        agregarBoton("Administradores", NavegationUtil.ADMINS);
        agregarBoton("Alumnos",         NavegationUtil.ALUMNOS);
        agregarBoton("Materias",        NavegationUtil.MATERIAS);
        agregarBoton("Maestros",        NavegationUtil.MAESTROS);
        agregarBoton("Grupos",          NavegationUtil.GRUPOS);
        agregarBoton("Inscripciones",   NavegationUtil.INSCRIPCIONES);

        agregarSeccion("SISTEMA");
        agregarBoton("Configuración", NavegationUtil.CONFIGURACION);
        agregarBoton("Respaldos",     NavegationUtil.RESPALDOS);

        agregarSeccion("CUENTA");
        Button btnPerfil = new Button("Mi perfil");
        btnPerfil.setMaxWidth(Double.MAX_VALUE);
        btnPerfil.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        btnPerfil.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.9); -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-border-width: 0; -fx-cursor: hand; -fx-padding: 8 12;");
        btnPerfil.setOnAction(e -> abrirPerfilFlotante());
        menuNavegacion.getChildren().add(btnPerfil);
    }

    private void agregarSeccion(String titulo) {
        Label label = new Label(titulo);
        label.setStyle("-fx-text-fill: rgba(255,255,255,0.55); -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 8 8 4 8;");
        menuNavegacion.getChildren().add(label);
    }

    private void agregarBoton(String texto, String rutaFxml) {
        Button boton = new Button(texto);
        boton.setMaxWidth(Double.MAX_VALUE);
        boton.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        boton.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.9); -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-border-width: 0; -fx-cursor: hand; -fx-padding: 8 12;");
        boton.setOnAction(e -> {
            actualizarBotonActivo(boton);
            NavegationUtil.cargarEnArea(areaPrincipal, rutaFxml);
        });
        menuNavegacion.getChildren().add(boton);
    }

    private void actualizarBotonActivo(Button boton) {
        if (botonActivo != null) {
            botonActivo.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.9); -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-border-width: 0; -fx-cursor: hand; -fx-padding: 8 12;");
        }
        botonActivo = boton;
        botonActivo.setStyle("-fx-background-color: rgba(255,255,255,0.18); -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-border-width: 0; -fx-cursor: hand; -fx-padding: 8 12;");
    }

    private String formatearRol(String rol) {
        return switch (rol) {
            case "admin"   -> "Administrador";
            case "maestro" -> "Docente";
            case "alumno"  -> "Estudiante";
            default        -> rol;
        };
    }

    // ==========================================
    // LÓGICA DE SESIÓN
    // ==========================================

    @FXML
    private void handleCerrarSesion() {
        SessionManagerUtil.cerrarSesion();

        javafx.stage.Stage stage = (javafx.stage.Stage) menuNavegacion.getScene().getWindow();

        // Restaurar dimensiones de la ventana de Login
        stage.setMaximized(false);
        stage.setResizable(false);
        stage.setMinWidth(700);
        stage.setMinHeight(440);
        stage.setWidth(700);
        stage.setHeight(440);

        NavegationUtil.irA(NavegationUtil.LOGIN);
        stage.centerOnScreen();
    }

    // ==========================================
    // GESTIÓN DE MI PERFIL
    // ==========================================

    private void abrirPerfilFlotante() {
        Usuario actual = SessionManagerUtil.getUsuarioActual();
        campoPerfilNombre.setText(actual.getNombre());
        campoPerfilEmail.setText(actual.getEmail());
        campoPerfilPassword.clear();

        panelPerfilFlotante.setVisible(true);
        panelPerfilFlotante.setManaged(true);
    }

    @FXML
    private void handleGuardarPerfil() {
        Usuario actual = SessionManagerUtil.getUsuarioActual();
        String nuevoNombre = campoPerfilNombre.getText().trim();
        String nuevoEmail = campoPerfilEmail.getText().trim();
        String nuevaPass = campoPerfilPassword.getText();

        if (nuevoNombre.isEmpty() || nuevoEmail.isEmpty()) {
            mostrarNotificacionPerfil("El nombre y correo no pueden estar vacíos.", true, false);
            return;
        }

        try {
            usuarioService.actualizarPerfil(actual.getId(), nuevoNombre, nuevoEmail, nuevaPass);

            actual.setNombre(nuevoNombre);
            actual.setEmail(nuevoEmail);
            labelNombreUsuario.setText(nuevoNombre);
            labelBienvenida.setText("Bienvenido, " + nuevoNombre.split(" ")[0] + ".");

            mostrarNotificacionPerfil("Perfil actualizado con éxito.", false, false);

            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            pause.setOnFinished(e -> handleCerrarPerfil());
            pause.play();

        } catch (Exception e) {
            mostrarNotificacionPerfil(e.getMessage(), true, false);
        }
    }

    @FXML
    private void handleCerrarPerfil() {
        panelPerfilFlotante.setVisible(false);
        panelPerfilFlotante.setManaged(false);
    }

    private void mostrarNotificacionPerfil(String mensaje, boolean esError, boolean persistente) {
        mensajePerfil.setText(mensaje);
        mensajePerfil.setOpacity(1.0);
        mensajePerfil.setVisible(true);
        mensajePerfil.setManaged(true);

        if (esError) {
            mensajePerfil.setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-padding: 8; -fx-background-radius: 5;");
        } else {
            mensajePerfil.setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-padding: 8; -fx-background-radius: 5;");
        }

        if (!persistente) {
            javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(Duration.seconds(1), mensajePerfil);
            fade.setDelay(Duration.seconds(2));
            fade.setToValue(0.0);
            fade.setOnFinished(e -> {
                mensajePerfil.setVisible(false);
                mensajePerfil.setManaged(false);
            });
            fade.play();
        }
    }
}