package com.example.hopitalapp.controller.secretaire;

import com.example.hopitalapp.dao.impl.MedecinDAOImpl;
import com.example.hopitalapp.dao.impl.PatientDAOImpl;
import com.example.hopitalapp.dao.impl.RendezVousDAOImpl;
import com.example.hopitalapp.dao.interfaces.MedecinDAO;
import com.example.hopitalapp.dao.interfaces.PatientDAO;
import com.example.hopitalapp.dao.interfaces.RendezVousDAO;
import com.example.hopitalapp.model.Medecin;
import com.example.hopitalapp.model.Patient;
import com.example.hopitalapp.model.RendezVous;
import com.example.hopitalapp.util.AlertUtil;
import com.example.hopitalapp.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class SecretaireRendezVousController implements Initializable {

    @FXML private TableView<RendezVous> rendezVousTable;
    @FXML private TableColumn<RendezVous, Integer> idColumn;
    @FXML private TableColumn<RendezVous, String> dateColumn;
    @FXML private TableColumn<RendezVous, String> heureColumn;
    @FXML private TableColumn<RendezVous, String> patientColumn;
    @FXML private TableColumn<RendezVous, String> medecinColumn;
    @FXML private TableColumn<RendezVous, String> motifColumn;
    @FXML private TableColumn<RendezVous, String> statutColumn;
    @FXML private TableColumn<RendezVous, Integer> dureeColumn;
    @FXML private TableColumn<RendezVous, Void> actionsColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterStatutComboBox;
    @FXML private ComboBox<Medecin> filterMedecinComboBox;
    @FXML private DatePicker filterDatePicker;
    @FXML private Label totalRendezVousLabel;
    @FXML private Button addButton;

    private final RendezVousDAO rendezVousDAO = new RendezVousDAOImpl();
    private final PatientDAO patientDAO = new PatientDAOImpl();
    private final MedecinDAO medecinDAO = new MedecinDAOImpl();
    private final ObservableList<RendezVous> rendezVousList = FXCollections.observableArrayList();
    private FilteredList<RendezVous> filteredRendezVous;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rendezVousTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        
        setupTableColumns();
        setupFilters();
        loadRendezVous();
        updateStatistics();
    }

    private void setupTableColumns() {
        // Colonnes simples
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        patientColumn.setCellValueFactory(new PropertyValueFactory<>("patientNomComplet"));
        medecinColumn.setCellValueFactory(new PropertyValueFactory<>("medecinNomComplet"));
        motifColumn.setCellValueFactory(new PropertyValueFactory<>("motif"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        dureeColumn.setCellValueFactory(new PropertyValueFactory<>("duree"));

        // Date formatée
        dateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDateRdv() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                return new SimpleStringProperty(cellData.getValue().getDateRdv().format(formatter));
            }
            return new SimpleStringProperty("");
        });

        // Heure formatée
        heureColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDateRdv() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                return new SimpleStringProperty(cellData.getValue().getDateRdv().format(formatter));
            }
            return new SimpleStringProperty("");
        });

        // Colonne actions
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("Voir");
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");

            {
                viewButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 8; -fx-background-radius: 3; -fx-cursor: hand;");
                editButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 8; -fx-background-radius: 3; -fx-cursor: hand;");
                deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 8; -fx-background-radius: 3; -fx-cursor: hand;");

                viewButton.setOnAction(event -> {
                    RendezVous rdv = getTableView().getItems().get(getIndex());
                    handleViewRendezVous(rdv);
                });

                editButton.setOnAction(event -> {
                    RendezVous rdv = getTableView().getItems().get(getIndex());
                    handleEditRendezVous(rdv);
                });

                deleteButton.setOnAction(event -> {
                    RendezVous rdv = getTableView().getItems().get(getIndex());
                    handleDeleteRendezVous(rdv);
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

    private void setupFilters() {
        // ComboBox filtres
        filterStatutComboBox.getItems().addAll("Tous", "EN_ATTENTE", "CONFIRME", "ANNULE", "TERMINE");
        filterStatutComboBox.setValue("Tous");
        
        // Charger les médecins
        List<Medecin> medecins = medecinDAO.findAll();
        filterMedecinComboBox.getItems().add(null); // Option "Tous"
        filterMedecinComboBox.getItems().addAll(medecins);
        filterMedecinComboBox.setValue(null);
        
        // Converter pour affichage des médecins
        filterMedecinComboBox.setConverter(new javafx.util.StringConverter<Medecin>() {
            @Override
            public String toString(Medecin medecin) {
                return medecin == null ? "Tous les médecins" : medecin.getFullName();
            }

            @Override
            public Medecin fromString(String string) {
                return null;
            }
        });

        // Filtrage et tri
        filteredRendezVous = new FilteredList<>(rendezVousList, p -> true);
        SortedList<RendezVous> sortedRendezVous = new SortedList<>(filteredRendezVous);
        sortedRendezVous.comparatorProperty().bind(rendezVousTable.comparatorProperty());
        rendezVousTable.setItems(sortedRendezVous);

        // Listeners pour filtrage en temps réel
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        filterStatutComboBox.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        filterMedecinComboBox.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        filterDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    private void applyFilters() {
        filteredRendezVous.setPredicate(rdv -> {
            // Filtre par recherche textuelle
            String searchText = searchField.getText();
            if (searchText != null && !searchText.isEmpty()) {
                String lowerCaseFilter = searchText.toLowerCase();
                if (!rdv.getPatientNomComplet().toLowerCase().contains(lowerCaseFilter) &&
                    !rdv.getMedecinNomComplet().toLowerCase().contains(lowerCaseFilter) &&
                    (rdv.getMotif() == null || !rdv.getMotif().toLowerCase().contains(lowerCaseFilter))) {
                    return false;
                }
            }

            // Filtre par statut
            String statutFilter = filterStatutComboBox.getValue();
            if (statutFilter != null && !statutFilter.equals("Tous")) {
                if (!statutFilter.equals(rdv.getStatut())) {
                    return false;
                }
            }

            // Filtre par médecin
            Medecin medecinFilter = filterMedecinComboBox.getValue();
            if (medecinFilter != null) {
                if (rdv.getMedecinId() != medecinFilter.getId()) {
                    return false;
                }
            }

            // Filtre par date
            if (filterDatePicker.getValue() != null) {
                if (rdv.getDateRdv() == null || 
                    !rdv.getDateRdv().toLocalDate().equals(filterDatePicker.getValue())) {
                    return false;
                }
            }

            return true;
        });
        
        updateStatistics();
    }

    private void loadRendezVous() {
        rendezVousList.clear();
        List<RendezVous> rendezVous = rendezVousDAO.findAll();
        rendezVousList.addAll(rendezVous);
    }

    private void updateStatistics() {
        totalRendezVousLabel.setText(String.valueOf(filteredRendezVous.size()));
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        filterStatutComboBox.setValue("Tous");
        filterMedecinComboBox.setValue(null);
        filterDatePicker.setValue(null);
        loadRendezVous();
        AlertUtil.showInfo("Actualisation", "Liste des rendez-vous actualisée avec succès!");
    }

    @FXML
    private void handleAddRendezVous() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hopitalapp/view/secretaire/secretaire-rendez-vous-form.fxml"));
            Parent root = loader.load();

            SecretaireRendezVousFormController controller = loader.getController();
            controller.setRendezVousController(this);

            Stage stage = new Stage();
            stage.setTitle("Nouveau Rendez-vous");
            stage.setScene(new Scene(root, 600, 500));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void handleViewRendezVous(RendezVous rdv) {
        String info = "📅 DÉTAILS DU RENDEZ-VOUS\n\n" +
                "ID: " + rdv.getId() + "\n" +
                "Patient: " + rdv.getPatientNomComplet() + "\n" +
                "Médecin: " + rdv.getMedecinNomComplet() + "\n" +
                "Date: " + (rdv.getDateRdv() != null ? rdv.getDateRdv().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")) : "N/A") + "\n" +
                "Durée: " + rdv.getDuree() + " minutes\n" +
                "Motif: " + (rdv.getMotif() != null ? rdv.getMotif() : "N/A") + "\n" +
                "Statut: " + rdv.getStatut() + "\n" +
                "Notes: " + (rdv.getNotes() != null ? rdv.getNotes() : "Aucune note");

        AlertUtil.showInfo("Détails du Rendez-vous", info);
    }

    private void handleEditRendezVous(RendezVous rdv) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hopitalapp/view/secretaire/secretaire-rendez-vous-form.fxml"));
            Parent root = loader.load();

            SecretaireRendezVousFormController controller = loader.getController();
            controller.setRendezVousController(this);
            controller.setRendezVous(rdv);

            Stage stage = new Stage();
            stage.setTitle("Modifier Rendez-vous - " + rdv.getPatientNomComplet());
            stage.setScene(new Scene(root, 600, 500));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void handleDeleteRendezVous(RendezVous rdv) {
        boolean confirm = AlertUtil.showConfirmation(
                "Confirmation de suppression",
                "Êtes-vous sûr de vouloir supprimer ce rendez-vous ?\n\n" +
                        "Patient: " + rdv.getPatientNomComplet() + "\n" +
                        "Médecin: " + rdv.getMedecinNomComplet() + "\n" +
                        "Date: " + (rdv.getDateRdv() != null ? rdv.getDateRdv().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")) : "N/A")
        );

        if (confirm) {
            rendezVousDAO.delete(rdv.getId());
            loadRendezVous();
            updateStatistics();
            AlertUtil.showSuccess("Succès", "Rendez-vous supprimé avec succès!");
        }
    }

    public void refreshTable() {
        loadRendezVous();
        updateStatistics();
    }
}