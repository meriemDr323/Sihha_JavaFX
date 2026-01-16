package com.example.hopitalapp.controller.shared;

import com.example.hopitalapp.controller.medecin.MesRendezVousController;
import com.example.hopitalapp.dao.impl.DossierMedicalDAOImpl;
import com.example.hopitalapp.dao.impl.MedecinDAOImpl;
import com.example.hopitalapp.dao.interfaces.DossierMedicalDAO;
import com.example.hopitalapp.model.DossierMedical;
import com.example.hopitalapp.model.Medecin;
import com.example.hopitalapp.model.User;
import com.example.hopitalapp.util.AlertUtil;
import com.example.hopitalapp.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class DossierMedicalFormController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private TextArea diagnosticArea;
    @FXML private TextArea symptomesArea;
    @FXML private TextArea traitementArea;
    @FXML private TextArea observationsArea;
    @FXML private TextField poidsField;
    @FXML private TextField tailleField;
    @FXML private TextField tensionField;
    @FXML private TextField temperatureField;
    @FXML private Button saveButton;

    private final DossierMedicalDAO dossierDAO = new DossierMedicalDAOImpl();
    private DossierMedical dossier;
    private MesRendezVousController mesRendezVousController;

    @Override
    public void initialize(URL location, ResourceBundle resources) { }

    public void setMesRendezVousController(MesRendezVousController controller) {
        this.mesRendezVousController = controller;
    }

    public void setDossierMedical(DossierMedical dossier) {
        this.dossier = dossier;

        // SÉCURITÉ : Vérifier que le médecin connecté est bien le médecin du dossier
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            Medecin currentMedecin = new MedecinDAOImpl().findByUserId(currentUser.getId());
            if (currentMedecin != null && dossier.getMedecinId() != 0 && dossier.getMedecinId() != currentMedecin.getId()) {
                AlertUtil.showError("Accès refusé", "Vous n'êtes pas autorisé à modifier ce dossier médical.");
                return;
            }
            // Si c'est un nouveau dossier, assigner le médecin connecté
            if (dossier.getMedecinId() == 0) {
                dossier.setMedecinId(currentMedecin.getId());
            }
        }

        if (dossier.getId() == 0) {
            titleLabel.setText("Nouveau dossier médical");
        } else {
            titleLabel.setText("Modifier dossier médical");
            diagnosticArea.setText(dossier.getDiagnostic());
            symptomesArea.setText(dossier.getSymptomes());
            traitementArea.setText(dossier.getTraitementPrescrit());
            observationsArea.setText(dossier.getObservations());
            if (dossier.getPoids() != null) poidsField.setText(String.valueOf(dossier.getPoids()));
            if (dossier.getTaille() != null) tailleField.setText(String.valueOf(dossier.getTaille()));
            tensionField.setText(dossier.getTensionArterielle());
            if (dossier.getTemperature() != null) temperatureField.setText(String.valueOf(dossier.getTemperature()));
        }
    }

    @FXML
    private void handleSave() {
        if (dossier == null) return;

        dossier.setDiagnostic(textOrNull(diagnosticArea));
        dossier.setSymptomes(textOrNull(symptomesArea));
        dossier.setTraitementPrescrit(textOrNull(traitementArea));
        dossier.setObservations(textOrNull(observationsArea));

        dossier.setPoids(parseDoubleOrNull(poidsField.getText()));
        dossier.setTaille(parseDoubleOrNull(tailleField.getText()));
        dossier.setTensionArterielle(textOrNull(tensionField));
        dossier.setTemperature(parseDoubleOrNull(temperatureField.getText()));

        if (dossier.getDateConsultation() == null) {
            dossier.setDateConsultation(LocalDateTime.now());
        }

        if (dossier.getId() == 0) {
            dossierDAO.save(dossier);
            AlertUtil.showSuccess("Succès", "Dossier médical créé avec succès.");
        } else {
            dossierDAO.update(dossier);
            AlertUtil.showSuccess("Succès", "Dossier médical mis à jour avec succès.");
        }

        if (mesRendezVousController != null) {
            mesRendezVousController.refresh();
        }

        closeWindow();
    }

    private String textOrNull(TextInputControl c) {
        return c.getText() != null && !c.getText().trim().isEmpty()
                ? c.getText().trim()
                : null;
    }

    private Double parseDoubleOrNull(String value) {
        if (value == null) return null;
        String v = value.trim();
        if (v.isEmpty()) return null;
        try {
            return Double.parseDouble(v);
        } catch (NumberFormatException e) {
            return null;
        }
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
