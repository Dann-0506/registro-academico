package com.academico.controller;

import com.academico.model.Alumno;
import com.academico.service.individuals.AlumnoService;
import com.academico.service.CargaDatosService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

public class AlumnosController {
    
    @FXML private TableView<Alumno> tablaAlumnos;
    @FXML private TableColumn<Alumno, String> colMatricula;
    @FXML private TableColumn<Alumno, String> colNombre;
    @FXML private TableColumn<Alumno, String> colEmail;
    @FXML private TableColumn<Alumno, Void> colAcciones;

    @FXML private Pagination paginacionAlumnos;

    @FXML private TextField campoBusqueda;

    @FXML private Label errorMatricula;
    @FXML private Label errorNombre;
    @FXML private Label errorEmail;
    @FXML private Label lblTituloConfirmacion;
    @FXML private Label lblMensajeConfirmacion;
    @FXML private Label labelTituloFormulario;
    @FXML private Label mensajeGeneral;
    @FXML private Label labelNotaPassword;

    @FXML private TextField campoMatricula;
    @FXML private TextField campoNombre;
    @FXML private TextField campoEmail;
    
    @FXML private Button botonGuardar;
    @FXML private Button btnRestablecerPassword;
    @FXML private Button btnConfirmarAccion;

    @FXML private StackPane panelConfirmacion;
    @FXML private StackPane panelFormulario;

    private Runnable accionPendiente;

    private AlumnoService alumnoService = new AlumnoService();
    private CargaDatosService cargaDatosService = new CargaDatosService();
    private ObservableList<Alumno> listaAlumnos = FXCollections.observableArrayList();
    private FilteredList<Alumno> alumnosFiltrados;
    private Alumno alumnoEnEdicion = null;
    private final int FILAS_POR_PAGINA = 15;

    @FXML
    public void initialize() {
        tablaAlumnos.setFixedCellSize(48);
        alumnosFiltrados = new FilteredList<>(listaAlumnos, p -> true);

        configurarColumnas();
        cargarDatos();
    }

