package com.example.hopitalapp.controller.secretaire;

import com.example.hopitalapp.dao.impl.MedecinDAOImpl;
import com.example.hopitalapp.dao.impl.PatientDAOImpl;
import com.example.hopitalapp.dao.impl.RendezVousDAOImpl;
import com.example.hopitalapp.dao.interfaces.MedecinDAO;
import com.example.hopitalapp.dao.interfaces.PatientDAO;
import com.example.hopitalapp.dao.interfaces.RendezVousDAO;
import com.example.hopitalapp.model.Medecin;
import com.example.hopitalapp.model.Patient;
import com.example.hopitalapp.model.RendezVous;
import com.example.hopitalapp.util.AlertUtil;
import com.example.hopitalapp.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;

public class SecretaireRendezVousFormController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private ComboBox<Patient> patientComboBox;
    @FXML private ComboBox<Medecin> medecinComboBox;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> heureComboBox;
    @FXML private ComboBox<Integer> dureeComboBox;
    @FXML private TextField motifField;
    @FXML private TextArea notesArea;
    @FXML private ComboBox<String> statutComboBox;
    @FXML private Button checkDisponibiliteButton;
    @FXML private Label disponibiliteLabel;
    @FXML private Button saveButton;

    private final PatientDAO patientDAO = new PatientDAOImpl();
    private final MedecinDAO medecinDAO = new MedecinDAOImpl();
    private final RendezVousDAO rendezVousDAO = new RendezVousDAOImpl();
    private SecretaireRendezVousController rendezVousController;
    private RendezVous currentRendezVous;
    private boolean isEditMode = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupComboBoxes();
        setupListeners();
    }

    private void setupComboBoxes() {
        // Patients
        List<Patient> patients = patientDAO.findAll();
        patientComboBox.getItems().addAll(patients);
        patientComboBox.setConverter(new javafx.util.StringConverter<Patient>() {
            @Override
            public String toString(Patient patient) {
                return patient == null ? "" : patient.getFullName() + " (" + patient.getTelephone() + ")";
            }

            @Override
            public Patient fromString(String string) {
                return null;
            }
        });

        // Médecins
        List<Medecin> medecins = medecinDAO.findAll();
        medecinComboBox.getItems().addAll(medecins);
        medecinComboBox.setConverter(new javafx.util.StringConverter<Medecin>() {
            @Override
            public String toString(Medecin medecin) {
                return medecin == null ? "" : medecin.getFullName() + " - " + 
                       (medecin.getSpecialite() != null ? medecin.getSpecialite() : "Généraliste");
            }

            @Override
            public Medecin fromString(String string) {
                return null;
            }
        });

        // Heures (de 8h à 18h par créneaux de 30 minutes)
        for (int hour = 8; hour <= 18; hour++) {
            for (int minute = 0; minute < 60; minute += 30) {
                if (hour == 18 && minute > 0) break; // Arrêter à 18h00
                heureComboBox.getItems().add(String.format("%02d:%02d", hour, minute));
            }
        }

        // Durées
        dureeComboBox.getItems().addAll(15, 30, 45, 60, 90, 120);
        dureeComboBox.setValue(30);

        // Statuts
        statutComboBox.getItems().addAll("EN_ATTENTE", "CONFIRME", "ANNULE", "TERMINE");
        statutComboBox.setValue("EN_ATTENTE");

        // Date par défaut : demain
        datePicker.setValue(LocalDate.now().plusDays(1));
    }

    private void setupListeners() {
        // Vérifier la disponibilité automatiquement quand les paramètres changent
        medecinComboBox.valueProperty().addListener((obs, oldVal, newVal) -> clearDisponibilite());
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> clearDisponibilite());
        heureComboBox.valueProperty().addListener((obs, oldVal, newVal) -> clearDisponibilite());
        dureeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> clearDisponibilite());
    }

    public void setRendezVousController(SecretaireRendezVousController controller) {
        this.rendezVousController = controller;
    }

    public void setRendezVous(RendezVous rendezVous) {
        this.currentRendezVous = rendezVous;
        this.isEditMode = true;
        titleLabel.setText("✏️ Modifier Rendez-vous");
        saveButton.setText("Mettre à jour");

        // Remplir les champs
        // Trouver et sélectionner le patient
        for (Patient patient : patientComboBox.getItems()) {
            if (patient.getId() == rendezVous.getPatientId()) {
                patientComboBox.setValue(patient);
                break;
            }
        }

        // Trouver et sélectionner le médecin
        for (Medecin medecin : medecinComboBox.getItems()) {
            if (medecin.getId() == rendezVous.getMedecinId()) {
                medecinComboBox.setValue(medecin);
                break;
            }
        }

        if (rendezVous.getDateRdv() != null) {
            datePicker.setValue(rendezVous.getDateRdv().toLocalDate());
            heureComboBox.setValue(rendezVous.getDateRdv().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
        }

        dureeComboBox.setValue(rendezVous.getDuree());
        motifField.setText(rendezVous.getMotif());
        notesArea.setText(rendezVous.getNotes());
        statutComboBox.setValue(rendezVous.getStatut());
    }

    @FXML
    private void handleCheckDisponibilite() {
        if (!validateDisponibiliteFields()) {
            return;
        }

        Medecin medecin = medecinComboBox.getValue();
        LocalDate date = datePicker.getValue();
        String heureStr = heureComboBox.getValue();
        Integer duree = dureeComboBox.getValue();

        LocalTime heure = LocalTime.parse(heureStr);
        LocalDateTime dateTime = LocalDateTime.of(date, heure);

        boolean disponible = rendezVousDAO.isMedecinDisponible(medecin.getId(), dateTime, duree);

        if (disponible) {
            disponibiliteLabel.setText("✅ Médecin disponible");
            disponibiliteLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        } else {
            disponibiliteLabel.setText("❌ Médecin non disponible");
            disponibiliteLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        }
        disponibiliteLabel.setVisible(true);
    }

    private void clearDisponibilite() {
        disponibiliteLabel.setVisible(false);
    }

    private boolean validateDisponibiliteFields() {
        if (medecinComboBox.getValue() == null) {
            AlertUtil.showError("Validation", "Veuillez sélectionner un médecin!");
            return false;
        }

        if (datePicker.getValue() == null) {
            AlertUtil.showError("Validation", "Veuillez sélectionner une date!");
            return false;
        }

        if (heureComboBox.getValue() == null) {
            AlertUtil.showError("Validation", "Veuillez sélectionner une heure!");
            return false;
        }

        if (dureeComboBox.getValue() == null) {
            AlertUtil.showError("Validation", "Veuillez sélectionner une durée!");
            return false;
        }

        return true;
    }

    @FXML
    private void handleSave() {
        if (!validateFields()) {
            return;
        }

        // Vérifier la disponibilité avant de sauvegarder
        if (!isEditMode || hasTimeChanged()) {
            Medecin medecin = medecinComboBox.getValue();
            LocalDate date = datePicker.getValue();
            String heureStr = heureComboBox.getValue();
            Integer duree = dureeComboBox.getValue();

            LocalTime heure = LocalTime.parse(heureStr);
            LocalDateTime dateTime = LocalDateTime.of(date, heure);

            boolean disponible = rendezVousDAO.isMedecinDisponible(medecin.getId(), dateTime, duree);
            if (!disponible) {
                AlertUtil.showError("Conflit d'horaire", 
                    "Le médecin n'est pas disponible à cette heure. Veuillez choisir un autre créneau.");
                return;
            }
        }

        RendezVous rendezVous = isEditMode ? currentRendezVous : new RendezVous();

        // Remplir l'objet
        rendezVous.setPatientId(patientComboBox.getValue().getId());
        rendezVous.setMedecinId(medecinComboBox.getValue().getId());
        
        LocalDate date = datePicker.getValue();
        String heureStr = heureComboBox.getValue();
        LocalTime heure = LocalTime.parse(heureStr);
        rendezVous.setDateRdv(LocalDateTime.of(date, heure));
        
        rendezVous.setDuree(dureeComboBox.getValue());
        rendezVous.setMotif(motifField.getText().trim());
        rendezVous.setNotes(notesArea.getText().trim());
        rendezVous.setStatut(statutComboBox.getValue());

        if (!isEditMode) {
            rendezVous.setCreatedBy(SessionManager.getInstance().getCurrentUser().getId());
        }

        // Sauvegarder
        if (isEditMode) {
            rendezVousDAO.update(rendezVous);
            AlertUtil.showSuccess("Succès", "Rendez-vous modifié avec succès!");
        } else {
            rendezVousDAO.save(rendezVous);
            AlertUtil.showSuccess("Succès", "Rendez-vous créé avec succès!");
        }

        // Actualiser la table
        if (rendezVousController != null) {
            rendezVousController.refreshTable();
        }

        closeWindow();
    }

    private boolean hasTimeChanged() {
        if (currentRendezVous == null || currentRendezVous.getDateRdv() == null) return true;
        
        LocalDate newDate = datePicker.getValue();
        String newHeureStr = heureComboBox.getValue();
        Integer newDuree = dureeComboBox.getValue();
        int newMedecinId = medecinComboBox.getValue().getId();
        
        LocalTime newHeure = LocalTime.parse(newHeureStr);
        LocalDateTime newDateTime = LocalDateTime.of(newDate, newHeure);
        
        return !currentRendezVous.getDateRdv().equals(newDateTime) || 
               currentRendezVous.getDuree() != newDuree ||
               currentRendezVous.getMedecinId() != newMedecinId;
    }

    private boolean validateFields() {
        if (patientComboBox.getValue() == null) {
            AlertUtil.showError("Validation", "Veuillez sélectionner un patient!");
            return false;
        }

        if (medecinComboBox.getValue() == null) {
            AlertUtil.showError("Validation", "Veuillez sélectionner un médecin!");
            return false;
        }

        if (datePicker.getValue() == null) {
            AlertUtil.showError("Validation", "Veuillez sélectionner une date!");
            return false;
        }

        if (datePicker.getValue().isBefore(LocalDate.now())) {
            AlertUtil.showError("Validation", "La date ne peut pas être dans le passé!");
            return false;
        }

        if (heureComboBox.getValue() == null) {
            AlertUtil.showError("Validation", "Veuillez sélectionner une heure!");
            return false;
        }

        if (dureeComboBox.getValue() == null) {
            AlertUtil.showError("Validation", "Veuillez sélectionner une durée!");
            return false;
        }

        if (motifField.getText().trim().isEmpty()) {
            AlertUtil.showError("Validation", "Le motif est obligatoire!");
            return false;
        }

        return true;
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}