package com.academico.controller;

import com.academico.model.Alumno;
import com.academico.model.Grupo;
import com.academico.model.Inscripcion;
import com.academico.service.CargaDatosService;
import com.academico.service.individuals.AlumnoService;
import com.academico.service.individuals.GrupoService;
import com.academico.service.individuals.InscripcionService;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class InscripcionesController {

    // === ELEMENTOS DE LA INTERFAZ ===
    @FXML private TableView<Inscripcion> tablaInscripciones;
    @FXML private TableColumn<Inscripcion, String> colMatricula, colAlumno, colGrupoClave, colMateria, colSemestre;
    @FXML private TableColumn<Inscripcion, LocalDate> colFecha;
    @FXML private TableColumn<Inscripcion, Void> colAcciones;
    @FXML private Pagination paginacionInscripciones;
    @FXML private TextField campoBusqueda;

    // === ELEMENTOS DEL FORMULARIO MODAL ===
    @FXML private StackPane panelFormulario, panelConfirmacion;
    @FXML private ComboBox<Alumno> cbAlumno;
    @FXML private ComboBox<Grupo> cbGrupo;
    @FXML private Label lblTituloConfirmacion, lblMensajeConfirmacion, mensajeGeneral;
    @FXML private Button btnConfirmarAccion;

    // === LISTAS FILTRABLES PARA LOS COMBOBOX ===
    private FilteredList<Alumno> alumnosFiltrados;
    private FilteredList<Grupo> gruposFiltrados;

    // === SERVICIOS Y ESTADO ===
    private final InscripcionService inscripcionService = new InscripcionService();
    private final AlumnoService alumnoService = new AlumnoService();
    private final GrupoService grupoService = new GrupoService();
    private final CargaDatosService cargaDatosService = new CargaDatosService();

    private final ObservableList<Inscripcion> listaInscripciones = FXCollections.observableArrayList();
    private FilteredList<Inscripcion> inscripcionesFiltradas;
    private Runnable accionPendiente;
    private final int FILAS_POR_PAGINA = 15;

    @FXML
    public void initialize() {
        tablaInscripciones.setFixedCellSize(48);
        configurarColumnas();
        cargarCatalogos();
        configurarBusquedaCombos();
        cargarDatos();
    }

    // ==========================================
    // CONFIGURACIÓN DE TABLA Y COLUMNAS
    // ==========================================

    private void configurarColumnas() {
        colMatricula.setCellValueFactory(new PropertyValueFactory<>("alumnoMatricula"));
        colAlumno.setCellValueFactory(new PropertyValueFactory<>("alumnoNombre"));
        colGrupoClave.setCellValueFactory(new PropertyValueFactory<>("grupoClave"));
        colMateria.setCellValueFactory(new PropertyValueFactory<>("materiaNombre"));
        colSemestre.setCellValueFactory(new PropertyValueFactory<>("semestre"));

        // Formato amigable para la fecha (DD/MM/YYYY)
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colFecha.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(formatter.format(date));
                }
            }
        });

        // Columna de Acciones (Solo Dar de Baja)
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnBaja = new Button("Dar de Baja");
            private final HBox panel = new HBox(btnBaja);

            {
                btnBaja.getStyleClass().addAll("danger", "flat");
                btnBaja.setTooltip(new Tooltip("Eliminar inscripción"));
                panel.setStyle("-fx-alignment: center;");

                btnBaja.setOnAction(e -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        confirmarEliminacion((Inscripcion) getTableRow().getItem());
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(panel);
                }
            }
        });
    }

    // ==========================================
    // CARGA DE DATOS Y FILTROS
    // ==========================================

    private void cargarCatalogos() {
        try {
            // Cargamos todos los alumnos
            List<Alumno> todosAlumnos = alumnoService.listarTodos();
            alumnosFiltrados = new FilteredList<>(FXCollections.observableArrayList(todosAlumnos), p -> true);
            cbAlumno.setItems(alumnosFiltrados);

            // Cargamos SOLO LOS GRUPOS ACTIVOS
            List<Grupo> gruposActivos = grupoService.listarTodos().stream()
                    .filter(Grupo::isActivo)
                    .collect(Collectors.toList());
            gruposFiltrados = new FilteredList<>(FXCollections.observableArrayList(gruposActivos), p -> true);
            cbGrupo.setItems(gruposFiltrados);

        } catch (Exception e) {
            mostrarNotificacion("Error al cargar los catálogos para el formulario.", true);
        }
    }

    private void configurarBusquedaCombos() {
        configurarFiltroPersonalizado(cbAlumno, alumnosFiltrados);
        configurarFiltroPersonalizado(cbGrupo, gruposFiltrados);
    }

    private <T> void configurarFiltroPersonalizado(ComboBox<T> combo, FilteredList<T> listaFiltrada) {
        // 1. EL SALVAVIDAS: Le enseñamos a JavaFX a convertir el Texto (String) de vuelta al Objeto (T)
        combo.setConverter(new javafx.util.StringConverter<T>() {
            @Override
            public String toString(T object) {
                return object == null ? "" : object.toString();
            }

            @Override
            public T fromString(String string) {
                // Busca en la lista el objeto que coincida exactamente con el texto escrito
                return combo.getItems().stream()
                        .filter(item -> item.toString().equals(string))
                        .findFirst().orElse(null);
            }
        });

        // 2. Mantenemos tu excelente lógica de búsqueda en tiempo real
        combo.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> {
                // Si el nuevo valor es nulo o coincide con el item seleccionado, no hacemos nada
                if (newVal == null || (combo.getSelectionModel().getSelectedItem() != null 
                    && combo.getSelectionModel().getSelectedItem().toString().equals(newVal))) {
                    return;
                }

                listaFiltrada.setPredicate(item -> {
                    if (newVal.isEmpty()) return true;
                    return item.toString().toLowerCase().contains(newVal.toLowerCase());
                });

                // AJUSTE: Solo mostrar si el combo tiene el foco (interacción del usuario)
                if (!listaFiltrada.isEmpty() && combo.getEditor().isFocused()) {
                    combo.show();
                } else {
                    combo.hide();
                }
            });
        });
    }

    private void cargarDatos() {
        try {
            // Nota: Requiere que hayas añadido el método listarTodas() en InscripcionService
            List<Inscripcion> inscripciones = inscripcionService.listarTodas();
            listaInscripciones.setAll(inscripciones);
            inscripcionesFiltradas = new FilteredList<>(listaInscripciones, p -> true);
            handleBusqueda();
        } catch (Exception e) {
            mostrarNotificacion(e.getMessage(), true);
        }
    }

    @FXML
    private void handleBusqueda() {
        String filtro = campoBusqueda.getText().toLowerCase().trim();
        inscripcionesFiltradas.setPredicate(ins -> {
            if (filtro.isEmpty()) return true;
            return (ins.getAlumnoMatricula() != null && ins.getAlumnoMatricula().toLowerCase().contains(filtro)) ||
                   (ins.getAlumnoNombre() != null && ins.getAlumnoNombre().toLowerCase().contains(filtro)) ||
                   (ins.getGrupoClave() != null && ins.getGrupoClave().toLowerCase().contains(filtro)) ||
                   (ins.getMateriaNombre() != null && ins.getMateriaNombre().toLowerCase().contains(filtro));
        });
        configurarPaginacion();
    }

    private void configurarPaginacion() {
        int total = inscripcionesFiltradas.size();
        int paginas = (int) Math.ceil((double) total / FILAS_POR_PAGINA);
        paginacionInscripciones.setPageCount(paginas > 0 ? paginas : 1);
        paginacionInscripciones.setPageFactory(idx -> {
            int desde = idx * FILAS_POR_PAGINA;
            int hasta = Math.min(desde + FILAS_POR_PAGINA, total);
            tablaInscripciones.setItems(FXCollections.observableArrayList(inscripcionesFiltradas.subList(desde, hasta)));
            tablaInscripciones.refresh();
            return new Region();
        });
    }

    // ==========================================
    // OPERACIONES CRUD Y ARCHIVOS
    // ==========================================

    @FXML
    private void handleImportarCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo CSV de Inscripciones");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos CSV", "*.csv"));
        
        File file = fileChooser.showOpenDialog(tablaInscripciones.getScene().getWindow());
        if (file != null) {
            try (InputStream is = new FileInputStream(file)) {
                List<String> errores = cargaDatosService.importarInscripcionesCsv(is);
                
                if (errores.isEmpty()) {
                    mostrarNotificacion("Inscripciones importadas masivamente con éxito.", false);
                } else {
                    mostrarNotificacion("Importación completada con " + errores.size() + " errores.", true);
                    mostrarDetallesErrores(errores, tablaInscripciones.getScene().getWindow());
                }
                cargarDatos();
            } catch (Exception e) {
                mostrarNotificacion("Error al procesar el archivo: " + e.getMessage(), true);
            }
        }
    }

    @FXML
    private void handleNuevo() {
        limpiarFormulario();
        panelFormulario.setVisible(true);
        panelFormulario.setManaged(true);
    }

    @FXML
    private void handleGuardar() {
        try {
            Alumno alu = cbAlumno.getValue();
            Grupo gru = cbGrupo.getValue();
            
            if (alu == null || gru == null) {
                throw new IllegalArgumentException("Debe seleccionar un alumno y un grupo de la lista.");
            }

            Inscripcion nueva = new Inscripcion();
            nueva.setAlumnoId(alu.getId());
            nueva.setGrupoId(gru.getId());

            inscripcionService.inscribir(nueva);
            
            mostrarNotificacion("Alumno inscrito exitosamente en el grupo.", false);
            handleCancelar();
            cargarDatos();

        } catch (Exception e) {
            mostrarNotificacion(e.getMessage(), true);
        }
    }

    @FXML 
    private void handleCancelar() { 
        panelFormulario.setVisible(false); 
        panelFormulario.setManaged(false);
        limpiarFormulario();
        tablaInscripciones.requestFocus(); 
    }
    
    private void limpiarFormulario() {
        // 1. Cerramos los popups para evitar el error visual
        cbAlumno.hide();
        cbGrupo.hide();

        // 2. Limpiamos la selección
        cbAlumno.setValue(null);
        cbGrupo.setValue(null);
        
        // 3. Limpiamos el texto interno por si acaso
        cbAlumno.getSelectionModel().clearSelection();
        cbGrupo.getSelectionModel().clearSelection();
    }

    // ==========================================
    // NOTIFICACIONES Y CONFIRMACIONES
    // ==========================================

    private void confirmarEliminacion(Inscripcion ins) {
        mostrarConfirmacion(
            "Dar de Baja", 
            "¿Deseas dar de baja a " + ins.getAlumnoNombre() + " del grupo " + ins.getGrupoClave() + "?\nEsta acción eliminará su registro de esta materia.", 
            "Dar de Baja", 
            "danger", 
            () -> {
                try {
                    inscripcionService.eliminar(ins.getId());
                    cargarDatos();
                    mostrarNotificacion("El alumno ha sido dado de baja exitosamente.", false);
                } catch (Exception e) { 
                    mostrarNotificacion(e.getMessage(), true); 
                }
            }
        );
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

    private void mostrarNotificacion(String msj, boolean error) {
        mensajeGeneral.setText(msj);
        mensajeGeneral.setVisible(true); mensajeGeneral.setManaged(true);
        mensajeGeneral.setStyle("-fx-background-color: " + (error ? "#f8d7da" : "#d4edda") + "; -fx-text-fill: " + (error ? "#721c24" : "#155724") + "; -fx-padding: 12 25; -fx-background-radius: 30; -fx-font-weight: bold;");
        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(Duration.seconds(1), mensajeGeneral);
        fade.setDelay(Duration.seconds(2)); fade.setFromValue(1.0); fade.setToValue(0.0);
        fade.setOnFinished(e -> { mensajeGeneral.setVisible(false); mensajeGeneral.setManaged(false); });
        fade.play();
    }

    private void mostrarConfirmacion(String tit, String msj, String txtBtn, String clase, Runnable acc) {
        lblTituloConfirmacion.setText(tit); 
        lblMensajeConfirmacion.setText(msj);
        btnConfirmarAccion.setText(txtBtn); 
        btnConfirmarAccion.getStyleClass().setAll("button", clase);
        this.accionPendiente = acc; 
        panelConfirmacion.setVisible(true); 
        panelConfirmacion.setManaged(true);
    }

    @FXML private void handleCancelarConfirmacion() { panelConfirmacion.setVisible(false); panelConfirmacion.setManaged(false); }
    @FXML private void handleEjecutarConfirmacion() { if (accionPendiente != null) accionPendiente.run(); handleCancelarConfirmacion(); }
}