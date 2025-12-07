package com.BFB.automobile.data.repository;

import com.BFB.automobile.data.*;
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
class ContratRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private ContratRepository contratRepository;
    
    private Client client;
    private Vehicule vehicule;
    private Contrat contrat1;
    
    @BeforeEach
    void setUp() {
        client = Client.builder()
                .nom("Dupont")
                .prenom("Jean")
                .dateNaissance(LocalDate.of(1990, 5, 15))
                .numeroPermis("567890123")
                .adresse("10 Rue de la Paix")
                .actif(true)
                .build();
        
        vehicule = Vehicule.builder()
                .marque("Peugeot")
                .modele("308")
                .motorisation("Diesel")
                .couleur("Blanc")
                .immatriculation("KL-012-MN")
                .dateAcquisition(LocalDate.of(2020, 1, 15))
                .etat(EtatVehicule.DISPONIBLE)
                .build();
        
        entityManager.persist(client);
        entityManager.persist(vehicule);
        
        contrat1 = Contrat.builder()
                .client(client)
                .vehicule(vehicule)
                .dateDebut(LocalDate.of(2024, 1, 10))
                .dateFin(LocalDate.of(2024, 1, 20))
                .etat(EtatContrat.EN_COURS)
                .build();
    }
    
    @Test
    void findContratsConflictuels_devraitDetecterChevauchement() {
        // Arrange
        entityManager.persist(contrat1);
        entityManager.flush();
        
        // Act - Nouveau contrat du 12 au 18 janvier (chevauche)
        List<Contrat> conflits = contratRepository.findContratsConflictuels(
            vehicule.getId(),
            LocalDate.of(2024, 1, 12),
            LocalDate.of(2024, 1, 18)
        );
        
        // Assert
        assertEquals(1, conflits.size());
    }
    
    @Test
    void findContratsClientSurPeriode_devraitRetournerContratsClient() {
        // Arrange
        entityManager.persist(contrat1);
        entityManager.flush();
        
        // Act
        List<Contrat> contrats = contratRepository.findContratsClientSurPeriode(
            client.getId(),
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 1, 31)
        );
        
        // Assert
        assertEquals(1, contrats.size());
    }
    
    @Test
    void findByVehiculeIdOrderByDateDebutDesc_devraitTrierParDate() {
        // Arrange
        Contrat contrat2 = Contrat.builder()
                .client(client)
                .vehicule(vehicule)
                .dateDebut(LocalDate.of(2024, 2, 1))
                .dateFin(LocalDate.of(2024, 2, 10))
                .etat(EtatContrat.EN_ATTENTE)
                .build();
        
        entityManager.persist(contrat1);
        entityManager.persist(contrat2);
        entityManager.flush();
        
        // Act
        List<Contrat> contrats = contratRepository.findByVehiculeIdOrderByDateDebutDesc(vehicule.getId());
        
        // Assert
        assertEquals(2, contrats.size());
        assertEquals(LocalDate.of(2024, 2, 1), contrats.get(0).getDateDebut());
    }
    
    @Test
    void findByClientIdOrderByDateDebutDesc_devraitTrierParDate() {
        // Arrange
        Contrat contrat2 = Contrat.builder()
                .client(client)
                .vehicule(vehicule)
                .dateDebut(LocalDate.of(2024, 2, 1))
                .dateFin(LocalDate.of(2024, 2, 10))
                .etat(EtatContrat.EN_ATTENTE)
                .build();
        
        entityManager.persist(contrat1);
        entityManager.persist(contrat2);
        entityManager.flush();
        
        // Act
        List<Contrat> contrats = contratRepository.findByClientIdOrderByDateDebutDesc(client.getId());
        
        // Assert
        assertEquals(2, contrats.size());
        assertEquals(LocalDate.of(2024, 2, 1), contrats.get(0).getDateDebut());
    }
}
