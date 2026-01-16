package com.example.hopitalapp.controller.admin;

import com.example.hopitalapp.dao.impl.UserDAOImpl;
import com.example.hopitalapp.dao.interfaces.UserDAO;
import com.example.hopitalapp.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ActivitesRecentesController implements Initializable {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> typeFilter;
    @FXML private TableView<SimpleActivity> activitesTable;
    @FXML private TableColumn<SimpleActivity, String> typeColumn;
    @FXML private TableColumn<SimpleActivity, String> descriptionColumn;
    @FXML private TableColumn<SimpleActivity, String> utilisateurColumn;
    @FXML private TableColumn<SimpleActivity, String> dateColumn;
    @FXML private TableColumn<SimpleActivity, String> statutColumn;
    @FXML private Label totalActivitesLabel;

    private final ObservableList<SimpleActivity> allActivitesList = FXCollections.observableArrayList();
    private final ObservableList<SimpleActivity> filteredActivitesList = FXCollections.observableArrayList();
    private final UserDAO userDAO = new UserDAOImpl();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("✅ DEBUG: ActivitesRecentesController initialisé");
        
        try {
            setupSimpleTable();
            setupSimpleFilters();
            setupSearch();
            loadSimpleActivites();
            System.out.println("✅ DEBUG: ActivitesRecentesController complètement initialisé");
        } catch (Exception e) {
            System.out.println("❌ DEBUG: Erreur lors de l'initialisation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupSimpleTable() {
        System.out.println("🔧 DEBUG: Configuration de la table");
        
        if (typeColumn != null) typeColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getType()));
        if (descriptionColumn != null) descriptionColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDescription()));
        if (utilisateurColumn != null) utilisateurColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getUtilisateur()));
        if (dateColumn != null) dateColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDateFormatted()));
        if (statutColumn != null) statutColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatut()));

        if (activitesTable != null) {
            activitesTable.setItems(filteredActivitesList);
        }
    }

    private void setupSimpleFilters() {
        System.out.println("🔧 DEBUG: Configuration des filtres");
        
        if (typeFilter != null) {
            typeFilter.getItems().addAll("Tous", "Médecin", "Secrétaire", "Admin");
            typeFilter.setValue("Tous");
            typeFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }
    }

    private void setupSearch() {
        System.out.println("🔧 DEBUG: Configuration de la recherche");
        
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }
    }

    private void loadSimpleActivites() {
        System.out.println("📊 DEBUG: Chargement des activités");
        
        allActivitesList.clear();

        // Charger les vrais utilisateurs
        try {
            System.out.println("🔍 DEBUG: Chargement des utilisateurs...");
            List<User> users = userDAO.findAll();
            System.out.println("✅ DEBUG: " + users.size() + " utilisateurs trouvés");
            
            for (User user : users) {
                if (user.getCreatedAt() != null) {
                    String type = "Utilisateur";
                    if (user.getRole() != null) {
                        switch (user.getRole()) {
                            case MEDECIN:
                                type = "Médecin";
                                break;
                            case SECRETAIRE:
                                type = "Secrétaire";
                                break;
                            case ADMINISTRATEUR:
                                type = "Admin";
                                break;
                        }
                    }

                    allActivitesList.add(new SimpleActivity(
                        type,
                        "Utilisateur ajouté au système",
                        user.getPrenom() + " " + user.getNom(),
                        user.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                        "ACTIF"
                    ));
                }
            }
            
        } catch (Exception e) {
            System.out.println("⚠️ DEBUG: Erreur lors du chargement des utilisateurs: " + e.getMessage());
            e.printStackTrace();
        }

        // Appliquer les filtres pour remplir la liste filtrée
        applyFilters();
        
        System.out.println("✅ DEBUG: " + allActivitesList.size() + " activités ajoutées au total");
    }

    private void applyFilters() {
        System.out.println("🔍 DEBUG: Application des filtres");
        
        String searchText = searchField != null && searchField.getText() != null ? 
                           searchField.getText().trim().toLowerCase() : "";
        String typeValue = typeFilter != null ? typeFilter.getValue() : "Tous";

        filteredActivitesList.clear();
        
        for (SimpleActivity activity : allActivitesList) {
            // Filtre par recherche textuelle
            boolean matchesSearch = searchText.isEmpty() ||
                    activity.getDescription().toLowerCase().contains(searchText) ||
                    activity.getUtilisateur().toLowerCase().contains(searchText) ||
                    activity.getType().toLowerCase().contains(searchText);

            // Filtre par type
            boolean matchesType = "Tous".equals(typeValue) || typeValue.equals(activity.getType());

            if (matchesSearch && matchesType) {
                filteredActivitesList.add(activity);
            }
        }
        
        if (totalActivitesLabel != null) {
            totalActivitesLabel.setText("Total: " + filteredActivitesList.size() + " activité(s)");
        }
        
        System.out.println("✅ DEBUG: " + filteredActivitesList.size() + " activités après filtrage");
    }

    @FXML
    private void handleRefresh() {
        System.out.println("🔄 DEBUG: Actualisation des activités");
        loadSimpleActivites();
    }

    @FXML
    private void handleClearFilters() {
        System.out.println("🗑️ DEBUG: Effacement des filtres");
        if (searchField != null) searchField.clear();
        if (typeFilter != null) typeFilter.setValue("Tous");
    }

    @FXML
    private void handleExport() {
        System.out.println("📤 DEBUG: Export demandé");
        com.example.hopitalapp.util.AlertUtil.showInfo("Export", "Fonctionnalité d'export");
    }

    // Classe pour les activités
    public static class SimpleActivity {
        private final String type;
        private final String description;
        private final String utilisateur;
        private final String dateFormatted;
        private final String statut;

        public SimpleActivity(String type, String description, String utilisateur, String dateFormatted, String statut) {
            this.type = type;
            this.description = description;
            this.utilisateur = utilisateur;
            this.dateFormatted = dateFormatted;
            this.statut = statut;
        }

        public String getType() { return type; }
        public String getDescription() { return description; }
        public String getUtilisateur() { return utilisateur; }
        public String getDateFormatted() { return dateFormatted; }
        public String getStatut() { return statut; }
    }
}