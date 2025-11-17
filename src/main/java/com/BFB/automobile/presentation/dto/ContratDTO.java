package com.BFB.automobile.presentation.dto;

import com.BFB.automobile.data.EtatContrat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * DTO pour la création et mise à jour de contrats
 */
public class ContratDTO {
    
    private Long id;
    
    @NotNull(message = "La date de début est obligatoire")
    private LocalDate dateDebut;
    
    @NotNull(message = "La date de fin est obligatoire")
    private LocalDate dateFin;
    
    private EtatContrat etat;
    
    @NotNull(message = "L'ID du client est obligatoire")
    private Long clientId;
    
    @NotNull(message = "L'ID du véhicule est obligatoire")
    private Long vehiculeId;
    
    private String commentaire;
    private LocalDate dateCreation;
    private LocalDate dateModification;
    
    // Pour les réponses, inclure les détails du client et du véhicule
    private ClientDTO client;
    private VehiculeDTO vehicule;
    
    // Constructeurs
    public ContratDTO() {}
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
    
    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }
    
    public EtatContrat getEtat() { return etat; }
    public void setEtat(EtatContrat etat) { this.etat = etat; }
    
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    
    public Long getVehiculeId() { return vehiculeId; }
    public void setVehiculeId(Long vehiculeId) { this.vehiculeId = vehiculeId; }
    
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    
    public LocalDate getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDate dateCreation) { this.dateCreation = dateCreation; }
    
    public LocalDate getDateModification() { return dateModification; }
    public void setDateModification(LocalDate dateModification) { this.dateModification = dateModification; }
    
    public ClientDTO getClient() { return client; }
    public void setClient(ClientDTO client) { this.client = client; }
    
    public VehiculeDTO getVehicule() { return vehicule; }
    public void setVehicule(VehiculeDTO vehicule) { this.vehicule = vehicule; }
}
