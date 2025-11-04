package com.BFB.automobile.data.repository;

import com.BFB.automobile.data.Vehicule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * COUCHE STOCKAGE - Repository
 * ═══════════════════════════════════════════════════════════════════════════
 * 
 * PATTERNS UTILISÉS:
 * 
 * 1. Repository Pattern (Data Access Object - DAO)
 *    - Abstraction de la couche de persistance
 *    - Sépare la logique métier de l'accès aux données
 *    - Interface entre le domaine et la base de données
 *    POURQUOI: on peut changer MongoDB pour PostgreSQL sans toucher au service
 * 
 * 2. Spring Data Repository Pattern
 *    - Extension du Repository Pattern par Spring
 *    - Génération automatique des implémentations (pas besoin de code !)
 *    - Méthodes CRUD générées automatiquement
 *    POURQUOI: gain de temps énorme, moins de code boilerplate
 * 
 * 3. Derived Query Methods
 *    - Spring génère les requêtes à partir du nom de la méthode
 *    - Exemple: findByMarque → SELECT * FROM vehicules WHERE marque = ?
 *    POURQUOI: pas besoin d'écrire de requêtes MongoDB manuellement
 * 
 * RESPONSABILITÉS DE CETTE COUCHE:
 * ✅ Communication avec MongoDB
 * ✅ Opérations CRUD (Create, Read, Update, Delete)
 * ✅ Requêtes personnalisées (filtres, tris)
 * ❌ PAS de logique métier (juste de la persistance)
 * ❌ PAS de transformation des données (le service s'en charge)
 * 
 * POURQUOI UNE INTERFACE ET PAS UNE CLASSE:
 * Spring Data crée automatiquement l'implémentation au démarrage
 * Il génère une classe proxy qui fait le lien avec MongoDB
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Repository // PATTERN: Repository Stereotype - Spring détecte et crée un bean
public interface VehiculeRepository extends MongoRepository<Vehicule, String> {
    // MongoRepository<Vehicule, String>
    //                 ↑         ↑
    //                 Type      Type de l'ID
    //               de l'entité
    
    // ========== MÉTHODES CRUD HÉRITÉES (générées automatiquement) ==========
    // Spring Data génère automatiquement l'implémentation de:
    // - save(Vehicule vehicule)              → Insère ou met à jour
    // - findById(String id)                  → Recherche par ID (retourne Optional)
    // - findAll()                            → Récupère tous les véhicules
    // - deleteById(String id)                → Supprime par ID
    // - existsById(String id)                → Vérifie l'existence
    // - count()                              → Compte le nombre de véhicules
    
    // ========== PATTERN: Derived Query Methods ==========
    // Spring Data génère automatiquement les requêtes MongoDB à partir du nom de la méthode
    
    /**
     * QUERY DÉRIVÉE: findByMarque
     * Spring génère automatiquement: db.vehicules.find({ marque: marque })
     * POURQUOI ce nom: "findBy" + "Marque" (nom du champ dans Vehicule.java)
     */
    List<Vehicule> findByMarque(String marque);
    
    /**
     * QUERY DÉRIVÉE: findByAnneeGreaterThan
     * Spring génère: db.vehicules.find({ annee: { $gt: annee } })
     * POURQUOI: "GreaterThan" est un mot-clé reconnu par Spring Data
     */
    List<Vehicule> findByAnneeGreaterThan(Integer annee);
    
    /**
     * QUERY DÉRIVÉE: findByPrixLessThan
     * Spring génère: db.vehicules.find({ prix: { $lt: prix } })
     * Autres mots-clés possibles: Between, Like, StartingWith, EndingWith, etc.
     */
    List<Vehicule> findByPrixLessThan(Double prix);
    
    // ========== EXEMPLES DE REQUÊTES POSSIBLES (non implémentées) ==========
    // List<Vehicule> findByMarqueAndAnnee(String marque, Integer annee);
    // List<Vehicule> findByMarqueOrderByPrixAsc(String marque);
    // List<Vehicule> findByPrixBetween(Double min, Double max);
    // @Query("{ 'marque': ?0, 'annee': { $gte: ?1 } }") // Requête MongoDB manuelle
    // List<Vehicule> recherchePersonnalisee(String marque, Integer anneeMin);
}
