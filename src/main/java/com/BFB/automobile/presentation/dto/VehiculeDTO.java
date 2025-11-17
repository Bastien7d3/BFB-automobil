package com.BFB.automobile.presentation.dto;

import com.BFB.automobile.data.EtatVehicule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

/**
 * DTO pour la création et mise à jour de véhicules
 */
public class VehiculeDTO {
    
    private Long id;
    
    @NotBlank(message = "La marque est obligatoire")
    private String marque;
    
    @NotBlank(message = "Le modèle est obligatoire")
    private String modele;
    
    @NotBlank(message = "La motorisation est obligatoire")
    private String motorisation;
    
    @NotBlank(message = "La couleur est obligatoire")
    private String couleur;
    
    @NotBlank(message = "L'immatriculation est obligatoire")
    private String immatriculation;
    
    @NotNull(message = "La date d'acquisition est obligatoire")
    @Past(message = "La date d'acquisition doit être dans le passé")
    private LocalDate dateAcquisition;
    
    private EtatVehicule etat;
    
    // Constructeurs
    public VehiculeDTO() {}
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getMarque() { return marque; }
    public void setMarque(String marque) { this.marque = marque; }
    
    public String getModele() { return modele; }
    public void setModele(String modele) { this.modele = modele; }
    
    public String getMotorisation() { return motorisation; }
    public void setMotorisation(String motorisation) { this.motorisation = motorisation; }
    
    public String getCouleur() { return couleur; }
    public void setCouleur(String couleur) { this.couleur = couleur; }
    
    public String getImmatriculation() { return immatriculation; }
    public void setImmatriculation(String immatriculation) { this.immatriculation = immatriculation; }
    
    public LocalDate getDateAcquisition() { return dateAcquisition; }
    public void setDateAcquisition(LocalDate dateAcquisition) { this.dateAcquisition = dateAcquisition; }
    
    public EtatVehicule getEtat() { return etat; }
    public void setEtat(EtatVehicule etat) { this.etat = etat; }
}
