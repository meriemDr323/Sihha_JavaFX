package com.example.hopitalapp.model;

import java.time.LocalDateTime;

public class Hospital {
    private int id;
    private String nom;
    private String adresse;
    private String ville;
    private String codePostal;
    private String telephone;
    private String email;
    private String siteWeb;
    private int nombreLitsTotal;
    private int capaciteUrgences;
    private String logoPath;
    private String description;
    private LocalDateTime updatedAt;

    public Hospital() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public String getCodePostal() { return codePostal; }
    public void setCodePostal(String codePostal) { this.codePostal = codePostal; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSiteWeb() { return siteWeb; }
    public void setSiteWeb(String siteWeb) { this.siteWeb = siteWeb; }

    public int getNombreLitsTotal() { return nombreLitsTotal; }
    public void setNombreLitsTotal(int nombreLitsTotal) { this.nombreLitsTotal = nombreLitsTotal; }

    public int getCapaciteUrgences() { return capaciteUrgences; }
    public void setCapaciteUrgences(int capaciteUrgences) { this.capaciteUrgences = capaciteUrgences; }

    public String getLogoPath() { return logoPath; }
    public void setLogoPath(String logoPath) { this.logoPath = logoPath; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
