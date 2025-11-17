package com.BFB.automobile.data;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entité Contrat - Représente un contrat de location liant un client à un véhicule
 * Gère les différents états du cycle de vie d'une location
 */
@Entity
@Table(name = "contrats",
    indexes = {
        @Index(name = "idx_contrat_client", columnList = "client_id"),
        @Index(name = "idx_contrat_vehicule", columnList = "vehicule_id"),
        @Index(name = "idx_contrat_dates", columnList = "date_debut, date_fin"),
        @Index(name = "idx_contrat_etat", columnList = "etat")
    }
)
public class Contrat {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "La date de début est obligatoire")
    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;
    
    @NotNull(message = "La date de fin est obligatoire")
    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;
    
    @NotNull(message = "L'état est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EtatContrat etat;
    
    @NotNull(message = "Le client est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    
    @NotNull(message = "Le véhicule est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicule_id", nullable = false)
    private Vehicule vehicule;
    
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDate dateCreation;
    
    @Column(name = "date_modification")
    private LocalDate dateModification;
    
    @Column(length = 1000)
    private String commentaire;
    
    // Constructeurs
    public Contrat() {
        this.dateCreation = LocalDate.now();
        this.etat = EtatContrat.EN_ATTENTE;
    }
    
    public Contrat(LocalDate dateDebut, LocalDate dateFin, Client client, Vehicule vehicule) {
        this();
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.client = client;
        this.vehicule = vehicule;
    }
    
    // Getters et Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDate getDateDebut() {
        return dateDebut;
    }
    
    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }
    
    public LocalDate getDateFin() {
        return dateFin;
    }
    
    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }
    
    public EtatContrat getEtat() {
        return etat;
    }
    
    public void setEtat(EtatContrat etat) {
        this.etat = etat;
        this.dateModification = LocalDate.now();
    }
    
    public Client getClient() {
        return client;
    }
    
    public void setClient(Client client) {
        this.client = client;
    }
    
    public Vehicule getVehicule() {
        return vehicule;
    }
    
    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
    }
    
    public LocalDate getDateCreation() {
        return dateCreation;
    }
    
    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }
    
    public LocalDate getDateModification() {
        return dateModification;
    }
    
    public void setDateModification(LocalDate dateModification) {
        this.dateModification = dateModification;
    }
    
    public String getCommentaire() {
        return commentaire;
    }
    
    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }
    
    // Méthodes métier utiles
    public boolean estActif() {
        return etat == EtatContrat.EN_COURS || etat == EtatContrat.EN_ATTENTE;
    }
    
    public boolean estEnRetard() {
        return etat == EtatContrat.EN_RETARD;
    }
    
    public boolean chevauche(LocalDate debut, LocalDate fin) {
        return !(this.dateFin.isBefore(debut) || this.dateDebut.isAfter(fin));
    }
    
    public boolean doitCommencerAujourdhui() {
        return this.dateDebut.equals(LocalDate.now()) && 
               this.etat == EtatContrat.EN_ATTENTE;
    }
    
    public boolean estTermine() {
        return LocalDate.now().isAfter(this.dateFin);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contrat contrat = (Contrat) o;
        return Objects.equals(id, contrat.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Contrat{" +
                "id=" + id +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", etat=" + etat +
                ", clientId=" + (client != null ? client.getId() : null) +
                ", vehiculeId=" + (vehicule != null ? vehicule.getId() : null) +
                '}';
    }
}
