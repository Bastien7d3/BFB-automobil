package com.BFB.automobile.data;

/**
 * Énumération des états possibles d'un contrat de location
 */
public enum EtatContrat {
    EN_ATTENTE("En attente"),
    EN_COURS("En cours"),
    TERMINE("Terminé"),
    EN_RETARD("En retard"),
    ANNULE("Annulé");
    
    private final String libelle;
    
    EtatContrat(String libelle) {
        this.libelle = libelle;
    }
    
    public String getLibelle() {
        return libelle;
    }
}
