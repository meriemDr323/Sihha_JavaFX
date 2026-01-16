package com.example.hopitalapp.dao.impl;

import com.example.hopitalapp.dao.interfaces.PatientDAO;
import com.example.hopitalapp.model.Patient;
import com.example.hopitalapp.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientDAOImpl implements PatientDAO {

    @Override
    public Patient findById(int id) {
        String query = "SELECT * FROM patients WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractPatientFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Patient> findAll() {
        List<Patient> patients = new ArrayList<>();
        String query = "SELECT * FROM patients ORDER BY nom, prenom";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                patients.add(extractPatientFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return patients;
    }

    @Override
    public List<Patient> searchByName(String keyword) {
        List<Patient> patients = new ArrayList<>();
        String query = "SELECT * FROM patients WHERE nom LIKE ? OR prenom LIKE ? ORDER BY nom, prenom";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                patients.add(extractPatientFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return patients;
    }

    @Override
    public void save(Patient patient) {
        String query = "INSERT INTO patients (nom, prenom, date_naissance, sexe, telephone, email, " +
                "adresse, ville, code_postal, numero_securite_sociale, groupe_sanguin, allergies, " +
                "assurance_provider, numero_assurance, contact_urgence_nom, contact_urgence_telephone) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, patient.getNom());
            stmt.setString(2, patient.getPrenom());
            stmt.setDate(3, patient.getDateNaissance() != null ? Date.valueOf(patient.getDateNaissance()) : null);
            stmt.setString(4, patient.getSexe());
            stmt.setString(5, patient.getTelephone());
            stmt.setString(6, patient.getEmail());
            stmt.setString(7, patient.getAdresse());
            stmt.setString(8, patient.getVille());
            stmt.setString(9, patient.getCodePostal());
            stmt.setString(10, patient.getNumeroSecuriteSociale());
            stmt.setString(11, patient.getGroupeSanguin());
            stmt.setString(12, patient.getAllergies());
            stmt.setString(13, patient.getAssuranceProvider());
            stmt.setString(14, patient.getNumeroAssurance());
            stmt.setString(15, patient.getContactUrgenceNom());
            stmt.setString(16, patient.getContactUrgenceTelephone());

            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                patient.setId(generatedKeys.getInt(1));
            }

            System.out.println("✓ Patient ajouté: " + patient.getFullName());
        } catch (SQLException e) {
            System.err.println("✗ Erreur lors de l'ajout du patient: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<Patient> findByMedecin(int medecinId) {
        List<Patient> patients = new ArrayList<>();

        String sql = """
        SELECT DISTINCT p.*
        FROM patients p
        JOIN rendez_vous r ON r.patient_id = p.id
        WHERE r.medecin_id = ?
        ORDER BY p.nom, p.prenom
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, medecinId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    patients.add(extractPatientFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return patients;
    }


    @Override
    public void update(Patient patient) {
        String query = "UPDATE patients SET nom=?, prenom=?, date_naissance=?, sexe=?, telephone=?, " +
                "email=?, adresse=?, ville=?, code_postal=?, numero_securite_sociale=?, " +
                "groupe_sanguin=?, allergies=?, assurance_provider=?, numero_assurance=?, " +
                "contact_urgence_nom=?, contact_urgence_telephone=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, patient.getNom());
            stmt.setString(2, patient.getPrenom());
            stmt.setDate(3, patient.getDateNaissance() != null ? Date.valueOf(patient.getDateNaissance()) : null);
            stmt.setString(4, patient.getSexe());
            stmt.setString(5, patient.getTelephone());
            stmt.setString(6, patient.getEmail());
            stmt.setString(7, patient.getAdresse());
            stmt.setString(8, patient.getVille());
            stmt.setString(9, patient.getCodePostal());
            stmt.setString(10, patient.getNumeroSecuriteSociale());
            stmt.setString(11, patient.getGroupeSanguin());
            stmt.setString(12, patient.getAllergies());
            stmt.setString(13, patient.getAssuranceProvider());
            stmt.setString(14, patient.getNumeroAssurance());
            stmt.setString(15, patient.getContactUrgenceNom());
            stmt.setString(16, patient.getContactUrgenceTelephone());
            stmt.setInt(17, patient.getId());

            stmt.executeUpdate();
            System.out.println("✓ Patient modifié: " + patient.getFullName());
        } catch (SQLException e) {
            System.err.println("✗ Erreur lors de la modification du patient: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int id) {
        String query = "DELETE FROM patients WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("✓ Patient supprimé (ID: " + id + ")");
        } catch (SQLException e) {
            System.err.println("✗ Erreur lors de la suppression du patient: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public long count() {
        String query = "SELECT COUNT(*) FROM patients";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Patient extractPatientFromResultSet(ResultSet rs) throws SQLException {
        Patient patient = new Patient();
        patient.setId(rs.getInt("id"));
        patient.setNom(rs.getString("nom"));
        patient.setPrenom(rs.getString("prenom"));

        Date dateNaissance = rs.getDate("date_naissance");
        if (dateNaissance != null) {
            patient.setDateNaissance(dateNaissance.toLocalDate());
        }

        patient.setSexe(rs.getString("sexe"));
        patient.setTelephone(rs.getString("telephone"));
        patient.setEmail(rs.getString("email"));
        patient.setAdresse(rs.getString("adresse"));
        patient.setVille(rs.getString("ville"));
        patient.setCodePostal(rs.getString("code_postal"));
        patient.setNumeroSecuriteSociale(rs.getString("numero_securite_sociale"));
        patient.setGroupeSanguin(rs.getString("groupe_sanguin"));
        patient.setAllergies(rs.getString("allergies"));
        patient.setAssuranceProvider(rs.getString("assurance_provider"));
        patient.setNumeroAssurance(rs.getString("numero_assurance"));
        patient.setContactUrgenceNom(rs.getString("contact_urgence_nom"));
        patient.setContactUrgenceTelephone(rs.getString("contact_urgence_telephone"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            patient.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            patient.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return patient;
    }
}
