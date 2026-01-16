package com.example.hopitalapp.controller.medecin;

import com.example.hopitalapp.controller.shared.DossierMedicalFormController;
import com.example.hopitalapp.dao.impl.DossierMedicalDAOImpl;
import com.example.hopitalapp.dao.impl.MedecinDAOImpl;
import com.example.hopitalapp.dao.impl.RendezVousDAOImpl;
import com.example.hopitalapp.dao.interfaces.DossierMedicalDAO;
import com.example.hopitalapp.dao.interfaces.MedecinDAO;
import com.example.hopitalapp.dao.interfaces.RendezVousDAO;
import com.example.hopitalapp.model.DossierMedical;
import com.example.hopitalapp.model.Medecin;
import com.example.hopitalapp.model.RendezVous;
import com.example.hopitalapp.model.User;
import com.example.hopitalapp.util.AlertUtil;
import com.example.hopitalapp.util.SessionManager;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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

public class MesRendezVousController implements Initializable {

    @FXML private TextField searchField;  // ajouter dans ton FXML en haut de la vue
    @FXML private ComboBox<String> statusFilter;  // ajouter dans ton FXML
    @FXML private TableView<RendezVous> rendezVousTable;
    @FXML private TableColumn<RendezVous, String> dateColumn;
    @FXML private TableColumn<RendezVous, String> patientColumn;
    @FXML private TableColumn<RendezVous, String> motifColumn;
    @FXML private TableColumn<RendezVous, String> statutColumn;
    @FXML private TableColumn<RendezVous, Void> actionsColumn;

    private final RendezVousDAO rendezVousDAO = new RendezVousDAOImpl();
    private final DossierMedicalDAO dossierDAO = new DossierMedicalDAOImpl();

