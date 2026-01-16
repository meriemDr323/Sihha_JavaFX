package com.example.hopitalapp.controller.admin;

import com.example.hopitalapp.dao.impl.DepartementDAOImpl;
import com.example.hopitalapp.dao.interfaces.DepartementDAO;
import com.example.hopitalapp.model.Departement;
import com.example.hopitalapp.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
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
import java.util.List;
import java.util.ResourceBundle;

public class DepartementController implements Initializable {

    @FXML private TableView<Departement> departementsTable;

    @FXML private TableColumn<Departement, Integer> idColumn;
    @FXML private TableColumn<Departement, String> nomColumn;
    @FXML private TableColumn<Departement, String> descriptionColumn;
    @FXML private TableColumn<Departement, String> etageColumn;
    @FXML private TableColumn<Departement, Integer> nombreLitsColumn;
    @FXML private TableColumn<Departement, String> chefColumn;
    @FXML private TableColumn<Departement, String> telephoneColumn;
    @FXML private TableColumn<Departement, String> emailColumn;
    @FXML private TableColumn<Departement, Void>   actionsColumn;

    @FXML private TextField searchField;
    @FXML private Label totalDepartementsLabel;
    @FXML private Button addButton;

    private final DepartementDAO departementDAO = new DepartementDAOImpl();
    private final ObservableList<Departement> departementsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Scroll horizontal si le tableau dépasse
        departementsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        setupTableColumns();
        loadDepartements();
        updateStatistics();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        etageColumn.setCellValueFactory(new PropertyValueFactory<>("etage"));
        nombreLitsColumn.setCellValueFactory(new PropertyValueFactory<>("nombreLits"));
        chefColumn.setCellValueFactory(new PropertyValueFactory<>("chefNomComplet"));
        telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("chefEmail"));

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("Voir");
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");

            {
                viewButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 8; -fx-background-radius: 3; -fx-cursor: hand;");
                editButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 8; -fx-background-radius: 3; -fx-cursor: hand;");
                deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 8; -fx-background-radius: 3; -fx-cursor: hand;");

                viewButton.setOnAction(event -> {
                    Departement d = getTableView().getItems().get(getIndex());
                    handleViewDepartement(d);
                });

                editButton.setOnAction(event -> {
                    Departement d = getTableView().getItems().get(getIndex());
                    handleEditDepartement(d);
                });

                deleteButton.setOnAction(event -> {
                    Departement d = getTableView().getItems().get(getIndex());
                    handleDeleteDepartement(d);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(5, viewButton, editButton, deleteButton);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });
    }

    private void loadDepartements() {
        departementsList.clear();
        List<Departement> list = departementDAO.findAll();
        departementsList.addAll(list);
        departementsTable.setItems(departementsList);
    }

    private void updateStatistics() {
        long total = departementDAO.count();
        totalDepartementsLabel.setText(String.valueOf(total));
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText() != null ? searchField.getText().trim() : "";

        if (keyword.isEmpty()) {
            loadDepartements();
            return;
        }

        departementsList.clear();
        for (Departement d : departementDAO.findAll()) {
            if ((d.getNom() != null && d.getNom().toLowerCase().contains(keyword.toLowerCase())) ||
                    (d.getChefNomComplet() != null && d.getChefNomComplet().toLowerCase().contains(keyword.toLowerCase()))) {
                departementsList.add(d);
            }
        }
        departementsTable.setItems(departementsList);
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        loadDepartements();
        updateStatistics();
        AlertUtil.showInfo("Actualisation", "Liste des départements actualisée avec succès!");
    }

    @FXML
    private void handleAddDepartement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hopitalapp/view/admin/departement-form.fxml"));
            Parent root = loader.load();

            DepartementFormController controller = loader.getController();
            controller.setDepartementController(this);

            Stage stage = new Stage();
            stage.setTitle("Nouveau Département");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void handleViewDepartement(Departement d) {
        String info = "🏥 INFORMATIONS DU DÉPARTEMENT\n\n" +
                "Nom: " + safe(d.getNom()) + "\n" +
                "Description: " + safe(d.getDescription()) + "\n" +
                "Étage: " + safe(d.getEtage()) + "\n" +
                "Nombre de lits: " + d.getNombreLits() + "\n" +
                "Chef de service: " + safe(d.getChefNomComplet()) + "\n" +
                "Téléphone: " + safe(d.getTelephone()) + "\n" +
                "Email du chef: " + safe(d.getChefEmail()) + "\n";

        AlertUtil.showInfo("Détails du Département", info);
    }

    private String safe(String v) {
        return v != null && !v.isEmpty() ? v : "N/A";
    }

    private void handleEditDepartement(Departement d) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hopitalapp/view/admin/departement-form.fxml"));
            Parent root = loader.load();

            DepartementFormController controller = loader.getController();
            controller.setDepartementController(this);
            controller.setDepartement(d);

            Stage stage = new Stage();
            stage.setTitle("Modifier Département - " + d.getNom());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void handleDeleteDepartement(Departement d) {
        boolean confirm = AlertUtil.showConfirmation(
                "Confirmation de suppression",
                "Êtes-vous sûr de vouloir supprimer le département:\n" + d.getNom() + " ?"
        );

        if (confirm) {
            departementDAO.delete(d.getId());
            loadDepartements();
            updateStatistics();
            AlertUtil.showSuccess("Succès", "Département supprimé avec succès!");
        }
    }

    public void refreshTable() {
        loadDepartements();
        updateStatistics();
    }
}
