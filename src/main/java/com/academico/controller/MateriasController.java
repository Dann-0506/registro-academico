package com.academico.controller;

import com.academico.model.Materia;
import com.academico.model.Unidad;
import com.academico.service.CargaDatosService;
import com.academico.service.individuals.MateriaService;
import com.academico.service.individuals.UnidadService;

import javafx.animation.PauseTransition;
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
import java.util.List;

public class MateriasController {

    // === ELEMENTOS PRINCIPALES ===
    @FXML private TableView<Materia> tablaMaterias;
    @FXML private TableColumn<Materia, String> colClave;
    @FXML private TableColumn<Materia, String> colNombre;
    @FXML private TableColumn<Materia, Integer> colTotalUnidades;
    @FXML private TableColumn<Materia, Void> colAcciones;
    @FXML private Pagination paginacionMaterias;
    @FXML private TextField campoBusqueda;

    // === FORMULARIO MATERIA ===
    @FXML private StackPane panelFormulario;
    @FXML private Label labelTituloFormulario;
    @FXML private TextField campoClave;
    @FXML private TextField campoNombreMateria;
    @FXML private TextField campoTotalUnidades;

    // === PANEL UNIDADES ===
    @FXML private StackPane panelUnidades;
    @FXML private Label lblTituloUnidades;
    @FXML private TableView<Unidad> tablaUnidades;
    @FXML private TableColumn<Unidad, Integer> colUnidadNumero;
    @FXML private TableColumn<Unidad, String> colUnidadNombre;
    @FXML private TextField campoNombreUnidad;
    @FXML private Button btnGuardarUnidad;

    // === GLOBALES ===
    @FXML private StackPane panelConfirmacion;
    @FXML private Label lblTituloConfirmacion;
    @FXML private Label lblMensajeConfirmacion;
    @FXML private Button btnConfirmarAccion;
    @FXML private Label mensajeGeneral;

    private final MateriaService materiaService = new MateriaService();
    private final UnidadService unidadService = new UnidadService();
    private final CargaDatosService cargaDatosService = new CargaDatosService();
    
    private final ObservableList<Materia> listaMaterias = FXCollections.observableArrayList();
    private FilteredList<Materia> materiasFiltradas;
    private Materia materiaEnEdicion = null;
    private Unidad unidadSeleccionada = null;
    private Materia materiaParaUnidades = null;
    private Runnable accionPendiente;
    private final int FILAS_POR_PAGINA = 15;

    @FXML
    public void initialize() {
        tablaMaterias.setFixedCellSize(48);
        tablaUnidades.setFixedCellSize(40);
        
        configurarColumnasMaterias();
        configurarColumnasUnidades();
        cargarDatos();
    }

    // ==========================================
    // LOGICA DE MATERIAS
    // ==========================================

