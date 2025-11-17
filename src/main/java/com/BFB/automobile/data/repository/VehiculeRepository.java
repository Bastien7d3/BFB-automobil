package com.BFB.automobile.data.repository;

import com.BFB.automobile.data.EtatVehicule;
import com.BFB.automobile.data.Vehicule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des véhicules
 * Fournit les méthodes de recherche et validation
 */
@Repository
public interface VehiculeRepository extends JpaRepository<Vehicule, Long> {
    
    /**
     * Recherche par immatriculation (unique)
     */
    Optional<Vehicule> findByImmatriculation(String immatriculation);
    
    /**
     * Vérifie si une immatriculation existe déjà
     */
    boolean existsByImmatriculation(String immatriculation);
    
    /**
     * Recherche par état
     */
    List<Vehicule> findByEtat(EtatVehicule etat);
    
    /**
     * Recherche des véhicules disponibles
     */
    List<Vehicule> findByEtatOrderByMarqueAscModeleAsc(EtatVehicule etat);
    
    /**
     * Recherche par marque (insensible à la casse)
     */
    List<Vehicule> findByMarqueContainingIgnoreCase(String marque);
    
    /**
     * Recherche par modèle (insensible à la casse)
     */
    List<Vehicule> findByModeleContainingIgnoreCase(String modele);
    
    /**
     * Recherche par marque ET modèle
     */
    @Query("SELECT v FROM Vehicule v WHERE LOWER(v.marque) LIKE LOWER(CONCAT('%', :marque, '%')) " +
           "AND LOWER(v.modele) LIKE LOWER(CONCAT('%', :modele, '%'))")
    List<Vehicule> searchByMarqueAndModele(
        @Param("marque") String marque, 
        @Param("modele") String modele);
    
    /**
     * Compte le nombre de véhicules par état
     */
    long countByEtat(EtatVehicule etat);
}
