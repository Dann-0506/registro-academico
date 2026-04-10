package com.academico.controller;

import com.academico.model.Usuario;
import com.academico.service.individuals.AdminService;
import com.academico.util.SessionManagerUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.List;

public class AdminsController {

    @FXML private TableView<Usuario> tablaAdmins;
    @FXML private TableColumn<Usuario, String> colNombre;
    @FXML private TableColumn<Usuario, String> colEmail;
    @FXML private TableColumn<Usuario, Void> colAcciones;

    @FXML private Pagination paginacionAdmins;

    @FXML private TextField campoBusqueda;
    @FXML private TextField campoNombre;
    @FXML private TextField campoEmail;

    @FXML private Label labelTituloFormulario;
    @FXML private Label mensajeGeneral;
    @FXML private Label labelNotaPassword;
    @FXML private Label lblTituloConfirmacion;
    @FXML private Label lblMensajeConfirmacion;

    @FXML private Button btnConfirmarAccion;
    @FXML private Button btnRestablecerPassword;

    @FXML private StackPane panelConfirmacion;
    @FXML private StackPane panelFormulario;

    private Runnable accionPendiente;

    private final AdminService adminService = new AdminService();
    private final ObservableList<Usuario> listaAdmins = FXCollections.observableArrayList();
    private FilteredList<Usuario> adminsFiltrados;
    private Usuario adminEnEdicion = null;
    private final int FILAS_POR_PAGINA = 15;

    @FXML
    public void initialize() {
        tablaAdmins.setFixedCellSize(48);
        configurarColumnas();
        cargarDatos();
    }

