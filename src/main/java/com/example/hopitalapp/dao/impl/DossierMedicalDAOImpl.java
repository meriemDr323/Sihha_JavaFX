package com.example.hopitalapp.dao.impl;

import com.example.hopitalapp.dao.interfaces.DossierMedicalDAO;
import com.example.hopitalapp.model.DossierMedical;
import com.example.hopitalapp.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;

public class DossierMedicalDAOImpl implements DossierMedicalDAO {

    private DossierMedical map(ResultSet rs) throws SQLException {
        DossierMedical d = new DossierMedical();
        d.setId(rs.getInt("id"));
        d.setPatientId(rs.getInt("patient_id"));
        d.setRendezVousId(rs.getInt("rendez_vous_id"));
        d.setMedecinId(rs.getInt("medecin_id"));

        Timestamp t = rs.getTimestamp("date_consultation");
        if (t != null) d.setDateConsultation(t.toLocalDateTime());

        d.setDiagnostic(rs.getString("diagnostic"));
        d.setSymptomes(rs.getString("symptomes"));
        d.setTraitementPrescrit(rs.getString("traitement_prescrit"));
        d.setObservations(rs.getString("observations"));
        d.setPoids(rs.getObject("poids") != null ? rs.getDouble("poids") : null);
        d.setTaille(rs.getObject("taille") != null ? rs.getDouble("taille") : null);
        d.setTensionArterielle(rs.getString("tension_arterielle"));
        d.setTemperature(rs.getObject("temperature") != null ? rs.getDouble("temperature") : null);

        Timestamp c = rs.getTimestamp("created_at");
        if (c != null) d.setCreatedAt(c.toLocalDateTime());

        d.setPatientNomComplet(rs.getString("patient_nom"));
        d.setMedecinNomComplet(rs.getString("medecin_nom"));

        return d;
    }

    @Override
    public DossierMedical findByRendezVousId(int rendezVousId) {
        String sql = """
            SELECT dm.*,
                   CONCAT(p.nom, ' ', p.prenom) AS patient_nom,
                   CONCAT(u.nom, ' ', u.prenom) AS medecin_nom
            FROM dossiers_medicaux dm
            JOIN patients p ON dm.patient_id = p.id
            JOIN medecins m ON dm.medecin_id = m.id
            JOIN users u ON m.user_id = u.id
            WHERE dm.rendez_vous_id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, rendezVousId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public java.util.List<DossierMedical> findByPatient(int patientId) {
        java.util.List<DossierMedical> dossiers = new java.util.ArrayList<>();
        String sql = """
            SELECT dm.*,
                   CONCAT(p.nom, ' ', p.prenom) AS patient_nom,
                   CONCAT(u.nom, ' ', u.prenom) AS medecin_nom
            FROM dossiers_medicaux dm
            JOIN patients p ON dm.patient_id = p.id
            JOIN medecins m ON dm.medecin_id = m.id
            JOIN users u ON m.user_id = u.id
            WHERE dm.patient_id = ?
            ORDER BY dm.date_consultation DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dossiers.add(map(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dossiers;
    }

    @Override
    public void save(DossierMedical d) {
        String sql = """
            INSERT INTO dossiers_medicaux
            (patient_id, rendez_vous_id, medecin_id, date_consultation,
             diagnostic, symptomes, traitement_prescrit, observations,
             poids, taille, tension_arterielle, temperature)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, d.getPatientId());
            ps.setInt(2, d.getRendezVousId());
            ps.setInt(3, d.getMedecinId());
            ps.setTimestamp(4, Timestamp.valueOf(
                    d.getDateConsultation() != null ? d.getDateConsultation() : LocalDateTime.now()
            ));
            ps.setString(5, d.getDiagnostic());
            ps.setString(6, d.getSymptomes());
            ps.setString(7, d.getTraitementPrescrit());
            ps.setString(8, d.getObservations());
            if (d.getPoids() != null) ps.setDouble(9, d.getPoids()); else ps.setNull(9, Types.DECIMAL);
            if (d.getTaille() != null) ps.setDouble(10, d.getTaille()); else ps.setNull(10, Types.DECIMAL);
            ps.setString(11, d.getTensionArterielle());
            if (d.getTemperature() != null) ps.setDouble(12, d.getTemperature()); else ps.setNull(12, Types.DECIMAL);

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) d.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(DossierMedical d) {
        String sql = """
            UPDATE dossiers_medicaux SET
                diagnostic = ?,
                symptomes = ?,
                traitement_prescrit = ?,
                observations = ?,
                poids = ?,
                taille = ?,
                tension_arterielle = ?,
                temperature = ?
            WHERE id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, d.getDiagnostic());
            ps.setString(2, d.getSymptomes());
            ps.setString(3, d.getTraitementPrescrit());
            ps.setString(4, d.getObservations());

            if (d.getPoids() != null) ps.setDouble(5, d.getPoids()); else ps.setNull(5, Types.DECIMAL);
            if (d.getTaille() != null) ps.setDouble(6, d.getTaille()); else ps.setNull(6, Types.DECIMAL);
            ps.setString(7, d.getTensionArterielle());
            if (d.getTemperature() != null) ps.setDouble(8, d.getTemperature()); else ps.setNull(8, Types.DECIMAL);

            ps.setInt(9, d.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
