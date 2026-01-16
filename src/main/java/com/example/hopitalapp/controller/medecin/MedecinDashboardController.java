package com.example.hopitalapp.controller.medecin;

import com.example.hopitalapp.dao.impl.RendezVousDAOImpl;
import com.example.hopitalapp.dao.interfaces.RendezVousDAO;
import com.example.hopitalapp.model.RendezVous;
import com.example.hopitalapp.model.User;
import com.example.hopitalapp.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

public class MedecinDashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label rdvTodayLabel;
    @FXML private Label rdvWeekLabel;
    @FXML private Label patientsCountLabel;
    @FXML private Label rdvPendingLabel;
    @FXML private ListView<String> todayAppointmentsList;

    private final RendezVousDAO rendezVousDAO = new RendezVousDAOImpl();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        welcomeLabel.setText("Bonjour Dr " + user.getFullName());

        int medecinId = new com.example.hopitalapp.dao.impl.MedecinDAOImpl()
                .findByUserId(user.getId())
                .getId();

        List<RendezVous> rdvs = rendezVousDAO.findByMedecin(medecinId);

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate weekEnd = weekStart.plusDays(6);

        long todayCount = rdvs.stream()
                .filter(r -> r.getDateRdv() != null && r.getDateRdv().toLocalDate().equals(today))
                .count();

        long weekCount = rdvs.stream()
                .filter(r -> r.getDateRdv() != null && 
                        !r.getDateRdv().toLocalDate().isBefore(weekStart) && 
                        !r.getDateRdv().toLocalDate().isAfter(weekEnd))
                .count();

        long pendingCount = rdvs.stream()
                .filter(r -> "EN_ATTENTE".equals(r.getStatut()))
                .count();

        Set<Integer> patients = new HashSet<>();
        for (RendezVous r : rdvs) {
            patients.add(r.getPatientId());
        }

        rdvTodayLabel.setText(String.valueOf(todayCount));
        rdvWeekLabel.setText(String.valueOf(weekCount));
        patientsCountLabel.setText(String.valueOf(patients.size()));
        rdvPendingLabel.setText(String.valueOf(pendingCount));

        // Charger les RDV d'aujourd'hui
        loadTodayAppointments(rdvs, today);
    }

    private void loadTodayAppointments(List<RendezVous> rdvs, LocalDate today) {
        if (todayAppointmentsList == null) return;

        todayAppointmentsList.getItems().clear();
        
        List<RendezVous> todayRdvs = rdvs.stream()
                .filter(r -> r.getDateRdv() != null && r.getDateRdv().toLocalDate().equals(today))
                .sorted((r1, r2) -> r1.getDateRdv().compareTo(r2.getDateRdv()))
                .toList();

        if (todayRdvs.isEmpty()) {
            todayAppointmentsList.getItems().add("Aucun rendez-vous aujourd'hui");
        } else {
            for (RendezVous rdv : todayRdvs) {
                String item = String.format("%s - %s (%s)",
                        rdv.getDateRdv().format(timeFormatter),
                        rdv.getPatientNomComplet() != null ? rdv.getPatientNomComplet() : "Patient inconnu",
                        rdv.getStatut() != null ? rdv.getStatut() : "N/A"
                );
                todayAppointmentsList.getItems().add(item);
            }
        }
    }

    @FXML
    private void handleRefresh() {
        initialize(null, null);
    }
}
