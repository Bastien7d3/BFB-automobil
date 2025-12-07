package com.BFB.automobile.business.validation;

import com.BFB.automobile.business.exception.BusinessException;
import com.BFB.automobile.data.Contrat;
import org.springframework.stereotype.Component;

/**
 * Handler de validation du client d'un contrat
 * Vérifie que le client est actif et peut louer un véhicule
 */
@Component
public class ClientValidationHandler extends ValidationHandler {
    
    @Override
    public void valider(Contrat contrat) {
        // Vérifier que le client n'est pas null
        if (contrat.getClient() == null) {
            throw new BusinessException(
                "CLIENT_NULL",
                "Le client ne peut pas être null");
        }
        
        // Vérifier que le client est actif
        if (!contrat.getClient().getActif()) {
            throw new BusinessException(
                "CLIENT_INACTIF",
                "Le client doit être actif pour louer un véhicule");
        }
        
        // Vérifier que le client a bien un ID (est persisté)
        if (contrat.getClient().getId() == null) {
            throw new BusinessException(
                "CLIENT_NON_PERSISTE",
                "Le client doit être enregistré en base de données");
        }
        
        // Passer au handler suivant
        validerSuivant(contrat);
    }
}
