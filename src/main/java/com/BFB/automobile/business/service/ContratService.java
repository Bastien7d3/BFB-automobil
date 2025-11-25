package com.BFB.automobile.business.service;

import com.BFB.automobile.business.exception.BusinessException;
import com.BFB.automobile.data.*;
import com.BFB.automobile.data.repository.ClientRepository;
import com.BFB.automobile.data.repository.ContratRepository;
import com.BFB.automobile.data.repository.VehiculeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service métier pour la gestion des contrats de location
 * Implémente toutes les règles métier complexes :
 * - Validation des périodes de location
 * - Gestion des états des contrats
 * - Traitement automatique des retards et annulations
 * 
 * DESIGN PATTERNS GoF UTILISÉS :
 * - FACADE PATTERN : Encapsule la logique métier complexe des contrats
 * - STRATEGY PATTERN : @Transactional pour la gestion des transactions
 * - SINGLETON PATTERN : Instance unique créée par Spring
 * - TEMPLATE METHOD PATTERN : Algorithme de traitement avec étapes fixes
 * - STATE PATTERN (implicite) : Gestion des états des contrats (EN_ATTENTE, EN_COURS,
 *   EN_RETARD, TERMINE, ANNULE) avec transitions contrôlées
 * - COMMAND PATTERN (implicite) : Les méthodes @Scheduled encapsulent des commandes
 *   de traitement automatique (démarrer contrats, marquer retards, annuler contrats bloqués)
 */
@Service
@Transactional
public class ContratService {
    
    private final ContratRepository contratRepository;
    private final ClientRepository clientRepository;
    private final VehiculeRepository vehiculeRepository;
    
    @Autowired
    public ContratService(ContratRepository contratRepository,
                         ClientRepository clientRepository,
                         VehiculeRepository vehiculeRepository) {
        this.contratRepository = contratRepository;
        this.clientRepository = clientRepository;
        this.vehiculeRepository = vehiculeRepository;
    }
    
    /**
     * Crée un nouveau contrat de location avec toutes les validations
     */
    public Contrat creerContrat(Contrat contrat) {
        // Validation 1 : Le client existe et est actif
        Client client = clientRepository.findById(contrat.getClient().getId())
            .orElseThrow(() -> new BusinessException(
                "CLIENT_NON_TROUVE",
                "Le client spécifié n'existe pas"));
        
        if (!client.getActif()) {
            throw new BusinessException(
                "CLIENT_INACTIF",
                "Le client n'est pas actif");
        }
        
        // Validation 2 : Le véhicule existe
        Vehicule vehicule = vehiculeRepository.findById(contrat.getVehicule().getId())
            .orElseThrow(() -> new BusinessException(
                "VEHICULE_NON_TROUVE",
                "Le véhicule spécifié n'existe pas"));
        
        // Règle : Les véhicules en panne ne peuvent pas être loués
        if (vehicule.estEnPanne()) {
            throw new BusinessException(
                "VEHICULE_EN_PANNE",
                "Ce véhicule est en panne et ne peut pas être loué");
        }
        
        // Validation 3 : Les dates sont cohérentes
        if (contrat.getDateDebut().isAfter(contrat.getDateFin())) {
            throw new BusinessException(
                "DATES_INCOHERENTES",
                "La date de début doit être antérieure à la date de fin");
        }
        
        if (contrat.getDateDebut().isBefore(LocalDate.now())) {
            throw new BusinessException(
                "DATE_DEBUT_PASSEE",
                "La date de début ne peut pas être dans le passé");
        }
        
        // Règle : Un véhicule ne peut être loué que par un seul client sur une période donnée
        List<Contrat> contratsConflictuels = contratRepository.findContratsConflictuels(
            vehicule.getId(),
            contrat.getDateDebut(),
            contrat.getDateFin()
        );
        
        if (!contratsConflictuels.isEmpty()) {
            throw new BusinessException(
                "VEHICULE_DEJA_LOUE",
                "Ce véhicule est déjà loué sur cette période");
        }
        
        // Réattacher les entités gérées
        contrat.setClient(client);
        contrat.setVehicule(vehicule);
        
        // Si le contrat commence aujourd'hui, le mettre directement en cours
        if (contrat.getDateDebut().equals(LocalDate.now())) {
            contrat.setEtat(EtatContrat.EN_COURS);
            vehicule.setEtat(EtatVehicule.EN_LOCATION);
            vehiculeRepository.save(vehicule);
        }
        
        return contratRepository.save(contrat);
    }
    
