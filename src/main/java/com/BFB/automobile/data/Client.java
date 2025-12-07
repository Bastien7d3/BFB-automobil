package com.BFB.automobile.data;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entité Client - Représente une personne physique pouvant louer des véhicules
 * Contraintes d'unicité :
 * - Combinaison nom + prénom + date de naissance unique
 * - Numéro de permis unique
 */
@Entity
@Table(name = "clients", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_client_identity", 
                         columnNames = {"nom", "prenom", "date_naissance"}),
        @UniqueConstraint(name = "uk_client_permis", 
                         columnNames = {"numero_permis"})
    }
)
public class Client {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Le nom est obligatoire")
    @Column(nullable = false, length = 100)
    private String nom;
    
    @NotBlank(message = "Le prénom est obligatoire")
    @Column(nullable = false, length = 100)
    private String prenom;
    
    @NotNull(message = "La date de naissance est obligatoire")
    @Past(message = "La date de naissance doit être dans le passé")
    @Column(name = "date_naissance", nullable = false)
    private LocalDate dateNaissance;
    
    @NotBlank(message = "Le numéro de permis est obligatoire")
    @Column(name = "numero_permis", nullable = false, unique = true, length = 50)
    private String numeroPermis;
    
    @NotBlank(message = "L'adresse est obligatoire")
    @Column(nullable = false, length = 500)
    private String adresse;
    
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDate dateCreation;
    
    @Column(name = "actif", nullable = false)
    private Boolean actif = true;
    
    // Constructeurs
    public Client() {
        this.dateCreation = LocalDate.now();
        this.actif = true;
    }
    
    public Client(String nom, String prenom, LocalDate dateNaissance, 
                  String numeroPermis, String adresse) {
        this();
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
        this.numeroPermis = numeroPermis;
        this.adresse = adresse;
    }
    
    // Getters et Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public LocalDate getDateNaissance() {
        return dateNaissance;
    }
    
    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }
    
    public String getNumeroPermis() {
        return numeroPermis;
    }
    
    public void setNumeroPermis(String numeroPermis) {
        this.numeroPermis = numeroPermis;
    }
    
    public String getAdresse() {
        return adresse;
    }
    
    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }
    
    public LocalDate getDateCreation() {
        return dateCreation;
    }
    
    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }
    
    public Boolean getActif() {
        return actif;
    }
    
    public void setActif(Boolean actif) {
        this.actif = actif;
    }
    
    // Méthodes equals et hashCode basées sur les critères d'unicité métier
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return Objects.equals(nom, client.nom) &&
               Objects.equals(prenom, client.prenom) &&
               Objects.equals(dateNaissance, client.dateNaissance);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(nom, prenom, dateNaissance);
    }
    
    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", dateNaissance=" + dateNaissance +
                ", numeroPermis='" + numeroPermis + '\'' +
                ", actif=" + actif +
                '}';
    }
    
    /**
     * BUILDER PATTERN : Permet de construire un objet Client de manière fluide et lisible
     */
    public static ClientBuilder builder() {
        return new ClientBuilder();
    }
    
    public static class ClientBuilder {
        private String nom;
        private String prenom;
        private LocalDate dateNaissance;
        private String numeroPermis;
        private String adresse;
        private Boolean actif = true;
        
        private ClientBuilder() {}
        
        public ClientBuilder nom(String nom) {
            this.nom = nom;
            return this;
        }
        
        public ClientBuilder prenom(String prenom) {
            this.prenom = prenom;
            return this;
        }
        
        public ClientBuilder dateNaissance(LocalDate dateNaissance) {
            this.dateNaissance = dateNaissance;
            return this;
        }
        
        public ClientBuilder numeroPermis(String numeroPermis) {
            this.numeroPermis = numeroPermis;
            return this;
        }
        
        public ClientBuilder adresse(String adresse) {
            this.adresse = adresse;
            return this;
        }
        
        public ClientBuilder actif(Boolean actif) {
            this.actif = actif;
            return this;
        }
        
        public Client build() {
            Client client = new Client();
            client.setNom(this.nom);
            client.setPrenom(this.prenom);
            client.setDateNaissance(this.dateNaissance);
            client.setNumeroPermis(this.numeroPermis);
            client.setAdresse(this.adresse);
            client.setActif(this.actif);
            return client;
        }
    }
}
