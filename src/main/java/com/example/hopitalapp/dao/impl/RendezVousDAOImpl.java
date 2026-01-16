package com.example.hopitalapp.dao.impl;

import com.example.hopitalapp.dao.interfaces.RendezVousDAO;
import com.example.hopitalapp.model.RendezVous;
import com.example.hopitalapp.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RendezVousDAOImpl implements RendezVousDAO {

    private RendezVous map(ResultSet rs) throws SQLException {
        RendezVous r = new RendezVous();
        r.setId(rs.getInt("id"));
        r.setPatientId(rs.getInt("patient_id"));
        r.setMedecinId(rs.getInt("medecin_id"));
        Timestamp ts = rs.getTimestamp("date_rdv");
        if (ts != null) r.setDateRdv(ts.toLocalDateTime());
        r.setDuree(rs.getInt("duree"));
        r.setMotif(rs.getString("motif"));
        r.setNotes(rs.getString("notes"));
        r.setStatut(rs.getString("statut"));
        r.setCreatedBy(rs.getInt("created_by"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) r.setCreatedAt(created.toLocalDateTime());
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) r.setUpdatedAt(updated.toLocalDateTime());
        r.setPatientNomComplet(rs.getString("patient_nom"));
        r.setMedecinNomComplet(rs.getString("medecin_nom"));
        return r;
    }

    @Override
    public List<RendezVous> findAll() {
        String sql = """
        SELECT rv.*,
               CONCAT(p.nom, ' ', p.prenom) AS patient_nom,
               CONCAT(u.nom, ' ', u.prenom) AS medecin_nom
        FROM rendez_vous rv
        JOIN patients p ON rv.patient_id = p.id
        JOIN medecins m ON rv.medecin_id = m.id
        JOIN users u ON m.user_id = u.id
        ORDER BY rv.date_rdv DESC
        """;

        List<RendezVous> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


    @Override
    public List<RendezVous> findByMedecin(int medecinId) {
        String sql = """
        SELECT rv.*,
               CONCAT(p.nom, ' ', p.prenom) AS patient_nom,
               CONCAT(u.nom, ' ', u.prenom) AS medecin_nom
        FROM rendez_vous rv
        JOIN patients p ON rv.patient_id = p.id
        JOIN medecins m ON rv.medecin_id = m.id
        JOIN users u ON m.user_id = u.id
        WHERE rv.medecin_id = ?
        ORDER BY rv.date_rdv DESC
        """;

        List<RendezVous> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, medecinId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<RendezVous> findByPatient(int patientId) {
        String sql = """
        SELECT rv.*,
               CONCAT(p.nom, ' ', p.prenom) AS patient_nom,
               CONCAT(u.nom, ' ', u.prenom) AS medecin_nom
        FROM rendez_vous rv
        JOIN patients p ON rv.patient_id = p.id
        JOIN medecins m ON rv.medecin_id = m.id
        JOIN users u ON m.user_id = u.id
        WHERE rv.patient_id = ?
        ORDER BY rv.date_rdv DESC
        """;

        List<RendezVous> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


    @Override
    public void save(RendezVous r) {
        String sql = """
            INSERT INTO rendez_vous
            (patient_id, medecin_id, date_rdv, duree, motif, notes, statut, created_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, r.getPatientId());
            ps.setInt(2, r.getMedecinId());
            ps.setTimestamp(3, Timestamp.valueOf(r.getDateRdv()));
            ps.setInt(4, r.getDuree());
            ps.setString(5, r.getMotif());
            ps.setString(6, r.getNotes());
            ps.setString(7, r.getStatut());
            ps.setInt(8, r.getCreatedBy());

            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) r.setId(keys.getInt(1));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(RendezVous r) {
        String sql = """
            UPDATE rendez_vous SET
                patient_id = ?,
                medecin_id = ?,
                date_rdv = ?,
                duree = ?,
                motif = ?,
                notes = ?,
                statut = ?
            WHERE id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, r.getPatientId());
            ps.setInt(2, r.getMedecinId());
            ps.setTimestamp(3, Timestamp.valueOf(r.getDateRdv()));
            ps.setInt(4, r.getDuree());
            ps.setString(5, r.getMotif());
            ps.setString(6, r.getNotes());
            ps.setString(7, r.getStatut());
            ps.setInt(8, r.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM rendez_vous WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM rendez_vous";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean isMedecinDisponible(int medecinId, LocalDateTime dateDebut, int dureeMinutes) {
        // Vérifie conflit simple : chevauchement de créneaux
        String sql = """
            SELECT COUNT(*) FROM rendez_vous
            WHERE medecin_id = ?
              AND statut IN ('EN_ATTENTE','CONFIRME')
              AND date_rdv < DATE_ADD(?, INTERVAL ? MINUTE)
              AND DATE_ADD(date_rdv, INTERVAL duree MINUTE) > ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, medecinId);
            ps.setTimestamp(2, Timestamp.valueOf(dateDebut));
            ps.setInt(3, dureeMinutes);
            ps.setTimestamp(4, Timestamp.valueOf(dateDebut));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
