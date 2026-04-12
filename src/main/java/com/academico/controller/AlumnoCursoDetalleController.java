package com.academico.controller;

import com.academico.model.*;
import com.academico.service.CalificacionService;
import com.academico.service.ReporteService;
import com.academico.service.individuals.ActividadGrupoService;
import com.academico.service.individuals.BonusService;
import com.academico.service.individuals.ResultadoService;
import com.academico.service.individuals.UnidadService;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

public class AlumnoCursoDetalleController {

    @FXML private Label lblTituloMateria;
    @FXML private Label lblEstadoCurso;
    
    // Tabla
    @FXML private TableView<FilaUnidadDTO> tablaUnidades;
    @FXML private TableColumn<FilaUnidadDTO, String> colUnidad;
    @FXML private TableColumn<FilaUnidadDTO, String> colCalificacion;
    @FXML private TableColumn<FilaUnidadDTO, String> colEstado;
    @FXML private TableColumn<FilaUnidadDTO, FilaUnidadDTO> colAcciones;

    // Resumen Final
    @FXML private Label lblPromedioBase;
    @FXML private Label lblBonus;
    @FXML private Label lblCalificacionFinal;
    @FXML private Label lblEstadoFinal;
    
    // Override
    @FXML private VBox panelOverride;
    @FXML private Label lblJustificacionOverride;

    // === SERVICIOS ===
    private final ReporteService reporteService = new ReporteService();
    private final UnidadService unidadService = new UnidadService();
    private final CalificacionService calificacionService = new CalificacionService();
    private final ActividadGrupoService actividadService = new ActividadGrupoService();
    private final ResultadoService resultadoService = new ResultadoService();
    private final BonusService bonusService = new BonusService();

    private Grupo cursoActual;
    private Alumno alumnoActual;
    private int inscripcionIdActual = -1;

    @FXML
    public void initialize() {
        if (DashboardAlumnoController.instancia != null) {
            this.cursoActual = DashboardAlumnoController.instancia.getCursoSeleccionado();
            this.alumnoActual = DashboardAlumnoController.instancia.getPerfilAlumno();
            
            configurarCabecera();
            configurarColumnas();
            cargarDatosGlobales();
        }
    }

    private void configurarCabecera() {
        lblTituloMateria.setText(cursoActual.getClave() + " - " + cursoActual.getMateriaNombre());
        if (cursoActual.isCerrado()) {
            lblEstadoCurso.setText("CERRADO (ACTA FIRMADA)");
            lblEstadoCurso.setStyle("-fx-text-fill: #cf222e; -fx-background-color: #ffebe9;");
        } else {
            lblEstadoCurso.setText("EN EVALUACIÓN");
            lblEstadoCurso.setStyle("-fx-text-fill: #0969da; -fx-background-color: #ddf4ff;");
        }
    }

