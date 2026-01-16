package com.example.hopitalapp.controller.admin;

import com.example.hopitalapp.dao.impl.DepartementDAOImpl;
import com.example.hopitalapp.dao.impl.MedecinDAOImpl;
import com.example.hopitalapp.dao.impl.UserDAOImpl;
import com.example.hopitalapp.dao.interfaces.DepartementDAO;
import com.example.hopitalapp.dao.interfaces.MedecinDAO;
import com.example.hopitalapp.dao.interfaces.UserDAO;
import com.example.hopitalapp.model.Departement;
import com.example.hopitalapp.model.Medecin;
import com.example.hopitalapp.model.User;
import com.example.hopitalapp.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.example.hopitalapp.model.RoleEnum;


import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.ResourceBundle;

public class MedecinFormController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField telephoneField;
    @FXML private TextField emailField;
    @FXML private TextField adresseField;
    @FXML private TextField villeField;
    @FXML private ComboBox<String> specialiteComboBox;
    @FXML private ComboBox<String> departementComboBox;
    @FXML private TextField numeroOrdreField;
    @FXML private TextField experienceField;
    @FXML private TextField tarifField;
    @FXML private TextArea diplomesArea;
    @FXML private TextField imageField;
    @FXML private Button browseImageButton;
    @FXML private CheckBox actifCheckBox;
    @FXML private Button saveButton;

    private final MedecinDAO medecinDAO = new MedecinDAOImpl();
    private final DepartementDAO departementDAO = new DepartementDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();

    private MedecinController medecinController;
    private Medecin currentMedecin;
    private boolean isEditMode = false;

    private List<Departement> departements;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        specialiteComboBox.getItems().addAll(
                "Cardiologue",
                "Pédiatre",
                "Neurologue",
                "Oncologue",
                "Radiologue",
                "Dermatologue",
                "Gynécologue",
                "Urologue",
                "Chirurgien Général",
                "Médecin Urgentiste",
                "Endocrinologue",
                "Gastro-entérologue",
                "Orthopédiste",
                "ORL",
                "Anesthésiste",
                "Médecin Généraliste"
        );
        loadDepartements();
    }

    private void loadDepartements() {
        try {
            departements = departementDAO.findAll();
            departementComboBox.getItems().clear();
            for (Departement dept : departements) {
                departementComboBox.getItems().add(dept.getNom());
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des départements: " + e.getMessage());
        }
    }

    public void setMedecinController(MedecinController controller) {
        this.medecinController = controller;
    }

    public void setMedecin(Medecin medecin) {
        this.currentMedecin = medecin;
        this.isEditMode = true;
        titleLabel.setText("✏️ Modifier Médecin");
        saveButton.setText("Mettre à jour");

        nomField.setText(safeString(medecin.getNom()));
        prenomField.setText(safeString(medecin.getPrenom()));
        telephoneField.setText(safeString(medecin.getTelephone()));
        emailField.setText(safeString(medecin.getEmail()));
        adresseField.setText(safeString(medecin.getAdresse()));
        villeField.setText(safeString(medecin.getVille()));
        specialiteComboBox.setValue(medecin.getSpecialite());

        if (medecin.getDepartementNom() != null) {
            departementComboBox.setValue(medecin.getDepartementNom());
        }

        numeroOrdreField.setText(safeString(medecin.getNumeroOrdre()));

        if (medecin.getAnneesExperience() != null) {
            experienceField.setText(String.valueOf(medecin.getAnneesExperience()));
        }

        if (medecin.getTarifConsultation() != null) {
            tarifField.setText(medecin.getTarifConsultation().toString());
        }

        diplomesArea.setText(safeString(medecin.getDiplomes()));
        imageField.setText(safeString(medecin.getImage()));
        actifCheckBox.setSelected(medecin.isActif());
    }

    // Helpers pour éviter les NPE
    private String safeText(TextField field) {
        return field != null && field.getText() != null ? field.getText().trim() : "";
    }

    private String safeTextArea(TextArea area) {
        return area != null && area.getText() != null ? area.getText().trim() : "";
    }

    private String safeString(String value) {
        return value != null ? value : "";
    }

    @FXML
    private void handleSave() {
        if (!validateFields()) {
            return;
        }

        Medecin medecin = isEditMode ? currentMedecin : new Medecin();

        medecin.setNom(safeText(nomField));
        medecin.setPrenom(safeText(prenomField));
        medecin.setTelephone(safeText(telephoneField));
        medecin.setEmail(safeText(emailField));
        medecin.setAdresse(safeText(adresseField));
        medecin.setVille(safeText(villeField));
        medecin.setSpecialite(specialiteComboBox.getValue());

        String departementNom = departementComboBox.getValue();
        if (departementNom != null && !departementNom.isEmpty() && departements != null) {
            for (Departement dept : departements) {
                if (dept.getNom().equals(departementNom)) {
                    medecin.setDepartementId(dept.getId());
                    break;
                }
            }
        }

        medecin.setNumeroOrdre(safeText(numeroOrdreField));

        String expText = safeText(experienceField);
        if (!expText.isEmpty()) {
            try {
                medecin.setAnneesExperience(Integer.parseInt(expText));
            } catch (NumberFormatException e) {
                AlertUtil.showError("Validation", "Les années d'expérience doivent être un nombre entier!");
                return;
            }
        } else {
            medecin.setAnneesExperience(null);
        }

        String tarifText = safeText(tarifField);
        if (!tarifText.isEmpty()) {
            try {
                medecin.setTarifConsultation(new BigDecimal(tarifText));
            } catch (NumberFormatException e) {
                AlertUtil.showError("Validation", "Le tarif doit être un nombre valide!");
                return;
            }
        } else {
            medecin.setTarifConsultation(null);
        }

        medecin.setDiplomes(safeTextArea(diplomesArea));
        medecin.setImage(safeText(imageField));
        medecin.setActif(actifCheckBox.isSelected());

        // Création du User uniquement en mode ajout
        if (!isEditMode) {
            User user = new User();
            user.setNom(medecin.getNom());
            user.setPrenom(medecin.getPrenom());
            user.setEmail(medecin.getEmail());
            user.setTelephone(medecin.getTelephone());
            user.setAdresse(medecin.getAdresse());
            user.setRole(RoleEnum.MEDECIN);   // <== utilise ton enum
            user.setActif(true);
            user.setPassword("123456");       // TODO: générer / hasher plus tard

            userDAO.save(user);               // remplit user.id
            medecin.setUserId(user.getId());  // FK vers users.id
        }

        if (isEditMode) {
            medecinDAO.update(medecin);
            AlertUtil.showSuccess("Succès", "Médecin modifié avec succès!");
        } else {
            medecinDAO.save(medecin);
            AlertUtil.showSuccess("Succès", "Médecin ajouté avec succès!");
        }

        if (medecinController != null) {
            medecinController.refreshTable();
        }

        closeWindow();
    }


    private boolean validateFields() {
        if (safeText(nomField).isEmpty()) {
            AlertUtil.showError("Validation", "Le nom est obligatoire!");
            nomField.requestFocus();
            return false;
        }

        if (safeText(prenomField).isEmpty()) {
            AlertUtil.showError("Validation", "Le prénom est obligatoire!");
            prenomField.requestFocus();
            return false;
        }

        if (safeText(telephoneField).isEmpty()) {
            AlertUtil.showError("Validation", "Le téléphone est obligatoire!");
            telephoneField.requestFocus();
            return false;
        }

        if (safeText(emailField).isEmpty()) {
            AlertUtil.showError("Validation", "L'email est obligatoire!");
            emailField.requestFocus();
            return false;
        }

        String email = safeText(emailField);
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            AlertUtil.showError("Validation", "Format d'email invalide!");
            emailField.requestFocus();
            return false;
        }

        String spec = specialiteComboBox.getValue();
        if (spec == null || spec.trim().isEmpty()) {
            AlertUtil.showError("Validation", "La spécialité est obligatoire!");
            specialiteComboBox.requestFocus();
            return false;
        }

        return true;
    }

    @FXML
    private void handleBrowseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        
        // Filtres pour les images
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter(
            "Images (*.png, *.jpg, *.jpeg, *.gif)", "*.png", "*.jpg", "*.jpeg", "*.gif");
        fileChooser.getExtensionFilters().add(imageFilter);
        
        // Répertoire initial
        String userHome = System.getProperty("user.home");
        File initialDirectory = new File(userHome, "Pictures");
        if (!initialDirectory.exists()) {
            initialDirectory = new File(userHome);
        }
        fileChooser.setInitialDirectory(initialDirectory);
        
        Stage stage = (Stage) browseImageButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            try {
                // Créer le répertoire des images s'il n'existe pas
                Path imagesDir = Paths.get("images", "medecins");
                Files.createDirectories(imagesDir);
                
                // Générer un nom unique pour l'image
                String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                Path targetPath = imagesDir.resolve(fileName);
                
                // Copier le fichier
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                
                // Mettre à jour le champ avec le chemin relatif
                imageField.setText(targetPath.toString());
                
                AlertUtil.showSuccess("Succès", "Image importée avec succès!");
                
            } catch (IOException e) {
                e.printStackTrace();
                AlertUtil.showError("Erreur", "Impossible d'importer l'image: " + e.getMessage());
            }
        }
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
