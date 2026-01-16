package com.example.hopitalapp.dao.interfaces;

import com.example.hopitalapp.model.DossierMedical;
import java.util.List;

public interface DossierMedicalDAO {

    DossierMedical findByRendezVousId(int rendezVousId);
    
    List<DossierMedical> findByPatient(int patientId);

    void save(DossierMedical dossier);

    void update(DossierMedical dossier);
}