    private void configurarColumnas() {
        colMatricula.setCellValueFactory(new PropertyValueFactory<>("matricula"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnEstado = new Button(); // Dinámico (Activar/Desactivar)
            private final Button btnEliminar = new Button("Eliminar");
            private final HBox panel = new HBox(8, btnEditar, btnEstado, btnEliminar);

            {
                btnEditar.getStyleClass().addAll("accent", "flat");
                btnEstado.getStyleClass().addAll("flat");
                btnEliminar.getStyleClass().addAll("danger", "flat");
                panel.setStyle("-fx-alignment: center;");

                btnEditar.setOnAction(e -> abrirEdicion(getTableView().getItems().get(getIndex())));
                btnEliminar.setOnAction(e -> confirmarEliminacion(getTableView().getItems().get(getIndex())));
                btnEstado.setOnAction(e -> confirmarCambioEstado(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Alumno a = (Alumno) getTableRow().getItem();
                    
                    btnEstado.getStyleClass().removeAll("success", "warning");

                    if (a.isActivo()) {
                        btnEstado.setText("Desactivar");
                        btnEstado.getStyleClass().add("warning");
                    } else {
                        btnEstado.setText("Activar");
                        btnEstado.getStyleClass().add("success");
                    }
                    
                    setGraphic(panel);
                }
            }
        });
    }

    private void cargarDatos() {
        try {
            List<Alumno> alumnos = alumnoService.listarTodos();
            listaAlumnos.setAll(alumnos);
            handleBusqueda();
        } catch (Exception e) {
            mostrarNotificacion(e.getMessage(), true);
        }
    }

    private void configurarPaginacion() {
        int totalFilas = alumnosFiltrados.size();
        int totalPaginas = (int) Math.ceil((double) totalFilas / FILAS_POR_PAGINA);

        paginacionAlumnos.setPageCount(totalPaginas > 0 ? totalPaginas : 1);
        
        paginacionAlumnos.setPageFactory(pageIndex -> {
            int desde = pageIndex * FILAS_POR_PAGINA;
            int hasta = Math.min(desde + FILAS_POR_PAGINA, totalFilas);
            
            if (desde < totalFilas) {
                tablaAlumnos.setItems(FXCollections.observableArrayList(alumnosFiltrados.subList(desde, hasta)));
            } else {
                tablaAlumnos.setItems(FXCollections.observableArrayList());
            }
            return new Region(); 
        });
    }

    @FXML
    private void handleGuardar() {
        // 1. Recolectar datos de la UI
        Alumno temporal = (alumnoEnEdicion != null) ? alumnoEnEdicion : new Alumno();
        temporal.setMatricula(campoMatricula.getText().trim());
        temporal.setNombre(campoNombre.getText().trim());
        temporal.setEmail(campoEmail.getText().trim());

        try {
            // 2. Llamar al servicio
            alumnoService.guardar(temporal, alumnoEnEdicion != null);
            
            // 3. Notificar éxito
            mostrarNotificacion("Operación realizada con éxito", false);
            handleCancelar();
            cargarDatos();
        } catch (Exception e) {
            // 4. Notificar error traducido
            mostrarNotificacion(e.getMessage(), true);
        }
    }

    private void abrirEdicion(Alumno a) {
        alumnoEnEdicion = a;
        
        campoMatricula.setText(a.getMatricula());
        campoNombre.setText(a.getNombre());
        campoEmail.setText(a.getEmail());
        
        labelTituloFormulario.setText("Editar Alumno");
        labelNotaPassword.setText("Nota: Si el usuario olvidó su acceso, puedes restablecer su contraseña.");
        btnRestablecerPassword.setVisible(true);
        btnRestablecerPassword.setManaged(true);

        panelFormulario.setVisible(true);
        panelFormulario.setManaged(true);
    }

    private void confirmarEliminacion(Alumno a) {
        mostrarConfirmacion(
            "Advertencia Crítica",
            "Vas a eliminar permanentemente a " + a.getNombre() + ".\nEsta acción es irreversible. ¿Deseas continuar?",
            "Eliminar definitivamente",
            "danger", // Botón rojo
            () -> {
                try {
                    alumnoService.eliminar(a.getId());
                    cargarDatos();
                    mostrarNotificacion("Alumno eliminado permanentemente.", false);
                } catch (Exception e) {
                    mostrarNotificacion(e.getMessage(), true);
                }
            }
        );
    }

    private void confirmarCambioEstado(Alumno a) {
        boolean nuevoEstado = !a.isActivo();
        String accionText = nuevoEstado ? "Activar" : "Desactivar";
        
        mostrarConfirmacion(
            "Confirmar " + accionText,
            "¿Deseas " + accionText.toLowerCase() + " el acceso de " + a.getNombre() + "?",
            accionText,
            nuevoEstado ? "accent" : "danger", // Azul si activa, Rojo si desactiva
            () -> {
                try {
                    alumnoService.cambiarEstado(a.getId(), nuevoEstado); 
                    cargarDatos(); 
                    mostrarNotificacion("Cuenta " + (nuevoEstado ? "activada" : "desactivada") + " correctamente.", false);
                } catch (Exception e) {
                    mostrarNotificacion(e.getMessage(), true);
                }
            }
        );
    }

    @FXML 
    private void handleNuevo() { 
        alumnoEnEdicion = null; 
        limpiarFormulario();
        
        labelTituloFormulario.setText("Nuevo Alumno");
        labelNotaPassword.setText("Nota: Los alumnos nuevos se crean con la contraseña predeterminada '123456'.");
        btnRestablecerPassword.setVisible(false);
        btnRestablecerPassword.setManaged(false);

        panelFormulario.setVisible(true); 
        panelFormulario.setManaged(true); 
    }

    @FXML private void handleCancelar() { 
        panelFormulario.setVisible(false);
        panelFormulario.setManaged(false);
        limpiarFormulario();
    }

    private void limpiarFormulario() {
        campoMatricula.clear();
        campoNombre.clear();
        campoEmail.clear();
    }

    private void mostrarNotificacion(String mensaje, boolean esError) {
        mensajeGeneral.setText(mensaje);
        mensajeGeneral.setOpacity(1.0);
        mensajeGeneral.setVisible(true);
        mensajeGeneral.setManaged(true); 

        if (esError) {
            mensajeGeneral.setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-padding: 12 25; -fx-background-radius: 30; -fx-font-weight: bold;");
        } else {
            mensajeGeneral.setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-padding: 12 25; -fx-background-radius: 30; -fx-font-weight: bold;");
        }

        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.seconds(1), mensajeGeneral);
        fade.setDelay(javafx.util.Duration.seconds(2));
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(e -> {
            mensajeGeneral.setVisible(false);
        });
        fade.play();
    }

