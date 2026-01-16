package com.example.hopitalapp.controller.admin;

import com.example.hopitalapp.dao.impl.UserDAOImpl;
import com.example.hopitalapp.dao.interfaces.UserDAO;
import com.example.hopitalapp.model.RoleEnum;
import com.example.hopitalapp.model.User;
import com.example.hopitalapp.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class SecretaireController implements Initializable {

    @FXML private TableView<User> secretairesTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> nomColumn;
    @FXML private TableColumn<User, String> prenomColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> telephoneColumn;
    @FXML private TableColumn<User, String> adresseColumn;
    @FXML private TableColumn<User, String> passwordColumn;
    @FXML private TableColumn<User, String> statutColumn;
    @FXML private TableColumn<User, Void> actionsColumn;

    @FXML private TextField searchField;
    @FXML private Label totalSecretairesLabel;
    @FXML private Button addButton;

    private final UserDAO userDAO = new UserDAOImpl();
    private final ObservableList<User> secretairesList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadSecretaires();
        updateStatistics();
        setupSearch();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        adresseColumn.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        
        // Colonne mot de passe (visible pour l'admin)
        passwordColumn.setCellValueFactory(new PropertyValueFactory<>("password"));
        
        // Colonne statut
        statutColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isActif() ? "Actif" : "Inactif")
        );

        // Colonne actions
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("✏️ Modifier");
            private final Button deleteButton = new Button("🗑️ Supprimer");

            {
                editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 4 8; -fx-background-radius: 3;");
                deleteButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 4 8; -fx-background-radius: 3;");
                
                editButton.setOnAction(e -> {
                    User secretaire = getTableView().getItems().get(getIndex());
                    handleEditSecretaire(secretaire);
                });
                
                deleteButton.setOnAction(e -> {
                    User secretaire = getTableView().getItems().get(getIndex());
                    handleDeleteSecretaire(secretaire);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(5, editButton, deleteButton);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });

        secretairesTable.setItems(secretairesList);
    }

    private void loadSecretaires() {
        secretairesList.clear();
        List<User> secretaires = userDAO.findAll().stream()
                .filter(user -> user.getRole() == RoleEnum.SECRETAIRE)
                .collect(Collectors.toList());
        secretairesList.addAll(secretaires);
    }

    private void updateStatistics() {
        totalSecretairesLabel.setText("Total: " + secretairesList.size() + " secrétaire(s)");
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, old, value) -> {
            String filter = value == null ? "" : value.trim().toLowerCase();
            if (filter.isEmpty()) {
                secretairesTable.setItems(secretairesList);
            } else {
                List<User> filtered = secretairesList.stream()
                        .filter(s ->
                                (s.getNom() != null && s.getNom().toLowerCase().contains(filter)) ||
                                (s.getPrenom() != null && s.getPrenom().toLowerCase().contains(filter)) ||
                                (s.getEmail() != null && s.getEmail().toLowerCase().contains(filter)))
                        .collect(Collectors.toList());
                secretairesTable.setItems(FXCollections.observableArrayList(filtered));
            }
        });
    }

    @FXML
    private void handleAddSecretaire() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hopitalapp/view/admin/secretaire-form.fxml"));
            Parent root = loader.load();

            SecretaireFormController controller = loader.getController();
            controller.setSecretaireController(this);

            Stage stage = new Stage();
            stage.setTitle("Nouveau Secrétaire");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void handleEditSecretaire(User secretaire) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hopitalapp/view/admin/secretaire-form.fxml"));
            Parent root = loader.load();

            SecretaireFormController controller = loader.getController();
            controller.setSecretaireController(this);
            controller.setSecretaire(secretaire);

            Stage stage = new Stage();
            stage.setTitle("Modifier Secrétaire");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void handleDeleteSecretaire(User secretaire) {
        boolean confirm = AlertUtil.showConfirmation(
                "Confirmation de suppression",
                "Êtes-vous sûr de vouloir supprimer ce secrétaire ?\n\n" +
                        "Nom: " + secretaire.getFullName() + "\n" +
                        "Email: " + secretaire.getEmail() + "\n\n" +
                        "Cette action est irréversible."
        );

        if (confirm) {
            try {
                userDAO.delete(secretaire.getId());
                refreshTable();
                AlertUtil.showSuccess("Succès", "Secrétaire supprimé avec succès!");
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showError("Erreur", "Impossible de supprimer le secrétaire: " + e.getMessage());
            }
        }
    }

    public void refreshTable() {
        loadSecretaires();
        updateStatistics();
    }
}