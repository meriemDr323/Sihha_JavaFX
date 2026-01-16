package com.example.hopitalapp.controller.shared;

import com.example.hopitalapp.dao.impl.PatientDAOImpl;
import com.example.hopitalapp.dao.interfaces.PatientDAO;
import com.example.hopitalapp.model.Patient;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class PatientController implements Initializable, PatientControllerInterface {

    @FXML private TableView<Patient> patientsTable;
    @FXML private TableColumn<Patient, Integer> idColumn;
    @FXML private TableColumn<Patient, String> nomColumn;
    @FXML private TableColumn<Patient, String> prenomColumn;
    @FXML private TableColumn<Patient, String> dateNaissanceColumn;
    @FXML private TableColumn<Patient, Integer> ageColumn;
    @FXML private TableColumn<Patient, String> sexeColumn;
    @FXML private TableColumn<Patient, String> telephoneColumn;
    @FXML private TableColumn<Patient, String> villeColumn;
    @FXML private TableColumn<Patient, String> groupeSanguinColumn;
    @FXML private TableColumn<Patient, Void> actionsColumn;

    @FXML private TextField searchField;
    @FXML private Label totalPatientsLabel;
    @FXML private Button addButton;

    private final PatientDAO patientDAO = new PatientDAOImpl();
    private final ObservableList<Patient> patientsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Scroll horizontal comme pour les médecins
        patientsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        setupTableColumns();
        loadPatients();
        updateStatistics();
    }

    private void setupTableColumns() {
        // Colonnes simples
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        sexeColumn.setCellValueFactory(new PropertyValueFactory<>("sexe"));
        telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        villeColumn.setCellValueFactory(new PropertyValueFactory<>("ville"));
        groupeSanguinColumn.setCellValueFactory(new PropertyValueFactory<>("groupeSanguin"));

        // Colonne date de naissance formatée
        dateNaissanceColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDateNaissance() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                return new SimpleStringProperty(cellData.getValue().getDateNaissance().format(formatter));
            }
            return new SimpleStringProperty("");
        });

        // Colonne âge
        ageColumn.setCellValueFactory(new PropertyValueFactory<>("age"));

        // Colonne actions avec boutons texte
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("Voir");
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");

            {
                viewButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 8; -fx-background-radius: 3; -fx-cursor: hand;");
                editButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 8; -fx-background-radius: 3; -fx-cursor: hand;");
                deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 8; -fx-background-radius: 3; -fx-cursor: hand;");

                viewButton.setOnAction(event -> {
                    Patient patient = getTableView().getItems().get(getIndex());
                    handleViewPatient(patient);
                });

                editButton.setOnAction(event -> {
                    Patient patient = getTableView().getItems().get(getIndex());
                    handleEditPatient(patient);
                });

                deleteButton.setOnAction(event -> {
                    Patient patient = getTableView().getItems().get(getIndex());
                    handleDeletePatient(patient);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, viewButton, editButton, deleteButton);
                    buttons.setAlignment(Pos.CENTER);
                    setGraphic(buttons);
                }
            }
        });
    }

    private void loadPatients() {
        patientsList.clear();
        List<Patient> patients = patientDAO.findAll();
        patientsList.addAll(patients);
        patientsTable.setItems(patientsList);
    }

    private void updateStatistics() {
        long total = patientDAO.count();
        totalPatientsLabel.setText(String.valueOf(total));
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();

        if (keyword.isEmpty()) {
            loadPatients();
            return;
        }

        patientsList.clear();
        List<Patient> results = patientDAO.searchByName(keyword);
        patientsList.addAll(results);
        patientsTable.setItems(patientsList);
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        loadPatients();
        updateStatistics();
        AlertUtil.showInfo("Actualisation", "Liste des patients actualisée avec succès!");
    }

    @FXML
    private void handleAddPatient() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hopitalapp/view/shared/patient-form.fxml"));
            Parent root = loader.load();

            PatientFormController controller = loader.getController();
            controller.setPatientController(this);

            Stage stage = new Stage();
            stage.setTitle("Nouveau Patient");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void handleViewPatient(Patient patient) {
        String info = "📋 INFORMATIONS DU PATIENT\n\n" +
                "Nom complet: " + patient.getFullName() + "\n" +
                "Date de naissance: " + (patient.getDateNaissance() != null
                ? patient.getDateNaissance().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "N/A") + "\n" +
                "Âge: " + patient.getAge() + " ans\n" +
                "Sexe: " + patient.getSexe() + "\n" +
                "Téléphone: " + (patient.getTelephone() != null ? patient.getTelephone() : "N/A") + "\n" +
                "Email: " + (patient.getEmail() != null ? patient.getEmail() : "N/A") + "\n" +
                "Ville: " + (patient.getVille() != null ? patient.getVille() : "N/A") + "\n" +
                "Groupe sanguin: " + (patient.getGroupeSanguin() != null ? patient.getGroupeSanguin() : "N/A") + "\n" +
                "Allergies: " + (patient.getAllergies() != null ? patient.getAllergies() : "Aucune");

        AlertUtil.showInfo("Détails du Patient", info);
    }

    private void handleEditPatient(Patient patient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hopitalapp/view/shared/patient-form.fxml"));
            Parent root = loader.load();

            PatientFormController controller = loader.getController();
            controller.setPatientController(this);
            controller.setPatient(patient);

            Stage stage = new Stage();
            stage.setTitle("Modifier Patient - " + patient.getFullName());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void handleDeletePatient(Patient patient) {
        boolean confirm = AlertUtil.showConfirmation(
                "Confirmation de suppression",
                "Êtes-vous sûr de vouloir supprimer le patient:\n" + patient.getFullName() + " ?"
        );

        if (confirm) {
            patientDAO.delete(patient.getId());
            loadPatients();
            updateStatistics();
            AlertUtil.showSuccess("Succès", "Patient supprimé avec succès!");
        }
    }

    public void refreshTable() {
        loadPatients();
        updateStatistics();
    }
}
