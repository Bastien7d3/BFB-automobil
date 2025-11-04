package com.BFB.automobile.business.service;

import com.BFB.automobile.data.producer.VehiculeProducer;
import com.BFB.automobile.data.repository.VehiculeRepository;
import com.BFB.automobile.data.Vehicule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * COUCHE LOGIQUE MÉTIER - Service Layer
 * ═══════════════════════════════════════════════════════════════════════════
 * 
 * PATTERNS UTILISÉS:
 * 
 * 1. Service Layer Pattern
 *    - Couche qui encapsule la logique métier
 *    - Intermédiaire entre la présentation et le stockage
 *    - Coordonne les opérations complexes
 *    POURQUOI: centraliser la logique métier, réutilisabilité, testabilité
 * 
 * 2. Facade Pattern
 *    - Interface simplifiée pour des opérations complexes
 *    - Cache la complexité d'orchestration repository + producer
 *    POURQUOI: le controller n'a pas besoin de savoir qu'on appelle 2 composants
 * 
 * 3. Dependency Injection (Constructor Injection)
 *    - Injection du repository ET du producer
 *    - Couplage faible avec les couches inférieures
 *    POURQUOI: testabilité (mock facile), flexibilité, immutabilité
 * 
 * 4. Orchestration Pattern
 *    - Le service orchestre plusieurs appels (repository + producer)
 *    - Gère l'ordre des opérations et la cohérence
 *    POURQUOI: logique métier complexe nécessite coordination
 * 
 * RESPONSABILITÉS DE CETTE COUCHE:
 * ✅ Manipulation et transformation des POJO
 * ✅ Règles métier (validation, calculs, cohérence)
 * ✅ Orchestration des appels repository + producer
 * ✅ Gestion des transactions (si nécessaire avec @Transactional)
 * ❌ PAS de gestion HTTP (codes de réponse, requêtes)
 * ❌ PAS d'accès direct à MongoDB (délégation au repository)
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Service // PATTERN: Service Component - Spring détecte et crée un bean singleton automatiquement
public class VehiculeService {
    
    // ========== PATTERN: Dependency Injection (Constructor Injection) ==========
    // POURQUOI deux dépendances:
    // - VehiculeRepository: pour la persistance MongoDB
    // - VehiculeProducer: pour la communication avec systèmes externes
    // POURQUOI final: immutabilité → thread-safe
    private final VehiculeRepository vehiculeRepository;
    private final VehiculeProducer vehiculeProducer;
    
    @Autowired // Spring injecte automatiquement les deux dépendances (IoC)
    public VehiculeService(VehiculeRepository vehiculeRepository, 
                          VehiculeProducer vehiculeProducer) {
        this.vehiculeRepository = vehiculeRepository;
        this.vehiculeProducer = vehiculeProducer;
    }
    
    /**
     * ========== MÉTHODE: Récupération de tous les véhicules ==========
     * PATTERN: Simple Delegation - Pas de logique métier ici, juste délégation
     * POURQUOI quand même passer par le service:
     * - Abstraction: le controller ne connaît pas le repository
     * - Évolutivité: on peut ajouter du filtrage/tri plus tard
     */
    public List<Vehicule> obtenirTousLesVehicules() {
        return vehiculeRepository.findAll(); // Délégation directe au repository
    }
    
    /**
     * ========== MÉTHODE: Récupération par ID ==========
     * PATTERN: Optional Pattern - Gestion élégante de l'absence de résultat
     * POURQUOI Optional:
     * - Évite NullPointerException
     * - Force le client à gérer l'absence de résultat
     */
    public Optional<Vehicule> obtenirVehiculeParId(String id) {
        return vehiculeRepository.findById(id);
    }
    
    /**
     * ========== MÉTHODE: Création d'un véhicule ==========
     * PATTERN: Orchestration Pattern - Coordination de plusieurs opérations
     * 
     * FLUX D'ORCHESTRATION:
     * 1. Appel au producer pour récupérer la cotation (système externe)
     * 2. Sauvegarde dans MongoDB via repository
     * 3. Publication du véhicule créé vers systèmes externes (notification)
     * 
     * POURQUOI CET ORDRE:
     * - Cotation d'abord: on pourrait valider que le prix est cohérent
     * - Sauvegarde ensuite: on garantit la persistance
     * - Publication en dernier: on notifie uniquement si tout s'est bien passé
     * 
     * LOGIQUE MÉTIER:
     * - Dans un vrai projet, on comparerait cotation vs prix
     * - Si incohérence, on lèverait une BusinessException
     * - Ici c'est simplifié pour la démo
     */
    public Vehicule creerVehicule(Vehicule vehicule) {
        // ORCHESTRATION ÉTAPE 1: Récupération cotation externe
        // (Dans un vrai projet, on comparerait cotation vs prix et on lèverait une exception si incohérence)
        vehiculeProducer.obtenirCotation(
            vehicule.getMarque(), 
            vehicule.getModele(), 
            vehicule.getAnnee()
        );
        
        // ORCHESTRATION ÉTAPE 2: Sauvegarde dans la base de données (couche stockage)
        Vehicule vehiculeSauvegarde = vehiculeRepository.save(vehicule);
        
        // ORCHESTRATION ÉTAPE 3: Publication vers système externe (notification)
        vehiculeProducer.publierVehicule(vehiculeSauvegarde);
        
        return vehiculeSauvegarde;
    }
    
    /**
     * ========== MÉTHODE: Mise à jour d'un véhicule ==========
     * PATTERN: Guard Clause - Vérification d'existence avant traitement
     * LOGIQUE MÉTIER: Validation que le véhicule existe
     */
    public Vehicule mettreAJourVehicule(String id, Vehicule vehicule) {
        // LOGIQUE MÉTIER: vérifier que le véhicule existe
        // Si absent, on lève une exception (le controller gèrera HTTP 404)
        if (!vehiculeRepository.existsById(id)) {
            throw new RuntimeException("Véhicule non trouvé avec l'ID: " + id);
        }
        
        // Mise à jour de l'ID (pour garantir cohérence)
        vehicule.setId(id);
        return vehiculeRepository.save(vehicule);
    }
    
    /**
     * ========== MÉTHODE: Suppression d'un véhicule ==========
     * PATTERN: Simple Delegation
     */
    public void supprimerVehicule(String id) {
        vehiculeRepository.deleteById(id); // Délégation au repository
    }
    
    /**
     * ========== MÉTHODE: Recherche par marque ==========
     * PATTERN: Query Method Delegation
     * POURQUOI dans le service: on pourrait ajouter de la logique (filtrage, tri)
     */
    public List<Vehicule> rechercherParMarque(String marque) {
        return vehiculeRepository.findByMarque(marque);
    }
    
    /**
     * ========== MÉTHODE: Véhicules récents ==========
     * LOGIQUE MÉTIER: "Récent" = année > seuil
     * Le seuil pourrait venir de la configuration (externalisation)
     */
    public List<Vehicule> obtenirVehiculesRecents(Integer anneeMin) {
        return vehiculeRepository.findByAnneeGreaterThan(anneeMin);
    }
}
