package com.BFB.automobile.business.factory;

import com.BFB.automobile.data.Client;
import com.BFB.automobile.data.Contrat;
import com.BFB.automobile.data.Vehicule;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Factory pour la création de contrats selon différents types de location
 * 
 * DESIGN PATTERN GoF UTILISÉ :
 * 
 * FACTORY PATTERN : Cette classe centralise la logique de création des contrats
 * selon différents types de location (courte durée, longue durée, weekend).
 * 
 * Avantages :
 * - Encapsule la logique de création complexe
 * - Facilite l'ajout de nouveaux types de contrats
 * - Assure la cohérence dans la création des contrats
 * - Sépare la logique de création de la logique métier
 */
@Component
public class ContratFactory {
    
    /**
     * Crée un contrat de courte durée (location à la journée)
     * 
     * @param client Le client qui loue le véhicule
     * @param vehicule Le véhicule à louer
     * @param dateDebut Date de début de la location
     * @param nbJours Nombre de jours de location
     * @return Un nouveau contrat configuré pour une location courte durée
     */
    public Contrat creerContratCourteDuree(Client client, Vehicule vehicule, 
                                            LocalDate dateDebut, int nbJours) {
        LocalDate dateFin = dateDebut.plusDays(nbJours);
        Contrat contrat = new Contrat(dateDebut, dateFin, client, vehicule);
        contrat.setCommentaire("Location courte durée - " + nbJours + " jour(s)");
        return contrat;
    }
    
    /**
     * Crée un contrat de longue durée (location au mois)
     * 
     * @param client Le client qui loue le véhicule
     * @param vehicule Le véhicule à louer
     * @param dateDebut Date de début de la location
     * @param nbMois Nombre de mois de location
     * @return Un nouveau contrat configuré pour une location longue durée
     */
    public Contrat creerContratLongueDuree(Client client, Vehicule vehicule, 
                                            LocalDate dateDebut, int nbMois) {
        LocalDate dateFin = dateDebut.plusMonths(nbMois);
        Contrat contrat = new Contrat(dateDebut, dateFin, client, vehicule);
        contrat.setCommentaire("Location longue durée - " + nbMois + " mois");
        return contrat;
    }
    
    /**
     * Crée un contrat forfait weekend (du vendredi au lundi)
     * 
     * @param client Le client qui loue le véhicule
     * @param vehicule Le véhicule à louer
     * @param dateDebut Date de début de la location (généralement un vendredi)
     * @return Un nouveau contrat configuré pour un forfait weekend
     */
    public Contrat creerContratWeekend(Client client, Vehicule vehicule, 
                                        LocalDate dateDebut) {
        // Weekend : forfait de 3 jours (vendredi soir au lundi matin)
        LocalDate dateFin = dateDebut.plusDays(3);
        Contrat contrat = new Contrat(dateDebut, dateFin, client, vehicule);
        contrat.setCommentaire("Forfait weekend (3 jours)");
        return contrat;
    }
    
    /**
     * Crée un contrat personnalisé avec des dates spécifiques
     * 
     * @param client Le client qui loue le véhicule
     * @param vehicule Le véhicule à louer
     * @param dateDebut Date de début de la location
     * @param dateFin Date de fin de la location
     * @param commentaire Commentaire personnalisé
     * @return Un nouveau contrat avec configuration personnalisée
     */
    public Contrat creerContratPersonnalise(Client client, Vehicule vehicule, 
                                             LocalDate dateDebut, LocalDate dateFin,
                                             String commentaire) {
        Contrat contrat = new Contrat(dateDebut, dateFin, client, vehicule);
        contrat.setCommentaire(commentaire != null ? commentaire : "Location personnalisée");
        return contrat;
    }
}
