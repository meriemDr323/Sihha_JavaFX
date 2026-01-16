package com.example.hopitalapp.model;

import java.time.LocalDateTime;

public class DossierMedical {

    private int id;
    private int patientId;
    private int rendezVousId;
    private int medecinId;
    private LocalDateTime dateConsultation;
    private String diagnostic;
    private String symptomes;
    private String traitementPrescrit;
    private String observations;
    private Double poids;
    private Double taille;
    private String tensionArterielle;
    private Double temperature;
    private LocalDateTime createdAt;

    private String patientNomComplet;
    private String medecinNomComplet;


    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public int getRendezVousId() { return rendezVousId; }
    public void setRendezVousId(int rendezVousId) { this.rendezVousId = rendezVousId; }

    public int getMedecinId() { return medecinId; }
    public void setMedecinId(int medecinId) { this.medecinId = medecinId; }

    public LocalDateTime getDateConsultation() { return dateConsultation; }
    public void setDateConsultation(LocalDateTime dateConsultation) { this.dateConsultation = dateConsultation; }

    public String getDiagnostic() { return diagnostic; }
    public void setDiagnostic(String diagnostic) { this.diagnostic = diagnostic; }

    public String getSymptomes() { return symptomes; }
    public void setSymptomes(String symptomes) { this.symptomes = symptomes; }

    public String getTraitementPrescrit() { return traitementPrescrit; }
    public void setTraitementPrescrit(String traitementPrescrit) { this.traitementPrescrit = traitementPrescrit; }

    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }

    public Double getPoids() { return poids; }
    public void setPoids(Double poids) { this.poids = poids; }

    public Double getTaille() { return taille; }
    public void setTaille(Double taille) { this.taille = taille; }

    public String getTensionArterielle() { return tensionArterielle; }
    public void setTensionArterielle(String tensionArterielle) { this.tensionArterielle = tensionArterielle; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getPatientNomComplet() { return patientNomComplet; }
    public void setPatientNomComplet(String patientNomComplet) { this.patientNomComplet = patientNomComplet; }

    public String getMedecinNomComplet() { return medecinNomComplet; }
    public void setMedecinNomComplet(String medecinNomComplet) { this.medecinNomComplet = medecinNomComplet; }
}
