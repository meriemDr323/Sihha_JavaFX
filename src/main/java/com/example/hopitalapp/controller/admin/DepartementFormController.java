package com.example.hopitalapp.controller.admin;

import com.example.hopitalapp.dao.impl.DepartementDAOImpl;
import com.example.hopitalapp.dao.interfaces.DepartementDAO;
import com.example.hopitalapp.model.Departement;
import com.example.hopitalapp.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class DepartementFormController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private TextField nomField;
    @FXML private TextField etageField;
    @FXML private TextField nombreLitsField;
    @FXML private TextField telephoneField;
    @FXML private TextArea descriptionArea;
    @FXML private Button saveButton;

    private final DepartementDAO departementDAO = new DepartementDAOImpl();
    private DepartementController departementController;
    private Departement currentDepartement;
    private boolean isEditMode = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // rien de particulier à l'init
    }

    public void setDepartementController(DepartementController controller) {
        this.departementController = controller;
    }

    public void setDepartement(Departement departement) {
        this.currentDepartement = departement;
        this.isEditMode = true;

        titleLabel.setText("Modifier Département");
        saveButton.setText("Mettre à jour");

        nomField.setText(safeString(departement.getNom()));
        etageField.setText(safeString(departement.getEtage()));
        nombreLitsField.setText(String.valueOf(departement.getNombreLits()));
        telephoneField.setText(safeString(departement.getTelephone()));
        descriptionArea.setText(safeString(departement.getDescription()));
    }

    private String safeString(String value) {
        return value != null ? value : "";
    }

    private String safeText(TextField field) {
        return field != null && field.getText() != null ? field.getText().trim() : "";
    }

    private String safeTextArea(TextArea area) {
        return area != null && area.getText() != null ? area.getText().trim() : "";
    }

    @FXML
    private void handleSave() {
        if (!validateFields()) {
            return;
        }

        Departement d = isEditMode ? currentDepartement : new Departement();

        d.setNom(safeText(nomField));
        d.setEtage(safeText(etageField));
        d.setTelephone(safeText(telephoneField));
        d.setDescription(safeTextArea(descriptionArea));

        String lits = safeText(nombreLitsField);
        if (!lits.isEmpty()) {
            try {
                d.setNombreLits(Integer.parseInt(lits));
            } catch (NumberFormatException e) {
                AlertUtil.showError("Validation", "Le nombre de lits doit être un entier.");
                return;
            }
        } else {
            d.setNombreLits(0);
        }

        if (isEditMode) {
            departementDAO.update(d);
            AlertUtil.showSuccess("Succès", "Département modifié avec succès!");
        } else {
            departementDAO.save(d);
            AlertUtil.showSuccess("Succès", "Département ajouté avec succès!");
        }

        if (departementController != null) {
            departementController.refreshTable();
        }

        closeWindow();
    }

    private boolean validateFields() {
        if (safeText(nomField).isEmpty()) {
            AlertUtil.showError("Validation", "Le nom du département est obligatoire!");
            nomField.requestFocus();
            return false;
        }
        return true;
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}
