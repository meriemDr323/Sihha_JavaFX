package com.example.hopitalapp.controller.admin;

import com.example.hopitalapp.controller.shared.MainController;
import com.example.hopitalapp.dao.impl.DepartementDAOImpl;
import com.example.hopitalapp.dao.impl.MedecinDAOImpl;
import com.example.hopitalapp.dao.impl.RendezVousDAOImpl;
import com.example.hopitalapp.dao.impl.UserDAOImpl;
import com.example.hopitalapp.dao.interfaces.DepartementDAO;
import com.example.hopitalapp.dao.interfaces.MedecinDAO;
import com.example.hopitalapp.dao.interfaces.PatientDAO;
import com.example.hopitalapp.dao.impl.PatientDAOImpl;
import com.example.hopitalapp.dao.interfaces.RendezVousDAO;
import com.example.hopitalapp.dao.interfaces.UserDAO;
import com.example.hopitalapp.model.RendezVous;
import com.example.hopitalapp.model.RoleEnum;
import com.example.hopitalapp.model.User;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML private Label patientsCountLabel;
    @FXML private Label medecinsCountLabel;
    @FXML private Label departementsCountLabel;
    @FXML private Label rdvCountLabel;
    @FXML private VBox activityContainer;

    private final PatientDAO patientDAO = new PatientDAOImpl();
    private final MedecinDAO medecinDAO = new MedecinDAOImpl();
    private final DepartementDAO departementDAO = new DepartementDAOImpl();
    private final RendezVousDAO rendezVousDAO = new RendezVousDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadStats();
        loadRecentActivity();
    }

    private void loadStats() {
        long patients = patientDAO.count();
        long medecins = medecinDAO.count();
        long departements = departementDAO.count();
        long rdv = rendezVousDAO.count();

        patientsCountLabel.setText(String.valueOf(patients));
        medecinsCountLabel.setText(String.valueOf(medecins));
        departementsCountLabel.setText(String.valueOf(departements));
        rdvCountLabel.setText(String.valueOf(rdv));
    }

    private void loadRecentActivity() {
        if (activityContainer == null) return;

        activityContainer.getChildren().clear();
        
        try {
            List<ActivityItem> activities = new ArrayList<>();
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            
            // Récents utilisateurs créés (derniers 7 jours)
            List<User> recentUsers = userDAO.findAll().stream()
                    .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(sevenDaysAgo))
                    .sorted((u1, u2) -> u2.getCreatedAt().compareTo(u1.getCreatedAt()))
                    .limit(10)
                    .toList();

            for (User user : recentUsers) {
                String icon = switch (user.getRole()) {
                    case MEDECIN -> "👨‍⚕️";
                    case SECRETAIRE -> "👩‍💼";
                    case ADMINISTRATEUR -> "👨‍💻";
                };
                String roleText = switch (user.getRole()) {
                    case MEDECIN -> "Médecin";
                    case SECRETAIRE -> "Secrétaire";
                    case ADMINISTRATEUR -> "Admin";
                };
                
                activities.add(new ActivityItem(
                    icon,
                    roleText + " ajouté",
                    user.getPrenom() + " " + user.getNom(),
                    user.getCreatedAt(),
                    "#4CAF50"
                ));
            }

            // Récents rendez-vous (derniers 7 jours)
            List<RendezVous> recentRdv = rendezVousDAO.findAll().stream()
                    .filter(r -> r.getDateRdv() != null && r.getDateRdv().isAfter(sevenDaysAgo))
                    .sorted((r1, r2) -> r2.getDateRdv().compareTo(r1.getDateRdv()))
                    .limit(10)
                    .toList();

            for (RendezVous rdv : recentRdv) {
                String statusColor = switch (rdv.getStatut()) {
                    case "EN_ATTENTE" -> "#FF9800";
                    case "CONFIRME" -> "#2196F3";
                    case "TERMINE" -> "#4CAF50";
                    case "ANNULE" -> "#F44336";
                    default -> "#757575";
                };
                
                activities.add(new ActivityItem(
                    "📅",
                    "Rendez-vous " + rdv.getStatut().toLowerCase(),
                    rdv.getPatientNomComplet() != null ? rdv.getPatientNomComplet() : "Patient",
                    rdv.getDateRdv(),
                    statusColor
                ));
            }

            // Trier toutes les activités par date
            activities.sort(Comparator.comparing(ActivityItem::getDateTime).reversed());
            
            // Limiter à 15 activités
            activities = activities.stream().limit(15).toList();

            if (activities.isEmpty()) {
                Label emptyLabel = new Label("Aucune activité récente");
                emptyLabel.setStyle("-fx-text-fill: #90A4AE; -fx-font-style: italic; -fx-padding: 20;");
                activityContainer.getChildren().add(emptyLabel);
            } else {
                for (ActivityItem activity : activities) {
                    activityContainer.getChildren().add(createActivityCard(activity));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Label errorLabel = new Label("Erreur lors du chargement de l'activité");
            errorLabel.setStyle("-fx-text-fill: #F44336; -fx-padding: 20;");
            activityContainer.getChildren().add(errorLabel);
        }
    }

    private VBox createActivityCard(ActivityItem activity) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: #FAFAFA; -fx-background-radius: 8; -fx-padding: 12; -fx-border-color: #E0E0E0; -fx-border-radius: 8; -fx-border-width: 1;");
        
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label iconLabel = new Label(activity.getIcon());
        iconLabel.setStyle("-fx-font-size: 16px;");
        
        VBox content = new VBox(2);
        HBox.setHgrow(content, Priority.ALWAYS);
        
        Label titleLabel = new Label(activity.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #37474F; -fx-font-size: 13px;");
        
        Label descLabel = new Label(activity.getDescription());
        descLabel.setStyle("-fx-text-fill: #616161; -fx-font-size: 12px;");
        
        content.getChildren().addAll(titleLabel, descLabel);
        
        VBox timeBox = new VBox();
        timeBox.setAlignment(Pos.CENTER_RIGHT);
        
        Label timeLabel = new Label(getRelativeTime(activity.getDateTime()));
        timeLabel.setStyle("-fx-text-fill: #90A4AE; -fx-font-size: 11px;");
        
        Label statusDot = new Label("●");
        statusDot.setStyle("-fx-text-fill: " + activity.getColor() + "; -fx-font-size: 14px;");
        
        timeBox.getChildren().addAll(statusDot, timeLabel);
        
        header.getChildren().addAll(iconLabel, content, timeBox);
        card.getChildren().add(header);
        
        return card;
    }

    private String getRelativeTime(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        long days = ChronoUnit.DAYS.between(dateTime, now);
        
        if (minutes < 1) return "À l'instant";
        if (minutes < 60) return minutes + "min";
        if (hours < 24) return hours + "h";
        if (days < 7) return days + "j";
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM"));
    }

    @FXML
    private void handleRefreshActivity() {
        System.out.println("🔄 DEBUG: Actualisation de l'activité récente");
        loadRecentActivity();
    }

    @FXML
    private void handleViewAllActivity() {
        System.out.println("📋 DEBUG: Tentative de navigation vers activites-recentes");
        // Naviguer vers la page complète des activités
        try {
            // Essayer plusieurs méthodes pour récupérer le MainController
            MainController mainController = null;
            
            // Méthode 1: Via la scène actuelle
            if (activityContainer != null && activityContainer.getScene() != null) {
                Object userData = activityContainer.getScene().getUserData();
                System.out.println("🔍 DEBUG: UserData de la scène: " + userData);
                if (userData instanceof MainController) {
                    mainController = (MainController) userData;
                    System.out.println("✅ DEBUG: MainController trouvé via scène");
                }
            }
            
            // Méthode 2: Via la fenêtre principale (fallback)
            if (mainController == null && activityContainer != null && activityContainer.getScene() != null) {
                Object windowUserData = activityContainer.getScene().getWindow().getUserData();
                System.out.println("🔍 DEBUG: UserData de la fenêtre: " + windowUserData);
                if (windowUserData instanceof MainController) {
                    mainController = (MainController) windowUserData;
                    System.out.println("✅ DEBUG: MainController trouvé via fenêtre");
                }
            }
            
            // Méthode 3: Via la référence statique (fallback final)
            if (mainController == null) {
                mainController = MainController.getInstance();
                if (mainController != null) {
                    System.out.println("✅ DEBUG: MainController trouvé via référence statique");
                } else {
                    System.out.println("❌ DEBUG: Référence statique MainController est null");
                }
            }
            
            if (mainController != null) {
                System.out.println("✅ DEBUG: MainController trouvé, navigation vers activites-recentes");
                mainController.loadView("activites-recentes");
            } else {
                System.out.println("❌ DEBUG: MainController non trouvé, affichage du message d'info");
                com.example.hopitalapp.util.AlertUtil.showInfo("Navigation", 
                    "Utilisez le menu '📊 Activités récentes' dans la barre latérale pour accéder à la page complète.");
            }
        } catch (Exception e) {
            System.out.println("❌ DEBUG: Erreur lors de la navigation: " + e.getMessage());
            e.printStackTrace();
            com.example.hopitalapp.util.AlertUtil.showInfo("Navigation", 
                "Utilisez le menu '📊 Activités récentes' dans la barre latérale pour accéder à la page complète.");
        }
    }

    // Classe interne pour représenter une activité
    private static class ActivityItem {
        private final String icon;
        private final String title;
        private final String description;
        private final LocalDateTime dateTime;
        private final String color;

        public ActivityItem(String icon, String title, String description, LocalDateTime dateTime, String color) {
            this.icon = icon;
            this.title = title;
            this.description = description;
            this.dateTime = dateTime;
            this.color = color;
        }

        public String getIcon() { return icon; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public LocalDateTime getDateTime() { return dateTime; }
        public String getColor() { return color; }
    }
}