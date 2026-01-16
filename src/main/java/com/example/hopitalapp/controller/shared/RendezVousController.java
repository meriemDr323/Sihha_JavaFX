package com.example.hopitalapp.controller.shared;

import com.example.hopitalapp.dao.impl.RendezVousDAOImpl;
import com.example.hopitalapp.dao.interfaces.RendezVousDAO;
import com.example.hopitalapp.model.RendezVous;
import com.example.hopitalapp.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class RendezVousController implements Initializable {

    @FXML private TableView<RendezVous> rendezVousTable;
    @FXML private TableColumn<RendezVous, Integer> idColumn;
    @FXML private TableColumn<RendezVous, String> dateColumn;
    @FXML private TableColumn<RendezVous, String> patientColumn;
    @FXML private TableColumn<RendezVous, String> medecinColumn;
    @FXML private TableColumn<RendezVous, String> motifColumn;
    @FXML private TableColumn<RendezVous, String> statutColumn;
    @FXML private TableColumn<RendezVous, Void> actionsColumn;

    @FXML private TextField searchField;
    @FXML private Button addButton;

    private final RendezVousDAO rendezVousDAO = new RendezVousDAOImpl();
    private final ObservableList<RendezVous> rendezVousList = FXCollections.observableArrayList();

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadRendezVous();
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        dateColumn.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> cellData.getValue().getDateRdv() != null
                                ? cellData.getValue().getDateRdv().format(formatter)
                                : ""
                ));

        patientColumn.setCellValueFactory(new PropertyValueFactory<>("patientNomComplet"));
        medecinColumn.setCellValueFactory(new PropertyValueFactory<>("medecinNomComplet"));
        motifColumn.setCellValueFactory(new PropertyValueFactory<>("motif"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");

            {
                editButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 4 8;");
                deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 4 8;");

                editButton.setOnAction(e -> {
                    RendezVous r = getTableView().getItems().get(getIndex());
                    handleEditRendezVous(r);
                });

                deleteButton.setOnAction(e -> {
                    RendezVous r = getTableView().getItems().get(getIndex());
                    handleDeleteRendezVous(r);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(5, editButton, deleteButton);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });
    }

    private void loadRendezVous() {
        rendezVousList.clear();
        List<RendezVous> list = rendezVousDAO.findAll();
        rendezVousList.addAll(list);
        rendezVousTable.setItems(rendezVousList);
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";

        if (keyword.isEmpty()) {
            loadRendezVous();
            return;
        }

        rendezVousList.clear();
        for (RendezVous r : rendezVousDAO.findAll()) {
            if ((r.getPatientNomComplet() != null && r.getPatientNomComplet().toLowerCase().contains(keyword)) ||
                    (r.getMedecinNomComplet() != null && r.getMedecinNomComplet().toLowerCase().contains(keyword)) ||
                    (r.getMotif() != null && r.getMotif().toLowerCase().contains(keyword)) ||
                    (r.getStatut() != null && r.getStatut().toLowerCase().contains(keyword))) {
                rendezVousList.add(r);
            }
        }
        rendezVousTable.setItems(rendezVousList);
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        loadRendezVous();
        AlertUtil.showInfo("Actualisation", "Liste des rendez-vous actualisée avec succès!");
    }

    @FXML
    private void handleAddRendezVous() {
        openForm(null);
    }

    private void handleEditRendezVous(RendezVous r) {
        openForm(r);
    }

    private void openForm(RendezVous r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hopitalapp/view/shared/rendez-vous-form.fxml"));
            Parent root = loader.load();

            RendezVousFormController controller = loader.getController();
            controller.setRendezVousController(this);
            controller.setRendezVous(r);

            Stage stage = new Stage();
            stage.setTitle(r == null ? "Nouveau Rendez-vous" : "Modifier Rendez-vous");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void handleDeleteRendezVous(RendezVous r) {
        boolean confirm = AlertUtil.showConfirmation(
                "Confirmation de suppression",
                "Supprimer le rendez-vous du " +
                        (r.getDateRdv() != null ? r.getDateRdv().format(formatter) : "") +
                        " pour " + r.getPatientNomComplet() + " ?"
        );

        if (confirm) {
            rendezVousDAO.delete(r.getId());
            loadRendezVous();
            AlertUtil.showSuccess("Succès", "Rendez-vous supprimé avec succès!");
        }
    }

    public void refreshTable() {
        loadRendezVous();
    }
}
