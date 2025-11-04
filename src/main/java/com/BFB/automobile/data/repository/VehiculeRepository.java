package com.BFB.automobile.data.repository;

import com.BFB.automobile.data.Vehicule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * COUCHE STOCKAGE - Repository
 * Interface d'accès aux données MongoDB.
 * Spring Data génère automatiquement les implémentations.
 */
@Repository
public interface VehiculeRepository extends MongoRepository<Vehicule, String> {
    
    // Requêtes personnalisées (Spring Data génère l'implémentation)
    List<Vehicule> findByMarque(String marque);
    List<Vehicule> findByAnneeGreaterThan(Integer annee);
    List<Vehicule> findByPrixLessThan(Double prix);
}
