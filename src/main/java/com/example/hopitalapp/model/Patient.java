package com.example.hopitalapp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Patient {
    private int id;
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private String sexe;
    private String telephone;
    private String email;
    private String adresse;
    private String ville;
    private String codePostal;
    private String numeroSecuriteSociale;
    private String groupeSanguin;
    private String allergies;
    private String assuranceProvider;
    private String numeroAssurance;
    private String contactUrgenceNom;
    private String contactUrgenceTelephone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructeur vide
    public Patient() {}

    // Constructeur avec paramètres essentiels
    public Patient(String nom, String prenom, LocalDate dateNaissance, String sexe, String telephone) {
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
        this.sexe = sexe;
        this.telephone = telephone;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }

    public String getSexe() { return sexe; }
    public void setSexe(String sexe) { this.sexe = sexe; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public String getCodePostal() { return codePostal; }
    public void setCodePostal(String codePostal) { this.codePostal = codePostal; }

    public String getNumeroSecuriteSociale() { return numeroSecuriteSociale; }
    public void setNumeroSecuriteSociale(String numeroSecuriteSociale) {
        this.numeroSecuriteSociale = numeroSecuriteSociale;
    }

    public String getGroupeSanguin() { return groupeSanguin; }
    public void setGroupeSanguin(String groupeSanguin) { this.groupeSanguin = groupeSanguin; }

    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }

    public String getAssuranceProvider() { return assuranceProvider; }
    public void setAssuranceProvider(String assuranceProvider) {
        this.assuranceProvider = assuranceProvider;
    }

    public String getNumeroAssurance() { return numeroAssurance; }
    public void setNumeroAssurance(String numeroAssurance) {
        this.numeroAssurance = numeroAssurance;
    }

    public String getContactUrgenceNom() { return contactUrgenceNom; }
    public void setContactUrgenceNom(String contactUrgenceNom) {
        this.contactUrgenceNom = contactUrgenceNom;
    }

    public String getContactUrgenceTelephone() { return contactUrgenceTelephone; }
    public void setContactUrgenceTelephone(String contactUrgenceTelephone) {
        this.contactUrgenceTelephone = contactUrgenceTelephone;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Méthodes utilitaires
    public String getFullName() {
        return prenom + " " + nom;
    }

    public int getAge() {
        if (dateNaissance != null) {
            return LocalDate.now().getYear() - dateNaissance.getYear();
        }
        return 0;
    }

    @Override
    public String toString() {
        return getFullName() + " (" + getAge() + " ans)";
    }
}
