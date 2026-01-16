package com.example.hopitalapp.controller.secretaire;

import com.example.hopitalapp.dao.impl.DossierMedicalDAOImpl;
import com.example.hopitalapp.dao.interfaces.DossierMedicalDAO;
import com.example.hopitalapp.model.DossierMedical;
import com.example.hopitalapp.model.Patient;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class SecretaireDossierMedicalController implements Initializable {

    @FXML private Label patientNameLabel;
    @FXML private Label patientInfoLabel;
    @FXML private TableView<DossierMedical> dossierTable;
    @FXML private TableColumn<DossierMedical, String> dateColumn;
    @FXML private TableColumn<DossierMedical, String> medecinColumn;
    @FXML private TableColumn<DossierMedical, String> diagnosticColumn;
    @FXML private TableColumn<DossierMedical, String> symptomesColumn;
    @FXML private TableColumn<DossierMedical, String> traitementColumn;
    @FXML private TextArea detailsArea;
    @FXML private Button closeButton;

    private final DossierMedicalDAO dossierDAO = new DossierMedicalDAOImpl();
    private final ObservableList<DossierMedical> dossiersList = FXCollections.observableArrayList();
    private Patient currentPatient;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupTableSelection();
    }

    public void setPatient(Patient patient) {
        this.currentPatient = patient;
        
        patientNameLabel.setText(patient.getFullName());
        patientInfoLabel.setText(String.format("Âge: %d ans | Sexe: %s | Groupe sanguin: %s", 
                patient.getAge(), 
                patient.getSexe(), 
                patient.getGroupeSanguin() != null ? patient.getGroupeSanguin() : "N/A"));
        
        loadDossierMedical();
    }

    private void setupTableColumns() {
        // Date consultation
        dateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDateConsultation() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                return new SimpleStringProperty(cellData.getValue().getDateConsultation().format(formatter));
            }
            return new SimpleStringProperty("");
        });

        // Médecin
        medecinColumn.setCellValueFactory(new PropertyValueFactory<>("medecinNomComplet"));
        
        // Diagnostic (tronqué pour l'affichage)
        diagnosticColumn.setCellValueFactory(cellData -> {
            String diagnostic = cellData.getValue().getDiagnostic();
            if (diagnostic != null && diagnostic.length() > 50) {
                return new SimpleStringProperty(diagnostic.substring(0, 50) + "...");
            }
            return new SimpleStringProperty(diagnostic != null ? diagnostic : "");
        });

        // Symptômes (tronqué)
        symptomesColumn.setCellValueFactory(cellData -> {
            String symptomes = cellData.getValue().getSymptomes();
            if (symptomes != null && symptomes.length() > 40) {
                return new SimpleStringProperty(symptomes.substring(0, 40) + "...");
            }
            return new SimpleStringProperty(symptomes != null ? symptomes : "");
        });

        // Traitement (tronqué)
        traitementColumn.setCellValueFactory(cellData -> {
            String traitement = cellData.getValue().getTraitementPrescrit();
            if (traitement != null && traitement.length() > 40) {
                return new SimpleStringProperty(traitement.substring(0, 40) + "...");
            }
            return new SimpleStringProperty(traitement != null ? traitement : "");
        });
    }

    private void setupTableSelection() {
        dossierTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displayDossierDetails(newSelection);
            } else {
                detailsArea.clear();
            }
        });
    }

    private void loadDossierMedical() {
        if (currentPatient == null) return;
        
        dossiersList.clear();
        List<DossierMedical> dossiers = dossierDAO.findByPatient(currentPatient.getId());
        dossiersList.addAll(dossiers);
        dossierTable.setItems(dossiersList);
        
        if (dossiers.isEmpty()) {
            detailsArea.setText("Aucun dossier médical trouvé pour ce patient.");
        }
    }

    private void displayDossierDetails(DossierMedical dossier) {
        StringBuilder details = new StringBuilder();
        
        details.append("📅 CONSULTATION DU ");
        if (dossier.getDateConsultation() != null) {
            details.append(dossier.getDateConsultation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")));
        }
        details.append("\n");
        details.append("👨‍⚕️ Médecin: ").append(dossier.getMedecinNomComplet() != null ? dossier.getMedecinNomComplet() : "N/A").append("\n\n");
        
        details.append("🔍 DIAGNOSTIC:\n");
        details.append(dossier.getDiagnostic() != null ? dossier.getDiagnostic() : "Non renseigné").append("\n\n");
        
        details.append("🤒 SYMPTÔMES:\n");
        details.append(dossier.getSymptomes() != null ? dossier.getSymptomes() : "Non renseigné").append("\n\n");
        
        details.append("💊 TRAITEMENT PRESCRIT:\n");
        details.append(dossier.getTraitementPrescrit() != null ? dossier.getTraitementPrescrit() : "Aucun traitement prescrit").append("\n\n");
        
        details.append("📝 OBSERVATIONS:\n");
        details.append(dossier.getObservations() != null ? dossier.getObservations() : "Aucune observation").append("\n\n");
        
        details.append("📊 DONNÉES VITALES:\n");
        if (dossier.getPoids() != null) {
            details.append("• Poids: ").append(dossier.getPoids()).append(" kg\n");
        }
        if (dossier.getTaille() != null) {
            details.append("• Taille: ").append(dossier.getTaille()).append(" cm\n");
        }
        if (dossier.getTensionArterielle() != null) {
            details.append("• Tension artérielle: ").append(dossier.getTensionArterielle()).append("\n");
        }
        if (dossier.getTemperature() != null) {
            details.append("• Température: ").append(dossier.getTemperature()).append("°C\n");
        }
        
        detailsArea.setText(details.toString());
    }

    @FXML
    private void handleClose() {
        closeButton.getScene().getWindow().hide();
    }
}