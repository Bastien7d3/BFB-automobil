package com.BFB.automobile.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * COUCHE STOCKAGE - Entity / Domain Model
 * ═══════════════════════════════════════════════════════════════════════════
 * 
 * PATTERN: POJO (Plain Old Java Object) / Entity Pattern
 * 
 * DESCRIPTION:
 * Un POJO est un objet Java simple sans dépendance à un framework spécifique.
 * Ici, on l'utilise comme:
 * - Modèle de données (Entity) pour MongoDB
 * - DTO (Data Transfer Object) pour les échanges entre couches
 * - Objet métier partagé par toutes les couches
 * 
 * POURQUOI DANS LA COUCHE DATA:
 * - C'est une entité de persistance (@Document MongoDB)
 * - Elle fait partie de la couche stockage
 * - Les couches présentation et métier l'importent pour l'utiliser
 * 
 * POURQUOI CE CHOIX:
 * - Simplicité: pas besoin de DTO séparés pour un projet simple
 * - Cohérence: même structure partout (présentation, métier, stockage)
 * - Réutilisabilité: un seul objet à maintenir
 * 
 * ANNOTATIONS UTILISÉES:
 * - @Document: indique que c'est une entité MongoDB (collection "vehicules")
 * - @Id: identifiant unique généré par MongoDB
 * - @NotNull, @Min: contraintes de validation (Bean Validation JSR-303)
 * 
 * AMÉLIORATION POSSIBLE:
 * Dans un projet plus complexe, on séparerait:
 * - VehiculeEntity (couche stockage - ce fichier)
 * - VehiculeDTO (couche présentation)
 * - VehiculeDomain (couche métier)
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Document(collection = "vehicules") // PATTERN: Document/Entity Pattern - Mapping objet ↔ collection MongoDB
public class Vehicule {
    
    // ========== PATTERN: Identity Field ==========
    // Chaque entité a un identifiant unique pour la persistance
    // MongoDB génère automatiquement cet ID si non fourni
    @Id
    private String id;
    
    // ========== PATTERN: Bean Validation (JSR-303/380) ==========
    // Les contraintes sont définies sur le modèle, pas dans la logique métier
    // Avantage: validation déclarative, réutilisable, centralisée
    
    @NotNull(message = "La marque est obligatoire")
    private String marque;
    
    @NotNull(message = "Le modèle est obligatoire")
    private String modele;
    
    @Min(value = 1900, message = "L'année doit être supérieure à 1900")
    private Integer annee;
    
    @Min(value = 0, message = "Le prix doit être positif")
    private Double prix;
    
    // ========== PATTERN: JavaBean Convention ==========
    // Constructeur par défaut requis pour:
    // - Frameworks de sérialisation (Jackson pour JSON)
    // - Spring Data MongoDB (création d'instances via réflexion)
    // - Frameworks de validation
    public Vehicule() {
    }
    
    // Constructeur avec paramètres pour faciliter la création en code
    // Pas obligatoire mais pratique pour les tests et le code métier
    public Vehicule(String marque, String modele, Integer annee, Double prix) {
        this.marque = marque;
        this.modele = modele;
        this.annee = annee;
        this.prix = prix;
    }
    
    // ========== PATTERN: Encapsulation ==========
    // Getters/Setters pour respecter l'encapsulation
    // Spring Data et Jackson utilisent ces méthodes pour accéder aux champs
    // POURQUOI: protection des données, validation possible dans les setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getMarque() {
        return marque;
    }
    
    public void setMarque(String marque) {
        this.marque = marque;
    }
    
    public String getModele() {
        return modele;
    }
    
    public void setModele(String modele) {
        this.modele = modele;
    }
    
    public Integer getAnnee() {
        return annee;
    }
    
    public void setAnnee(Integer annee) {
        this.annee = annee;
    }
    
    public Double getPrix() {
        return prix;
    }
    
    public void setPrix(Double prix) {
        this.prix = prix;
    }
}