    private void configurarColumnasMaterias() {
        colClave.setCellValueFactory(new PropertyValueFactory<>("clave"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTotalUnidades.setCellValueFactory(new PropertyValueFactory<>("totalUnidades"));

        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnUnidades = new Button("Temario"); 
            private final Button btnEliminar = new Button("Eliminar"); // NUEVO
            
            private final HBox panel = new HBox(8, btnEditar, btnUnidades, btnEliminar); 
            
            {
                btnEditar.getStyleClass().addAll("accent", "flat");
                btnUnidades.getStyleClass().addAll("success", "flat");
                btnEliminar.getStyleClass().addAll("danger", "flat");
                panel.setStyle("-fx-alignment: center;");

                btnEditar.setOnAction(e -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        abrirEdicion((Materia) getTableRow().getItem());
                    }
                });
                btnUnidades.setOnAction(e -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        abrirPanelUnidades((Materia) getTableRow().getItem());
                    }
                });
                btnEliminar.setOnAction(e -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        confirmarEliminacion((Materia) getTableRow().getItem());
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

    private void cargarDatos() {
        try {
            listaMaterias.setAll(materiaService.listarTodas());
            materiasFiltradas = new FilteredList<>(listaMaterias, p -> true);
            handleBusqueda();
        } catch (Exception e) {
            mostrarNotificacion(e.getMessage(), true);
        }
    }

    @FXML
    private void handleBusqueda() {
        String filtro = campoBusqueda.getText().toLowerCase().trim();
        materiasFiltradas.setPredicate(m -> {
            if (filtro.isEmpty()) return true;
            return m.getNombre().toLowerCase().contains(filtro) || m.getClave().toLowerCase().contains(filtro);
        });
        configurarPaginacion();
    }

    private void configurarPaginacion() {
        int total = materiasFiltradas.size();
        int paginas = (int) Math.ceil((double) total / FILAS_POR_PAGINA);
        paginacionMaterias.setPageCount(paginas > 0 ? paginas : 1);
        paginacionMaterias.setPageFactory(idx -> {
            int desde = idx * FILAS_POR_PAGINA;
            int hasta = Math.min(desde + FILAS_POR_PAGINA, total);
            tablaMaterias.setItems(FXCollections.observableArrayList(materiasFiltradas.subList(desde, hasta)));
            tablaMaterias.refresh();
            return new Region();
        });
    }

    @FXML 
    private void handleNuevo() { 
        materiaEnEdicion = null; 
        campoClave.clear(); 
        campoNombreMateria.clear(); 
        campoTotalUnidades.clear();
        campoTotalUnidades.setDisable(false); // Editable si es nueva
        
        labelTituloFormulario.setText("Nueva Materia");
        panelFormulario.setVisible(true); 
        panelFormulario.setManaged(true); 
    }

    private void abrirEdicion(Materia m) {
        materiaEnEdicion = m;
        campoClave.setText(m.getClave());
        campoNombreMateria.setText(m.getNombre());
        campoTotalUnidades.setText(String.valueOf(m.getTotalUnidades()));
        
        campoTotalUnidades.setDisable(true); // Bloqueamos cambiar la cantidad de unidades por seguridad
        
        labelTituloFormulario.setText("Editar Materia");
        panelFormulario.setVisible(true);
        panelFormulario.setManaged(true);
    }

    @FXML
    private void handleGuardar() {
        Materia m = (materiaEnEdicion != null) ? materiaEnEdicion : new Materia();
        m.setClave(campoClave.getText().trim());
        m.setNombre(campoNombreMateria.getText().trim());
        
        try {
            m.setTotalUnidades(Integer.parseInt(campoTotalUnidades.getText().trim()));
            materiaService.guardar(m, materiaEnEdicion != null);
            mostrarNotificacion("Materia guardada con éxito.", false);
            cargarDatos();
            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            pause.setOnFinished(e -> handleCancelar());
            pause.play();
        } catch (NumberFormatException e) {
            mostrarNotificacion("El total de unidades debe ser un número válido.", true);
        } catch (Exception e) {
            mostrarNotificacion(e.getMessage(), true);
        }
    }

    @FXML 
    private void handleCancelar() { 
        panelFormulario.setVisible(false); 
        panelFormulario.setManaged(false); 
    }

    // ==========================================
    // LÓGICA DE UNIDADES (HIJOS)
    // ==========================================

    private void configurarColumnasUnidades() {
        colUnidadNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colUnidadNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        // Listener para detectar qué unidad selecciona el usuario en la tabla
        tablaUnidades.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                unidadSeleccionada = newSelection;
                campoNombreUnidad.setText(newSelection.getNombre());
                campoNombreUnidad.setDisable(false);
                btnGuardarUnidad.setDisable(false);
            }
        });
    }

    private void abrirPanelUnidades(Materia m) {
        this.materiaParaUnidades = m;
        lblTituloUnidades.setText("Temario de: " + m.getNombre());
        campoNombreUnidad.clear();
        campoNombreUnidad.setDisable(true);
        btnGuardarUnidad.setDisable(true);
        unidadSeleccionada = null;

        try {
            List<Unidad> unidades = unidadService.listarPorMateria(m.getId());
            tablaUnidades.setItems(FXCollections.observableArrayList(unidades));
            panelUnidades.setVisible(true);
            panelUnidades.setManaged(true);
        } catch (Exception e) {
            mostrarNotificacion(e.getMessage(), true);
        }
    }

    @FXML
    private void handleGuardarNombreUnidad() {
        if (unidadSeleccionada == null) return;
        try {
            unidadService.actualizarNombre(unidadSeleccionada.getId(), campoNombreUnidad.getText().trim());
            // Recargamos la tabla de unidades
            abrirPanelUnidades(materiaParaUnidades); // Reutilizamos el método de carga
            mostrarNotificacion("Nombre de la unidad actualizado.", false);
        } catch (Exception e) {
            mostrarNotificacion(e.getMessage(), true);
        }
    }

    @FXML
    private void handleCerrarUnidades() {
        panelUnidades.setVisible(false);
        panelUnidades.setManaged(false);
    }

    // ==========================================
    // COMPONENTES GLOBALES (CSV, Notificaciones, Modales)
    // ==========================================

    @FXML
    private void handleImportarCsv() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File file = fc.showOpenDialog(tablaMaterias.getScene().getWindow());
        if (file != null) {
            try (FileInputStream fis = new FileInputStream(file)) {
                List<String> errores = cargaDatosService.importarMateriasCsv(fis);
                if (errores.isEmpty()) {
                    mostrarNotificacion("Materias y Unidades auto-generadas con éxito.", false);
                } else {
                    mostrarNotificacion("Completado con " + errores.size() + " errores.", true);
                    mostrarDetallesErrores(errores, tablaMaterias.getScene().getWindow());
                }
                cargarDatos();
            } catch (Exception e) {
                mostrarNotificacion("Error al procesar archivo.", true);
            }
        }
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
        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(Duration.seconds(1), mensajeGeneral);
        fade.setDelay(Duration.seconds(2));
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(e -> {
            mensajeGeneral.setVisible(false);
            mensajeGeneral.setManaged(false);
        });
        fade.play();
    }

    private void confirmarEliminacion(Materia m) {
        mostrarConfirmacion(
            "Advertencia Crítica",
            "Vas a eliminar permanentemente la materia '" + m.getNombre() + "' y todo su temario asociado.\n¿Deseas continuar?",
            "Eliminar definitivamente",
            "danger",
            () -> {
                try {
                    materiaService.eliminar(m.getId());
                    cargarDatos();
                    mostrarNotificacion("Materia y unidades eliminadas.", false);
                } catch (Exception e) {
                    mostrarNotificacion(e.getMessage(), true); // Mostrará si ya tiene grupos
                }
            }
        );
    }

    private void mostrarConfirmacion(String titulo, String mensaje, String textoBoton, String claseCSSBoton, Runnable accion) {
        lblTituloConfirmacion.setText(titulo);
        lblMensajeConfirmacion.setText(mensaje);
        btnConfirmarAccion.setText(textoBoton);
        btnConfirmarAccion.getStyleClass().removeAll("accent", "danger");
        btnConfirmarAccion.getStyleClass().add(claseCSSBoton);
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
        if (accionPendiente != null) accionPendiente.run(); 
        handleCancelarConfirmacion();
    }
}