    @FXML 
    private void handleBusqueda() {
        String textoFiltro = campoBusqueda.getText().toLowerCase().trim();

        alumnosFiltrados.setPredicate(alumno -> {
            if (textoFiltro.isEmpty()) return true;

            return alumno.getNombre().toLowerCase().contains(textoFiltro) ||
                   alumno.getMatricula().toLowerCase().contains(textoFiltro);
        });

        configurarPaginacion();
    }


    @FXML
    private void handleImportarCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo de Alumnos (CSV)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos CSV", "*.csv"));

        File archivo = fileChooser.showOpenDialog(tablaAlumnos.getScene().getWindow());

        if (archivo != null) {
            try (FileInputStream fis = new FileInputStream(archivo)) {
                List<String> errores = cargaDatosService.importarAlumnosCsv(fis);

                if (errores.isEmpty()) {
                    mostrarNotificacion("¡Todos los alumnos importados con éxito!", false);
                } else {
                    mostrarNotificacion("Importación completada con " + errores.size() + " errores.", true);
                    mostrarDetallesErrores(errores, tablaAlumnos.getScene().getWindow()); // Usamos el nuevo método
                }
                
                cargarDatos(); 

            } catch (Exception e) {
                mostrarNotificacion("Error crítico al procesar el archivo.", true);
            }
        }
    }

    @FXML
    private void handleRestablecerPassword() {
        if (alumnoEnEdicion == null) return;

        mostrarConfirmacion(
            "Restablecer Contraseña",
            "¿Deseas restablecer la contraseña de " + alumnoEnEdicion.getNombre() + "?\nSu contraseña volverá a ser '123456' temporalmente.",
            "Restablecer",
            "danger", // Botón rojo
            () -> {
                try {
                    alumnoService.restablecerPassword(alumnoEnEdicion.getId());
                    mostrarNotificacion("Contraseña restablecida a '123456'.", false);
                    
                    handleCancelar();
                } catch (Exception e) {
                    mostrarNotificacion(e.getMessage(), true);
                }
            }
        );
    }

    // === LÓGICA DEL PANEL DE CONFIRMACIÓN ===

    private void mostrarConfirmacion(String titulo, String mensaje, String textoBoton, String claseCSSBoton, Runnable accion) {
        lblTituloConfirmacion.setText(titulo);
        lblMensajeConfirmacion.setText(mensaje);
        btnConfirmarAccion.setText(textoBoton);

        // Limpiamos estilos anteriores y aplicamos el nuevo (accent o danger)
        btnConfirmarAccion.getStyleClass().removeAll("accent", "danger");
        btnConfirmarAccion.getStyleClass().add(claseCSSBoton);

        // Guardamos la acción a ejecutar
        this.accionPendiente = accion;

        panelConfirmacion.setVisible(true);
        panelConfirmacion.setManaged(true);
    }

    @FXML
    private void handleCancelarConfirmacion() {
        panelConfirmacion.setVisible(false);
        panelConfirmacion.setManaged(false);
        accionPendiente = null;
    }

    @FXML
    private void handleEjecutarConfirmacion() {
        if (accionPendiente != null) {
            accionPendiente.run(); 
        }
        handleCancelarConfirmacion();
    }

    private void mostrarDetallesErrores(List<String> errores, javafx.stage.Window ventanaPadre) {
        Alert alert = new Alert(Alert.AlertType.WARNING);

        alert.initOwner(ventanaPadre); 
        
        alert.setTitle("Detalle de la Importación");
        alert.setHeaderText("Algunas filas no pudieron procesarse:");

        TextArea textArea = new TextArea(String.join("\n", errores));
        textArea.setEditable(false);
        textArea.setWrapText(false);

        textArea.setPrefWidth(550);
        textArea.setPrefHeight(250);

        alert.getDialogPane().setContent(textArea);
        
        alert.showAndWait();
    }
}