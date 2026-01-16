package com.example.hopitalapp.dao.impl;

import com.example.hopitalapp.dao.interfaces.MedecinDAO;
import com.example.hopitalapp.model.Medecin;
import com.example.hopitalapp.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedecinDAOImpl implements MedecinDAO {

    @Override
    public Medecin findById(int id) {
        String query = "SELECT m.*, d.nom as departement_nom " +
                "FROM medecins m " +
                "LEFT JOIN departements d ON m.departement_id = d.id " +
                "WHERE m.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractMedecinFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Medecin> findAll() {
        List<Medecin> medecins = new ArrayList<>();

        String query =
                "SELECT " +
                        "m.id, " +
                        "m.user_id, " +
                        "m.departement_id, " +
                        "m.specialite, " +
                        "m.numero_ordre, " +
                        "m.diplomes, " +
                        "m.annees_experience, " +
                        "m.tarif_consultation, " +
                        "m.actif, " +
                        "m.image, " +
                        "u.nom, " +
                        "u.prenom, " +
                        "u.email, " +
                        "u.password, " +
                        "u.telephone, " +
                        "u.adresse, " +
                        "d.nom AS departement_nom " +
                        "FROM medecins m " +
                        "JOIN users u ON m.user_id = u.id " +
                        "LEFT JOIN departements d ON m.departement_id = d.id " +
                        "ORDER BY m.id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                medecins.add(extractMedecinFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("✗ Erreur lors de la récupération des médecins: " + e.getMessage());
            e.printStackTrace();
        }
        return medecins;
    }



    @Override
    public List<Medecin> searchByName(String keyword) {
        List<Medecin> medecins = new ArrayList<>();
        String query =
                "SELECT m.id, m.user_id, m.departement_id, m.specialite, m.numero_ordre, " +
                        "m.diplomes, m.annees_experience, m.tarif_consultation, m.actif, m.image, " +
                        "u.nom, u.prenom, u.email, u.password, u.telephone, u.adresse, " +
                        "d.nom AS departement_nom " +
                        "FROM medecins m " +
                        "JOIN users u ON m.user_id = u.id " +
                        "LEFT JOIN departements d ON m.departement_id = d.id " +
                        "WHERE u.nom LIKE ? OR u.prenom LIKE ? OR m.specialite LIKE ? " +
                        "ORDER BY u.nom, u.prenom";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    medecins.add(extractMedecinFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("✗ Erreur recherche médecins: " + e.getMessage());
            e.printStackTrace();
        }
        return medecins;
    }


    @Override
    public List<Medecin> findBySpecialite(String specialite) {
        List<Medecin> medecins = new ArrayList<>();
        String query = "SELECT m.*, d.nom as departement_nom " +
                "FROM medecins m " +
                "LEFT JOIN departements d ON m.departement_id = d.id " +
                "WHERE m.specialite = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, specialite);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                medecins.add(extractMedecinFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return medecins;
    }

    @Override
    public List<Medecin> findByDepartement(int departementId) {
        List<Medecin> medecins = new ArrayList<>();
        String query = "SELECT m.*, d.nom as departement_nom " +
                "FROM medecins m " +
                "LEFT JOIN departements d ON m.departement_id = d.id " +
                "WHERE m.departement_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, departementId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                medecins.add(extractMedecinFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return medecins;
    }

    // MedecinDAOImpl.java
    @Override
    public void save(Medecin medecin) {
        String query = "INSERT INTO medecins (" +
                "user_id, departement_id, specialite, numero_ordre, diplomes, " +
                "annees_experience, tarif_consultation, actif, image" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, medecin.getUserId());          // DOIT être un id existant dans users
            stmt.setObject(2, medecin.getDepartementId());
            stmt.setString(3, medecin.getSpecialite());
            stmt.setString(4, medecin.getNumeroOrdre());
            stmt.setString(5, medecin.getDiplomes());
            stmt.setObject(6, medecin.getAnneesExperience());
            stmt.setObject(7, medecin.getTarifConsultation());
            stmt.setBoolean(8, medecin.isActif());
            stmt.setString(9, medecin.getImage());

            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                medecin.setId(generatedKeys.getInt(1));
            }
            System.out.println("✓ Médecin ajouté: " + medecin.getFullName());
        } catch (SQLException e) {
            System.err.println("✗ Erreur lors de l'ajout du médecin: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public Medecin findByUserId(int userId) {
        String sql =
                "SELECT " +
                        "m.id, m.user_id, m.departement_id, m.specialite, m.numero_ordre, " +
                        "m.diplomes, m.annees_experience, m.tarif_consultation, m.actif, m.image, " +
                        "u.nom, u.prenom, u.email, u.password, u.telephone, u.adresse, " +
                        "d.nom AS departement_nom " +
                        "FROM medecins m " +
                        "JOIN users u ON m.user_id = u.id " +
                        "LEFT JOIN departements d ON m.departement_id = d.id " +
                        "WHERE m.user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractMedecinFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }




    @Override
    public void update(Medecin medecin) {
        String query = "UPDATE medecins SET " +
                "user_id = ?, departement_id = ?, specialite = ?, numero_ordre = ?, " +
                "diplomes = ?, annees_experience = ?, tarif_consultation = ?, " +
                "actif = ?, image = ? " +
                "WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, medecin.getUserId());
            stmt.setObject(2, medecin.getDepartementId());
            stmt.setString(3, medecin.getSpecialite());
            stmt.setString(4, medecin.getNumeroOrdre());
            stmt.setString(5, medecin.getDiplomes());
            stmt.setObject(6, medecin.getAnneesExperience());
            stmt.setObject(7, medecin.getTarifConsultation());
            stmt.setBoolean(8, medecin.isActif());
            stmt.setString(9, medecin.getImage());
            stmt.setInt(10, medecin.getId());

            stmt.executeUpdate();
            System.out.println("✓ Médecin modifié: " + medecin.getFullName());
        } catch (SQLException e) {
            System.err.println("✗ Erreur lors de la modification du médecin: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int id) {
        String query = "DELETE FROM medecins WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("✓ Médecin supprimé (ID: " + id + ")");
        } catch (SQLException e) {
            System.err.println("✗ Erreur lors de la suppression du médecin: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public long count() {
        String query = "SELECT COUNT(*) FROM medecins";
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

    private Medecin extractMedecinFromResultSet(ResultSet rs) throws SQLException {
        Medecin m = new Medecin();
        m.setId(rs.getInt("id"));
        m.setUserId(rs.getInt("user_id"));
        m.setDepartementId((Integer) rs.getObject("departement_id"));
        m.setSpecialite(rs.getString("specialite"));
        m.setNumeroOrdre(rs.getString("numero_ordre"));
        m.setDiplomes(rs.getString("diplomes"));
        m.setAnneesExperience((Integer) rs.getObject("annees_experience"));
        m.setTarifConsultation(rs.getBigDecimal("tarif_consultation"));
        m.setActif(rs.getBoolean("actif"));
        m.setImage(rs.getString("image"));

        // champs hérités de users
        m.setNom(rs.getString("nom"));
        m.setPrenom(rs.getString("prenom"));
        m.setEmail(rs.getString("email"));
        m.setPassword(rs.getString("password"));
        m.setTelephone(rs.getString("telephone"));
        m.setAdresse(rs.getString("adresse"));

        m.setDepartementNom(rs.getString("departement_nom"));
        return m;
    }

}
