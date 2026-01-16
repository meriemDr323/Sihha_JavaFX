package com.example.hopitalapp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Medecin {
    private int id;
    private int userId;
    private Integer departementId;
    private String specialite;
    private String numeroOrdre;
    private String diplomes;
    private Integer anneesExperience;
    private BigDecimal tarifConsultation;
    private boolean actif;
    private String image;

    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String adresse;
    private String ville;
    private String password;

    private String departementNom;

    public Medecin() {
        this.actif = true;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Integer getDepartementId() {
        return departementId;
    }

    public void setDepartementId(Integer departementId) {
        this.departementId = departementId;
    }

    public String getSpecialite() {
        return specialite;
    }

    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }

    public String getNumeroOrdre() {
        return numeroOrdre;
    }

    public void setNumeroOrdre(String numeroOrdre) {
        this.numeroOrdre = numeroOrdre;
    }

    public String getDiplomes() {
        return diplomes;
    }

    public void setDiplomes(String diplomes) {
        this.diplomes = diplomes;
    }

    public Integer getAnneesExperience() {
        return anneesExperience;
    }

    public void setAnneesExperience(Integer anneesExperience) {
        this.anneesExperience = anneesExperience;
    }

    public BigDecimal getTarifConsultation() {
        return tarifConsultation;
    }

    public void setTarifConsultation(BigDecimal tarifConsultation) {
        this.tarifConsultation = tarifConsultation;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getDepartementNom() {
        return departementNom;
    }

    public void setDepartementNom(String departementNom) {
        this.departementNom = departementNom;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Méthodes utilitaires
    public String getFullName() {
        return "Dr. " + prenom + " " + nom;
    }

    public String getStatutText() {
        return actif ? "Actif" : "Inactif";
    }


    @Override
    public String toString() {
        return getFullName() + " - " + (specialite != null ? specialite : "Généraliste");
    }
}