    /**
     * Met à jour un contrat existant
     */
    public Contrat mettreAJourContrat(Long id, Contrat contratModifie) {
        Contrat contratExistant = contratRepository.findById(id)
            .orElseThrow(() -> new BusinessException(
                "CONTRAT_NON_TROUVE",
                "Contrat avec l'ID " + id + " non trouvé"));
        
        // On ne peut modifier que les contrats en attente
        if (contratExistant.getEtat() != EtatContrat.EN_ATTENTE) {
            throw new BusinessException(
                "CONTRAT_NON_MODIFIABLE",
                "Seuls les contrats en attente peuvent être modifiés");
        }
        
        // Vérifier les nouvelles dates
        if (contratModifie.getDateDebut().isAfter(contratModifie.getDateFin())) {
            throw new BusinessException(
                "DATES_INCOHERENTES",
                "La date de début doit être antérieure à la date de fin");
        }
        
        // Vérifier la disponibilité du véhicule sur les nouvelles dates
        List<Contrat> contratsConflictuels = contratRepository.findContratsConflictuels(
            contratExistant.getVehicule().getId(),
            contratModifie.getDateDebut(),
            contratModifie.getDateFin()
        ).stream()
            .filter(c -> !c.getId().equals(id)) // Exclure le contrat actuel
            .toList();
        
        if (!contratsConflictuels.isEmpty()) {
            throw new BusinessException(
                "VEHICULE_DEJA_LOUE",
                "Ce véhicule est déjà loué sur cette période");
        }
        
        contratExistant.setDateDebut(contratModifie.getDateDebut());
        contratExistant.setDateFin(contratModifie.getDateFin());
        contratExistant.setCommentaire(contratModifie.getCommentaire());
        
        return contratRepository.save(contratExistant);
    }
    
    /**
     * Annule un contrat
     */
    public Contrat annulerContrat(Long id, String motif) {
        Contrat contrat = contratRepository.findById(id)
            .orElseThrow(() -> new BusinessException(
                "CONTRAT_NON_TROUVE",
                "Contrat avec l'ID " + id + " non trouvé"));
        
        if (contrat.getEtat() == EtatContrat.TERMINE || 
            contrat.getEtat() == EtatContrat.ANNULE) {
            throw new BusinessException(
                "CONTRAT_NON_ANNULABLE",
                "Ce contrat ne peut pas être annulé (déjà terminé ou annulé)");
        }
        
        contrat.setEtat(EtatContrat.ANNULE);
        contrat.setCommentaire(motif);
        
        // Si le véhicule était en location pour ce contrat, le remettre disponible
        if (contrat.getVehicule().getEtat() == EtatVehicule.EN_LOCATION) {
            contrat.getVehicule().setEtat(EtatVehicule.DISPONIBLE);
            vehiculeRepository.save(contrat.getVehicule());
        }
        
        return contratRepository.save(contrat);
    }
    
    /**
     * Termine un contrat (retour du véhicule)
     */
    public Contrat terminerContrat(Long id) {
        Contrat contrat = contratRepository.findById(id)
            .orElseThrow(() -> new BusinessException(
                "CONTRAT_NON_TROUVE",
                "Contrat avec l'ID " + id + " non trouvé"));
        
        if (contrat.getEtat() != EtatContrat.EN_COURS && 
            contrat.getEtat() != EtatContrat.EN_RETARD) {
            throw new BusinessException(
                "CONTRAT_NON_TERMINABLE",
                "Seuls les contrats en cours ou en retard peuvent être terminés");
        }
        
        contrat.setEtat(EtatContrat.TERMINE);
        
        // Remettre le véhicule disponible
        Vehicule vehicule = contrat.getVehicule();
        vehicule.setEtat(EtatVehicule.DISPONIBLE);
        vehiculeRepository.save(vehicule);
        
        return contratRepository.save(contrat);
    }
    
    /**
     * Tâche planifiée : Traite automatiquement les changements d'état des contrats
     * Exécutée chaque jour à minuit
     */
    @Scheduled(cron = "0 0 0 * * *") // Tous les jours à minuit
    public void traiterChangementsEtatAutomatiques() {
        LocalDate aujourdhui = LocalDate.now();
        
        // 1. Démarrer les contrats qui doivent commencer aujourd'hui
        demarrerContratsAujourdhui(aujourdhui);
        
        // 2. Marquer les contrats en retard
        marquerContratsEnRetard(aujourdhui);
        
        // 3. Annuler les contrats bloqués par des retards
        annulerContratsBloquesParRetard(aujourdhui);
    }
    
