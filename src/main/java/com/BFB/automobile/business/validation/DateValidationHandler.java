package com.BFB.automobile.business.validation;

import com.BFB.automobile.business.exception.BusinessException;
import com.BFB.automobile.data.Contrat;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Handler de validation des dates d'un contrat
 * Vérifie que la date de début est avant la date de fin
 * et que la date de début n'est pas dans le passé
 */
@Component
public class DateValidationHandler extends ValidationHandler {
    
    @Override
    public void valider(Contrat contrat) {
        // Vérifier que dateDebut < dateFin
        if (contrat.getDateDebut().isAfter(contrat.getDateFin())) {
            throw new BusinessException(
                "DATE_INVALIDE",
                "La date de début doit être avant la date de fin");
        }
        
        // Vérifier que dateDebut >= aujourd'hui (pas de location rétroactive)
        if (contrat.getDateDebut().isBefore(LocalDate.now())) {
            throw new BusinessException(
                "DATE_PASSEE",
                "La date de début ne peut pas être dans le passé");
        }
        
        // Vérifier que la durée est raisonnable (max 365 jours)
        long dureeJours = java.time.temporal.ChronoUnit.DAYS.between(
            contrat.getDateDebut(), contrat.getDateFin());
        
        if (dureeJours > 365) {
            throw new BusinessException(
                "DUREE_EXCESSIVE",
                "La durée de location ne peut pas dépasser 365 jours");
        }
        
        // Passer au handler suivant
        validerSuivant(contrat);
    }
}
