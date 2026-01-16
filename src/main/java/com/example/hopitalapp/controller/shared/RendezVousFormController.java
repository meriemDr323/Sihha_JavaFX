package com.example.hopitalapp.controller.shared;

import com.example.hopitalapp.dao.impl.MedecinDAOImpl;
import com.example.hopitalapp.dao.impl.PatientDAOImpl;
import com.example.hopitalapp.dao.impl.RendezVousDAOImpl;
import com.example.hopitalapp.dao.interfaces.MedecinDAO;
import com.example.hopitalapp.dao.interfaces.PatientDAO;
import com.example.hopitalapp.dao.interfaces.RendezVousDAO;
import com.example.hopitalapp.model.Medecin;
import com.example.hopitalapp.model.Patient;
import com.example.hopitalapp.model.RendezVous;
import com.example.hopitalapp.model.User;
import com.example.hopitalapp.util.AlertUtil;
import com.example.hopitalapp.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ResourceBundle;

public class RendezVousFormController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private ComboBox<Patient> patientCombo;
    @FXML private ComboBox<Medecin> medecinCombo;
    @FXML private DatePicker datePicker;
    @FXML private TextField heureField;
    @FXML private TextField dureeField;
    @FXML private TextField motifField;
    @FXML private TextArea notesArea;
    @FXML private ComboBox<String> statutCombo;
    @FXML private Button saveButton;

    private final RendezVousDAO rendezVousDAO = new RendezVousDAOImpl();
    private final PatientDAO patientDAO = new PatientDAOImpl();
    private final MedecinDAO medecinDAO = new MedecinDAOImpl();

    private RendezVousController rendezVousController;
    private RendezVous currentRendezVous;
    private boolean isEditMode = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        patientCombo.setItems(FXCollections.observableArrayList(patientDAO.findAll()));
        medecinCombo.setItems(FXCollections.observableArrayList(medecinDAO.findAll()));

        statutCombo.setItems(FXCollections.observableArrayList(
                "EN_ATTENTE", "CONFIRME", "ANNULE", "TERMINE"
        ));
        statutCombo.getSelectionModel().select("EN_ATTENTE");

        dureeField.setText("30");
    }

    public void setRendezVousController(RendezVousController controller) {
        this.rendezVousController = controller;
    }

    public void setRendezVous(RendezVous rdv) {
        if (rdv == null) {
            this.currentRendezVous = new RendezVous();
            this.isEditMode = false;
            titleLabel.setText("Nouveau Rendez-vous");
            saveButton.setText("Créer");
            return;
        }

        this.currentRendezVous = rdv;
        this.isEditMode = true;

        titleLabel.setText("Modifier Rendez-vous");
        saveButton.setText("Mettre à jour");

        // Sélection patient / médecin
        for (Patient p : patientCombo.getItems()) {
            if (p.getId() == rdv.getPatientId()) {
                patientCombo.getSelectionModel().select(p);
                break;
            }
        }
        for (Medecin m : medecinCombo.getItems()) {
            if (m.getId() == rdv.getMedecinId()) {
                medecinCombo.getSelectionModel().select(m);
                break;
            }
        }

        if (rdv.getDateRdv() != null) {
            datePicker.setValue(rdv.getDateRdv().toLocalDate());
            heureField.setText(rdv.getDateRdv().toLocalTime().toString().substring(0,5));
        }
        dureeField.setText(String.valueOf(rdv.getDuree()));
        motifField.setText(rdv.getMotif());
        notesArea.setText(rdv.getNotes());
        statutCombo.getSelectionModel().select(rdv.getStatut());
    }

    @FXML
    private void handleSave() {
        if (!validate()) return;

        Patient p = patientCombo.getSelectionModel().getSelectedItem();
        Medecin m = medecinCombo.getSelectionModel().getSelectedItem();
        LocalDate date = datePicker.getValue();
        LocalTime time = LocalTime.parse(heureField.getText().trim());
        int duree = Integer.parseInt(dureeField.getText().trim());

        LocalDateTime dateTime = LocalDateTime.of(date, time);

        // Vérif disponibilité médecin
        if (!rendezVousDAO.isMedecinDisponible(m.getId(), dateTime, duree)
                && (!isEditMode || !dateTime.equals(currentRendezVous.getDateRdv()))) {
            AlertUtil.showError("Conflit", "Le médecin a déjà un rendez-vous sur ce créneau.");
            return;
        }

        currentRendezVous.setPatientId(p.getId());
        currentRendezVous.setMedecinId(m.getId());
        currentRendezVous.setDateRdv(dateTime);
        currentRendezVous.setDuree(duree);
        currentRendezVous.setMotif(
                motifField.getText() != null ? motifField.getText().trim() : null
        );
        currentRendezVous.setNotes(
                notesArea.getText() != null ? notesArea.getText().trim() : null
        );
        currentRendezVous.setStatut(statutCombo.getSelectionModel().getSelectedItem());

        if (!isEditMode) {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser != null) {
                currentRendezVous.setCreatedBy(currentUser.getId());
            }
            rendezVousDAO.save(currentRendezVous);
            AlertUtil.showSuccess("Succès", "Rendez-vous créé avec succès!");
        } else {
            rendezVousDAO.update(currentRendezVous);
            AlertUtil.showSuccess("Succès", "Rendez-vous modifié avec succès!");
        }

        if (rendezVousController != null) {
            rendezVousController.refreshTable();
        }

        closeWindow();
    }

    private boolean validate() {
        if (patientCombo.getSelectionModel().isEmpty()) {
            AlertUtil.showError("Validation", "Veuillez sélectionner un patient.");
            return false;
        }
        if (medecinCombo.getSelectionModel().isEmpty()) {
            AlertUtil.showError("Validation", "Veuillez sélectionner un médecin.");
            return false;
        }
        if (datePicker.getValue() == null) {
            AlertUtil.showError("Validation", "Veuillez choisir une date.");
            return false;
        }
        String heure = heureField.getText() != null ? heureField.getText().trim() : "";
        if (heure.isEmpty()) {
            AlertUtil.showError("Validation", "Veuillez saisir une heure (HH:MM).");
            return false;
        }
        try {
            LocalTime.parse(heure);
        } catch (Exception e) {
            AlertUtil.showError("Validation", "Heure invalide. Format attendu: HH:MM");
            return false;
        }
        String dureeStr = dureeField.getText() != null ? dureeField.getText().trim() : "";
        if (!dureeStr.isEmpty()) {
            try {
                Integer.parseInt(dureeStr);
            } catch (NumberFormatException e) {
                AlertUtil.showError("Validation", "La durée doit être un entier.");
                return false;
            }
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
