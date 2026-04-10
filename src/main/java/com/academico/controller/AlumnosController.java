package com.academico.controller;

import com.academico.model.Alumno;
import com.academico.service.individuals.AlumnoService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.util.Callback;

import java.util.List;
import java.util.Optional;

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

    @FXML private VBox panelFormulario;
    @FXML private Label labelTituloFormulario;
    @FXML private TextField campoMatricula;
    @FXML private TextField campoNombre;
    @FXML private TextField campoEmail;
    @FXML private Label mensajeGeneral;
    @FXML private Button botonGuardar;

    private AlumnoService alumnoService = new AlumnoService();
    private ObservableList<Alumno> listaAlumnos = FXCollections.observableArrayList();
    private Alumno alumnoEnEdicion = null;
    private final int FILAS_POR_PAGINA = 10;

    @FXML
    public void initialize() {

        tablaAlumnos.getColumns().forEach(column -> column.setReorderable(false));
    
        tablaAlumnos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        configurarColumnas();
        cargarDatos();
    }

    private void configurarColumnas() {
        colMatricula.setCellValueFactory(new PropertyValueFactory<>("matricula"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        Callback<TableColumn<Alumno, Void>, TableCell<Alumno, Void>> cellFactory = param -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnEliminar = new Button("Eliminar");
            private final HBox panel = new HBox(8, btnEditar, btnEliminar);

            {
                btnEditar.getStyleClass().addAll("accent", "flat");
                btnEliminar.getStyleClass().addAll("danger", "flat");
                panel.setStyle("-fx-alignment: center;");

                btnEditar.setOnAction(e -> abrirEdicion(getTableView().getItems().get(getIndex())));
                btnEliminar.setOnAction(e -> confirmarEliminacion(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : panel);
            }
        };
        colAcciones.setCellFactory(cellFactory);
    }

    private void cargarDatos() {
        try {
            List<Alumno> alumnos = alumnoService.listarTodos();
            listaAlumnos.setAll(alumnos);
            configurarPaginacion();
        } catch (Exception e) {
            mostrarNotificacion(e.getMessage(), true);
        }
    }

    private void configurarPaginacion() {
        int totalPaginas = (int) Math.ceil((double) listaAlumnos.size() / FILAS_POR_PAGINA);
        paginacionAlumnos.setPageCount(totalPaginas > 0 ? totalPaginas : 1);
        
        paginacionAlumnos.setPageFactory(pageIndex -> {
            int desde = pageIndex * FILAS_POR_PAGINA;
            int hasta = Math.min(desde + FILAS_POR_PAGINA, listaAlumnos.size());
            
            if (desde < listaAlumnos.size()) {
                tablaAlumnos.setItems(FXCollections.observableArrayList(listaAlumnos.subList(desde, hasta)));
            } else {
                tablaAlumnos.setItems(FXCollections.observableArrayList());
            }
            // Devolvemos un Region vacío para que no interfiera con la tabla del FXML
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
        panelFormulario.setVisible(true);
        panelFormulario.setManaged(true);
    }

    private void confirmarEliminacion(Alumno a) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Deseas eliminar a " + a.getNombre() + "?");
        alert.setContentText("Esta acción no se puede deshacer.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // El controlador solo pide eliminar, el servicio se encarga de la lógica
                alumnoService.eliminar(a.getId());
                
                // Si tiene éxito, refrescamos y notificamos
                cargarDatos();
                mostrarNotificacion("Alumno eliminado correctamente.", false);
            } catch (Exception e) {
                // Si el servicio detecta que tiene calificaciones (FK), 
                // nos lanzará la excepción con el mensaje: 
                // "No se puede eliminar: El alumno tiene historial académico."
                mostrarNotificacion(e.getMessage(), true);
            }
        }
    }

    @FXML private void handleNuevo() { 
        alumnoEnEdicion = null;
        limpiarFormulario();
        labelTituloFormulario.setText("Nuevo Alumno");
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
        mensajeGeneral.setOpacity(1.0); // Reset de opacidad obligatorio
        mensajeGeneral.setVisible(true);
        mensajeGeneral.setManaged(true);

        // Colores según el éxito o error (usando estilos de tu proyecto)
        if (esError) {
            mensajeGeneral.setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24;");
        } else {
            mensajeGeneral.setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724;");
        }

        // Animación: Se muestra y luego se desvanece
        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.seconds(1), mensajeGeneral);
        fade.setDelay(javafx.util.Duration.seconds(2)); // Visible por 2 segundos
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(e -> {
            mensajeGeneral.setVisible(false);
            mensajeGeneral.setManaged(false);
        });
        fade.play();
    }

    @FXML private void handleBusqueda() {}
    @FXML private void handleImportarCsv() {}
}