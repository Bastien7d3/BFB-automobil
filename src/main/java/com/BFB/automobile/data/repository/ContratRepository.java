package com.BFB.automobile.data.repository;

import com.BFB.automobile.data.Contrat;
import com.BFB.automobile.data.EtatContrat;
import com.BFB.automobile.data.Vehicule;
import com.BFB.automobile.data.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository pour la gestion des contrats de location
 * Fournit les méthodes de recherche et validation
 */
@Repository
public interface ContratRepository extends JpaRepository<Contrat, Long> {
    
    /**
     * Recherche tous les contrats d'un client
     */
    List<Contrat> findByClient(Client client);
    
    /**
     * Recherche tous les contrats d'un véhicule
     */
    List<Contrat> findByVehicule(Vehicule vehicule);
    
    /**
     * Recherche tous les contrats d'un client par ID
     */
    List<Contrat> findByClientIdOrderByDateDebutDesc(Long clientId);
    
    /**
     * Recherche tous les contrats d'un véhicule par ID
     */
    List<Contrat> findByVehiculeIdOrderByDateDebutDesc(Long vehiculeId);
    
    /**
     * Recherche par état
     */
    List<Contrat> findByEtat(EtatContrat etat);
    
    /**
     * Recherche les contrats actifs (en attente ou en cours)
     */
    @Query("SELECT c FROM Contrat c WHERE c.etat IN ('EN_ATTENTE', 'EN_COURS')")
    List<Contrat> findContratsActifs();
    
    /**
     * Vérifie si un véhicule est déjà loué sur une période donnée
     * (exclut les contrats annulés et terminés)
     */
    @Query("SELECT c FROM Contrat c WHERE c.vehicule.id = :vehiculeId " +
           "AND c.etat NOT IN ('ANNULE', 'TERMINE') " +
           "AND ((c.dateDebut <= :dateFin AND c.dateFin >= :dateDebut))")
    List<Contrat> findContratsConflictuels(
        @Param("vehiculeId") Long vehiculeId,
        @Param("dateDebut") LocalDate dateDebut,
        @Param("dateFin") LocalDate dateFin);
    
    /**
     * Trouve les contrats en attente pour un véhicule spécifique
     */
    @Query("SELECT c FROM Contrat c WHERE c.vehicule.id = :vehiculeId " +
           "AND c.etat = 'EN_ATTENTE' " +
           "ORDER BY c.dateDebut ASC")
    List<Contrat> findContratsEnAttenteByVehicule(@Param("vehiculeId") Long vehiculeId);
    
    /**
     * Trouve les contrats qui doivent commencer aujourd'hui
     */
    @Query("SELECT c FROM Contrat c WHERE c.dateDebut = :date " +
           "AND c.etat = 'EN_ATTENTE'")
    List<Contrat> findContratsADemarrerAujourdhui(@Param("date") LocalDate date);
    
    /**
     * Trouve les contrats en cours qui sont en retard
     */
    @Query("SELECT c FROM Contrat c WHERE c.dateFin < :date " +
           "AND c.etat = 'EN_COURS'")
    List<Contrat> findContratsEnRetard(@Param("date") LocalDate date);
    
    /**
     * Trouve les contrats en cours qui doivent se terminer aujourd'hui
     */
    @Query("SELECT c FROM Contrat c WHERE c.dateFin = :date " +
           "AND c.etat = 'EN_COURS'")
    List<Contrat> findContratsATerminerAujourdhui(@Param("date") LocalDate date);
    
    /**
     * Trouve tous les contrats d'un client sur une période donnée
     */
    @Query("SELECT c FROM Contrat c WHERE c.client.id = :clientId " +
           "AND c.etat NOT IN ('ANNULE', 'TERMINE') " +
           "AND ((c.dateDebut <= :dateFin AND c.dateFin >= :dateDebut))")
    List<Contrat> findContratsClientSurPeriode(
        @Param("clientId") Long clientId,
        @Param("dateDebut") LocalDate dateDebut,
        @Param("dateFin") LocalDate dateFin);
    
    /**
     * Compte le nombre de contrats par état
     */
    long countByEtat(EtatContrat etat);
}
