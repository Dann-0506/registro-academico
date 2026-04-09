package com.academico.controller;

import com.academico.dao.AlumnoDAO;
import com.academico.dao.UsuarioDAO;
import com.academico.model.Alumno;
import com.academico.model.Usuario;
import com.academico.service.CargaDatosService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.sql.SQLException;
import java.util.List;

public class AlumnosController {

    @FXML private TextField   campoBusqueda;
    @FXML private TableView<Alumno> tablaAlumnos;
    @FXML private TableColumn<Alumno, String> colMatricula;
    @FXML private TableColumn<Alumno, String> colNombre;
    @FXML private TableColumn<Alumno, String> colEmail;
    @FXML private TableColumn<Alumno, Void>   colAcciones;

    @FXML private VBox   panelFormulario;
    @FXML private Label  labelTituloFormulario;
    @FXML private TextField campoMatricula;
    @FXML private TextField campoNombre;
    @FXML private TextField campoEmail;
    @FXML private Label  errorMatricula;
    @FXML private Label  errorNombre;
    @FXML private Label  errorEmail;
    @FXML private Label  mensajeGeneral;
    @FXML private Button botonGuardar;

    private final AlumnoDAO      alumnoDAO      = new AlumnoDAO();
    private final UsuarioDAO     usuarioDAO     = new UsuarioDAO();
    private final CargaDatosService cargaService = new CargaDatosService();

