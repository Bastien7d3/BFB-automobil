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
 * Tests d'intégration pour ContratRepository
 * Teste notamment la requête complexe de détection de conflits
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
    private Contrat contrat2;
    
    @BeforeEach
    void setUp() {
        // Créer un client
        client = Client.builder()
                .nom("Dupont")
                .prenom("Jean")
                .dateNaissance(LocalDate.of(1990, 5, 15))
                .numeroPermis("567890123")
                .adresse("10 Rue de la Paix")
                .actif(true)
                .build();
        
        // Créer un véhicule
        vehicule = Vehicule.builder()
                .marque("Peugeot")
                .modele("308")
                .motorisation("Diesel")
                .couleur("Blanc")
                .immatriculation("KL-012-MN")
                .dateAcquisition(LocalDate.of(2020, 1, 15))
                .etat(EtatVehicule.DISPONIBLE)
                .build();
        
        // Persister client et véhicule
        entityManager.persist(client);
        entityManager.persist(vehicule);
        
        // Créer des contrats
        contrat1 = Contrat.builder()
                .client(client)
                .vehicule(vehicule)
                .dateDebut(LocalDate.of(2024, 1, 10))
                .dateFin(LocalDate.of(2024, 1, 20))
                .etat(EtatContrat.EN_COURS)
                .build();
        
        contrat2 = Contrat.builder()
                .client(client)
                .vehicule(vehicule)
                .dateDebut(LocalDate.of(2024, 2, 1))
                .dateFin(LocalDate.of(2024, 2, 10))
                .etat(EtatContrat.EN_ATTENTE)
                .build();
    }
    
    // ========== Tests de sauvegarde ==========
    
    @Test
    void save_devraitPersisterContrat() {
        // Act
        Contrat saved = contratRepository.save(contrat1);
        
        // Assert
        assertNotNull(saved.getId());
        assertNotNull(saved.getDateCreation());
        assertEquals(EtatContrat.EN_COURS, saved.getEtat());
    }
    
    // ========== Tests de recherche par client ==========
    
    @Test
    void findByClientIdOrderByDateDebutDesc_devraitRetournerContratsTriesParDate() {
        // Arrange
        entityManager.persist(contrat1); // 10-20 janvier
        entityManager.persist(contrat2); // 1-10 février
        entityManager.flush();
        
        // Act
        List<Contrat> contrats = contratRepository.findByClientIdOrderByDateDebutDesc(client.getId());
        
        // Assert
        assertEquals(2, contrats.size());
        // Février avant janvier (DESC)
        assertEquals(LocalDate.of(2024, 2, 1), contrats.get(0).getDateDebut());
        assertEquals(LocalDate.of(2024, 1, 10), contrats.get(1).getDateDebut());
    }
    
    @Test
    void findByClient_devraitRetournerTousLesContratsClient() {
        // Arrange
        entityManager.persist(contrat1);
        entityManager.persist(contrat2);
        entityManager.flush();
        
        // Act
        List<Contrat> contrats = contratRepository.findByClient(client);
        
        // Assert
        assertEquals(2, contrats.size());
    }
    
    // ========== Tests de recherche par véhicule ==========
    
    @Test
    void findByVehiculeIdOrderByDateDebutDesc_devraitRetournerContratsTriesParDate() {
        // Arrange
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
    void findByVehicule_devraitRetournerTousLesContratsVehicule() {
        // Arrange
        entityManager.persist(contrat1);
        entityManager.persist(contrat2);
        entityManager.flush();
        
        // Act
        List<Contrat> contrats = contratRepository.findByVehicule(vehicule);
        
        // Assert
        assertEquals(2, contrats.size());
    }
    
    // ========== Tests de recherche par état ==========
    
    @Test
    void findByEtat_devraitRetournerSeulementContratsAvecEtatDonne() {
        // Arrange
        Client client1 = new Client();
        client1.setNom("TestClient1");
        client1.setPrenom("Test1");
        client1.setDateNaissance(LocalDate.of(1990, 1, 1));
        client1.setNumeroPermis("PERM_ETAT_001");
        client1.setAdresse("Adresse 1");
        client1.setActif(true);
        entityManager.persist(client1);
        
        Vehicule vehicule1 = new Vehicule();
        vehicule1.setMarque("Marque1");
        vehicule1.setModele("Modele1");
        vehicule1.setMotorisation("Essence");
        vehicule1.setCouleur("Bleu");
        vehicule1.setImmatriculation("ETAT-001-AA");
        vehicule1.setDateAcquisition(LocalDate.of(2020, 1, 1));
        vehicule1.setEtat(EtatVehicule.DISPONIBLE);
        entityManager.persist(vehicule1);
        
        Contrat c1 = new Contrat();
        c1.setClient(client1);
        c1.setVehicule(vehicule1);
        c1.setDateDebut(LocalDate.of(2024, 1, 1));
        c1.setDateFin(LocalDate.of(2024, 1, 10));
        c1.setEtat(EtatContrat.EN_COURS);
        
        Contrat c2 = new Contrat();
        c2.setClient(client1);
        c2.setVehicule(vehicule1);
        c2.setDateDebut(LocalDate.of(2024, 2, 1));
        c2.setDateFin(LocalDate.of(2024, 2, 10));
        c2.setEtat(EtatContrat.EN_ATTENTE);
        
        entityManager.persist(c1);
        entityManager.persist(c2);
        entityManager.flush();
        
        // Act
        List<Contrat> enCours = contratRepository.findByEtat(EtatContrat.EN_COURS);
        List<Contrat> enAttente = contratRepository.findByEtat(EtatContrat.EN_ATTENTE);
        
        // Assert
        assertEquals(1, enCours.size());
        assertEquals(1, enAttente.size());
        assertEquals(EtatContrat.EN_COURS, enCours.get(0).getEtat());
    }
    
    @Test
    void findContratsActifs_devraitRetournerSeulementEnAttenteEtEnCours() {
        // Arrange
        Client client2 = new Client();
        client2.setNom("TestClient2");
        client2.setPrenom("Test2");
        client2.setDateNaissance(LocalDate.of(1991, 2, 2));
        client2.setNumeroPermis("PERM_ACTIF_002");
        client2.setAdresse("Adresse 2");
        client2.setActif(true);
        entityManager.persist(client2);
        
        Vehicule vehicule2 = new Vehicule();
        vehicule2.setMarque("Marque2");
        vehicule2.setModele("Modele2");
        vehicule2.setMotorisation("Diesel");
        vehicule2.setCouleur("Rouge");
        vehicule2.setImmatriculation("ACTIF-002-BB");
        vehicule2.setDateAcquisition(LocalDate.of(2020, 2, 2));
        vehicule2.setEtat(EtatVehicule.DISPONIBLE);
        entityManager.persist(vehicule2);
        
        Contrat c1 = new Contrat();
        c1.setClient(client2);
        c1.setVehicule(vehicule2);
        c1.setDateDebut(LocalDate.of(2024, 1, 1));
        c1.setDateFin(LocalDate.of(2024, 1, 10));
        c1.setEtat(EtatContrat.EN_COURS);
        
        Contrat c2 = new Contrat();
        c2.setClient(client2);
        c2.setVehicule(vehicule2);
        c2.setDateDebut(LocalDate.of(2024, 2, 1));
        c2.setDateFin(LocalDate.of(2024, 2, 10));
        c2.setEtat(EtatContrat.EN_ATTENTE);
        
        Contrat c3 = new Contrat();
        c3.setClient(client2);
        c3.setVehicule(vehicule2);
        c3.setDateDebut(LocalDate.of(2024, 3, 1));
        c3.setDateFin(LocalDate.of(2024, 3, 10));
        c3.setEtat(EtatContrat.TERMINE);
        
        entityManager.persist(c1);
        entityManager.persist(c2);
        entityManager.persist(c3);
        entityManager.flush();
        
        // Act
        List<Contrat> actifs = contratRepository.findContratsActifs();
        
        // Assert
        assertEquals(2, actifs.size());
        assertTrue(actifs.stream().noneMatch(c -> c.getEtat() == EtatContrat.TERMINE));
    }
    
    @Test
    void countByEtat_devraitCompterContratsParEtat() {
        // Arrange
        Client client3 = new Client();
        client3.setNom("TestClient3");
        client3.setPrenom("Test3");
        client3.setDateNaissance(LocalDate.of(1992, 3, 3));
        client3.setNumeroPermis("PERM_COUNT_003");
        client3.setAdresse("Adresse 3");
        client3.setActif(true);
        entityManager.persist(client3);
        
        Vehicule vehicule3 = new Vehicule();
        vehicule3.setMarque("Marque3");
        vehicule3.setModele("Modele3");
        vehicule3.setMotorisation("Hybride");
        vehicule3.setCouleur("Vert");
        vehicule3.setImmatriculation("COUNT-003-CC");
        vehicule3.setDateAcquisition(LocalDate.of(2020, 3, 3));
        vehicule3.setEtat(EtatVehicule.DISPONIBLE);
        entityManager.persist(vehicule3);
        
        Contrat c1 = new Contrat();
        c1.setClient(client3);
        c1.setVehicule(vehicule3);
        c1.setDateDebut(LocalDate.of(2024, 1, 1));
        c1.setDateFin(LocalDate.of(2024, 1, 10));
        c1.setEtat(EtatContrat.EN_COURS);
        
        Contrat c2 = new Contrat();
        c2.setClient(client3);
        c2.setVehicule(vehicule3);
        c2.setDateDebut(LocalDate.of(2024, 2, 1));
        c2.setDateFin(LocalDate.of(2024, 2, 10));
        c2.setEtat(EtatContrat.EN_ATTENTE);
        
        entityManager.persist(c1);
        entityManager.persist(c2);
        entityManager.flush();
        
        // Act
        long countEnCours = contratRepository.countByEtat(EtatContrat.EN_COURS);
        long countEnAttente = contratRepository.countByEtat(EtatContrat.EN_ATTENTE);
        long countTermine = contratRepository.countByEtat(EtatContrat.TERMINE);
        
        // Assert
        assertEquals(1, countEnCours);
        assertEquals(1, countEnAttente);
        assertEquals(0, countTermine);
    }
    
    // ========== Tests de détection de conflits (CRITIQUE) ==========
    
    @Test
    void findContratsConflictuels_devraitDetecterChevauchementTotal() {
        // Arrange - Contrat existant du 10 au 20 janvier
        entityManager.persist(contrat1);
        entityManager.flush();
        
        // Act - Nouveau contrat du 12 au 18 janvier (complètement inclus)
        List<Contrat> conflits = contratRepository.findContratsConflictuels(
            vehicule.getId(),
            LocalDate.of(2024, 1, 12),
            LocalDate.of(2024, 1, 18)
        );
        
        // Assert
        assertEquals(1, conflits.size());
    }
    
    @Test
    void findContratsConflictuels_devraitDetecterChevauchementDebut() {
        // Arrange - Contrat existant du 10 au 20 janvier
        entityManager.persist(contrat1);
        entityManager.flush();
        
        // Act - Nouveau contrat du 5 au 15 janvier (chevauche le début)
        List<Contrat> conflits = contratRepository.findContratsConflictuels(
            vehicule.getId(),
            LocalDate.of(2024, 1, 5),
            LocalDate.of(2024, 1, 15)
        );
        
        // Assert
        assertEquals(1, conflits.size());
    }
    
    @Test
    void findContratsConflictuels_devraitDetecterChevauchementFin() {
        // Arrange - Contrat existant du 10 au 20 janvier
        entityManager.persist(contrat1);
        entityManager.flush();
        
        // Act - Nouveau contrat du 15 au 25 janvier (chevauche la fin)
        List<Contrat> conflits = contratRepository.findContratsConflictuels(
            vehicule.getId(),
            LocalDate.of(2024, 1, 15),
            LocalDate.of(2024, 1, 25)
        );
        
        // Assert
        assertEquals(1, conflits.size());
    }
    
    @Test
    void findContratsConflictuels_devraitDetecterEnglobement() {
        // Arrange - Contrat existant du 10 au 20 janvier
        entityManager.persist(contrat1);
        entityManager.flush();
        
        // Act - Nouveau contrat du 5 au 25 janvier (englobe complètement)
        List<Contrat> conflits = contratRepository.findContratsConflictuels(
            vehicule.getId(),
            LocalDate.of(2024, 1, 5),
            LocalDate.of(2024, 1, 25)
        );
        
        // Assert
        assertEquals(1, conflits.size());
    }
    
    @Test
    void findContratsConflictuels_neDevraitPasDetecterSiPasDeConflit() {
        // Arrange - Contrat existant du 10 au 20 janvier
        entityManager.persist(contrat1);
        entityManager.flush();
        
        // Act - Nouveau contrat du 25 au 30 janvier (après, pas de conflit)
        List<Contrat> conflits = contratRepository.findContratsConflictuels(
            vehicule.getId(),
            LocalDate.of(2024, 1, 25),
            LocalDate.of(2024, 1, 30)
        );
        
        // Assert
        assertTrue(conflits.isEmpty());
    }
    
    @Test
    void findContratsConflictuels_neDevraitPasIncluireContratsAnnulesOuTermines() {
        // Arrange
        contrat1.setEtat(EtatContrat.TERMINE);
        entityManager.persist(contrat1);
        entityManager.flush();
        
        // Act - Même période que contrat1 mais il est TERMINE
        List<Contrat> conflits = contratRepository.findContratsConflictuels(
            vehicule.getId(),
            LocalDate.of(2024, 1, 10),
            LocalDate.of(2024, 1, 20)
        );
        
        // Assert
        assertTrue(conflits.isEmpty());
    }
    
    // ========== Tests des requêtes pour tâches planifiées ==========
    
    @Test
    void findContratsEnAttenteByVehicule_devraitRetournerSeulementEnAttente() {
        // Arrange
        contrat1.setEtat(EtatContrat.EN_COURS);
        contrat2.setEtat(EtatContrat.EN_ATTENTE);
        entityManager.persist(contrat1);
        entityManager.persist(contrat2);
        entityManager.flush();
        
        // Act
        List<Contrat> enAttente = contratRepository.findContratsEnAttenteByVehicule(vehicule.getId());
        
        // Assert
        assertEquals(1, enAttente.size());
        assertEquals(EtatContrat.EN_ATTENTE, enAttente.get(0).getEtat());
    }
    
    @Test
    void findContratsADemarrerAujourdhui_devraitTrouverContratsQuiCommencent() {
        // Arrange
        LocalDate aujourdhui = LocalDate.now();
        contrat1.setDateDebut(aujourdhui);
        contrat1.setEtat(EtatContrat.EN_ATTENTE);
        
        contrat2.setDateDebut(aujourdhui.plusDays(1));
        contrat2.setEtat(EtatContrat.EN_ATTENTE);
        
        entityManager.persist(contrat1);
        entityManager.persist(contrat2);
        entityManager.flush();
        
        // Act
        List<Contrat> aDemarrer = contratRepository.findContratsADemarrerAujourdhui(aujourdhui);
        
        // Assert
        assertEquals(1, aDemarrer.size());
        assertEquals(aujourdhui, aDemarrer.get(0).getDateDebut());
    }
    
    @Test
    void findContratsEnRetard_devraitTrouverContratsDepasses() {
        // Arrange
        Client client4 = new Client();
        client4.setNom("TestClient4");
        client4.setPrenom("Test4");
        client4.setDateNaissance(LocalDate.of(1993, 4, 4));
        client4.setNumeroPermis("PERM_RETARD_004");
        client4.setAdresse("Adresse 4");
        client4.setActif(true);
        entityManager.persist(client4);
        
        Vehicule vehicule4 = new Vehicule();
        vehicule4.setMarque("Marque4");
        vehicule4.setModele("Modele4");
        vehicule4.setMotorisation("Électrique");
        vehicule4.setCouleur("Blanc");
        vehicule4.setImmatriculation("RETARD-004-DD");
        vehicule4.setDateAcquisition(LocalDate.of(2020, 4, 4));
        vehicule4.setEtat(EtatVehicule.DISPONIBLE);
        entityManager.persist(vehicule4);
        
        LocalDate aujourdhui = LocalDate.now();
        
        Contrat c1 = new Contrat();
        c1.setClient(client4);
        c1.setVehicule(vehicule4);
        c1.setDateDebut(aujourdhui.minusDays(10));
        c1.setDateFin(aujourdhui.minusDays(5));
        c1.setEtat(EtatContrat.EN_COURS);
        
        Contrat c2 = new Contrat();
        c2.setClient(client4);
        c2.setVehicule(vehicule4);
        c2.setDateDebut(aujourdhui.plusDays(1));
        c2.setDateFin(aujourdhui.plusDays(5));
        c2.setEtat(EtatContrat.EN_COURS);
        
        entityManager.persist(c1);
        entityManager.persist(c2);
        entityManager.flush();
        
        // Act
        List<Contrat> enRetard = contratRepository.findContratsEnRetard(aujourdhui);
        
        // Assert
        assertEquals(1, enRetard.size());
        assertTrue(enRetard.get(0).getDateFin().isBefore(aujourdhui));
    }
    
    @Test
    void findContratsClientSurPeriode_devraitTrouverContratsClientSurPeriode() {
        // Arrange
        entityManager.persist(contrat1); // 10-20 janvier
        entityManager.persist(contrat2); // 1-10 février
        entityManager.flush();
        
        // Act - Chercher contrats du client en janvier
        List<Contrat> enJanvier = contratRepository.findContratsClientSurPeriode(
            client.getId(),
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 1, 31)
        );
        
        // Assert
        assertEquals(1, enJanvier.size());
        assertEquals(LocalDate.of(2024, 1, 10), enJanvier.get(0).getDateDebut());
    }
}
