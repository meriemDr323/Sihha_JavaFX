package com.example.hopitalapp.controller.medecin;

import com.example.hopitalapp.controller.shared.MainController;
import com.example.hopitalapp.controller.shared.RendezVousFormController;
import com.example.hopitalapp.dao.impl.MedecinDAOImpl;
import com.example.hopitalapp.dao.impl.PatientDAOImpl;
import com.example.hopitalapp.dao.impl.RendezVousDAOImpl;
import com.example.hopitalapp.dao.interfaces.PatientDAO;
import com.example.hopitalapp.dao.interfaces.RendezVousDAO;
import com.example.hopitalapp.model.Medecin;
import com.example.hopitalapp.model.Patient;
import com.example.hopitalapp.model.RendezVous;
import com.example.hopitalapp.model.User;
import com.example.hopitalapp.util.AlertUtil;
import com.example.hopitalapp.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MesPatientsController implements Initializable {

    @FXML private TextField searchField;
    @FXML private TableView<Patient> patientsTable;
    @FXML private TableColumn<Patient, String> nomColumn;
    @FXML private TableColumn<Patient, String> prenomColumn;
    @FXML private TableColumn<Patient, String> telephoneColumn;
    @FXML private TableColumn<Patient, Integer> ageColumn;
    @FXML private TableColumn<Patient, String> dernierRdvColumn;
    @FXML private TableColumn<Patient, Void> actionsColumn;

    private final PatientDAO patientDAO = new PatientDAOImpl();
    private final RendezVousDAO rendezVousDAO = new RendezVousDAOImpl();
    private final ObservableList<Patient> patientsList = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private int currentMedecinId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            AlertUtil.showError("Erreur", "Utilisateur non connecté.");
            return;
        }

        Medecin medecin = new MedecinDAOImpl().findByUserId(user.getId());
        if (medecin == null) {
            AlertUtil.showError("Erreur", "Aucun médecin associé à cet utilisateur.");
            return;
        }
        currentMedecinId = medecin.getId();

        setupTable();
        loadPatients();
        setupSearch();
    }

    private void setupTable() {
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        ageColumn.setCellValueFactory(new PropertyValueFactory<>("age"));
        
        // Colonne dernier RDV - afficher la date du dernier rendez-vous
        dernierRdvColumn.setCellValueFactory(cellData -> {
            Patient patient = cellData.getValue();
            List<RendezVous> rdvs = rendezVousDAO.findByPatient(patient.getId());
            
            if (rdvs.isEmpty()) {
                return new javafx.beans.property.SimpleStringProperty("Aucun");
            }
            
            // Trouver le RDV le plus récent
            RendezVous dernierRdv = rdvs.stream()
                    .filter(r -> r.getDateRdv() != null)
                    .max((r1, r2) -> r1.getDateRdv().compareTo(r2.getDateRdv()))
                    .orElse(null);
            
            if (dernierRdv != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    dernierRdv.getDateRdv().format(dateFormatter)
                );
            } else {
                return new javafx.beans.property.SimpleStringProperty("Aucun");
            }
        });

        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button rdvButton = new Button("Voir RDV");
            private final Button detailsButton = new Button("Détails");
            private final Button nouveauRdvButton = new Button("Nouveau RDV");

            {
                rdvButton.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; -fx-font-size: 9px; -fx-padding: 3 5; -fx-background-radius: 3;");
                detailsButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 9px; -fx-padding: 3 5; -fx-background-radius: 3;");
                nouveauRdvButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 9px; -fx-padding: 3 5; -fx-background-radius: 3;");
                
                rdvButton.setOnAction(e -> {
                    Patient p = getTableView().getItems().get(getIndex());
                    ouvrirRendezVousPatient(p);
                });
                
                detailsButton.setOnAction(e -> {
                    Patient p = getTableView().getItems().get(getIndex());
                    afficherDetailsPatient(p);
                });
                
                nouveauRdvButton.setOnAction(e -> {
                    Patient p = getTableView().getItems().get(getIndex());
                    creerNouveauRendezVous(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(2, rdvButton, detailsButton, nouveauRdvButton);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });

        patientsTable.setItems(patientsList);
    }

    private void loadPatients() {
        patientsList.clear();
        List<Patient> list = patientDAO.findByMedecin(currentMedecinId);
        patientsList.addAll(list);
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, old, value) -> {
            String filter = value == null ? "" : value.trim().toLowerCase();
            if (filter.isEmpty()) {
                patientsTable.setItems(patientsList);
            } else {
                List<Patient> filtered = patientsList.stream()
                        .filter(p ->
                                (p.getNom() != null && p.getNom().toLowerCase().contains(filter)) ||
                                        (p.getPrenom() != null && p.getPrenom().toLowerCase().contains(filter)))
                        .collect(Collectors.toList());
                patientsTable.setItems(FXCollections.observableArrayList(filtered));
            }
        });
    }

    private void ouvrirRendezVousPatient(Patient patient) {
        String fullName = patient.getNom() + " " + patient.getPrenom();

        // remonter jusqu'à la scène principale
        Node anyNode = patientsTable;
        if (anyNode.getScene() == null) {
            AlertUtil.showError("Erreur", "Impossible de trouver la fenêtre principale.");
            return;
        }

        Object userData = anyNode.getScene().getUserData();
        if (userData instanceof MainController mainController) {
            mainController.openMesRendezVousAndFilter(fullName);
        } else {
            AlertUtil.showError("Erreur", "Contrôleur principal non disponible.");
        }
    }

    private void afficherDetailsPatient(Patient patient) {
        String info = "👤 INFORMATIONS DU PATIENT\n\n" +
                "Nom complet: " + patient.getFullName() + "\n" +
                "Âge: " + patient.getAge() + " ans\n" +
                "Téléphone: " + (patient.getTelephone() != null ? patient.getTelephone() : "N/A") + "\n" +
                "Email: " + (patient.getEmail() != null ? patient.getEmail() : "N/A") + "\n" +
                "Adresse: " + (patient.getAdresse() != null ? patient.getAdresse() : "N/A") + "\n" +
                "Ville: " + (patient.getVille() != null ? patient.getVille() : "N/A") + "\n" +
                "Groupe sanguin: " + (patient.getGroupeSanguin() != null ? patient.getGroupeSanguin() : "N/A") + "\n" +
                "Allergies: " + (patient.getAllergies() != null ? patient.getAllergies() : "Aucune");

        AlertUtil.showInfo("Détails du Patient", info);
    }

    private void creerNouveauRendezVous(Patient patient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hopitalapp/view/shared/rendez-vous-form.fxml"));
            Parent root = loader.load();

            RendezVousFormController controller = loader.getController();
            
            // Créer un nouveau rendez-vous avec le patient présélectionné
            RendezVous nouveauRdv = new RendezVous();
            nouveauRdv.setPatientId(patient.getId());
            nouveauRdv.setMedecinId(currentMedecinId);
            controller.setRendezVous(nouveauRdv);

            Stage stage = new Stage();
            stage.setTitle("Nouveau Rendez-vous - " + patient.getFullName());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Rafraîchir la liste après création
            loadPatients();

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible d'ouvrir le formulaire de rendez-vous: " + e.getMessage());
        }
    }
}
