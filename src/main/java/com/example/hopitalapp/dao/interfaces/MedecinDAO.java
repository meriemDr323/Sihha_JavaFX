package com.example.hopitalapp.dao.interfaces;

import com.example.hopitalapp.model.Medecin;
import java.util.List;

public interface MedecinDAO {
    Medecin findById(int id);
    List<Medecin> findAll();
    List<Medecin> searchByName(String keyword);
    List<Medecin> findBySpecialite(String specialite);
    List<Medecin> findByDepartement(int departementId);
    Medecin findByUserId(int userId);
    void save(Medecin medecin);
    void update(Medecin medecin);
    void delete(int id);
    long count();
}
