package com.BFB.automobile.business.service;

import com.BFB.automobile.business.exception.BusinessException;
import com.BFB.automobile.data.Contrat;
import com.BFB.automobile.data.EtatContrat;
import com.BFB.automobile.data.EtatVehicule;
import com.BFB.automobile.data.Vehicule;
import com.BFB.automobile.data.repository.ContratRepository;
import com.BFB.automobile.data.repository.VehiculeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service métier pour la gestion des véhicules
 * Implémente les règles métier :
 * - Un véhicule doit être unique (par immatriculation)
 * - Les véhicules en panne ne peuvent pas être loués
 * - Si un véhicule est déclaré en panne, les contrats en attente doivent être annulés
 */
@Service
@Transactional
public class VehiculeService {
    
    private final VehiculeRepository vehiculeRepository;
    private final ContratRepository contratRepository;
    
    @Autowired
    public VehiculeService(VehiculeRepository vehiculeRepository, 
                          ContratRepository contratRepository) {
        this.vehiculeRepository = vehiculeRepository;
        this.contratRepository = contratRepository;
    }
    
    /**
     * Crée un nouveau véhicule après validation
     */
    public Vehicule creerVehicule(Vehicule vehicule) {
        // Règle : Un véhicule doit être unique (par immatriculation)
        if (vehiculeRepository.existsByImmatriculation(vehicule.getImmatriculation())) {
            throw new BusinessException(
                "IMMATRICULATION_EXISTE",
                "Un véhicule avec cette immatriculation existe déjà");
        }
        
        try {
            return vehiculeRepository.save(vehicule);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(
                "ERREUR_CREATION_VEHICULE",
                "Impossible de créer le véhicule : violation de contrainte d'unicité", e);
        }
    }
    
    /**
     * Met à jour un véhicule existant
     */
    public Vehicule mettreAJourVehicule(Long id, Vehicule vehiculeModifie) {
        Vehicule vehiculeExistant = vehiculeRepository.findById(id)
            .orElseThrow(() -> new BusinessException(
                "VEHICULE_NON_TROUVE",
                "Véhicule avec l'ID " + id + " non trouvé"));
        
        // Vérifier si la nouvelle immatriculation n'est pas déjà utilisée
        if (!vehiculeExistant.getImmatriculation().equals(vehiculeModifie.getImmatriculation())) {
            if (vehiculeRepository.existsByImmatriculation(vehiculeModifie.getImmatriculation())) {
                throw new BusinessException(
                    "IMMATRICULATION_EXISTE",
                    "Un véhicule avec cette immatriculation existe déjà");
            }
        }
        
        // Mise à jour des champs
        vehiculeExistant.setMarque(vehiculeModifie.getMarque());
        vehiculeExistant.setModele(vehiculeModifie.getModele());
        vehiculeExistant.setMotorisation(vehiculeModifie.getMotorisation());
        vehiculeExistant.setCouleur(vehiculeModifie.getCouleur());
        vehiculeExistant.setImmatriculation(vehiculeModifie.getImmatriculation());
        vehiculeExistant.setDateAcquisition(vehiculeModifie.getDateAcquisition());
        
        return vehiculeRepository.save(vehiculeExistant);
    }
    
    /**
     * Change l'état d'un véhicule
     * Règle : Si un véhicule est déclaré en panne, 
     * les contrats en attente doivent être annulés automatiquement
     */
    public Vehicule changerEtatVehicule(Long id, EtatVehicule nouvelEtat) {
        Vehicule vehicule = vehiculeRepository.findById(id)
            .orElseThrow(() -> new BusinessException(
                "VEHICULE_NON_TROUVE",
                "Véhicule avec l'ID " + id + " non trouvé"));
        
        EtatVehicule ancienEtat = vehicule.getEtat();
        vehicule.setEtat(nouvelEtat);
        
        // Règle métier : Si passage en panne, annuler les contrats en attente
        if (nouvelEtat == EtatVehicule.EN_PANNE && ancienEtat != EtatVehicule.EN_PANNE) {
            annulerContratsEnAttente(vehicule);
        }
        
        return vehiculeRepository.save(vehicule);
    }
    
    /**
     * Annule tous les contrats en attente pour un véhicule
     */
    private void annulerContratsEnAttente(Vehicule vehicule) {
        List<Contrat> contratsEnAttente = contratRepository
            .findContratsEnAttenteByVehicule(vehicule.getId());
        
        for (Contrat contrat : contratsEnAttente) {
            contrat.setEtat(EtatContrat.ANNULE);
            contrat.setCommentaire(
                "Contrat annulé automatiquement : véhicule déclaré en panne");
            contratRepository.save(contrat);
        }
    }
    
    /**
     * Récupère tous les véhicules
     */
    @Transactional(readOnly = true)
    public List<Vehicule> obtenirTousLesVehicules() {
        return vehiculeRepository.findAll();
    }
    
    /**
     * Récupère un véhicule par son ID
     */
    @Transactional(readOnly = true)
    public Vehicule obtenirVehiculeParId(Long id) {
        return vehiculeRepository.findById(id)
            .orElseThrow(() -> new BusinessException(
                "VEHICULE_NON_TROUVE",
                "Véhicule avec l'ID " + id + " non trouvé"));
    }
    
    /**
     * Récupère tous les véhicules disponibles
     */
    @Transactional(readOnly = true)
    public List<Vehicule> obtenirVehiculesDisponibles() {
        return vehiculeRepository.findByEtatOrderByMarqueAscModeleAsc(EtatVehicule.DISPONIBLE);
    }
    
    /**
     * Récupère les véhicules par état
     */
    @Transactional(readOnly = true)
    public List<Vehicule> obtenirVehiculesParEtat(EtatVehicule etat) {
        return vehiculeRepository.findByEtat(etat);
    }
    
    /**
     * Recherche des véhicules par marque et/ou modèle
     */
    @Transactional(readOnly = true)
    public List<Vehicule> rechercherVehicules(String marque, String modele) {
        if (marque != null && modele != null) {
            return vehiculeRepository.searchByMarqueAndModele(marque, modele);
        } else if (marque != null) {
            return vehiculeRepository.findByMarqueContainingIgnoreCase(marque);
        } else if (modele != null) {
            return vehiculeRepository.findByModeleContainingIgnoreCase(modele);
        } else {
            return vehiculeRepository.findAll();
        }
    }
    
    /**
     * Recherche un véhicule par son immatriculation
     */
    @Transactional(readOnly = true)
    public Optional<Vehicule> rechercherParImmatriculation(String immatriculation) {
        return vehiculeRepository.findByImmatriculation(immatriculation);
    }
    
    /**
     * Supprime un véhicule (si aucun contrat actif)
     */
    public void supprimerVehicule(Long id) {
        Vehicule vehicule = vehiculeRepository.findById(id)
            .orElseThrow(() -> new BusinessException(
                "VEHICULE_NON_TROUVE",
                "Véhicule avec l'ID " + id + " non trouvé"));
        
        // Vérifier qu'il n'y a pas de contrats actifs
        List<Contrat> contratsActifs = contratRepository.findByVehicule(vehicule).stream()
            .filter(Contrat::estActif)
            .toList();
        
        if (!contratsActifs.isEmpty()) {
            throw new BusinessException(
                "VEHICULE_EN_LOCATION",
                "Impossible de supprimer le véhicule : il a des contrats actifs");
        }
        
        vehiculeRepository.delete(vehicule);
    }
}
