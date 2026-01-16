package com.example.hopitalapp.dao.interfaces;

import com.example.hopitalapp.model.RendezVous;

import java.time.LocalDateTime;
import java.util.List;

public interface RendezVousDAO {

    List<RendezVous> findAll();

    List<RendezVous> findByMedecin(int medecinId);

    List<RendezVous> findByPatient(int patientId);

    void save(RendezVous rdv);

    void update(RendezVous rdv);

    void delete(int id);

    long count();

    boolean isMedecinDisponible(int medecinId, LocalDateTime dateDebut, int dureeMinutes);
}
