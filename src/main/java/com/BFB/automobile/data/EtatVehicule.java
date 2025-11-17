package com.BFB.automobile.data;

/**
 * Énumération des états possibles d'un véhicule
 */
public enum EtatVehicule {
    DISPONIBLE("Disponible"),
    EN_LOCATION("En location"),
    EN_PANNE("En panne");
    
    private final String libelle;
    
    EtatVehicule(String libelle) {
        this.libelle = libelle;
    }
    
    public String getLibelle() {
        return libelle;
    }
}