    /**
     * Démarre les contrats qui doivent commencer aujourd'hui
     */
    private void demarrerContratsAujourdhui(LocalDate aujourdhui) {
        List<Contrat> contratsADemarrer = contratRepository
            .findContratsADemarrerAujourdhui(aujourdhui);
        
        for (Contrat contrat : contratsADemarrer) {
            // Vérifier que le véhicule est disponible
            if (contrat.getVehicule().estDisponible()) {
                contrat.setEtat(EtatContrat.EN_COURS);
                contrat.getVehicule().setEtat(EtatVehicule.EN_LOCATION);
                vehiculeRepository.save(contrat.getVehicule());
                contratRepository.save(contrat);
            } else {
                // Le véhicule n'est pas disponible, annuler le contrat
                contrat.setEtat(EtatContrat.ANNULE);
                contrat.setCommentaire(
                    "Contrat annulé automatiquement : véhicule non disponible");
                contratRepository.save(contrat);
            }
        }
    }
    
    /**
     * Règle : Si un client ne ramène pas le véhicule avant la date de fin,
     * le contrat doit passer au statut "en retard"
     */
    private void marquerContratsEnRetard(LocalDate aujourdhui) {
        List<Contrat> contratsEnRetard = contratRepository
            .findContratsEnRetard(aujourdhui);
        
        for (Contrat contrat : contratsEnRetard) {
            contrat.setEtat(EtatContrat.EN_RETARD);
            contrat.setCommentaire(
                "Contrat en retard depuis le " + contrat.getDateFin());
            contratRepository.save(contrat);
        }
    }
    
    /**
     * Règle : Si un retard empêche le démarrage du contrat suivant,
     * celui-ci doit passer au statut "annulé"
     */
    private void annulerContratsBloquesParRetard(LocalDate aujourdhui) {
        // Trouver tous les contrats qui devraient commencer aujourd'hui ou avant
        List<Contrat> contratsEnAttente = contratRepository
            .findByEtat(EtatContrat.EN_ATTENTE).stream()
            .filter(c -> !c.getDateDebut().isAfter(aujourdhui))
            .toList();
        
        for (Contrat contrat : contratsEnAttente) {
            Vehicule vehicule = contrat.getVehicule();
            
            // Vérifier si le véhicule est bloqué par un contrat en retard
            List<Contrat> contratsEnRetardPourCeVehicule = contratRepository
                .findByVehicule(vehicule).stream()
                .filter(Contrat::estEnRetard)
                .toList();
            
            if (!contratsEnRetardPourCeVehicule.isEmpty()) {
                contrat.setEtat(EtatContrat.ANNULE);
                contrat.setCommentaire(
                    "Contrat annulé automatiquement : véhicule bloqué par un retard");
                contratRepository.save(contrat);
            }
        }
    }
    
    /**
     * Récupère tous les contrats
     */
    @Transactional(readOnly = true)
    public List<Contrat> obtenirTousLesContrats() {
        return contratRepository.findAll();
    }
    
    /**
     * Récupère un contrat par son ID
     */
    @Transactional(readOnly = true)
    public Contrat obtenirContratParId(Long id) {
        return contratRepository.findById(id)
            .orElseThrow(() -> new BusinessException(
                "CONTRAT_NON_TROUVE",
                "Contrat avec l'ID " + id + " non trouvé"));
    }
    
    /**
     * Récupère tous les contrats d'un client
     */
    @Transactional(readOnly = true)
    public List<Contrat> obtenirContratsParClient(Long clientId) {
        return contratRepository.findByClientIdOrderByDateDebutDesc(clientId);
    }
    
    /**
     * Récupère tous les contrats d'un véhicule
     */
    @Transactional(readOnly = true)
    public List<Contrat> obtenirContratsParVehicule(Long vehiculeId) {
        return contratRepository.findByVehiculeIdOrderByDateDebutDesc(vehiculeId);
    }
    
    /**
     * Récupère les contrats par état
     */
    @Transactional(readOnly = true)
    public List<Contrat> obtenirContratsParEtat(EtatContrat etat) {
        return contratRepository.findByEtat(etat);
    }
    
    /**
     * Récupère les contrats actifs
     */
    @Transactional(readOnly = true)
    public List<Contrat> obtenirContratsActifs() {
        return contratRepository.findContratsActifs();
    }
}
