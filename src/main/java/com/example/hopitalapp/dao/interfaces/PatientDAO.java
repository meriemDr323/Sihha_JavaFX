package com.example.hopitalapp.dao.interfaces;

import com.example.hopitalapp.model.Patient;
import java.util.List;

public interface PatientDAO {
    Patient findById(int id);
    List<Patient> findAll();
    List<Patient> searchByName(String keyword);
    List<Patient> findByMedecin(int medecinId);

    void save(Patient patient);
    void update(Patient patient);
    void delete(int id);
    long count();
}
