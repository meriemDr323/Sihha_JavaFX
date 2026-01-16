package com.example.hopitalapp.controller.shared;

import com.example.hopitalapp.dao.impl.UserDAOImpl;
import com.example.hopitalapp.dao.interfaces.UserDAO;
import com.example.hopitalapp.model.User;
import com.example.hopitalapp.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    private UserDAO userDAO = new UserDAOImpl();

    @FXML
    public void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }

        User user = userDAO.authenticate(email, password);

        if (user != null) {
            SessionManager.getInstance().setCurrentUser(user);
            System.out.println("✓ Connexion réussie: " + user.getFullName() + " (" + user.getRole() + ")");
            openMainWindow();
        } else {
            showError("Email ou mot de passe incorrect");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void openMainWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hopitalapp/view/shared/main.fxml"));
            Parent root = loader.load();
            
            MainController mainController = loader.getController();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.setUserData(mainController); // Important pour la navigation entre vues
            stage.setScene(scene);
            stage.setTitle("Hôpital Sihha - " + SessionManager.getInstance().getCurrentUser().getFullName());
            stage.setMaximized(true);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de l'ouverture de l'application: " + e.getMessage());
        }
    }
}
