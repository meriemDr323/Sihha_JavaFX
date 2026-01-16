package com.example.hopitalapp.controller.admin;

import com.example.hopitalapp.dao.impl.HospitalDAOImpl;
import com.example.hopitalapp.dao.interfaces.HospitalDAO;
import com.example.hopitalapp.model.Hospital;
import com.example.hopitalapp.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class HospitalInfoController implements Initializable {

    @FXML private Label nomLabel;
    @FXML private Label adresseLabel;
    @FXML private Label villeLabel;
    @FXML private Label codePostalLabel;
    @FXML private Label telephoneLabel;
    @FXML private Label emailLabel;
    @FXML private Label siteWebLabel;
    @FXML private Label nombreLitsLabel;
    @FXML private Label capaciteUrgencesLabel;
    @FXML private Label updatedAtLabel;
    @FXML private TextArea descriptionArea;
    @FXML private Button editButton;

    private final HospitalDAO hospitalDAO = new HospitalDAOImpl();
    private Hospital currentHospital;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadHospitalInfo();
    }

    private void loadHospitalInfo() {
        currentHospital = hospitalDAO.getInfo();
        if (currentHospital == null) {
            nomLabel.setText("Informations non configurées");
            descriptionArea.setText("");
            return;
        }

        nomLabel.setText(safe(currentHospital.getNom()));
        adresseLabel.setText(safe(currentHospital.getAdresse()));
        villeLabel.setText(safe(currentHospital.getVille()));
        codePostalLabel.setText(safe(currentHospital.getCodePostal()));
        telephoneLabel.setText(safe(currentHospital.getTelephone()));
        emailLabel.setText(safe(currentHospital.getEmail()));
        siteWebLabel.setText(safe(currentHospital.getSiteWeb()));
        nombreLitsLabel.setText(String.valueOf(currentHospital.getNombreLitsTotal()));
        capaciteUrgencesLabel.setText(String.valueOf(currentHospital.getCapaciteUrgences()));
        descriptionArea.setText(safe(currentHospital.getDescription()));

        if (currentHospital.getUpdatedAt() != null) {
            updatedAtLabel.setText(currentHospital.getUpdatedAt()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        } else {
            updatedAtLabel.setText("N/A");
        }
    }

    private String safe(String v) {
        return v != null && !v.isEmpty() ? v : "N/A";
    }

    @FXML
    private void handleEdit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hopitalapp/view/admin/hospital-info-form.fxml"));
            Parent root = loader.load();

            HospitalInfoFormController controller = loader.getController();
            controller.setHospitalInfoController(this);
            controller.setHospital(currentHospital);

            Stage stage = new Stage();
            stage.setTitle("Modifier les informations de l'hôpital");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    public void refresh() {
        loadHospitalInfo();
    }
}
