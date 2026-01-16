package com.example.hopitalapp.controller.admin;

import com.example.hopitalapp.dao.impl.UserDAOImpl;
import com.example.hopitalapp.dao.interfaces.UserDAO;
import com.example.hopitalapp.model.RoleEnum;
import com.example.hopitalapp.model.User;
import com.example.hopitalapp.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class SecretaireFormController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private TextArea adresseArea;
    @FXML private DatePicker dateNaissancePicker;
    @FXML private TextField passwordField;
    @FXML private CheckBox actifCheckBox;
    @FXML private Button saveButton;

    private final UserDAO userDAO = new UserDAOImpl();
    private SecretaireController secretaireController;
    private User currentSecretaire;
    private boolean isEditMode = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configuration par défaut
        actifCheckBox.setSelected(true);
    }

    public void setSecretaireController(SecretaireController controller) {
        this.secretaireController = controller;
    }

    public void setSecretaire(User secretaire) {
        if (secretaire == null) {
            this.currentSecretaire = new User();
            this.isEditMode = false;
            titleLabel.setText("➕ Nouveau Secrétaire");
            saveButton.setText("Créer");
            passwordField.setPromptText("Mot de passe par défaut: secretaire123");
            return;
        }

        this.currentSecretaire = secretaire;
        this.isEditMode = true;

        titleLabel.setText("✏️ Modifier Secrétaire");
        saveButton.setText("Mettre à jour");

        // Remplir les champs
        nomField.setText(secretaire.getNom());
        prenomField.setText(secretaire.getPrenom());
        emailField.setText(secretaire.getEmail());
        telephoneField.setText(secretaire.getTelephone());
        adresseArea.setText(secretaire.getAdresse());
        passwordField.setText(secretaire.getPassword());
        actifCheckBox.setSelected(secretaire.isActif());
        
        if (secretaire.getDateNaissance() != null) {
            dateNaissancePicker.setValue(secretaire.getDateNaissance());
        }
    }

    @FXML
    private void handleSave() {
        if (!validateFields()) {
            return;
        }

        User secretaire = isEditMode ? currentSecretaire : new User();

        secretaire.setNom(safeText(nomField));
        secretaire.setPrenom(safeText(prenomField));
        secretaire.setEmail(safeText(emailField));
        secretaire.setTelephone(safeText(telephoneField));
        secretaire.setAdresse(safeTextArea(adresseArea));
        secretaire.setDateNaissance(dateNaissancePicker.getValue());
        secretaire.setActif(actifCheckBox.isSelected());
        secretaire.setRole(RoleEnum.SECRETAIRE);

        // Gestion du mot de passe
        String password = safeText(passwordField);
        if (password.isEmpty()) {
            password = "secretaire123"; // Mot de passe par défaut
        }
        secretaire.setPassword(password);

        try {
            if (isEditMode) {
                userDAO.update(secretaire);
                AlertUtil.showSuccess("Succès", "Secrétaire modifié avec succès!");
            } else {
                userDAO.save(secretaire);
                AlertUtil.showSuccess("Succès", "Secrétaire ajouté avec succès!\nMot de passe: " + password);
            }

            if (secretaireController != null) {
                secretaireController.refreshTable();
            }

            closeWindow();

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible de sauvegarder: " + e.getMessage());
        }
    }

    private boolean validateFields() {
        if (safeText(nomField).isEmpty()) {
            AlertUtil.showError("Validation", "Le nom est obligatoire!");
            nomField.requestFocus();
            return false;
        }

        if (safeText(prenomField).isEmpty()) {
            AlertUtil.showError("Validation", "Le prénom est obligatoire!");
            prenomField.requestFocus();
            return false;
        }

        if (safeText(emailField).isEmpty()) {
            AlertUtil.showError("Validation", "L'email est obligatoire!");
            emailField.requestFocus();
            return false;
        }

        String email = safeText(emailField);
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            AlertUtil.showError("Validation", "Format d'email invalide!");
            emailField.requestFocus();
            return false;
        }

        // Vérifier l'unicité de l'email (sauf en mode édition pour le même utilisateur)
        if (!isEditMode || !email.equals(currentSecretaire.getEmail())) {
            User existingUser = userDAO.findByEmail(email);
            if (existingUser != null) {
                AlertUtil.showError("Validation", "Cet email est déjà utilisé!");
                emailField.requestFocus();
                return false;
            }
        }

        return true;
    }

    private String safeText(TextField field) {
        return field.getText() != null ? field.getText().trim() : "";
    }

    private String safeTextArea(TextArea area) {
        return area.getText() != null ? area.getText().trim() : "";
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