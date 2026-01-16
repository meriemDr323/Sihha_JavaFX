package com.example.hopitalapp.controller.admin;

import com.example.hopitalapp.dao.impl.MedecinDAOImpl;
import com.example.hopitalapp.dao.interfaces.MedecinDAO;
import com.example.hopitalapp.model.Medecin;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.ResourceBundle;

public class MedecinController implements Initializable {

    @FXML private TableView<Medecin> medecinsTable;

    @FXML private TableColumn<Medecin, Integer> idColumn;
    @FXML private TableColumn<Medecin, Void>    imageColumn;
    @FXML private TableColumn<Medecin, String>  nomColumn;
    @FXML private TableColumn<Medecin, String>  prenomColumn;
    @FXML private TableColumn<Medecin, String>  specialiteColumn;
    @FXML private TableColumn<Medecin, String>  departementColumn;
    @FXML private TableColumn<Medecin, String>  telephoneColumn;
    @FXML private TableColumn<Medecin, String>  emailColumn;
    @FXML private TableColumn<Medecin, String>  passwordColumn;
    @FXML private TableColumn<Medecin, Integer> experienceColumn;
    @FXML private TableColumn<Medecin, String>  tarifColumn;
    @FXML private TableColumn<Medecin, String>  statutColumn;
    @FXML private TableColumn<Medecin, Void>    actionsColumn;

    @FXML private TextField searchField;
    @FXML private Label totalMedecinsLabel;
    @FXML private Button addButton;

