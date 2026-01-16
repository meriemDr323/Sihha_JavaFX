package com.example.hopitalapp.controller.shared;

import com.example.hopitalapp.controller.medecin.MesRendezVousController;
import com.example.hopitalapp.model.RoleEnum;
import com.example.hopitalapp.model.User;
import com.example.hopitalapp.util.AlertUtil;
import com.example.hopitalapp.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private Label userNameLabel;
    @FXML private Label welcomeMessage;
    @FXML private Button logoutButton;
    @FXML private VBox sidebar;
    @FXML private StackPane contentArea;

    private Object currentController;   // <- nouveau
    private static MainController instance; // <- référence statique pour accès global

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this; // <- stocker la référence
        User currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser != null) {
            userNameLabel.setText(currentUser.getFullName() + " (" + currentUser.getRole() + ")");
            welcomeMessage.setText("Vous êtes connecté en tant que " + currentUser.getRole());

            setupSidebarMenu(currentUser.getRole());
            loadDefaultView(currentUser.getRole());
        }
    }

    // Méthode statique pour accéder à l'instance
    public static MainController getInstance() {
        return instance;
    }


    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hopitalapp/view/shared/main.fxml"));
        Parent root = loader.load();

        MainController mainController = loader.getController();

        Scene scene = new Scene(root, 1200, 700);
        scene.setUserData(mainController); // <<< important pour MesPatientsController

        primaryStage.setScene(scene);
        primaryStage.setTitle("Hôpital Sihha");
        primaryStage.setMaximized(false);
        primaryStage.show();
    }


    private void setupSidebarMenu(RoleEnum role) {
        sidebar.getChildren().clear();

        Label menuTitle = new Label("MENU");
        menuTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #757575; -fx-padding: 10 0 10 0;");
        sidebar.getChildren().add(menuTitle);

        switch (role) {
            case ADMINISTRATEUR -> sidebar.getChildren().addAll(
                    createMenuButton("🏠 Dashboard", "admin-dashboard"),
                    createMenuButton("📊 Activités récentes", "activites-recentes"),
                    createMenuButton("🏥 Informations Hôpital", "hopital-info"),
                    createMenuButton("🏢 Départements", "departements"),
                    createMenuButton("👨‍⚕️ Médecins", "medecins"),
                    createMenuButton("👩‍💼 Secrétaires", "secretaires"),
                    createMenuButton("👥 Patients", "patients"),
                    createMenuButton("📅 Rendez-vous", "rendez-vous")
            );
            case MEDECIN -> sidebar.getChildren().addAll(
                    createMenuButton("🏠 Dashboard", "medecin-dashboard"),
                    createMenuButton("📅 Mes Rendez-vous", "mes-rendez-vous"),
                    createMenuButton("👥 Mes Patients", "mes-patients"),
                    createMenuButton("👤 Mon Profil", "mon-profil")
            );
            case SECRETAIRE -> sidebar.getChildren().addAll(
                    createMenuButton("🏠 Dashboard", "secretaire-dashboard"),
                    createMenuButton("👥 Patients", "secretaire-patients"),
                    createMenuButton("📅 Rendez-vous", "secretaire-rendez-vous"),
                    createMenuButton("👤 Mon Profil", "secretaire-profil")
            );
        }
    }

    private Button createMenuButton(String text, String viewId) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-alignment: CENTER_LEFT; " +
                        "-fx-padding: 12 15; " +
                        "-fx-font-size: 14px; " +
                        "-fx-cursor: hand; " +
                        "-fx-background-radius: 5;"
        );

        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: #E3F2FD; " +
                                "-fx-alignment: CENTER_LEFT; " +
                                "-fx-padding: 12 15; " +
                                "-fx-font-size: 14px; " +
                                "-fx-cursor: hand; " +
                                "-fx-background-radius: 5;"
                )
        );

        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: transparent; " +
                                "-fx-alignment: CENTER_LEFT; " +
                                "-fx-padding: 12 15; " +
                                "-fx-font-size: 14px; " +
                                "-fx-cursor: hand; " +
                                "-fx-background-radius: 5;"
                )
        );

        button.setOnAction(e -> loadView(viewId));
        return button;
    }

    private void loadDefaultView(RoleEnum role) {
        switch (role) {
            case ADMINISTRATEUR -> loadView("admin-dashboard");
            case MEDECIN        -> loadView("mes-rendez-vous");
            case SECRETAIRE     -> loadView("secretaire-dashboard");
        }
    }

    public void loadView(String viewId) {
        System.out.println("🔍 DEBUG: Tentative de chargement de la vue: " + viewId);
        try {
            String fxmlPath = switch (viewId) {
                // Admin
                case "admin-dashboard"    -> "/com/example/hopitalapp/view/admin/admin-dashboard.fxml";
                case "activites-recentes" -> "/com/example/hopitalapp/view/admin/activites-recentes.fxml";
                case "hopital-info"       -> "/com/example/hopitalapp/view/admin/hospital-info.fxml";
                case "departements"       -> "/com/example/hopitalapp/view/admin/departements-liste.fxml";
                case "medecins"           -> "/com/example/hopitalapp/view/admin/medecins-liste.fxml";
                case "secretaires"        -> "/com/example/hopitalapp/view/admin/secretaires-liste.fxml";
                case "patients"           -> "/com/example/hopitalapp/view/shared/patients-liste.fxml";
                case "rendez-vous"        -> "/com/example/hopitalapp/view/shared/rendez-vous-liste.fxml";

                // Médecin
                case "medecin-dashboard"  -> "/com/example/hopitalapp/view/medecin/medecin-dashboard.fxml";
                case "mes-rendez-vous"    -> "/com/example/hopitalapp/view/medecin/mes-rendez-vous.fxml";
                case "mes-patients"       -> "/com/example/hopitalapp/view/medecin/mes-patients.fxml";
                case "mon-profil"         -> "/com/example/hopitalapp/view/medecin/mon-profil.fxml";

                // Secrétaire
                case "secretaire-dashboard" -> "/com/example/hopitalapp/view/secretaire/secretaire-dashboard.fxml";
                case "secretaire-patients"  -> "/com/example/hopitalapp/view/secretaire/secretaire-patients.fxml";
                case "secretaire-rendez-vous" -> "/com/example/hopitalapp/view/secretaire/secretaire-rendez-vous.fxml";
                case "secretaire-profil"    -> "/com/example/hopitalapp/view/secretaire/secretaire-profil.fxml";

                default -> null;
            };

            if (fxmlPath != null) {
                System.out.println("✅ DEBUG: Chemin FXML trouvé: " + fxmlPath);
                
                // Vérifier si le fichier existe
                URL resourceUrl = getClass().getResource(fxmlPath);
                if (resourceUrl == null) {
                    System.out.println("❌ DEBUG: Fichier FXML non trouvé: " + fxmlPath);
                    AlertUtil.showError("Erreur", "Fichier de vue non trouvé: " + fxmlPath);
                    return;
                }
                
                System.out.println("✅ DEBUG: Fichier FXML existe: " + resourceUrl);
                
                contentArea.getChildren().clear();
                FXMLLoader loader = new FXMLLoader(resourceUrl);
                Parent view = loader.load();
                currentController = loader.getController();   // mémoriser le contrôleur
                contentArea.getChildren().add(view);
                System.out.println("✅ DEBUG: Vue chargée avec succès: " + viewId);
            } else {
                System.out.println("❌ DEBUG: Aucun chemin FXML trouvé pour: " + viewId);
                AlertUtil.showError("Erreur", "Vue non reconnue: " + viewId);
            }
        } catch (Exception e) {
            System.out.println("❌ DEBUG: Erreur lors du chargement de la vue " + viewId + ": " + e.getMessage());
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible de charger la vue: " + e.getMessage());
        }
    }

    // Méthode publique pour permettre la navigation depuis d'autres contrôleurs
    public void navigateToView(String viewId) {
        loadView(viewId);
    }

    // --- utilisé par MesPatientsController ---
    public void openMesRendezVousAndFilter(String patientFullName) {
        loadView("mes-rendez-vous");
        if (currentController instanceof MesRendezVousController ctrl) {
            ctrl.filterByPatientName(patientFullName);
        }
    }

    @FXML
    public void handleLogout() {
        SessionManager.getInstance().logout();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hopitalapp/view/shared/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root, 450, 600));
            stage.setTitle("Connexion - Hôpital Sihha");
            stage.setResizable(false);
            stage.centerOnScreen();

        } catch (Exception e) {
            System.out.println("❌ DEBUG: Erreur lors de la déconnexion: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
