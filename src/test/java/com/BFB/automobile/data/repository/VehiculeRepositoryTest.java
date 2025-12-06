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
 * Tests d'intégration pour VehiculeRepository
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
        vehicule1 = new Vehicule();
        vehicule1.setMarque("Peugeot");
        vehicule1.setModele("308");
        vehicule1.setMotorisation("Diesel");
        vehicule1.setCouleur("Blanc");
        vehicule1.setImmatriculation("AA-111-AA");
        vehicule1.setDateAcquisition(LocalDate.of(2020, 1, 15));
        vehicule1.setEtat(EtatVehicule.DISPONIBLE);
        
        vehicule2 = new Vehicule();
        vehicule2.setMarque("Renault");
        vehicule2.setModele("Clio");
        vehicule2.setMotorisation("Essence");
        vehicule2.setCouleur("Rouge");
        vehicule2.setImmatriculation("BB-222-BB");
        vehicule2.setDateAcquisition(LocalDate.of(2021, 6, 10));
        vehicule2.setEtat(EtatVehicule.EN_LOCATION);
        
        vehicule3 = new Vehicule();
        vehicule3.setMarque("Peugeot");
        vehicule3.setModele("208");
        vehicule3.setMotorisation("Essence");
        vehicule3.setCouleur("Noir");
        vehicule3.setImmatriculation("CC-333-CC");
        vehicule3.setDateAcquisition(LocalDate.of(2022, 3, 5));
        vehicule3.setEtat(EtatVehicule.EN_PANNE);
    }
    
    // ========== Tests de sauvegarde ==========
    
    @Test
    void save_devraitPersisterVehicule() {
        // Arrange
        vehicule1.setImmatriculation("DD-444-DD");
        
        // Act
        Vehicule saved = vehiculeRepository.save(vehicule1);
        
        // Assert
        assertNotNull(saved.getId());
        assertEquals("Peugeot", saved.getMarque());
        assertEquals("DD-444-DD", saved.getImmatriculation());
    }
    
    // ========== Tests d'unicité ==========
    
    @Test
    void existsByImmatriculation_devraitRetournerTrue_siImmatriculationExiste() {
        // Arrange
        vehicule1.setImmatriculation("CD-456-EF");
        entityManager.persist(vehicule1);
        entityManager.flush();
        
        // Act
        boolean exists = vehiculeRepository.existsByImmatriculation("CD-456-EF");
        
        // Assert
        assertTrue(exists);
    }
    
    @Test
    void existsByImmatriculation_devraitRetournerFalse_siImmatriculationNExistePas() {
        // Act
        boolean exists = vehiculeRepository.existsByImmatriculation("ZZ-000-ZZ");
        
        // Assert
        assertFalse(exists);
    }
    
    @Test
    void save_devraitEchouer_siImmatriculationDupliquee() {
        // Arrange
        vehicule1.setImmatriculation("GH-789-IJ");
        entityManager.persist(vehicule1);
        entityManager.flush();
        
        Vehicule duplicate = new Vehicule();
        duplicate.setMarque("Autre");
        duplicate.setModele("Modele");
        duplicate.setMotorisation("Essence");
        duplicate.setCouleur("Bleu");
        duplicate.setImmatriculation("GH-789-IJ"); // Même immatriculation
        duplicate.setDateAcquisition(LocalDate.now());
        duplicate.setEtat(EtatVehicule.DISPONIBLE);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            vehiculeRepository.save(duplicate);
            entityManager.flush();
        });
    }
    
    // ========== Tests de recherche par état ==========
    
    @Test
    void findByEtatOrderByMarqueAscModeleAsc_devraitRetournerVehiculesDansOrdre() {
        // Arrange
        vehicule1.setImmatriculation("EE-555-EE");
        vehicule3.setImmatriculation("FF-666-FF");
        entityManager.persist(vehicule1); // Peugeot 308
        entityManager.persist(vehicule3); // Peugeot 208
        
        vehicule1.setEtat(EtatVehicule.DISPONIBLE);
        vehicule3.setEtat(EtatVehicule.DISPONIBLE);
        entityManager.flush();
        
        // Act
        List<Vehicule> disponibles = vehiculeRepository
            .findByEtatOrderByMarqueAscModeleAsc(EtatVehicule.DISPONIBLE);
        
        // Assert
        assertEquals(2, disponibles.size());
        // Peugeot 208 avant Peugeot 308 (ordre alphabétique sur modèle)
        assertEquals("208", disponibles.get(0).getModele());
        assertEquals("308", disponibles.get(1).getModele());
    }
    
    @Test
    void findByEtatOrderByMarqueAscModeleAsc_devraitRetournerListeVide_siAucunVehiculeDansEtat() {
        // Arrange
        vehicule1.setImmatriculation("GG-777-GG");
        entityManager.persist(vehicule1);
        vehicule1.setEtat(EtatVehicule.DISPONIBLE);
        entityManager.flush();
        
        // Act
        List<Vehicule> enPanne = vehiculeRepository
            .findByEtatOrderByMarqueAscModeleAsc(EtatVehicule.EN_PANNE);
        
        // Assert
        assertTrue(enPanne.isEmpty());
    }
    
    // ========== Tests de recherche par marque et modèle ==========
    
    @Test
    void searchByMarqueAndModele_devraitTrouverParMarqueEtModele() {
        // Arrange
        vehicule1.setImmatriculation("HH-888-HH");
        vehicule2.setImmatriculation("II-999-II");
        vehicule3.setImmatriculation("JJ-101-JJ");
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
    void searchByMarqueAndModele_devraitIgnorerLaCasse() {
        // Arrange
        vehicule1.setImmatriculation("KK-111-KK");
        entityManager.persist(vehicule1);
        entityManager.flush();
        
        // Act
        List<Vehicule> results = vehiculeRepository.searchByMarqueAndModele("PEUGEOT", "308");
        
        // Assert
        assertEquals(1, results.size());
    }
    
    @Test
    void findByMarqueContainingIgnoreCase_devraitTrouverParMarqueSeule() {
        // Arrange
        vehicule1.setImmatriculation("LL-222-LL");
        vehicule2.setImmatriculation("MM-333-MM");
        vehicule3.setImmatriculation("NN-444-NN");
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
    
    @Test
    void findByMarqueContainingIgnoreCase_devraitTrouverParRecherchePaartielle() {
        // Arrange
        vehicule1.setImmatriculation("OO-555-OO");
        vehicule1.setMarque("Peugeot");
        vehicule2.setImmatriculation("PP-666-PP");
        vehicule2.setMarque("Renault");
        entityManager.persist(vehicule1);
        entityManager.persist(vehicule2);
        entityManager.flush();
        
        // Act
        List<Vehicule> results = vehiculeRepository.findByMarqueContainingIgnoreCase("eu");
        
        // Assert
        assertEquals(1, results.size()); // Seulement Peugeot contient "eu"
        assertEquals("Peugeot", results.get(0).getMarque());
    }
}
