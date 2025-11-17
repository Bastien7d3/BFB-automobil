package com.BFB.automobile.data;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entité Véhicule - Représente un véhicule disponible à la location
 * Contraintes d'unicité :
 * - Numéro d'immatriculation unique
 */
@Entity
@Table(name = "vehicules",
    uniqueConstraints = @UniqueConstraint(name = "uk_vehicule_immatriculation", 
                                          columnNames = "immatriculation")
)
public class Vehicule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "La marque est obligatoire")
    @Column(nullable = false, length = 100)
    private String marque;
    
    @NotBlank(message = "Le modèle est obligatoire")
    @Column(nullable = false, length = 100)
    private String modele;
    
    @NotBlank(message = "La motorisation est obligatoire")
    @Column(nullable = false, length = 100)
    private String motorisation;
    
    @NotBlank(message = "La couleur est obligatoire")
    @Column(nullable = false, length = 50)
    private String couleur;
    
    @NotBlank(message = "Le numéro d'immatriculation est obligatoire")
    @Column(nullable = false, unique = true, length = 20)
    private String immatriculation;
    
    @NotNull(message = "La date d'acquisition est obligatoire")
    @Past(message = "La date d'acquisition doit être dans le passé")
    @Column(name = "date_acquisition", nullable = false)
    private LocalDate dateAcquisition;
    
    @NotNull(message = "L'état est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EtatVehicule etat;
    
    // Constructeurs
    public Vehicule() {
        this.etat = EtatVehicule.DISPONIBLE;
    }
    
    public Vehicule(String marque, String modele, String motorisation, String couleur,
                    String immatriculation, LocalDate dateAcquisition) {
        this();
        this.marque = marque;
        this.modele = modele;
        this.motorisation = motorisation;
        this.couleur = couleur;
        this.immatriculation = immatriculation;
        this.dateAcquisition = dateAcquisition;
    }
    
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
    
    // Méthodes utiles
    public boolean estDisponible() {
        return this.etat == EtatVehicule.DISPONIBLE;
    }
    
    public boolean estEnPanne() {
        return this.etat == EtatVehicule.EN_PANNE;
    }
    
    // Méthodes equals et hashCode basées sur l'immatriculation
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vehicule vehicule = (Vehicule) o;
        return Objects.equals(immatriculation, vehicule.immatriculation);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(immatriculation);
    }
    
    @Override
    public String toString() {
        return "Vehicule{" +
                "id=" + id +
                ", marque='" + marque + '\'' +
                ", modele='" + modele + '\'' +
                ", immatriculation='" + immatriculation + '\'' +
                ", etat=" + etat +
                '}';
    }
}
