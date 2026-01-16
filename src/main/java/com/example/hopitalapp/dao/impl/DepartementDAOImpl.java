package com.example.hopitalapp.dao.impl;

import com.example.hopitalapp.dao.interfaces.DepartementDAO;
import com.example.hopitalapp.model.Departement;
import com.example.hopitalapp.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DepartementDAOImpl implements DepartementDAO {

    @Override
    public Departement findById(int id) {
        String query =
                "SELECT d.id, d.nom, d.description, d.etage, d.nombre_lits, d.chef_service_id, d.telephone, " +
                        "       u.nom AS chef_nom, u.prenom AS chef_prenom, u.email AS chef_email, " +
                        "       d.created_at, d.updated_at " +
                        "FROM departements d " +
                        "LEFT JOIN medecins m ON d.chef_service_id = m.id " +
                        "LEFT JOIN users u ON m.user_id = u.id " +
                        "WHERE d.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractDepartementFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Departement> findAll() {
        List<Departement> departements = new ArrayList<>();

        String query =
                "SELECT d.id, d.nom, d.description, d.etage, d.nombre_lits, d.chef_service_id, d.telephone, " +
                        "       u.nom AS chef_nom, u.prenom AS chef_prenom, u.email AS chef_email, " +
                        "       d.created_at, d.updated_at " +
                        "FROM departements d " +
                        "LEFT JOIN medecins m ON d.chef_service_id = m.id " +
                        "LEFT JOIN users u ON m.user_id = u.id " +
                        "ORDER BY d.id ASC";   // ID 1..15

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                departements.add(extractDepartementFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return departements;
    }

    @Override
    public void save(Departement departement) {
        String query =
                "INSERT INTO departements (nom, description, etage, nombre_lits, chef_service_id, telephone) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, departement.getNom());
            stmt.setString(2, departement.getDescription());
            stmt.setString(3, departement.getEtage());
            stmt.setInt(4, departement.getNombreLits());
            stmt.setObject(5, departement.getChefServiceId());
            stmt.setString(6, departement.getTelephone());

            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                departement.setId(generatedKeys.getInt(1));
            }
            System.out.println("✓ Département ajouté: " + departement.getNom());
        } catch (SQLException e) {
            System.err.println("✗ Erreur lors de l'ajout du département: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void update(Departement departement) {
        String query =
                "UPDATE departements SET nom = ?, description = ?, etage = ?, nombre_lits = ?, chef_service_id = ?, telephone = ? " +
                        "WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, departement.getNom());
            stmt.setString(2, departement.getDescription());
            stmt.setString(3, departement.getEtage());
            stmt.setInt(4, departement.getNombreLits());
            stmt.setObject(5, departement.getChefServiceId());
            stmt.setString(6, departement.getTelephone());
            stmt.setInt(7, departement.getId());

            stmt.executeUpdate();
            System.out.println("✓ Département modifié: " + departement.getNom());
        } catch (SQLException e) {
            System.err.println("✗ Erreur lors de la modification du département: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int id) {
        String query = "DELETE FROM departements WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("✓ Département supprimé (ID: " + id + ")");
        } catch (SQLException e) {
            System.err.println("✗ Erreur lors de la suppression du département: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public long count() {
        String query = "SELECT COUNT(*) FROM departements";

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

    private Departement extractDepartementFromResultSet(ResultSet rs) throws SQLException {
        Departement d = new Departement();
        d.setId(rs.getInt("id"));
        d.setNom(rs.getString("nom"));

        try { d.setDescription(rs.getString("description")); } catch (SQLException e) { d.setDescription(null); }
        try { d.setEtage(rs.getString("etage")); } catch (SQLException e) { d.setEtage(null); }
        try { d.setNombreLits(rs.getInt("nombre_lits")); } catch (SQLException e) { d.setNombreLits(0); }
        try { d.setChefServiceId((Integer) rs.getObject("chef_service_id")); } catch (SQLException e) { d.setChefServiceId(null); }
        try { d.setTelephone(rs.getString("telephone")); } catch (SQLException e) { d.setTelephone(null); }

        // Chef : nom complet + email via users
        String chefNom = null;
        String chefPrenom = null;
        String chefEmail = null;
        try { chefNom = rs.getString("chef_nom"); } catch (SQLException ignored) {}
        try { chefPrenom = rs.getString("chef_prenom"); } catch (SQLException ignored) {}
        try { chefEmail = rs.getString("chef_email"); } catch (SQLException ignored) {}

        if (chefNom != null) {
            d.setChefNomComplet((chefPrenom != null ? chefPrenom + " " : "") + chefNom);
        } else {
            d.setChefNomComplet(null);
        }
        d.setChefEmail(chefEmail);

        try {
            Timestamp createdAt = rs.getTimestamp("created_at");
            if (createdAt != null) d.setCreatedAt(createdAt.toLocalDateTime());
        } catch (SQLException ignored) {}

        try {
            Timestamp updatedAt = rs.getTimestamp("updated_at");
            if (updatedAt != null) d.setUpdatedAt(updatedAt.toLocalDateTime());
        } catch (SQLException ignored) {}

        return d;
    }
}