    private void configurarColumnas() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnEstado = new Button(); 
            private final Button btnEliminar = new Button("Eliminar");
            private final HBox panel = new HBox(8, btnEditar, btnEstado); 
            {
                btnEditar.getStyleClass().addAll("accent", "flat");
                btnEstado.getStyleClass().addAll("flat");
                btnEliminar.getStyleClass().addAll("danger", "flat");
                panel.setStyle("-fx-alignment: center;");

                btnEditar.setOnAction(e -> abrirEdicion(getTableView().getItems().get(getIndex())));
                btnEstado.setOnAction(e -> confirmarCambioEstado(getTableView().getItems().get(getIndex())));
                btnEliminar.setOnAction(e -> confirmarEliminacion(getTableView().getItems().get(getIndex())));
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableView().getItems().get(getIndex()) == null) {
                    setGraphic(null);
                } else {
                    Usuario u = getTableView().getItems().get(getIndex());
                    Usuario usuarioLogueado = SessionManagerUtil.getUsuarioActual();

                    btnEstado.getStyleClass().removeAll("success", "warning");
                    
                    if (u.isActivo()) {
                        btnEstado.setText("Desactivar");
                        btnEstado.getStyleClass().add("warning");
                    } else {
                        btnEstado.setText("Activar");
                        btnEstado.getStyleClass().add("success");
                    }

                    panel.getChildren().clear();
                    if (usuarioLogueado != null && u.getId() == usuarioLogueado.getId()) {
                        Label lblTu = new Label("Eres tú. Edita desde 'Mi Perfil'.");
                        lblTu.setStyle("-fx-text-fill: #6c757d; -fx-font-style: italic; -fx-font-size: 11px;");
                        panel.getChildren().add(lblTu);
                    } else {
                        panel.getChildren().addAll(btnEditar, btnEstado, btnEliminar);
                    }
                    setGraphic(panel);
                }
            }
        });
    }

    private void cargarDatos() {
        try {
            List<Usuario> admins = adminService.listarAdmins();
            listaAdmins.setAll(admins);
            adminsFiltrados = new FilteredList<>(listaAdmins, p -> true);
            handleBusqueda();
        } catch (Exception e) {
            mostrarNotificacion(e.getMessage(), true);
        }
    }

    @FXML
    private void handleBusqueda() {
        String filtro = campoBusqueda.getText().toLowerCase().trim();
        adminsFiltrados.setPredicate(u -> {
            if (filtro.isEmpty()) return true;
            return u.getNombre().toLowerCase().contains(filtro) || 
                   u.getEmail().toLowerCase().contains(filtro);
        });
        configurarPaginacion();
    }

    private void configurarPaginacion() {
        int total = adminsFiltrados.size();
        int paginas = (int) Math.ceil((double) total / FILAS_POR_PAGINA);
        paginacionAdmins.setPageCount(paginas > 0 ? paginas : 1);
        paginacionAdmins.setPageFactory(idx -> {
            int desde = idx * FILAS_POR_PAGINA;
            int hasta = Math.min(desde + FILAS_POR_PAGINA, total);
            tablaAdmins.setItems(FXCollections.observableArrayList(adminsFiltrados.subList(desde, hasta)));
            return new Region(); // Retornamos un nodo vacío porque la tabla ya se actualizó
        });
    }

    @FXML
    private void handleNuevo() { 
        adminEnEdicion = null; 
        limpiarFormulario(); 
        labelTituloFormulario.setText("Nuevo Administrador");

        labelNotaPassword.setText("Nota: Los administradores nuevos se crean con la contraseña predeterminada '123456'.");
        btnRestablecerPassword.setVisible(false);
        btnRestablecerPassword.setManaged(false);

        panelFormulario.setVisible(true); 
        panelFormulario.setManaged(true); 
    }

    private void abrirEdicion(Usuario u) {
        adminEnEdicion = u;
        campoNombre.setText(u.getNombre());
        campoEmail.setText(u.getEmail());
        labelTituloFormulario.setText("Editar Administrador");

        labelNotaPassword.setText("Nota: Si el usuario olvidó su acceso, puedes restablecer su contraseña.");
        btnRestablecerPassword.setVisible(true);
        btnRestablecerPassword.setManaged(true);

        panelFormulario.setVisible(true);
        panelFormulario.setManaged(true);
    }

    @FXML
    private void handleGuardar() {
        Usuario u = (adminEnEdicion != null) ? adminEnEdicion : new Usuario();
        u.setNombre(campoNombre.getText().trim());
        u.setEmail(campoEmail.getText().trim());

        try {
            adminService.guardar(u, adminEnEdicion != null);
            mostrarNotificacion("Administrador guardado con éxito", false);
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
    }

    @FXML
    private void handleRestablecerPassword() {
        if (adminEnEdicion == null) return;

        mostrarConfirmacion(
            "Restablecer Contraseña",
            "¿Deseas restablecer la contraseña de " + adminEnEdicion.getNombre() + "?\nSu contraseña volverá a ser '123456' temporalmente.",
            "Restablecer",
            "danger", // Botón rojo
            () -> {
                try {
                    adminService.restablecerPassword(adminEnEdicion.getId());
                    mostrarNotificacion("Contraseña restablecida a '123456'.", false);
                    
                    handleCancelar();
                } catch (Exception e) {
                    mostrarNotificacion(e.getMessage(), true);
                }
            }
        );
    }

    private void confirmarCambioEstado(Usuario u) {
        boolean nuevoEstado = !u.isActivo();
        String accionText = nuevoEstado ? "Activar" : "Desactivar";
        
        mostrarConfirmacion(
            "Confirmar " + accionText,
            "¿Deseas " + accionText.toLowerCase() + " el acceso de " + u.getNombre() + "?",
            accionText,
            nuevoEstado ? "accent" : "danger", // Azul si activa, Rojo si desactiva
            () -> {
                try {
                    adminService.cambiarEstado(u.getId(), nuevoEstado);
                    cargarDatos(); 
                    mostrarNotificacion("Cuenta " + (nuevoEstado ? "activada" : "desactivada") + ".", false);
                } catch (Exception e) {
                    mostrarNotificacion(e.getMessage(), true);
                }
            }
        );
    }

    private void confirmarEliminacion(Usuario u) {
        mostrarConfirmacion(
            "Advertencia Crítica",
            "Vas a eliminar permanentemente a " + u.getNombre() + ".\nEsta acción borrará su acceso al sistema de forma irreversible. ¿Deseas continuar?",
            "Eliminar definitivamente",
            "danger", // Botón rojo
            () -> {
                try {
                    adminService.eliminar(u.getId());
                    cargarDatos();
                    mostrarNotificacion("Administrador eliminado.", false);
                } catch (Exception e) {
                    mostrarNotificacion(e.getMessage(), true);
                }
            }
        );
    }

    private void limpiarFormulario() { 
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

    // === LÓGICA DEL PANEL DE CONFIRMACIÓN ===

    private void mostrarConfirmacion(String titulo, String mensaje, String textoBoton, String claseCSSBoton, Runnable accion) {
        lblTituloConfirmacion.setText(titulo);
        lblMensajeConfirmacion.setText(mensaje);
        btnConfirmarAccion.setText(textoBoton);

        // Limpiamos estilos anteriores y aplicamos el nuevo (accent o danger)
        btnConfirmarAccion.getStyleClass().removeAll("accent", "danger");
        btnConfirmarAccion.getStyleClass().add(claseCSSBoton);

        // Guardamos la acción que se ejecutará si hace clic en confirmar
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
            accionPendiente.run(); // Ejecuta el código guardado
        }
        handleCancelarConfirmacion(); // Cierra el modal
    }
}