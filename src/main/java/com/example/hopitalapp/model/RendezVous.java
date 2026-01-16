package com.example.hopitalapp.model;

import java.time.LocalDateTime;

public class RendezVous {

    private int id;
    private int patientId;
    private int medecinId;
    private LocalDateTime dateRdv;
    private int duree;
    private String motif;
    private String notes;
    private String statut;
    private int createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Champs dérivés pour affichage
    private String patientNomComplet;
    private String medecinNomComplet;

    public RendezVous() {
        this.duree = 30;
        this.statut = "EN_ATTENTE";
    }

    // Getters / Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public int getMedecinId() { return medecinId; }
    public void setMedecinId(int medecinId) { this.medecinId = medecinId; }

    public LocalDateTime getDateRdv() { return dateRdv; }
    public void setDateRdv(LocalDateTime dateRdv) { this.dateRdv = dateRdv; }

    public int getDuree() { return duree; }
    public void setDuree(int duree) { this.duree = duree; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getPatientNomComplet() { return patientNomComplet; }
    public void setPatientNomComplet(String patientNomComplet) { this.patientNomComplet = patientNomComplet; }

    public String getMedecinNomComplet() { return medecinNomComplet; }
    public void setMedecinNomComplet(String medecinNomComplet) { this.medecinNomComplet = medecinNomComplet; }
}
