package com.example.hopitalapp.model;

import java.time.LocalDateTime;

public class Departement {
    private int id;
    private String nom;
    private String description;
    private String etage;
    private int nombreLits;
    private Integer chefServiceId;
    private String telephone;

    private String chefNomComplet;
    private String chefEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Departement() {}

    public Departement(String nom) {
        this.nom = nom;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEtage() { return etage; }
    public void setEtage(String etage) { this.etage = etage; }

    public int getNombreLits() { return nombreLits; }
    public void setNombreLits(int nombreLits) { this.nombreLits = nombreLits; }

    public Integer getChefServiceId() { return chefServiceId; }
    public void setChefServiceId(Integer chefServiceId) { this.chefServiceId = chefServiceId; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getChefNomComplet() { return chefNomComplet; }
    public void setChefNomComplet(String chefNomComplet) { this.chefNomComplet = chefNomComplet; }

    public String getChefEmail() { return chefEmail; }
    public void setChefEmail(String chefEmail) { this.chefEmail = chefEmail; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return nom;
    }
}
