package com.BFB.automobile.data.repository;

import com.BFB.automobile.data.EtatVehicule;
import com.BFB.automobile.data.Vehicule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration Repository - Requêtes custom uniquement
 */
@DataJpaTest
@TestPropertySource(properties = {
    "spring.sql.init.mode=never"
})
@Transactional
class VehiculeRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private VehiculeRepository vehiculeRepository;
    
    private Vehicule vehicule1;
    private Vehicule vehicule2;
    private Vehicule vehicule3;
    
    @BeforeEach
    void setUp() {
        vehicule1 = Vehicule.builder()
                .marque("Peugeot")
                .modele("308")
                .motorisation("Diesel")
                .couleur("Blanc")
                .immatriculation("AA-111-AA")
                .dateAcquisition(LocalDate.of(2020, 1, 15))
                .etat(EtatVehicule.DISPONIBLE)
                .build();
        
        vehicule2 = Vehicule.builder()
                .marque("Renault")
                .modele("Clio")
                .motorisation("Essence")
                .couleur("Rouge")
                .immatriculation("BB-222-BB")
                .dateAcquisition(LocalDate.of(2021, 6, 10))
                .etat(EtatVehicule.EN_LOCATION)
                .build();
        
        vehicule3 = Vehicule.builder()
                .marque("Peugeot")
                .modele("208")
                .motorisation("Essence")
                .couleur("Noir")
                .immatriculation("CC-333-CC")
                .dateAcquisition(LocalDate.of(2022, 3, 5))
                .etat(EtatVehicule.EN_PANNE)
                .build();
    }
    
    @Test
    void findByEtatOrderByMarqueAscModeleAsc_devraitTrierCorrectement() {
        // Arrange
        entityManager.persist(vehicule1);
        entityManager.persist(vehicule3);
        vehicule1.setEtat(EtatVehicule.DISPONIBLE);
        vehicule3.setEtat(EtatVehicule.DISPONIBLE);
        entityManager.flush();
        
        // Act
        List<Vehicule> disponibles = vehiculeRepository
            .findByEtatOrderByMarqueAscModeleAsc(EtatVehicule.DISPONIBLE);
        
        // Assert
        assertEquals(2, disponibles.size());
        assertEquals("208", disponibles.get(0).getModele());
        assertEquals("308", disponibles.get(1).getModele());
    }
    
    @Test
    void searchByMarqueAndModele_devraitTrouverParRecherche() {
        // Arrange
        entityManager.persist(vehicule1);
        entityManager.persist(vehicule2);
        entityManager.persist(vehicule3);
        entityManager.flush();
        
        // Act
        List<Vehicule> results = vehiculeRepository.searchByMarqueAndModele("peugeot", "308");
        
        // Assert
        assertEquals(1, results.size());
        assertEquals("Peugeot", results.get(0).getMarque());
        assertEquals("308", results.get(0).getModele());
    }
    
    @Test
    void findByMarqueContainingIgnoreCase_devraitTrouverParMarque() {
        // Arrange
        entityManager.persist(vehicule1);
        entityManager.persist(vehicule2);
        entityManager.persist(vehicule3);
        entityManager.flush();
        
        // Act
        List<Vehicule> peugeots = vehiculeRepository.findByMarqueContainingIgnoreCase("peugeot");
        
        // Assert
        assertEquals(2, peugeots.size());
        assertTrue(peugeots.stream().allMatch(v -> v.getMarque().equals("Peugeot")));
    }
}