    private ObservableList<Alumno> listaAlumnos = FXCollections.observableArrayList();
    private FilteredList<Alumno>   listaFiltrada;
    private Alumno alumnoEnEdicion = null;

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarAlumnos();
    }

    // ── Configuración de tabla ────────────────────────────────────────────────

    private void configurarColumnas() {
        colMatricula.setCellValueFactory(new PropertyValueFactory<>("matricula"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar  = new Button("Editar");
            private final HBox   contenedor = new HBox(8, btnEditar);

            {
                btnEditar.getStyleClass().add("flat");
                btnEditar.setOnAction(e -> {
                    Alumno alumno = getTableView().getItems().get(getIndex());
                    handleEditar(alumno);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : contenedor);
            }
        });

        listaFiltrada = new FilteredList<>(listaAlumnos, p -> true);
        tablaAlumnos.setItems(listaFiltrada);
    }

    private void cargarAlumnos() {
        try {
            listaAlumnos.setAll(alumnoDAO.findAll());
        } catch (SQLException e) {
            mostrarError(mensajeGeneral, "Error al cargar alumnos: " + e.getMessage());
        }
    }

    // ── Búsqueda ─────────────────────────────────────────────────────────────

    @FXML
    private void handleBusqueda() {
        String texto = campoBusqueda.getText().toLowerCase().trim();
        listaFiltrada.setPredicate(alumno -> {
            if (texto.isEmpty()) return true;
            return alumno.getMatricula().toLowerCase().contains(texto)
                || (alumno.getNombre() != null
                    && alumno.getNombre().toLowerCase().contains(texto));
        });
    }

    // ── Formulario ───────────────────────────────────────────────────────────

    @FXML
    private void handleNuevo() {
        alumnoEnEdicion = null;
        labelTituloFormulario.setText("Nuevo alumno");
        limpiarFormulario();
        mostrarFormulario(true);
    }

    private void handleEditar(Alumno alumno) {
        alumnoEnEdicion = alumno;
        labelTituloFormulario.setText("Editar alumno");
        campoMatricula.setText(alumno.getMatricula());
        campoMatricula.setDisable(true); // matrícula no editable
        campoNombre.setText(alumno.getNombre() != null ? alumno.getNombre() : "");
        campoEmail.setText(alumno.getEmail() != null ? alumno.getEmail() : "");
        limpiarErrores();
        mostrarFormulario(true);
    }

    @FXML
    private void handleCancelar() {
        mostrarFormulario(false);
        limpiarFormulario();
    }

    @FXML
    private void handleGuardar() {
        if (!validarFormulario()) return;

        try {
            if (alumnoEnEdicion == null) {
                // Crear usuario y alumno nuevos
                Usuario usuario = new Usuario();
                usuario.setNombre(campoNombre.getText().trim());
                usuario.setEmail(campoEmail.getText().trim().isEmpty()
                    ? campoMatricula.getText().trim() + "@academico.local"
                    : campoEmail.getText().trim());
                usuario.setPasswordHash(
                    new com.academico.service.AuthService()
                        .hashearPassword(campoMatricula.getText().trim()));
                usuario.setRol("alumno");
                usuario.setActivo(true);
                Usuario usuarioGuardado = usuarioDAO.insertar(usuario);

                Alumno alumno = new Alumno();
                alumno.setUsuarioId(usuarioGuardado.getId());
                alumno.setMatricula(campoMatricula.getText().trim());
                alumnoDAO.insertar(alumno);

            } else {
                // Actualizar usuario vinculado
                if (alumnoEnEdicion.getUsuarioId() != null) {
                    Usuario usuario = usuarioDAO
                        .findById(alumnoEnEdicion.getUsuarioId())
                        .orElse(null);
                    if (usuario != null) {
                        usuario.setNombre(campoNombre.getText().trim());
                        if (!campoEmail.getText().trim().isEmpty()) {
                            usuario.setEmail(campoEmail.getText().trim());
                        }
                        usuarioDAO.actualizar(usuario);
                    }
                }
                alumnoDAO.actualizar(alumnoEnEdicion);
            }

            cargarAlumnos();
            mostrarFormulario(false);
            limpiarFormulario();

        } catch (SQLException e) {
            mostrarError(mensajeGeneral,
                "Error al guardar: " + e.getMessage());
        }
    }

    // ── Importación CSV ──────────────────────────────────────────────────────

    @FXML
    private void handleImportarCsv() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar archivo CSV de alumnos");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Archivos CSV", "*.csv"));

        File archivo = chooser.showOpenDialog(
            tablaAlumnos.getScene().getWindow());

        if (archivo == null) return;

        try (FileInputStream fis = new FileInputStream(archivo)) {
            List<String> duplicados = cargaService.importarAlumnosCsv(fis);
            cargarAlumnos();

            String mensaje = "Importación completada.";
            if (!duplicados.isEmpty()) {
                mensaje += " Matrículas ignoradas por duplicado: "
                    + String.join(", ", duplicados);
            }
            mostrarExito(mensajeGeneral, mensaje);
            mostrarFormulario(true);

        } catch (Exception e) {
            mostrarError(mensajeGeneral,
                "Error al importar CSV: " + e.getMessage());
            mostrarFormulario(true);
        }
    }

    // ── Validación ───────────────────────────────────────────────────────────

    private boolean validarFormulario() {
        limpiarErrores();
        boolean valido = true;

        if (campoMatricula.getText().trim().isEmpty()) {
            mostrarError(errorMatricula, "La matrícula es obligatoria.");
            valido = false;
        }

        if (campoNombre.getText().trim().isEmpty()) {
            mostrarError(errorNombre, "El nombre es obligatorio.");
            valido = false;
        }

        String email = campoEmail.getText().trim();
        if (!email.isEmpty() && !email.contains("@")) {
            mostrarError(errorEmail, "El correo no tiene un formato válido.");
            valido = false;
        }

        return valido;
    }

    // ── Helpers visuales ─────────────────────────────────────────────────────

    private void mostrarFormulario(boolean visible) {
        panelFormulario.setVisible(visible);
        panelFormulario.setManaged(visible);
        if (!visible) campoMatricula.setDisable(false);
    }

    private void limpiarFormulario() {
        campoMatricula.clear();
        campoNombre.clear();
        campoEmail.clear();
        campoMatricula.setDisable(false);
        limpiarErrores();
        ocultarMensaje(mensajeGeneral);
    }

    private void limpiarErrores() {
        ocultarMensaje(errorMatricula);
        ocultarMensaje(errorNombre);
        ocultarMensaje(errorEmail);
    }

    private void mostrarError(Label label, String mensaje) {
        label.setText(mensaje);
        label.getStyleClass().removeAll("text-success");
        label.getStyleClass().add("text-danger");
        label.setVisible(true);
        label.setManaged(true);
    }

    private void mostrarExito(Label label, String mensaje) {
        label.setText(mensaje);
        label.getStyleClass().removeAll("text-danger");
        label.getStyleClass().add("text-success");
        label.setVisible(true);
        label.setManaged(true);
    }

    private void ocultarMensaje(Label label) {
        label.setVisible(false);
        label.setManaged(false);
        label.setText("");
    }
}