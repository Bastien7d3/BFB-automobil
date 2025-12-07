package com.BFB.automobile.business.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Configuration de la chaîne de validation pour les contrats
 * Définit l'ordre d'exécution des validations
 * 
 * DESIGN PATTERN GoF UTILISÉ :
 * 
 * CHAIN OF RESPONSIBILITY PATTERN : Cette classe configure la chaîne de handlers
 * qui vont valider un contrat. Chaque handler effectue une validation spécifique
 * et passe au suivant si tout est OK.
 * 
 * Ordre des validations :
 * 1. DateValidationHandler - Vérifie les dates
 * 2. ClientValidationHandler - Vérifie le client
 * 3. VehiculeValidationHandler - Vérifie le véhicule
 * 4. DisponibiliteValidationHandler - Vérifie les chevauchements
 */
@Component
public class ContratValidationChain {
    
    @Autowired
    private DateValidationHandler dateHandler;
    
    @Autowired
    private ClientValidationHandler clientHandler;
    
    @Autowired
    private VehiculeValidationHandler vehiculeHandler;
    
    @Autowired
    private DisponibiliteValidationHandler disponibiliteHandler;
    
    /**
     * Construit et retourne la chaîne de validation complète
     * 
     * @return Le premier handler de la chaîne
     */
    public ValidationHandler getChain() {
        // Configuration de la chaîne : date → client → vehicule → disponibilite
        dateHandler
            .setNext(clientHandler)
            .setNext(vehiculeHandler)
            .setNext(disponibiliteHandler);
        
        return dateHandler;
    }
}