    private final MedecinDAO medecinDAO = new MedecinDAOImpl();
    private final ObservableList<Medecin> medecinsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Autoriser les colonnes à dépasser la largeur de la table => scroll horizontal
        medecinsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        setupTableColumns();
        loadMedecins();
        updateStatistics();
    }

    private void setupTableColumns() {
        // Colonnes simples
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        specialiteColumn.setCellValueFactory(new PropertyValueFactory<>("specialite"));
        departementColumn.setCellValueFactory(new PropertyValueFactory<>("departementNom"));
        telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        passwordColumn.setCellValueFactory(new PropertyValueFactory<>("password"));
        experienceColumn.setCellValueFactory(new PropertyValueFactory<>("anneesExperience"));

        // Colonne statut texte clair
        statutColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isActif() ? "Actif" : "Inactif")
        );

        // Colonne Image avec affichage
        imageColumn.setCellFactory(param -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitHeight(50);
                imageView.setFitWidth(50);
                imageView.setPreserveRatio(true);
                imageView.setStyle("-fx-background-radius: 25; -fx-border-radius: 25;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Medecin medecin = getTableView().getItems().get(getIndex());
                    if (medecin.getImage() != null && !medecin.getImage().isEmpty()) {
                        try {
                            Image image = new Image(medecin.getImage(), true);
                            imageView.setImage(image);
                            setGraphic(imageView);
                        } catch (Exception e) {
                            setGraphic(createDefaultAvatar());
                        }
                    } else {
                        setGraphic(createDefaultAvatar());
                    }
                }
            }

            private VBox createDefaultAvatar() {
                VBox avatar = new VBox();
                avatar.setPrefSize(50, 50);
                avatar.setAlignment(Pos.CENTER);
                avatar.setStyle("-fx-background-color: #E3F2FD; -fx-background-radius: 25;");
                Label label = new Label("👨‍⚕️");
                label.setStyle("-fx-font-size: 24px;");
                avatar.getChildren().add(label);
                return avatar;
            }
        });

        // Colonne tarif formatée
        tarifColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getTarifConsultation() != null) {
                DecimalFormat df = new DecimalFormat("#,##0.00");
                return new SimpleStringProperty(df.format(cellData.getValue().getTarifConsultation()) + " DH");
            }
            return new SimpleStringProperty("N/A");
        });

        // Colonne actions avec boutons
        actionsColumn.setCellFactory(param -> new TableCell<Medecin, Void>() {
            private final Button viewButton = new Button("Voir");
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");

            {
                viewButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 8; -fx-background-radius: 3;");
                editButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 8; -fx-background-radius: 3;");
                deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 8; -fx-background-radius: 3;");

                viewButton.setOnAction(event -> {
                    Medecin medecin = getTableView().getItems().get(getIndex());
                    handleViewMedecin(medecin);
                });
                editButton.setOnAction(event -> {
                    Medecin medecin = getTableView().getItems().get(getIndex());
                    handleEditMedecin(medecin);
                });
                deleteButton.setOnAction(event -> {
                    Medecin medecin = getTableView().getItems().get(getIndex());
                    handleDeleteMedecin(medecin);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, viewButton, editButton, deleteButton);
                    buttons.setAlignment(Pos.CENTER);
                    setGraphic(buttons);
                }
            }
        });
    }

    private void loadMedecins() {
        medecinsList.clear();
        List<Medecin> medecins = medecinDAO.findAll();
        System.out.println(medecins.size() + " médecins chargés");
        medecinsList.addAll(medecins);
        medecinsTable.setItems(medecinsList);
    }

    private void updateStatistics() {
        long total = medecinDAO.count();
        totalMedecinsLabel.setText(String.valueOf(total));
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();

        if (keyword.isEmpty()) {
            loadMedecins();
            return;
        }

        medecinsList.clear();
        List<Medecin> results = medecinDAO.searchByName(keyword);
        medecinsList.addAll(results);
        medecinsTable.setItems(medecinsList);
        System.out.println("✓ Recherche: " + results.size() + " résultat(s) pour '" + keyword + "'");
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        loadMedecins();
        updateStatistics();
        AlertUtil.showInfo("Actualisation", "Liste des médecins actualisée avec succès!");
    }

    @FXML
    private void handleAddMedecin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hopitalapp/view/admin/medecin-form.fxml"));
            Parent root = loader.load();

            MedecinFormController controller = loader.getController();
            controller.setMedecinController(this);

            Stage stage = new Stage();
            stage.setTitle("Nouveau Médecin");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void handleViewMedecin(Medecin medecin) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        String info = "👨‍⚕️ INFORMATIONS DU MÉDECIN\n\n" +
                "Nom complet: " + medecin.getFullName() + "\n" +
                "Spécialité: " + (medecin.getSpecialite() != null ? medecin.getSpecialite() : "N/A") + "\n" +
                "Département: " + (medecin.getDepartementNom() != null ? medecin.getDepartementNom() : "N/A") + "\n" +
                "N° Ordre: " + (medecin.getNumeroOrdre() != null ? medecin.getNumeroOrdre() : "N/A") + "\n" +
                "Téléphone: " + (medecin.getTelephone() != null ? medecin.getTelephone() : "N/A") + "\n" +
                "Email: " + (medecin.getEmail() != null ? medecin.getEmail() : "N/A") + "\n" +
                "Années d'expérience: " + (medecin.getAnneesExperience() != null ? medecin.getAnneesExperience() + " ans" : "N/A") + "\n" +
                "Tarif consultation: " + (medecin.getTarifConsultation() != null ? df.format(medecin.getTarifConsultation()) + " DH" : "N/A") + "\n" +
                "Statut: " + medecin.getStatutText() + "\n" +
                "Diplômes: " + (medecin.getDiplomes() != null ? medecin.getDiplomes() : "N/A");

        AlertUtil.showInfo("Détails du Médecin", info);
    }

    private void handleEditMedecin(Medecin medecin) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hopitalapp/view/admin/medecin-form.fxml"));
            Parent root = loader.load();

            MedecinFormController controller = loader.getController();
            controller.setMedecinController(this);
            controller.setMedecin(medecin);

            Stage stage = new Stage();
            stage.setTitle("Modifier Médecin - " + medecin.getFullName());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void handleDeleteMedecin(Medecin medecin) {
        boolean confirm = AlertUtil.showConfirmation(
                "Confirmation de suppression",
                "Êtes-vous sûr de vouloir supprimer le médecin:\n" + medecin.getFullName() + " ?"
        );

        if (confirm) {
            medecinDAO.delete(medecin.getId());
            loadMedecins();
            updateStatistics();
            AlertUtil.showSuccess("Succès", "Médecin supprimé avec succès!");
        }
    }

    public void refreshTable() {
        loadMedecins();
        updateStatistics();
    }
}
