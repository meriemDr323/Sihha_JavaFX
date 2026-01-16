package com.example.hopitalapp.controller.secretaire;

import com.example.hopitalapp.controller.shared.PatientControllerInterface;
import com.example.hopitalapp.controller.shared.PatientFormController;
import com.example.hopitalapp.dao.impl.PatientDAOImpl;
import com.example.hopitalapp.dao.interfaces.PatientDAO;
import com.example.hopitalapp.model.Patient;
import com.example.hopitalapp.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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

public class SecretairePatientsController implements Initializable {

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
    @FXML private ComboBox<String> filterSexeComboBox;
    @FXML private ComboBox<String> filterGroupeSanguinComboBox;
    @FXML private Label totalPatientsLabel;

    private final PatientDAO patientDAO = new PatientDAOImpl();
    private final ObservableList<Patient> patientsList = FXCollections.observableArrayList();
    private FilteredList<Patient> filteredPatients;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        patientsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        
        setupTableColumns();
        setupFilters();
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

        // Colonne actions - CRUD complet pour secrétaire
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("Voir");
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");
            private final Button dossierButton = new Button("Dossier");

            {
                viewButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 4 6; -fx-background-radius: 3; -fx-cursor: hand;");
                editButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 4 6; -fx-background-radius: 3; -fx-cursor: hand;");
                deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 4 6; -fx-background-radius: 3; -fx-cursor: hand;");
                dossierButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 4 6; -fx-background-radius: 3; -fx-cursor: hand;");

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

                dossierButton.setOnAction(event -> {
                    Patient patient = getTableView().getItems().get(getIndex());
                    handleViewDossierMedical(patient);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(3, viewButton, editButton, deleteButton, dossierButton);
                    buttons.setAlignment(Pos.CENTER);
                    setGraphic(buttons);
                }
            }
        });
    }

    private void setupFilters() {
        // ComboBox filtres
        filterSexeComboBox.getItems().addAll("Tous", "M", "F");
        filterSexeComboBox.setValue("Tous");
        
        filterGroupeSanguinComboBox.getItems().addAll("Tous", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
        filterGroupeSanguinComboBox.setValue("Tous");

        // Filtrage et tri
        filteredPatients = new FilteredList<>(patientsList, p -> true);
        SortedList<Patient> sortedPatients = new SortedList<>(filteredPatients);
        sortedPatients.comparatorProperty().bind(patientsTable.comparatorProperty());
        patientsTable.setItems(sortedPatients);

        // Listeners pour filtrage en temps réel
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        filterSexeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        filterGroupeSanguinComboBox.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    private void applyFilters() {
        filteredPatients.setPredicate(patient -> {
            // Filtre par recherche textuelle
            String searchText = searchField.getText();
            if (searchText != null && !searchText.isEmpty()) {
                String lowerCaseFilter = searchText.toLowerCase();
                if (!patient.getNom().toLowerCase().contains(lowerCaseFilter) &&
                    !patient.getPrenom().toLowerCase().contains(lowerCaseFilter) &&
                    (patient.getTelephone() == null || !patient.getTelephone().contains(lowerCaseFilter)) &&
                    (patient.getVille() == null || !patient.getVille().toLowerCase().contains(lowerCaseFilter))) {
                    return false;
                }
            }

            // Filtre par sexe
            String sexeFilter = filterSexeComboBox.getValue();
            if (sexeFilter != null && !sexeFilter.equals("Tous")) {
                if (!sexeFilter.equals(patient.getSexe())) {
                    return false;
                }
            }

            // Filtre par groupe sanguin
            String groupeFilter = filterGroupeSanguinComboBox.getValue();
            if (groupeFilter != null && !groupeFilter.equals("Tous")) {
                if (!groupeFilter.equals(patient.getGroupeSanguin())) {
                    return false;
                }
            }

            return true;
        });
        
        updateStatistics();
    }

    private void loadPatients() {
        patientsList.clear();
        List<Patient> patients = patientDAO.findAll();
        patientsList.addAll(patients);
    }

    private void updateStatistics() {
        totalPatientsLabel.setText(String.valueOf(filteredPatients.size()));
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        filterSexeComboBox.setValue("Tous");
        filterGroupeSanguinComboBox.setValue("Tous");
        loadPatients();
        AlertUtil.showInfo("Actualisation", "Liste des patients actualisée avec succès!");
    }

    @FXML
    private void handleAddPatient() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hopitalapp/view/shared/patient-form.fxml"));
            Parent root = loader.load();

            PatientFormController controller = loader.getController();
            controller.setPatientController(new PatientControllerAdapter());

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

    // Adapter pour permettre l'utilisation du PatientFormController existant
    private class PatientControllerAdapter implements PatientControllerInterface {
        public void refreshTable() {
            loadPatients();
            updateStatistics();
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
                "Adresse: " + (patient.getAdresse() != null ? patient.getAdresse() : "N/A") + "\n" +
                "Ville: " + (patient.getVille() != null ? patient.getVille() : "N/A") + "\n" +
                "Code postal: " + (patient.getCodePostal() != null ? patient.getCodePostal() : "N/A") + "\n" +
                "Groupe sanguin: " + (patient.getGroupeSanguin() != null ? patient.getGroupeSanguin() : "N/A") + "\n" +
                "Allergies: " + (patient.getAllergies() != null ? patient.getAllergies() : "Aucune") + "\n" +
                "Assurance: " + (patient.getAssuranceProvider() != null ? patient.getAssuranceProvider() : "N/A") + "\n" +
                "Contact d'urgence: " + (patient.getContactUrgenceNom() != null ? 
                    patient.getContactUrgenceNom() + " - " + patient.getContactUrgenceTelephone() : "N/A");

        AlertUtil.showInfo("Détails du Patient", info);
    }

    private void handleViewDossierMedical(Patient patient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hopitalapp/view/secretaire/secretaire-dossier-medical.fxml"));
            Parent root = loader.load();

            SecretaireDossierMedicalController controller = loader.getController();
            controller.setPatient(patient);

            Stage stage = new Stage();
            stage.setTitle("Dossier Médical - " + patient.getFullName());
            stage.setScene(new Scene(root, 900, 600));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible d'ouvrir le dossier médical: " + e.getMessage());
        }
    }

    private void handleEditPatient(Patient patient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hopitalapp/view/shared/patient-form.fxml"));
            Parent root = loader.load();

            PatientFormController controller = loader.getController();
            controller.setPatientController(new PatientControllerAdapter());
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
                "Êtes-vous sûr de vouloir supprimer le patient ?\n\n" +
                        "Patient: " + patient.getFullName() + "\n" +
                        "Âge: " + patient.getAge() + " ans\n" +
                        "Téléphone: " + (patient.getTelephone() != null ? patient.getTelephone() : "N/A") + "\n\n" +
                        "⚠️ Cette action est irréversible !"
        );

        if (confirm) {
            try {
                patientDAO.delete(patient.getId());
                loadPatients();
                updateStatistics();
                AlertUtil.showSuccess("Succès", "Patient supprimé avec succès!");
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showError("Erreur", "Impossible de supprimer le patient: " + e.getMessage());
            }
        }
    }
}