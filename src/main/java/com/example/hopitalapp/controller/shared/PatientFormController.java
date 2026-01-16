package com.example.hopitalapp.controller.shared;

import com.example.hopitalapp.dao.impl.PatientDAOImpl;
import com.example.hopitalapp.dao.interfaces.PatientDAO;
import com.example.hopitalapp.model.Patient;
import com.example.hopitalapp.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class PatientFormController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private DatePicker dateNaissancePicker;
    @FXML private ComboBox<String> sexeComboBox;
    @FXML private TextField telephoneField;
    @FXML private TextField emailField;
    @FXML private TextField adresseField;
    @FXML private TextField villeField;
    @FXML private TextField codePostalField;
    @FXML private TextField numSecuriteSocialeField;
    @FXML private ComboBox<String> groupeSanguinComboBox;
    @FXML private TextArea allergiesArea;
    @FXML private TextField assuranceProviderField;
    @FXML private TextField numAssuranceField;
    @FXML private TextField contactUrgenceNomField;
    @FXML private TextField contactUrgenceTelField;
    @FXML private Button saveButton;

    private PatientDAO patientDAO = new PatientDAOImpl();
    private PatientControllerInterface patientController;
    private Patient currentPatient;
    private boolean isEditMode = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Remplir les ComboBox
        sexeComboBox.getItems().addAll("Masculin", "Féminin");
        groupeSanguinComboBox.getItems().addAll("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
    }

    public void setPatientController(PatientControllerInterface controller) {
        this.patientController = controller;
    }

    public void setPatient(Patient patient) {
        this.currentPatient = patient;
        this.isEditMode = true;
        titleLabel.setText("✏️ Modifier Patient");
        saveButton.setText("Mettre à jour");

        // Remplir les champs avec les données du patient
        nomField.setText(patient.getNom());
        prenomField.setText(patient.getPrenom());
        dateNaissancePicker.setValue(patient.getDateNaissance());

        // Conversion M/F → Masculin/Féminin pour l'affichage
        if ("M".equals(patient.getSexe())) {
            sexeComboBox.setValue("Masculin");
        } else if ("F".equals(patient.getSexe())) {
            sexeComboBox.setValue("Féminin");
        }

        telephoneField.setText(patient.getTelephone());
        emailField.setText(patient.getEmail());
        adresseField.setText(patient.getAdresse());
        villeField.setText(patient.getVille());
        codePostalField.setText(patient.getCodePostal());
        numSecuriteSocialeField.setText(patient.getNumeroSecuriteSociale());
        groupeSanguinComboBox.setValue(patient.getGroupeSanguin());
        allergiesArea.setText(patient.getAllergies());
        assuranceProviderField.setText(patient.getAssuranceProvider());
        numAssuranceField.setText(patient.getNumeroAssurance());
        contactUrgenceNomField.setText(patient.getContactUrgenceNom());
        contactUrgenceTelField.setText(patient.getContactUrgenceTelephone());
    }

    @FXML
    private void handleSave() {
        // Validation des champs obligatoires
        if (!validateFields()) {
            return;
        }

        Patient patient = isEditMode ? currentPatient : new Patient();

        // Remplir l'objet patient avec les valeurs du formulaire
        patient.setNom(nomField.getText().trim());
        patient.setPrenom(prenomField.getText().trim());
        patient.setDateNaissance(dateNaissancePicker.getValue());

        // Conversion Masculin/Féminin → M/F pour la base de données
        String sexe = sexeComboBox.getValue();
        if (sexe != null) {
            if (sexe.equals("Masculin")) {
                patient.setSexe("M");
            } else if (sexe.equals("Féminin")) {
                patient.setSexe("F");
            }
        }

        patient.setTelephone(telephoneField.getText().trim());
        patient.setEmail(emailField.getText().trim());
        patient.setAdresse(adresseField.getText().trim());
        patient.setVille(villeField.getText().trim());
        patient.setCodePostal(codePostalField.getText().trim());
        patient.setNumeroSecuriteSociale(numSecuriteSocialeField.getText().trim());
        patient.setGroupeSanguin(groupeSanguinComboBox.getValue());
        patient.setAllergies(allergiesArea.getText().trim());
        patient.setAssuranceProvider(assuranceProviderField.getText().trim());
        patient.setNumeroAssurance(numAssuranceField.getText().trim());
        patient.setContactUrgenceNom(contactUrgenceNomField.getText().trim());
        patient.setContactUrgenceTelephone(contactUrgenceTelField.getText().trim());

        // Enregistrer dans la base de données
        if (isEditMode) {
            patientDAO.update(patient);
            AlertUtil.showSuccess("Succès", "Patient modifié avec succès!");
        } else {
            patientDAO.save(patient);
            AlertUtil.showSuccess("Succès", "Patient ajouté avec succès!");
        }

        // Actualiser la table dans PatientController
        if (patientController != null) {
            patientController.refreshTable();
        }

        // Fermer la fenêtre
        closeWindow();
    }

    private boolean validateFields() {
        if (nomField.getText().trim().isEmpty()) {
            AlertUtil.showError("Validation", "Le nom est obligatoire!");
            nomField.requestFocus();
            return false;
        }

        if (prenomField.getText().trim().isEmpty()) {
            AlertUtil.showError("Validation", "Le prénom est obligatoire!");
            prenomField.requestFocus();
            return false;
        }

        if (dateNaissancePicker.getValue() == null) {
            AlertUtil.showError("Validation", "La date de naissance est obligatoire!");
            dateNaissancePicker.requestFocus();
            return false;
        }

        if (dateNaissancePicker.getValue().isAfter(LocalDate.now())) {
            AlertUtil.showError("Validation", "La date de naissance ne peut pas être dans le futur!");
            dateNaissancePicker.requestFocus();
            return false;
        }

        if (sexeComboBox.getValue() == null || sexeComboBox.getValue().isEmpty()) {
            AlertUtil.showError("Validation", "Le sexe est obligatoire!");
            sexeComboBox.requestFocus();
            return false;
        }

        if (telephoneField.getText().trim().isEmpty()) {
            AlertUtil.showError("Validation", "Le téléphone est obligatoire!");
            telephoneField.requestFocus();
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
