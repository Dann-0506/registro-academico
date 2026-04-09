package com.academico.core.ui;

import com.academico.auth.Usuario;
import com.academico.core.util.SessionManager;
import com.academico.core.util.NavegationUtil;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

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
            NavegationUtil.irA(NavegationUtil.LOGIN);
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
            agregarBoton("Alumnos",       NavegationUtil.ALUMNOS);
            agregarBoton("Materias",      NavegationUtil.MATERIAS);
            agregarBoton("Maestros",      NavegationUtil.MAESTROS);
            agregarBoton("Grupos",        NavegationUtil.GRUPOS);
            agregarBoton("Inscripciones", NavegationUtil.INSCRIPCIONES);

            agregarSeccion("SISTEMA");
            agregarBoton("Configuración", NavegationUtil.CONFIGURACION);
            agregarBoton("Utilerías",     NavegationUtil.UTILERIA);
        }

        if ("maestro".equals(rol)) {
            agregarSeccion("MIS GRUPOS");
            agregarBoton("Actividades",    NavegationUtil.ACTIVIDADES);
            agregarBoton("Calificaciones", NavegationUtil.CALIFICACIONES);
            agregarBoton("Reportes",       NavegationUtil.REPORTES);

            agregarSeccion("CUENTA");
            agregarBoton("Mi perfil",      NavegationUtil.PERFIL);
}
    }

    private void agregarSeccion(String titulo) {
        Label label = new Label(titulo);
        label.getStyleClass().add("sidebar-titulo");
        menuNavegacion.getChildren().add(label);
    }

    // Después
    private void agregarBoton(String texto, String rutaFxml) {
        Button boton = new Button(texto);
        boton.getStyleClass().add("sidebar-boton");
        boton.setMaxWidth(Double.MAX_VALUE);
        boton.setOnAction(e -> {
            actualizarBotonActivo(boton);
            NavegationUtil.cargarEnArea(areaPrincipal, rutaFxml);
        });
        menuNavegacion.getChildren().add(boton);
    }

    private void actualizarBotonActivo(Button boton) {
        if (botonActivo != null) {
            botonActivo.getStyleClass().remove("sidebar-boton-activo");
        }
        botonActivo = boton;
        botonActivo.getStyleClass().add("sidebar-boton-activo");
    }

    @FXML
    private void handleCerrarSesion() {
        SessionManager.cerrarSesion();
        NavegationUtil.irA(NavegationUtil.LOGIN);
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