package com.example.hopitalapp.dao.impl;

import com.example.hopitalapp.dao.interfaces.HospitalDAO;
import com.example.hopitalapp.model.Hospital;
import com.example.hopitalapp.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;

public class HospitalDAOImpl implements HospitalDAO {

    @Override
    public Hospital getInfo() {
        String query = "SELECT * FROM hopital ORDER BY id LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return extractHospital(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void saveOrUpdate(Hospital hospital) {
        if (hospital.getId() == 0) {
            insert(hospital);
        } else {
            update(hospital);
        }
    }

    private void insert(Hospital h) {
        String query = "INSERT INTO hopital " +
                "(nom, adresse, ville, code_postal, telephone, email, site_web, " +
                " nombre_lits_total, capacite_urgences, logo_path, description) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            fillStatementFromHospital(stmt, h);
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                h.setId(keys.getInt(1));
            }
            System.out.println("✓ Infos hôpital créées.");
        } catch (SQLException e) {
            System.err.println("✗ Erreur lors de la création des infos hôpital: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void update(Hospital h) {
        String query = "UPDATE hopital SET " +
                "nom=?, adresse=?, ville=?, code_postal=?, telephone=?, email=?, site_web=?, " +
                "nombre_lits_total=?, capacite_urgences=?, logo_path=?, description=? " +
                "WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            fillStatementFromHospital(stmt, h);
            stmt.setInt(12, h.getId());

            stmt.executeUpdate();
            System.out.println("✓ Infos hôpital mises à jour.");
        } catch (SQLException e) {
            System.err.println("✗ Erreur lors de la mise à jour des infos hôpital: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void fillStatementFromHospital(PreparedStatement stmt, Hospital h) throws SQLException {
        stmt.setString(1, h.getNom());
        stmt.setString(2, h.getAdresse());
        stmt.setString(3, h.getVille());
        stmt.setString(4, h.getCodePostal());
        stmt.setString(5, h.getTelephone());
        stmt.setString(6, h.getEmail());
        stmt.setString(7, h.getSiteWeb());
        stmt.setInt(8, h.getNombreLitsTotal());
        stmt.setInt(9, h.getCapaciteUrgences());
        stmt.setString(10, h.getLogoPath());
        stmt.setString(11, h.getDescription());
    }

    private Hospital extractHospital(ResultSet rs) throws SQLException {
        Hospital h = new Hospital();
        h.setId(rs.getInt("id"));
        h.setNom(rs.getString("nom"));
        h.setAdresse(rs.getString("adresse"));
        h.setVille(rs.getString("ville"));
        h.setCodePostal(rs.getString("code_postal"));
        h.setTelephone(rs.getString("telephone"));
        h.setEmail(rs.getString("email"));
        h.setSiteWeb(rs.getString("site_web"));
        h.setNombreLitsTotal(rs.getInt("nombre_lits_total"));
        h.setCapaciteUrgences(rs.getInt("capacite_urgences"));
        h.setLogoPath(rs.getString("logo_path"));
        h.setDescription(rs.getString("description"));
        try {
            Timestamp ts = rs.getTimestamp("updated_at");
            if (ts != null) {
                h.setUpdatedAt(ts.toLocalDateTime());
            }
        } catch (SQLException ignored) {}
        return h;
    }
}
