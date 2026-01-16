package com.example.hopitalapp.controller.secretaire;

import com.example.hopitalapp.dao.impl.PatientDAOImpl;
import com.example.hopitalapp.dao.impl.RendezVousDAOImpl;
import com.example.hopitalapp.dao.interfaces.PatientDAO;
import com.example.hopitalapp.dao.interfaces.RendezVousDAO;
import com.example.hopitalapp.model.RendezVous;
import com.example.hopitalapp.model.User;
import com.example.hopitalapp.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class SecretaireDashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label rdvTodayLabel;
    @FXML private Label rdvWeekLabel;
    @FXML private Label patientsCountLabel;
    @FXML private Label rdvPendingLabel;

    private final PatientDAO patientDAO = new PatientDAOImpl();
    private final RendezVousDAO rendezVousDAO = new RendezVousDAOImpl();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        welcomeLabel.setText("Bonjour " + user.getFullName());
        loadStatistics();
    }

    private void loadStatistics() {
        // Statistiques patients
        long totalPatients = patientDAO.count();
        patientsCountLabel.setText(String.valueOf(totalPatients));

        // Statistiques rendez-vous
        List<RendezVous> allRdv = rendezVousDAO.findAll();
        
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate weekEnd = weekStart.plusDays(6);

        long rdvToday = allRdv.stream()
                .filter(r -> r.getDateRdv() != null && r.getDateRdv().toLocalDate().equals(today))
                .count();

        long rdvThisWeek = allRdv.stream()
                .filter(r -> r.getDateRdv() != null && 
                        !r.getDateRdv().toLocalDate().isBefore(weekStart) && 
                        !r.getDateRdv().toLocalDate().isAfter(weekEnd))
                .count();

        long rdvPending = allRdv.stream()
                .filter(r -> "EN_ATTENTE".equals(r.getStatut()))
                .count();

        rdvTodayLabel.setText(String.valueOf(rdvToday));
        rdvWeekLabel.setText(String.valueOf(rdvThisWeek));
        rdvPendingLabel.setText(String.valueOf(rdvPending));
    }
}