package com.example.hopitalapp.controller.medecin;

import com.example.hopitalapp.dao.impl.MedecinDAOImpl;
import com.example.hopitalapp.dao.impl.UserDAOImpl;
import com.example.hopitalapp.model.Medecin;
import com.example.hopitalapp.model.User;
import com.example.hopitalapp.util.AlertUtil;
import com.example.hopitalapp.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class MonProfilController implements Initializable {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private TextArea adresseArea;
    @FXML private TextField specialiteField;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button changePasswordButton;

    private User currentUser;
    private Medecin currentMedecin;

    private final UserDAOImpl userDAO = new UserDAOImpl();
    private final MedecinDAOImpl medecinDAO = new MedecinDAOImpl();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        currentMedecin = medecinDAO.findByUserId(currentUser.getId());

        loadData();
    }

    private void loadData() {
        nomField.setText(currentUser.getNom());
        prenomField.setText(currentUser.getPrenom());
        emailField.setText(currentUser.getEmail());
        telephoneField.setText(currentUser.getTelephone());
        adresseArea.setText(currentUser.getAdresse());

        if (currentMedecin != null) {
            specialiteField.setText(currentMedecin.getSpecialite());
        }

        // Rendre tous les champs en lecture seule sauf les mots de passe
        nomField.setEditable(false);
        prenomField.setEditable(false);
        emailField.setEditable(false);
        telephoneField.setEditable(false);
        adresseArea.setEditable(false);
        specialiteField.setEditable(false);
        
        // Style pour indiquer que c'est en lecture seule
        String readOnlyStyle = "-fx-background-color: #f5f5f5; -fx-opacity: 0.8;";
        nomField.setStyle(readOnlyStyle);
        prenomField.setStyle(readOnlyStyle);
        emailField.setStyle(readOnlyStyle);
        telephoneField.setStyle(readOnlyStyle);
        adresseArea.setStyle(readOnlyStyle);
        specialiteField.setStyle(readOnlyStyle);
    }

    @FXML
    private void handleClearPasswordFields() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    @FXML
    private void handleChangePassword() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validation
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            AlertUtil.showError("Erreur", "Tous les champs de mot de passe sont obligatoires.");
            return;
        }

        if (!currentUser.getPassword().equals(currentPassword)) {
            AlertUtil.showError("Erreur", "Le mot de passe actuel est incorrect.");
            return;
        }

        if (newPassword.length() < 6) {
            AlertUtil.showError("Erreur", "Le nouveau mot de passe doit contenir au moins 6 caractères.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            AlertUtil.showError("Erreur", "La confirmation du mot de passe ne correspond pas.");
            return;
        }

        if (currentPassword.equals(newPassword)) {
            AlertUtil.showError("Erreur", "Le nouveau mot de passe doit être différent de l'ancien.");
            return;
        }

        // Mettre à jour le mot de passe
        currentUser.setPassword(newPassword);
        userDAO.update(currentUser);
        
        // Vider les champs
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();

        AlertUtil.showSuccess("Succès", "Mot de passe modifié avec succès.");
    }
}
