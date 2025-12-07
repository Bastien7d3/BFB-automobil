package com.BFB.automobile.business.validation;

import com.BFB.automobile.data.Contrat;

/**
 * Classe abstraite pour la chaîne de validation de contrats
 * 
 * DESIGN PATTERN GoF UTILISÉ :
 * 
 * CHAIN OF RESPONSIBILITY PATTERN : Cette classe définit la structure d'une chaîne
 * de responsabilité où chaque handler peut traiter une validation ou la passer
 * au handler suivant dans la chaîne.
 * 
 * Avantages :
 * - Découple l'émetteur d'une requête de ses récepteurs
 * - Permet d'ajouter ou retirer des validations facilement
 * - Chaque validation a une seule responsabilité (SRP)
 * - L'ordre des validations est configurable
 */
public abstract class ValidationHandler {
    
    protected ValidationHandler next;
    
    /**
     * Définit le prochain handler dans la chaîne
     * 
     * @param handler Le prochain handler
     * @return Le handler passé en paramètre (pour chaînage fluent)
     */
    public ValidationHandler setNext(ValidationHandler handler) {
        this.next = handler;
        return handler;
    }
    
    /**
     * Méthode abstraite de validation à implémenter par chaque handler concret
     * 
     * @param contrat Le contrat à valider
     * @throws com.BFB.automobile.business.exception.BusinessException si la validation échoue
     */
    public abstract void valider(Contrat contrat);
    
    /**
     * Passe la validation au handler suivant dans la chaîne
     * 
     * @param contrat Le contrat à valider
     */
    protected void validerSuivant(Contrat contrat) {
        if (next != null) {
            next.valider(contrat);
        }
    }
}