    private final ObservableList<RendezVous> rendezVousList = FXCollections.observableArrayList();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int currentMedecinId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            AlertUtil.showError("Erreur", "Utilisateur non connecté.");
            return;
        }

        MedecinDAO medecinDAO = new MedecinDAOImpl();
        Medecin medecin = medecinDAO.findByUserId(user.getId());
        if (medecin == null) {
            AlertUtil.showError("Erreur", "Aucun médecin associé à cet utilisateur.");
            return;
        }

        currentMedecinId = medecin.getId();

        setupTable();
        loadRendezVous();
        setupSearch();
        setupStatusFilter();
    }

    private void setupTable() {
        dateColumn.setCellValueFactory(cell ->
                Bindings.createStringBinding(() ->
                        cell.getValue().getDateRdv() != null
                                ? cell.getValue().getDateRdv().format(formatter)
                                : ""
                ));

        patientColumn.setCellValueFactory(new PropertyValueFactory<>("patientNomComplet"));
        motifColumn.setCellValueFactory(new PropertyValueFactory<>("motif"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));

        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button dossierButton = new Button("Dossier");
            private final Button annulerButton = new Button("Annuler");
            private final Button terminerButton = new Button("Terminer");

            {
                dossierButton.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; -fx-font-size: 9px; -fx-padding: 3 5; -fx-background-radius: 3;");
                annulerButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-size: 9px; -fx-padding: 3 5; -fx-background-radius: 3;");
                terminerButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 9px; -fx-padding: 3 5; -fx-background-radius: 3;");
                
                dossierButton.setOnAction(e -> {
                    RendezVous r = getTableView().getItems().get(getIndex());
                    ouvrirDossierMedical(r);
                });
                
                annulerButton.setOnAction(e -> {
                    RendezVous r = getTableView().getItems().get(getIndex());
                    annulerRendezVous(r);
                });
                
                terminerButton.setOnAction(e -> {
                    RendezVous r = getTableView().getItems().get(getIndex());
                    terminerRendezVous(r);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    RendezVous rdv = getTableView().getItems().get(getIndex());
                    HBox box = new HBox(2);
                    box.setAlignment(Pos.CENTER);
                    
                    box.getChildren().add(dossierButton);
                    
                    // Afficher les boutons selon le statut
                    if ("EN_ATTENTE".equals(rdv.getStatut()) || "CONFIRME".equals(rdv.getStatut())) {
                        box.getChildren().add(terminerButton);
                        box.getChildren().add(annulerButton);
                    }
                    
                    setGraphic(box);
                }
            }
        });

        rendezVousTable.setItems(rendezVousList);
    }

    private void loadRendezVous() {
        rendezVousList.clear();
        List<RendezVous> list = rendezVousDAO.findByMedecin(currentMedecinId);
        rendezVousList.addAll(list);
    }

    private void setupSearch() {
        if (searchField == null) return;

        searchField.textProperty().addListener((obs, old, value) -> {
            applyFilters();
        });
    }

    private void setupStatusFilter() {
        if (statusFilter == null) return;

        statusFilter.getItems().addAll("Tous", "EN_ATTENTE", "CONFIRME", "TERMINE", "ANNULE");
        statusFilter.setValue("Tous");
        
        statusFilter.valueProperty().addListener((obs, old, value) -> {
            applyFilters();
        });
    }

    private void applyFilters() {
        String searchText = searchField != null ? searchField.getText() : "";
        String statusValue = statusFilter != null ? statusFilter.getValue() : "Tous";
        
        String filter = searchText == null ? "" : searchText.trim().toLowerCase();
        
        List<RendezVous> filtered = rendezVousList.stream()
                .filter(r -> {
                    // Filtre par nom de patient
                    boolean matchesName = filter.isEmpty() || 
                            (r.getPatientNomComplet() != null && 
                             r.getPatientNomComplet().toLowerCase().contains(filter));
                    
                    // Filtre par statut
                    boolean matchesStatus = "Tous".equals(statusValue) || 
                            statusValue.equals(r.getStatut());
                    
                    return matchesName && matchesStatus;
                })
                .collect(Collectors.toList());
        
        rendezVousTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private void ouvrirDossierMedical(RendezVous rdv) {
        try {
            DossierMedical dossier = dossierDAO.findByRendezVousId(rdv.getId());
            if (dossier == null) {
                dossier = new DossierMedical();
                dossier.setPatientId(rdv.getPatientId());
                dossier.setRendezVousId(rdv.getId());
                dossier.setMedecinId(rdv.getMedecinId());
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hopitalapp/view/shared/dossier-medical-form.fxml"));
            Parent root = loader.load();

            DossierMedicalFormController controller = loader.getController();
            controller.setDossierMedical(dossier);
            controller.setMesRendezVousController(this);

            Stage stage = new Stage();
            stage.setTitle("Dossier médical - " + rdv.getPatientNomComplet());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible d'ouvrir le dossier médical: " + e.getMessage());
        }
    }

    public void refresh() {
        loadRendezVous();
        applyFilters(); // Réappliquer les filtres après le rechargement
    }

    private void annulerRendezVous(RendezVous rdv) {
        boolean confirm = AlertUtil.showConfirmation(
                "Confirmation d'annulation",
                "Êtes-vous sûr de vouloir annuler ce rendez-vous ?\n\n" +
                        "Patient: " + rdv.getPatientNomComplet() + "\n" +
                        "Date: " + (rdv.getDateRdv() != null ? rdv.getDateRdv().format(formatter) : "N/A") + "\n" +
                        "Motif: " + (rdv.getMotif() != null ? rdv.getMotif() : "N/A")
        );

        if (confirm) {
            try {
                rdv.setStatut("ANNULE");
                rendezVousDAO.update(rdv);
                refresh();
                AlertUtil.showSuccess("Succès", "Rendez-vous annulé avec succès!");
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showError("Erreur", "Impossible d'annuler le rendez-vous: " + e.getMessage());
            }
        }
    }

    private void terminerRendezVous(RendezVous rdv) {
        boolean confirm = AlertUtil.showConfirmation(
                "Confirmation de fin de consultation",
                "Marquer ce rendez-vous comme terminé ?\n\n" +
                        "Patient: " + rdv.getPatientNomComplet() + "\n" +
                        "Date: " + (rdv.getDateRdv() != null ? rdv.getDateRdv().format(formatter) : "N/A") + "\n" +
                        "Motif: " + (rdv.getMotif() != null ? rdv.getMotif() : "N/A")
        );

        if (confirm) {
            try {
                rdv.setStatut("TERMINE");
                rendezVousDAO.update(rdv);
                refresh();
                AlertUtil.showSuccess("Succès", "Consultation terminée avec succès!");
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showError("Erreur", "Impossible de terminer le rendez-vous: " + e.getMessage());
            }
        }
    }

    // appelé depuis MesPatientsController via MainController
    public void filterByPatientName(String fullName) {
        if (searchField != null && fullName != null) {
            searchField.setText(fullName);
            // Les filtres seront automatiquement appliqués grâce au listener
        }
    }
}
