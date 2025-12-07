package com.BFB.automobile.business.validation;

import com.BFB.automobile.business.exception.BusinessException;
import com.BFB.automobile.data.Contrat;
import com.BFB.automobile.data.repository.ContratRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Handler de validation de la disponibilité d'un contrat
 * Vérifie qu'il n'y a pas de chevauchement avec d'autres contrats
 */
@Component
public class DisponibiliteValidationHandler extends ValidationHandler {
    
    @Autowired
    private ContratRepository contratRepository;
    
    @Override
    public void valider(Contrat contrat) {
        // Vérifier qu'il n'y a pas de contrats actifs pour le même véhicule sur la même période
        List<Contrat> contratsConflictuels = contratRepository.findContratsConflictuels(
            contrat.getVehicule().getId(),
            contrat.getDateDebut(),
            contrat.getDateFin()
        );
        
        if (!contratsConflictuels.isEmpty()) {
            throw new BusinessException(
                "VEHICULE_DEJA_LOUE",
                "Ce véhicule est déjà réservé ou en location pour la période demandée");
        }
        
        // Vérifier que le client n'a pas déjà un contrat actif sur la période
        List<Contrat> contratsClientSurPeriode = contratRepository.findContratsClientSurPeriode(
            contrat.getClient().getId(),
            contrat.getDateDebut(),
            contrat.getDateFin()
        );
        
        if (!contratsClientSurPeriode.isEmpty()) {
            throw new BusinessException(
                "CLIENT_DEJA_CONTRAT",
                "Ce client a déjà un contrat actif sur cette période");
        }
        
        // Passer au handler suivant
        validerSuivant(contrat);
    }
}
