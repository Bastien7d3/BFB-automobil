package com.BFB.automobile.data.repository;

import com.BFB.automobile.data.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des clients
 * Fournit les méthodes de recherche et validation
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    
    /**
     * Vérifie l'unicité d'un client par nom, prénom et date de naissance
     */
    Optional<Client> findByNomAndPrenomAndDateNaissance(
        String nom, String prenom, LocalDate dateNaissance);
    
    /**
     * Vérifie l'unicité du numéro de permis
     */
    Optional<Client> findByNumeroPermis(String numeroPermis);
    
    /**
     * Vérifie si un numéro de permis existe déjà (pour validation)
     */
    boolean existsByNumeroPermis(String numeroPermis);
    
    /**
     * Vérifie si un client existe déjà (pour validation)
     */
    boolean existsByNomAndPrenomAndDateNaissance(
        String nom, String prenom, LocalDate dateNaissance);
    
    /**
     * Recherche des clients actifs
     */
    List<Client> findByActifTrue();
    
    /**
     * Recherche par nom (insensible à la casse)
     */
    List<Client> findByNomContainingIgnoreCase(String nom);
    
    /**
     * Recherche par prénom (insensible à la casse)
     */
    List<Client> findByPrenomContainingIgnoreCase(String prenom);
    
    /**
     * Recherche par nom ET prénom (insensible à la casse)
     */
    @Query("SELECT c FROM Client c WHERE LOWER(c.nom) LIKE LOWER(CONCAT('%', :nom, '%')) " +
           "AND LOWER(c.prenom) LIKE LOWER(CONCAT('%', :prenom, '%'))")
    List<Client> searchByNomAndPrenom(
        @Param("nom") String nom, 
        @Param("prenom") String prenom);
}
