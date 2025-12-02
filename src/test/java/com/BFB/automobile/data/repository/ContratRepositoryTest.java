package com.BFB.automobile.data.repository;

import com.BFB.automobile.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration pour ContratRepository
 * 
 * JUSTIFICATION COUCHE DATA :
 * - Teste les requêtes complexes avec jointures
 * - Valide les relations entre entités
 * - Vérifie les contraintes de dates
 */
@DataJpaTest
@DisplayName("ContratRepository - Tests d'intégration")
class ContratRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ContratRepository contratRepository;

    private Client client;
    private Vehicule vehicule;
    private Contrat contratEnAttente;
    private Contrat contratConfirme;

    @BeforeEach
    void setUp() {
        // Client
        client = new Client();
        client.setNom("Dupont");
        client.setPrenom("Jean");
        client.setDateNaissance(LocalDate.of(1990, 1, 1));
        client.setNumeroPermis("PERM123456");
        client.setEmail("jean.dupont@email.com");

        // Véhicule
        vehicule = new Vehicule();
        vehicule.setImmatriculation("AB-123-CD");
        vehicule.setMarque("Peugeot");
        vehicule.setModele("308");
        vehicule.setAnnee(2023);
        vehicule.setPrixJournalier(new BigDecimal("50.00"));
        vehicule.setEtat(EtatVehicule.DISPONIBLE);

        // Contrats
        contratEnAttente = new Contrat();
        contratEnAttente.setClient(client);
        contratEnAttente.setVehicule(vehicule);
        contratEnAttente.setDateDebut(LocalDate.now().plusDays(1));
        contratEnAttente.setDateFin(LocalDate.now().plusDays(3));
        contratEnAttente.setPrixTotal(new BigDecimal("150.00"));
        contratEnAttente.setEtat(EtatContrat.EN_ATTENTE);

        contratConfirme = new Contrat();
        contratConfirme.setClient(client);
        contratConfirme.setVehicule(vehicule);
        contratConfirme.setDateDebut(LocalDate.now().plusDays(5));
        contratConfirme.setDateFin(LocalDate.now().plusDays(7));
        contratConfirme.setPrixTotal(new BigDecimal("100.00"));
        contratConfirme.setEtat(EtatContrat.CONFIRME);
    }

    @Test
    @DisplayName("Devrait sauvegarder et récupérer un contrat")
    void saveAndFindById_ShouldWork() {
        // Given
        Client savedClient = entityManager.persistAndFlush(client);
        Vehicule savedVehicule = entityManager.persistAndFlush(vehicule);
        contratEnAttente.setClient(savedClient);
        contratEnAttente.setVehicule(savedVehicule);

        // When
        Contrat savedContrat = contratRepository.save(contratEnAttente);
        Contrat foundContrat = contratRepository.findById(savedContrat.getId()).orElse(null);

        // Then
        assertThat(foundContrat).isNotNull();
        assertThat(foundContrat.getClient().getNom()).isEqualTo("Dupont");
        assertThat(foundContrat.getVehicule().getImmatriculation()).isEqualTo("AB-123-CD");
        assertThat(foundContrat.getEtat()).isEqualTo(EtatContrat.EN_ATTENTE);
    }

    @Test
    @DisplayName("Devrait retourner les contrats par état")
    void findByEtat_ShouldReturnContractsWithSpecificState() {
        // Given
        Client savedClient = entityManager.persistAndFlush(client);
        Vehicule savedVehicule = entityManager.persistAndFlush(vehicule);
        
        contratEnAttente.setClient(savedClient);
        contratEnAttente.setVehicule(savedVehicule);
        entityManager.persistAndFlush(contratEnAttente);

        contratConfirme.setClient(savedClient);
        contratConfirme.setVehicule(savedVehicule);
        entityManager.persistAndFlush(contratConfirme);

        // When
        List<Contrat> contratsEnAttente = contratRepository.findByEtat(EtatContrat.EN_ATTENTE);
        List<Contrat> contratsConfirmes = contratRepository.findByEtat(EtatContrat.CONFIRME);

        // Then
        assertThat(contratsEnAttente).hasSize(1);
        assertThat(contratsEnAttente.get(0).getPrixTotal()).isEqualTo(new BigDecimal("150.00"));

        assertThat(contratsConfirmes).hasSize(1);
        assertThat(contratsConfirmes.get(0).getPrixTotal()).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Devrait retourner les contrats par client")
    void findByClientId_ShouldReturnContractsForSpecificClient() {
        // Given
        Client savedClient = entityManager.persistAndFlush(client);
        Vehicule savedVehicule = entityManager.persistAndFlush(vehicule);
        
        contratEnAttente.setClient(savedClient);
        contratEnAttente.setVehicule(savedVehicule);
        entityManager.persistAndFlush(contratEnAttente);

        // When
        List<Contrat> contratsClient = contratRepository.findByClientId(savedClient.getId());

        // Then
        assertThat(contratsClient).hasSize(1);
        assertThat(contratsClient.get(0).getClient().getId()).isEqualTo(savedClient.getId());
    }

    @Test
    @DisplayName("Devrait retourner les contrats par véhicule")
    void findByVehiculeId_ShouldReturnContractsForSpecificVehicle() {
        // Given
        Client savedClient = entityManager.persistAndFlush(client);
        Vehicule savedVehicule = entityManager.persistAndFlush(vehicule);
        
        contratEnAttente.setClient(savedClient);
        contratEnAttente.setVehicule(savedVehicule);
        entityManager.persistAndFlush(contratEnAttente);

        // When
        List<Contrat> contratsVehicule = contratRepository.findByVehiculeId(savedVehicule.getId());

        // Then
        assertThat(contratsVehicule).hasSize(1);
        assertThat(contratsVehicule.get(0).getVehicule().getId()).isEqualTo(savedVehicule.getId());
    }

    @Test
    @DisplayName("Devrait retourner tous les contrats")
    void findAll_ShouldReturnAllContracts() {
        // Given
        Client savedClient = entityManager.persistAndFlush(client);
        Vehicule savedVehicule = entityManager.persistAndFlush(vehicule);
        
        contratEnAttente.setClient(savedClient);
        contratEnAttente.setVehicule(savedVehicule);
        entityManager.persistAndFlush(contratEnAttente);

        contratConfirme.setClient(savedClient);
        contratConfirme.setVehicule(savedVehicule);
        entityManager.persistAndFlush(contratConfirme);

        // When
        List<Contrat> contrats = contratRepository.findAll();

        // Then
        assertThat(contrats).hasSize(2);
        assertThat(contrats).extracting(Contrat::getEtat)
            .containsExactlyInAnyOrder(EtatContrat.EN_ATTENTE, EtatContrat.CONFIRME);
    }
}