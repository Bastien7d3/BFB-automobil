package com.BFB.automobile.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * COUCHE STOCKAGE - Entité Véhicule
 * Simple POJO avec validation.
 */
@Document(collection = "vehicules")
public class Vehicule {
    
    @Id
    private String id;
    
    @NotNull
    private String marque;
    
    @NotNull
    private String modele;
    
    @Min(1900)
    private Integer annee;
    
    @Min(0)
    private Double prix;
    
    // Constructeurs
    public Vehicule() {}
    
    public Vehicule(String marque, String modele, Integer annee, Double prix) {
        this.marque = marque;
        this.modele = modele;
        this.annee = annee;
        this.prix = prix;
    }
    
    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getMarque() { return marque; }
    public void setMarque(String marque) { this.marque = marque; }
    
    public String getModele() { return modele; }
    public void setModele(String modele) { this.modele = modele; }
    
    public Integer getAnnee() { return annee; }
    public void setAnnee(Integer annee) { this.annee = annee; }
    
    public Double getPrix() { return prix; }
    public void setPrix(Double prix) { this.prix = prix; }
}