    private void configurarColumnas() {
        colUnidad.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().nombreUnidad()));
        colUnidad.setStyle("-fx-alignment: CENTER-LEFT; -fx-font-weight: bold;");

        colCalificacion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().calificacion()));
        colCalificacion.setStyle("-fx-alignment: CENTER; -fx-font-size: 14px;");

        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().estado()));
        colEstado.setCellFactory(param -> new TableCell<>() {
            private final Label badge = new Label();
            { badge.setAlignment(Pos.CENTER); }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setGraphic(null);
                else {
                    badge.setText(item);
                    aplicarEstiloEstado(badge, item);
                    setGraphic(badge); setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        colAcciones.setCellValueFactory(p -> new SimpleObjectProperty<>(p.getValue()));
        colAcciones.setCellFactory(p -> new TableCell<>() {
            private final Button btn = new Button("Ver Detalles");
            {
                btn.getStyleClass().add("flat");
                btn.setStyle("-fx-text-fill: #0969da; -fx-cursor: hand;");
                btn.setOnAction(e -> mostrarDesgloseUnidad(getItem()));
            }
            @Override
            protected void updateItem(FilaUnidadDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setGraphic(null);
                else {
                    setGraphic(btn); setStyle("-fx-alignment: CENTER;");
                }
            }
        });
    }

    private void cargarDatosGlobales() {
        try {
            List<Unidad> unidades = unidadService.listarPorMateria(cursoActual.getMateriaId());
            List<CalificacionFinal> reporteGlobal = reporteService.generarReporteFinalGrupo(cursoActual.getId());
            
            CalificacionFinal misDatos = reporteGlobal.stream()
                .filter(cf -> cf.getAlumnoMatricula().equals(alumnoActual.getMatricula()))
                .findFirst()
                .orElse(null);

            if (misDatos != null) {
                this.inscripcionIdActual = misDatos.getInscripcionId();
            }

            ObservableList<FilaUnidadDTO> datosTabla = FXCollections.observableArrayList();
            for (Unidad unidad : unidades) {
                String calificacionStr = "-";
                String estadoStr = "PENDIENTE";

                if (misDatos != null) {
                    Optional<ResultadoUnidad> resultado = misDatos.getUnidades().stream()
                        .filter(u -> u.getUnidadId() == unidad.getId())
                        .findFirst();

                    if (resultado.isPresent() && resultado.get().getResultadoFinal() != null) {
                        calificacionStr = resultado.get().getResultadoFinal().toString();
                        estadoStr = calificacionService.determinarEstado(resultado.get().getResultadoFinal());
                    }
                }

                String tituloUnidad = "U" + unidad.getNumero() + " - " + unidad.getNombre();
                datosTabla.add(new FilaUnidadDTO(unidad.getId(), tituloUnidad, calificacionStr, estadoStr));
            }
            tablaUnidades.setItems(datosTabla);

            // Resumen inferior
            if (misDatos != null) {
                lblPromedioBase.setText(misDatos.getCalificacionCalculada() != null ? misDatos.getCalificacionCalculada().toString() : "-");

                if (misDatos.getBonusMateria() != null && misDatos.getBonusMateria().doubleValue() > 0) {
                    lblBonus.setText("+" + misDatos.getBonusMateria().toString() + " pts");
                    lblBonus.setStyle("-fx-text-fill: #2da44e;"); 
                } else {
                    lblBonus.setText("0.00 pts");
                    lblBonus.setStyle("-fx-text-fill: #57606a;"); 
                }

                String finalStr = misDatos.getCalificacionFinal() != null ? misDatos.getCalificacionFinal().toString() : "-";
                lblCalificacionFinal.setText(finalStr);

                String estadoGlobal = "PENDIENTE";
                try {
                    if (misDatos.getCalificacionFinal() != null) {
                        estadoGlobal = calificacionService.determinarEstado(misDatos.getCalificacionFinal());
                    }
                } catch (Exception ignored) {}
                
                lblEstadoFinal.setText(estadoGlobal);
                aplicarEstiloEstado(lblEstadoFinal, estadoGlobal);

                if (misDatos.isEsOverride()) {
                    lblCalificacionFinal.setText(finalStr + " (M)");
                    lblJustificacionOverride.setText("Motivo registrado: " + misDatos.getOverrideJustificacion());
                    panelOverride.setVisible(true);
                    panelOverride.setManaged(true);
                }
            }

        } catch (Exception e) {
            System.err.println("Error al cargar la boleta unificada: " + e.getMessage());
        }
    }

    /**
     * Refactorizado: Obtiene el desglose delegando todo a la capa de Servicios 
     * y cruzando los datos en memoria mediante Streams.
     */
    private void mostrarDesgloseUnidad(FilaUnidadDTO fila) {
        if (inscripcionIdActual == -1) return;

        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Desglose de Calificación");
        info.setHeaderText("Detalle de Evaluación\n" + fila.nombreUnidad());
        
        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 10;");

        try {
            // 1. Mostrar Actividades (Cruzando Estructura vs Resultados)
            List<ActividadGrupo> actividades = actividadService.buscarPorGrupoYUnidad(cursoActual.getId(), fila.unidadId());
            List<Resultado> resultados = resultadoService.buscarPorInscripcionYUnidad(inscripcionIdActual, fila.unidadId());

            if (actividades.isEmpty()) {
                content.getChildren().add(new Label("No hay actividades registradas en esta unidad."));
            } else {
                for (ActividadGrupo ag : actividades) {
                    // Buscar si el alumno tiene calificación en esta actividad
                    Optional<Resultado> resOpt = resultados.stream()
                            .filter(r -> r.getActividadGrupoId() == ag.getId())
                            .findFirst();

                    String calif = "Sin calificar";
                    if (resOpt.isPresent() && resOpt.get().getCalificacion() != null) {
                        calif = resOpt.get().getCalificacion().toString();
                    }

                    Label lblAct = new Label("• " + ag.getNombre() + " (" + ag.getPonderacion() + "%): " + calif);
                    lblAct.setStyle("-fx-font-size: 14px; -fx-text-fill: #24292f;");
                    content.getChildren().add(lblAct);
                }
            }

            // 2. Mostrar Bonus de Unidad (Delegado al servicio)
            Optional<Bonus> bonusOpt = bonusService.obtenerBonusUnidad(inscripcionIdActual, fila.unidadId());
            
            if (bonusOpt.isPresent()) {
                Bonus bonus = bonusOpt.get();
                content.getChildren().add(new Separator());
                
                Label lblBonus = new Label("Bonus de Unidad Aplicado: +" + bonus.getPuntos() + " pts");
                lblBonus.setStyle("-fx-font-weight: bold; -fx-text-fill: #2da44e;");
                
                String just = bonus.getJustificacion();
                Label lblJust = new Label("Motivo: " + (just != null && !just.isBlank() ? just : "Sin especificar"));
                lblJust.setStyle("-fx-font-style: italic; -fx-text-fill: #57606a;");
                
                content.getChildren().addAll(lblBonus, lblJust);
            }

        } catch (Exception e) {
            content.getChildren().add(new Label("Error al cargar el desglose: " + e.getMessage()));
        }

        info.getDialogPane().setContent(content);
        info.showAndWait();
    }

    private void aplicarEstiloEstado(Label badge, String estado) {
        if ("APROBADO".equals(estado)) {
            badge.setStyle("-fx-background-color: #dafbe1; -fx-text-fill: #1a7f37; -fx-font-weight: bold; -fx-padding: 4 12; -fx-background-radius: 12;");
        } else if ("REPROBADO".equals(estado)) {
            badge.setStyle("-fx-background-color: #ffebe9; -fx-text-fill: #cf222e; -fx-font-weight: bold; -fx-padding: 4 12; -fx-background-radius: 12;");
        } else {
            badge.setStyle("-fx-background-color: #e1e4e8; -fx-text-fill: #57606a; -fx-font-weight: bold; -fx-padding: 4 12; -fx-background-radius: 12;");
        }
    }

    public record FilaUnidadDTO(int unidadId, String nombreUnidad, String calificacion, String estado) {}
}