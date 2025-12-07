package com.BFB.automobile.business.validation;

import com.BFB.automobile.business.exception.BusinessException;
import com.BFB.automobile.data.Contrat;
import com.BFB.automobile.data.EtatVehicule;
import org.springframework.stereotype.Component;

/**
 * Handler de validation du véhicule d'un contrat
 * Vérifie que le véhicule est disponible pour la location
 */
@Component
public class VehiculeValidationHandler extends ValidationHandler {
    
    @Override
    public void valider(Contrat contrat) {
        // Vérifier que le véhicule n'est pas null
        if (contrat.getVehicule() == null) {
            throw new BusinessException(
                "VEHICULE_NULL",
                "Le véhicule ne peut pas être null");
        }
        
        // Vérifier que le véhicule est disponible
        if (contrat.getVehicule().getEtat() != EtatVehicule.DISPONIBLE) {
            throw new BusinessException(
                "VEHICULE_INDISPONIBLE",
                "Le véhicule doit être disponible pour être loué. État actuel : " 
                    + contrat.getVehicule().getEtat().getLibelle());
        }
        
        // Vérifier que le véhicule a bien un ID (est persisté)
        if (contrat.getVehicule().getId() == null) {
            throw new BusinessException(
                "VEHICULE_NON_PERSISTE",
                "Le véhicule doit être enregistré en base de données");
        }
        
        // Passer au handler suivant
        validerSuivant(contrat);
    }
}
