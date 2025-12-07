package com.BFB.automobile.data.repository;

import com.BFB.automobile.data.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration pour ClientRepository
 * Utilise @DataJpaTest pour tester avec une vraie base de données H2
 */
@DataJpaTest
@TestPropertySource(properties = {
    "spring.sql.init.mode=never"
})
@Transactional
class ClientRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private ClientRepository clientRepository;
    
    private Client client1;
    private Client client2;
    
    @BeforeEach
    void setUp() {
        client1 = Client.builder()
                .nom("Dupont")
                .prenom("Jean")
                .dateNaissance(LocalDate.of(1990, 5, 15))
                .numeroPermis("111111111")
                .adresse("10 Rue de la Paix, 75001 Paris")
                .actif(true)
                .build();
        
        client2 = Client.builder()
                .nom("Martin")
                .prenom("Sophie")
                .dateNaissance(LocalDate.of(1985, 8, 20))
                .numeroPermis("222222222")
                .adresse("25 Avenue des Champs, 69000 Lyon")
                .actif(true)
                .build();
    }
    
    // ========== Tests de sauvegarde ==========
    
    @Test
    void save_devraitPersisterClient() {
        // Arrange
        Client nouveauClient = Client.builder()
                .nom("Dupont")
                .prenom("Jean")
                .dateNaissance(LocalDate.of(1990, 5, 15))
                .numeroPermis("444444444")
                .adresse("10 Rue de la Paix, 75001 Paris")
                .actif(true)
                .build();
        
        // Act
        Client saved = clientRepository.save(nouveauClient);
        
        // Assert
        assertNotNull(saved.getId());
        assertEquals("Dupont", saved.getNom());
        assertEquals("Jean", saved.getPrenom());
        assertNotNull(saved.getDateCreation());
    }
    
    // ========== Tests d'unicité ==========
    
    @Test
    void existsByNomAndPrenomAndDateNaissance_devraitRetournerTrue_siClientExiste() {
        // Arrange
        client1.setNumeroPermis("333333333");
        entityManager.persist(client1);
        entityManager.flush();
        
        // Act
        boolean exists = clientRepository.existsByNomAndPrenomAndDateNaissance(
            "Dupont", "Jean", LocalDate.of(1990, 5, 15));
        
        // Assert
        assertTrue(exists);
    }
    
    @Test
    void existsByNomAndPrenomAndDateNaissance_devraitRetournerFalse_siClientNExistePas() {
        // Act
        boolean exists = clientRepository.existsByNomAndPrenomAndDateNaissance(
            "Inconnu", "Test", LocalDate.of(2000, 1, 1));
        
        // Assert
        assertFalse(exists);
    }
    
    @Test
    void existsByNumeroPermis_devraitRetournerTrue_siPermisExiste() {
        // Arrange
        client1.setNumeroPermis("234567890");
        entityManager.persist(client1);
        entityManager.flush();
        
        // Act
        boolean exists = clientRepository.existsByNumeroPermis("234567890");
        
        // Assert
        assertTrue(exists);
    }
    
    @Test
    void existsByNumeroPermis_devraitRetournerFalse_siPermisNExistePas() {
        // Act
        boolean exists = clientRepository.existsByNumeroPermis("000000000");
        
        // Assert
        assertFalse(exists);
    }
    
    // ========== Tests de recherche par numéro de permis ==========
    
    @Test
    void findByNumeroPermis_devraitRetournerClient_siExiste() {
        // Arrange
        client1.setNumeroPermis("345678901");
        entityManager.persist(client1);
        entityManager.flush();
        
        // Act
        Optional<Client> result = clientRepository.findByNumeroPermis("345678901");
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals("Dupont", result.get().getNom());
        assertEquals("Jean", result.get().getPrenom());
    }
    
    @Test
    void findByNumeroPermis_devraitRetournerEmpty_siNExistePas() {
        // Act
        Optional<Client> result = clientRepository.findByNumeroPermis("000000000");
        
        // Assert
        assertFalse(result.isPresent());
    }
    
    // ========== Tests de recherche par actif ==========
    
    @Test
    void findByActifTrue_devraitRetournerSeulementClientsActifs() {
        // Arrange
        client1.setNumeroPermis("666666666");
        client2.setNumeroPermis("777777777");
        client2.setActif(false);
        entityManager.persist(client1);
        entityManager.persist(client2);
        entityManager.flush();
        
        // Act
        List<Client> actifs = clientRepository.findByActifTrue();
        
        // Assert
        assertEquals(1, actifs.size());
        assertEquals("Dupont", actifs.get(0).getNom());
    }
    
    // ========== Tests de recherche par nom et prénom ==========
    
    @Test
    void findByNomContainingIgnoreCaseAndPrenomContainingIgnoreCase_devraitTrouverParNomEtPrenom() {
        // Arrange
        client1.setNumeroPermis("888888888");
        client2.setNumeroPermis("999999999");
        entityManager.persist(client1);
        entityManager.persist(client2);
        entityManager.flush();
        
        // Act
        List<Client> results = clientRepository
            .searchByNomAndPrenom("dupont", "jean");
        
        // Assert
        assertEquals(1, results.size());
        assertEquals("Dupont", results.get(0).getNom());
    }
    
    @Test
    void findByNomContainingIgnoreCaseAndPrenomContainingIgnoreCase_devraitIgnorerLaCasse() {
        // Arrange
        client1.setNumeroPermis("101010101");
        entityManager.persist(client1);
        entityManager.flush();
        
        // Act
        List<Client> results = clientRepository
            .searchByNomAndPrenom("DUPONT", "JEAN");
        
        // Assert
        assertEquals(1, results.size());
    }
    
    @Test
    void findByNomContainingIgnoreCase_devraitTrouverParNomSeul() {
        // Arrange
        client1.setNumeroPermis("121212121");
        client2.setNumeroPermis("131313131");
        entityManager.persist(client1);
        entityManager.persist(client2);
        entityManager.flush();
        
        // Act
        List<Client> results = clientRepository.findByNomContainingIgnoreCase("martin");
        
        // Assert
        assertEquals(1, results.size());
        assertEquals("Martin", results.get(0).getNom());
    }
    
    @Test
    void findByPrenomContainingIgnoreCase_devraitTrouverParPrenomSeul() {
        // Arrange
        client1.setNumeroPermis("141414141");
        client2.setNumeroPermis("151515151");
        entityManager.persist(client1);
        entityManager.persist(client2);
        entityManager.flush();
        
        // Act
        List<Client> results = clientRepository.findByPrenomContainingIgnoreCase("sophie");
        
        // Assert
        assertEquals(1, results.size());
        assertEquals("Sophie", results.get(0).getPrenom());
    }
    
    // ========== Tests de contraintes d'unicité ==========
    
    @Test
    void save_devraitEchouer_siClientDuplique() {
        // Arrange
        client1.setNumeroPermis("555555555");
        entityManager.persist(client1);
        entityManager.flush();
        
        Client duplicate = new Client();
        duplicate.setNom("Dupont");
        duplicate.setPrenom("Jean");
        duplicate.setDateNaissance(LocalDate.of(1990, 5, 15));
        duplicate.setNumeroPermis("AUTRE_PERMIS");
        duplicate.setAdresse("Autre adresse");
        duplicate.setActif(true);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            clientRepository.save(duplicate);
            entityManager.flush();
        });
    }
    
    @Test
    void save_devraitEchouer_siNumeroPermisDuplique() {
        // Arrange
        client1.setNumeroPermis("456789012");
        entityManager.persist(client1);
        entityManager.flush();
        
        Client duplicate = new Client();
        duplicate.setNom("Autre");
        duplicate.setPrenom("Personne");
        duplicate.setDateNaissance(LocalDate.of(1995, 1, 1));
        duplicate.setNumeroPermis("456789012"); // Même permis que client1
        duplicate.setAdresse("Autre adresse");
        duplicate.setActif(true);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            clientRepository.save(duplicate);
            entityManager.flush();
        });
    }
}
