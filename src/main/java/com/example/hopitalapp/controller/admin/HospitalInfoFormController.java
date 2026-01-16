package com.example.hopitalapp.controller.admin;

import com.example.hopitalapp.dao.impl.HospitalDAOImpl;
import com.example.hopitalapp.dao.interfaces.HospitalDAO;
import com.example.hopitalapp.model.Hospital;
import com.example.hopitalapp.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class HospitalInfoFormController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private TextField nomField;
    @FXML private TextField adresseField;
    @FXML private TextField villeField;
    @FXML private TextField codePostalField;
    @FXML private TextField telephoneField;
    @FXML private TextField emailField;
    @FXML private TextField siteWebField;
    @FXML private TextField nombreLitsField;
    @FXML private TextField capaciteUrgencesField;
    @FXML private TextField logoPathField;
    @FXML private TextArea descriptionArea;
    @FXML private Button saveButton;

    private final HospitalDAO hospitalDAO = new HospitalDAOImpl();
    private HospitalInfoController hospitalInfoController;
    private Hospital currentHospital;
    private boolean isEditMode = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // rien
    }

    public void setHospitalInfoController(HospitalInfoController controller) {
        this.hospitalInfoController = controller;
    }

    public void setHospital(Hospital hospital) {
        if (hospital == null) {
            currentHospital = new Hospital();
            isEditMode = false;
            titleLabel.setText("Créer les informations de l'hôpital");
            saveButton.setText("Créer");
        } else {
            currentHospital = hospital;
            isEditMode = true;
            titleLabel.setText("Modifier les informations de l'hôpital");
            saveButton.setText("Mettre à jour");

            nomField.setText(safeString(hospital.getNom()));
            adresseField.setText(safeString(hospital.getAdresse()));
            villeField.setText(safeString(hospital.getVille()));
            codePostalField.setText(safeString(hospital.getCodePostal()));
            telephoneField.setText(safeString(hospital.getTelephone()));
            emailField.setText(safeString(hospital.getEmail()));
            siteWebField.setText(safeString(hospital.getSiteWeb()));
            nombreLitsField.setText(String.valueOf(hospital.getNombreLitsTotal()));
            capaciteUrgencesField.setText(String.valueOf(hospital.getCapaciteUrgences()));
            logoPathField.setText(safeString(hospital.getLogoPath()));
            descriptionArea.setText(safeString(hospital.getDescription()));
        }
    }

    private String safeString(String value) {
        return value != null ? value : "";
    }

    private String safeText(TextField field) {
        return field != null && field.getText() != null ? field.getText().trim() : "";
    }

    private String safeTextArea(TextArea area) {
        return area != null && area.getText() != null ? area.getText().trim() : "";
    }

    @FXML
    private void handleSave() {
        if (!validate()) return;

        currentHospital.setNom(safeText(nomField));
        currentHospital.setAdresse(safeText(adresseField));
        currentHospital.setVille(safeText(villeField));
        currentHospital.setCodePostal(safeText(codePostalField));
        currentHospital.setTelephone(safeText(telephoneField));
        currentHospital.setEmail(safeText(emailField));
        currentHospital.setSiteWeb(safeText(siteWebField));
        currentHospital.setLogoPath(safeText(logoPathField));
        currentHospital.setDescription(safeTextArea(descriptionArea));

        String lits = safeText(nombreLitsField);
        currentHospital.setNombreLitsTotal(lits.isEmpty() ? 0 : Integer.parseInt(lits));

        String urg = safeText(capaciteUrgencesField);
        currentHospital.setCapaciteUrgences(urg.isEmpty() ? 0 : Integer.parseInt(urg));

        hospitalDAO.saveOrUpdate(currentHospital);

        AlertUtil.showSuccess("Succès", isEditMode ?
                "Informations mises à jour avec succès!" :
                "Informations créées avec succès!");

        if (hospitalInfoController != null) {
            hospitalInfoController.refresh();
        }

        closeWindow();
    }

    private boolean validate() {
        if (safeText(nomField).isEmpty()) {
            AlertUtil.showError("Validation", "Le nom de l'hôpital est obligatoire.");
            nomField.requestFocus();
            return false;
        }

        // Vérification numériques
        String lits = safeText(nombreLitsField);
        if (!lits.isEmpty()) {
            try {
                Integer.parseInt(lits);
            } catch (NumberFormatException e) {
                AlertUtil.showError("Validation", "Le nombre total de lits doit être un entier.");
                return false;
            }
        }

        String urg = safeText(capaciteUrgencesField);
        if (!urg.isEmpty()) {
            try {
                Integer.parseInt(urg);
            } catch (NumberFormatException e) {
                AlertUtil.showError("Validation", "La capacité urgences doit être un entier.");
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
