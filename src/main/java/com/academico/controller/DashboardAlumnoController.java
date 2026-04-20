package com.academico.controller;

import com.academico.model.Alumno;
import com.academico.model.Grupo;
import com.academico.model.Usuario;
import com.academico.service.individuals.AlumnoService;
import com.academico.service.individuals.UsuarioService;
import com.academico.util.NavegationUtil;
import com.academico.util.SessionManagerUtil;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class DashboardAlumnoController {

    public static DashboardAlumnoController instancia;

    // === ELEMENTOS UI ===
    @FXML private Label labelNombreUsuario;
    @FXML private Label labelMatricula;
    @FXML private VBox menuPrincipal;
    @FXML private VBox menuCursoContextual;
    @FXML private StackPane areaPrincipal;

    // === PERFIL ===
    @FXML private StackPane panelPerfilFlotante;
    @FXML private TextField campoPerfilNombre;
    @FXML private TextField campoPerfilEmail;
    @FXML private PasswordField campoPerfilPassword;
    @FXML private Label mensajePerfil;

    // === ESTADO Y SERVICIOS ===
    private final AlumnoService alumnoService = new AlumnoService();
    private final UsuarioService usuarioService = new UsuarioService();

    private Alumno perfilAlumno;
    private Grupo cursoSeleccionado;
    private Button botonActivo;

    private static final String ESTILO_BOTON_NORMAL = "-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.9); -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-border-width: 0; -fx-cursor: hand; -fx-padding: 8 12;";
    private static final String ESTILO_BOTON_ACTIVO = "-fx-background-color: rgba(255,255,255,0.18); -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-border-width: 0; -fx-cursor: hand; -fx-padding: 8 12;";
    private static final String ESTILO_SECCION = "-fx-text-fill: rgba(255,255,255,0.55); -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 8 8 4 8;";

    @FXML
    public void initialize() {
        instancia = this;
        Usuario usuario = SessionManagerUtil.getUsuarioActual();
        if (usuario == null) {
            NavegationUtil.irA(NavegationUtil.LOGIN);
            return;
        }

        try {
            perfilAlumno = alumnoService.buscarPorUsuarioId(usuario.getId());
            labelNombreUsuario.setText(usuario.getNombre());
            labelMatricula.setText("Matrícula: " + perfilAlumno.getMatricula());

            construirMenuPrincipal();
            abrirVista(NavegationUtil.MIS_CURSOS_ALUMNO, null);

            if (usuario.isRequiereCambioPassword()) {
                javafx.application.Platform.runLater(() -> {
                    abrirPerfilFlotante();
                    mostrarNotificacionPerfil("Por tu seguridad, actualiza tu contraseña predeterminada.", true, true);
                });
            }

        } catch (Exception e) {
            System.err.println("Error crítico al cargar perfil alumno: " + e.getMessage());
        }
    }

    private void construirMenuPrincipal() {
        menuPrincipal.getChildren().clear();

        Label titulo = new Label("MI ESPACIO");
        titulo.setStyle(ESTILO_SECCION);
        menuPrincipal.getChildren().add(titulo);

        Button btnMisCursos = crearBotonMenu("Mis Cursos", null);
        btnMisCursos.setOnAction(e -> {
            actualizarBotonActivo(btnMisCursos);
            menuCursoContextual.setVisible(false);
            menuCursoContextual.setManaged(false);
            this.cursoSeleccionado = null;
            NavegationUtil.cargarEnArea(areaPrincipal, NavegationUtil.MIS_CURSOS_ALUMNO);
        });

        Button btnPerfil = crearBotonMenu("Mi Perfil", null);
        btnPerfil.setOnAction(e -> abrirPerfilFlotante());

        menuPrincipal.getChildren().addAll(btnMisCursos, btnPerfil);
    }

    public void activarMenuDeCurso(Grupo curso) {
        this.cursoSeleccionado = curso;
        menuCursoContextual.getChildren().clear();

        Label titulo = new Label("CURSO ACTUAL");
        titulo.setStyle(ESTILO_SECCION);

        Label subtitulo = new Label(curso.getClave() + "\n" + curso.getMateriaNombre());
        subtitulo.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.75); -fx-padding: 0 0 10 12; -fx-font-weight: bold;");

        Button btnDetalles = crearBotonMenu("Mi Boleta", NavegationUtil.ALUMNO_CURSO_DETALLE);

        menuCursoContextual.getChildren().addAll(titulo, subtitulo, btnDetalles);

        menuCursoContextual.setVisible(true);
        menuCursoContextual.setManaged(true);

        btnDetalles.fire();
    }

    private Button crearBotonMenu(String texto, String rutaFxml) {
        Button boton = new Button(texto);
        boton.setMaxWidth(Double.MAX_VALUE);
        boton.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        boton.setStyle(ESTILO_BOTON_NORMAL);

        if (rutaFxml != null) {
            boton.setOnAction(e -> {
                actualizarBotonActivo(boton);
                NavegationUtil.cargarEnArea(areaPrincipal, rutaFxml);
            });
        }
        return boton;
    }

    private void actualizarBotonActivo(Button boton) {
        if (botonActivo != null) {
            botonActivo.setStyle(ESTILO_BOTON_NORMAL);
        }
        botonActivo = boton;
        botonActivo.setStyle(ESTILO_BOTON_ACTIVO);
    }

    public void abrirVista(String rutaFxml, Button botonOrigen) {
        if (botonOrigen != null) actualizarBotonActivo(botonOrigen);
        NavegationUtil.cargarEnArea(areaPrincipal, rutaFxml);
    }

    // === GESTIÓN DE PERFIL ===

    @FXML
    private void abrirPerfilFlotante() {
        Usuario u = SessionManagerUtil.getUsuarioActual();
        if (u != null) {
            campoPerfilNombre.setText(u.getNombre());
            campoPerfilEmail.setText(u.getEmail());
            campoPerfilPassword.clear();
            mensajePerfil.setVisible(false);
            mensajePerfil.setManaged(false);
            panelPerfilFlotante.setVisible(true);
            panelPerfilFlotante.setManaged(true);
        }
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
        mensajePerfil.setVisible(true);
        mensajePerfil.setManaged(true);
        mensajePerfil.setOpacity(1.0);
        mensajePerfil.setStyle(esError
                ? "-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-padding: 8; -fx-background-radius: 5;"
                : "-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-padding: 8; -fx-background-radius: 5;");

        if (!persistente) {
            FadeTransition fade = new FadeTransition(Duration.seconds(1), mensajePerfil);
            fade.setDelay(Duration.seconds(2));
            fade.setToValue(0.0);
            fade.play();
        }
    }

    @FXML
    private void handleCerrarSesion() {
        SessionManagerUtil.cerrarSesion();
        instancia = null;
        javafx.stage.Stage stage = (javafx.stage.Stage) menuPrincipal.getScene().getWindow();
        stage.setMaximized(false);
        stage.setResizable(false);
        stage.setMinWidth(700);
        stage.setMinHeight(440);
        stage.setWidth(700);
        stage.setHeight(440);
        NavegationUtil.irA(NavegationUtil.LOGIN);
        stage.centerOnScreen();
    }

    public Grupo getCursoSeleccionado() {
        return this.cursoSeleccionado;
    }

    public Alumno getPerfilAlumno() {
        return this.perfilAlumno;
    }
}