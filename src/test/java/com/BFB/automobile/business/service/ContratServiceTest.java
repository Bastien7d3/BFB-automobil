package com.BFB.automobile.business.service;

import com.BFB.automobile.business.exception.BusinessException;
import com.BFB.automobile.data.*;
import com.BFB.automobile.data.repository.ClientRepository;
import com.BFB.automobile.data.repository.ContratRepository;
import com.BFB.automobile.data.repository.VehiculeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour ContratService
 * Couvre toutes les règles métier complexes :
 * - Validation des dates (cohérence, futur, démarrage automatique)
 * - Validation du client (existant, actif)
 * - Validation du véhicule (existant, non en panne)
 * - Détection des conflits (véhicule déjà loué sur période)
 * - Transitions d'état automatiques
 */
@ExtendWith(MockitoExtension.class)
class ContratServiceTest {
    
    @Mock
    private ContratRepository contratRepository;
    
    @Mock
    private ClientRepository clientRepository;
    
    @Mock
    private VehiculeRepository vehiculeRepository;
    
    @InjectMocks
    private ContratService contratService;
    
    private Client clientActif;
    private Vehicule vehiculeDisponible;
    private Contrat contratValide;
    
    @BeforeEach
    void setUp() {
        // Client actif
        clientActif = Client.builder()
                .nom("Dupont")
                .prenom("Jean")
                .dateNaissance(LocalDate.of(1990, 5, 15))
                .numeroPermis("123456789")
                .adresse("10 rue de la Paix")
                .actif(true)
                .build();
        clientActif.setId(1L);
        
        // Véhicule disponible
        vehiculeDisponible = Vehicule.builder()
                .marque("Peugeot")
                .modele("308")
                .motorisation("Diesel")
                .couleur("Blanc")
                .immatriculation("AB-123-CD")
                .dateAcquisition(LocalDate.of(2020, 1, 15))
                .etat(EtatVehicule.DISPONIBLE)
                .build();
        vehiculeDisponible.setId(1L);
        
        // Contrat valide dans le futur
        contratValide = Contrat.builder()
                .client(clientActif)
                .vehicule(vehiculeDisponible)
                .dateDebut(LocalDate.now().plusDays(5))
                .dateFin(LocalDate.now().plusDays(10))
                .etat(EtatContrat.EN_ATTENTE)
                .build();
    }
    
    // ========== Tests de création de contrat ==========
    
    @Test
    void creerContrat_devraitReussir_avecDonneesValides() {
        // Arrange
        when(clientRepository.findById(1L)).thenReturn(Optional.of(clientActif));
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehiculeDisponible));
        when(contratRepository.findContratsConflictuels(anyLong(), any(), any()))
            .thenReturn(Arrays.asList());
        when(contratRepository.save(any(Contrat.class))).thenReturn(contratValide);
        
        // Act
        Contrat resultat = contratService.creerContrat(contratValide);
        
        // Assert
        assertNotNull(resultat);
        assertEquals(EtatContrat.EN_ATTENTE, resultat.getEtat());
        verify(contratRepository, times(1)).save(contratValide);
    }
    
    @Test
    void creerContrat_devraitDemarrerAutomatiquement_siDateDebutAujourdhui() {
        // Arrange
        contratValide.setDateDebut(LocalDate.now());
        contratValide.setDateFin(LocalDate.now().plusDays(5));
        
        when(clientRepository.findById(1L)).thenReturn(Optional.of(clientActif));
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehiculeDisponible));
        when(contratRepository.findContratsConflictuels(anyLong(), any(), any()))
            .thenReturn(Arrays.asList());
        when(contratRepository.save(any(Contrat.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(vehiculeRepository.save(any(Vehicule.class))).thenReturn(vehiculeDisponible);
        
        // Act
        Contrat resultat = contratService.creerContrat(contratValide);
        
        // Assert
        assertEquals(EtatContrat.EN_COURS, resultat.getEtat());
        verify(vehiculeRepository, times(1)).save(vehiculeDisponible);
        assertEquals(EtatVehicule.EN_LOCATION, vehiculeDisponible.getEtat());
    }
    
    @Test
    void creerContrat_devraitLeverException_siClientInexistant() {
        // Arrange
        when(clientRepository.findById(1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> contratService.creerContrat(contratValide));
        
        assertEquals("CLIENT_NON_TROUVE", exception.getCode());
        verify(contratRepository, never()).save(any());
    }
    
    @Test
    void creerContrat_devraitLeverException_siClientInactif() {
        // Arrange
        clientActif.setActif(false);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(clientActif));
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> contratService.creerContrat(contratValide));
        
        assertEquals("CLIENT_INACTIF", exception.getCode());
        verify(contratRepository, never()).save(any());
    }
    
    @Test
    void creerContrat_devraitLeverException_siVehiculeInexistant() {
        // Arrange
        when(clientRepository.findById(1L)).thenReturn(Optional.of(clientActif));
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> contratService.creerContrat(contratValide));
        
        assertEquals("VEHICULE_NON_TROUVE", exception.getCode());
        verify(contratRepository, never()).save(any());
    }
    
    @Test
    void creerContrat_devraitLeverException_siVehiculeEnPanne() {
        // Arrange
        vehiculeDisponible.setEtat(EtatVehicule.EN_PANNE);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(clientActif));
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehiculeDisponible));
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> contratService.creerContrat(contratValide));
        
        assertEquals("VEHICULE_EN_PANNE", exception.getCode());
        verify(contratRepository, never()).save(any());
    }
    
    @Test
    void creerContrat_devraitLeverException_siDateFinAvantDateDebut() {
        // Arrange
        contratValide.setDateDebut(LocalDate.now().plusDays(10));
        contratValide.setDateFin(LocalDate.now().plusDays(5));
        
        when(clientRepository.findById(1L)).thenReturn(Optional.of(clientActif));
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehiculeDisponible));
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> contratService.creerContrat(contratValide));
        
        assertEquals("DATES_INCOHERENTES", exception.getCode());
        verify(contratRepository, never()).save(any());
    }
    
    @Test
    void creerContrat_devraitLeverException_siDateDebutDansLePasse() {
        // Arrange
        contratValide.setDateDebut(LocalDate.now().minusDays(5));
        contratValide.setDateFin(LocalDate.now().plusDays(5));
        
        when(clientRepository.findById(1L)).thenReturn(Optional.of(clientActif));
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehiculeDisponible));
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> contratService.creerContrat(contratValide));
        
        assertEquals("DATE_DEBUT_PASSEE", exception.getCode());
        verify(contratRepository, never()).save(any());
    }
    
    @Test
    void creerContrat_devraitLeverException_siConflitAvecAutreContrat() {
        // Arrange
        Contrat contratExistant = new Contrat();
        contratExistant.setId(999L);
        
        when(clientRepository.findById(1L)).thenReturn(Optional.of(clientActif));
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehiculeDisponible));
        when(contratRepository.findContratsConflictuels(anyLong(), any(), any()))
            .thenReturn(Arrays.asList(contratExistant));
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> contratService.creerContrat(contratValide));
        
        assertEquals("VEHICULE_DEJA_LOUE", exception.getCode());
        verify(contratRepository, never()).save(any());
    }
    
    // ========== Tests de mise à jour ==========
    
    @Test
    void mettreAJourContrat_devraitReussir_avecDatesValidesEtSansConflit() {
        // Arrange
        Contrat contratExistant = new Contrat();
        contratExistant.setId(1L);
        contratExistant.setEtat(EtatContrat.EN_ATTENTE);
        contratExistant.setClient(clientActif);
        contratExistant.setVehicule(vehiculeDisponible);
        
        Contrat modifications = new Contrat();
        modifications.setDateDebut(LocalDate.now().plusDays(7));
        modifications.setDateFin(LocalDate.now().plusDays(12));
        
        when(contratRepository.findById(1L)).thenReturn(Optional.of(contratExistant));
        when(contratRepository.findContratsConflictuels(anyLong(), any(), any()))
            .thenReturn(Arrays.asList());
        when(contratRepository.save(any(Contrat.class))).thenReturn(contratExistant);
        
        // Act
        Contrat resultat = contratService.mettreAJourContrat(1L, modifications);
        
        // Assert
        assertNotNull(resultat);
        verify(contratRepository, times(1)).save(contratExistant);
    }
    
    @Test
    void mettreAJourContrat_devraitLeverException_siContratNonTrouve() {
        // Arrange
        when(contratRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(BusinessException.class,
            () -> contratService.mettreAJourContrat(999L, contratValide));
    }
    
    @Test
    void mettreAJourContrat_devraitLeverException_siContratPasEnAttente() {
        // Arrange
        Contrat contratEnCours = new Contrat();
        contratEnCours.setId(1L);
        contratEnCours.setEtat(EtatContrat.EN_COURS);
        
        when(contratRepository.findById(1L)).thenReturn(Optional.of(contratEnCours));
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> contratService.mettreAJourContrat(1L, contratValide));
        
        assertEquals("CONTRAT_NON_MODIFIABLE", exception.getCode());
    }
    
    // ========== Tests d'annulation ==========
    
    @Test
    void annulerContrat_devraitReussir_siContratEnAttente() {
        // Arrange
        Contrat contrat = new Contrat();
        contrat.setId(1L);
        contrat.setEtat(EtatContrat.EN_ATTENTE);
        contrat.setVehicule(vehiculeDisponible);
        
        when(contratRepository.findById(1L)).thenReturn(Optional.of(contrat));
        when(contratRepository.save(any(Contrat.class))).thenReturn(contrat);
        
        // Act
        Contrat resultat = contratService.annulerContrat(1L, "Test annulation");
        
        // Assert
        assertEquals(EtatContrat.ANNULE, resultat.getEtat());
        verify(contratRepository, times(1)).save(contrat);
    }
    
    @Test
    void annulerContrat_devraitLibererVehicule_siContratEnCours() {
        // Arrange
        vehiculeDisponible.setEtat(EtatVehicule.EN_LOCATION);
        
        Contrat contrat = new Contrat();
        contrat.setId(1L);
        contrat.setEtat(EtatContrat.EN_COURS);
        contrat.setVehicule(vehiculeDisponible);
        
        when(contratRepository.findById(1L)).thenReturn(Optional.of(contrat));
        when(contratRepository.save(any(Contrat.class))).thenReturn(contrat);
        when(vehiculeRepository.save(any(Vehicule.class))).thenReturn(vehiculeDisponible);
        
        // Act
        Contrat resultat = contratService.annulerContrat(1L, "Annulation en cours");
        
        // Assert
        assertEquals(EtatContrat.ANNULE, resultat.getEtat());
        assertEquals(EtatVehicule.DISPONIBLE, vehiculeDisponible.getEtat());
        verify(vehiculeRepository, times(1)).save(vehiculeDisponible);
    }
    
    @Test
    void annulerContrat_devraitLeverException_siContratDejAnnule() {
        // Arrange
        Contrat contrat = new Contrat();
        contrat.setId(1L);
        contrat.setEtat(EtatContrat.ANNULE);
        
        when(contratRepository.findById(1L)).thenReturn(Optional.of(contrat));
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> contratService.annulerContrat(1L, "Tentative annulation"));
        
        assertEquals("CONTRAT_NON_ANNULABLE", exception.getCode());
    }
    
    // ========== Tests de terminaison ==========
    
    @Test
    void terminerContrat_devraitReussir_siContratEnCours() {
        // Arrange
        vehiculeDisponible.setEtat(EtatVehicule.EN_LOCATION);
        
        Contrat contrat = new Contrat();
        contrat.setId(1L);
        contrat.setEtat(EtatContrat.EN_COURS);
        contrat.setVehicule(vehiculeDisponible);
        
        when(contratRepository.findById(1L)).thenReturn(Optional.of(contrat));
        when(contratRepository.save(any(Contrat.class))).thenReturn(contrat);
        when(vehiculeRepository.save(any(Vehicule.class))).thenReturn(vehiculeDisponible);
        
        // Act
        Contrat resultat = contratService.terminerContrat(1L);
        
        // Assert
        assertEquals(EtatContrat.TERMINE, resultat.getEtat());
        assertEquals(EtatVehicule.DISPONIBLE, vehiculeDisponible.getEtat());
        verify(contratRepository, times(1)).save(contrat);
        verify(vehiculeRepository, times(1)).save(vehiculeDisponible);
    }
    
    @Test
    void terminerContrat_devraitReussir_siContratEnRetard() {
        // Arrange
        vehiculeDisponible.setEtat(EtatVehicule.EN_LOCATION);
        
        Contrat contrat = new Contrat();
        contrat.setId(1L);
        contrat.setEtat(EtatContrat.EN_RETARD);
        contrat.setVehicule(vehiculeDisponible);
        
        when(contratRepository.findById(1L)).thenReturn(Optional.of(contrat));
        when(contratRepository.save(any(Contrat.class))).thenReturn(contrat);
        when(vehiculeRepository.save(any(Vehicule.class))).thenReturn(vehiculeDisponible);
        
        // Act
        Contrat resultat = contratService.terminerContrat(1L);
        
        // Assert
        assertEquals(EtatContrat.TERMINE, resultat.getEtat());
    }
    
    @Test
    void terminerContrat_devraitLeverException_siContratPasEnCours() {
        // Arrange
        Contrat contrat = new Contrat();
        contrat.setId(1L);
        contrat.setEtat(EtatContrat.EN_ATTENTE);
        
        when(contratRepository.findById(1L)).thenReturn(Optional.of(contrat));
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> contratService.terminerContrat(1L));
        
        assertEquals("CONTRAT_NON_TERMINABLE", exception.getCode());
    }
    
    // ========== Tests de récupération ==========
    
    @Test
    void obtenirTousLesContrats_devraitRetournerListeComplete() {
        // Arrange
        List<Contrat> contrats = Arrays.asList(new Contrat(), new Contrat());
        when(contratRepository.findAll()).thenReturn(contrats);
        
        // Act
        List<Contrat> resultat = contratService.obtenirTousLesContrats();
        
        // Assert
        assertEquals(2, resultat.size());
    }
    
    @Test
    void obtenirContratParId_devraitRetournerContrat_siExiste() {
        // Arrange
        Contrat contrat = new Contrat();
        contrat.setId(1L);
        when(contratRepository.findById(1L)).thenReturn(Optional.of(contrat));
        
        // Act
        Contrat resultat = contratService.obtenirContratParId(1L);
        
        // Assert
        assertNotNull(resultat);
        assertEquals(1L, resultat.getId());
    }
    
    @Test
    void obtenirContratParId_devraitLeverException_siNonTrouve() {
        // Arrange
        when(contratRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> contratService.obtenirContratParId(999L));
        
        assertEquals("CONTRAT_NON_TROUVE", exception.getCode());
    }
    
    @Test
    void obtenirContratsParClient_devraitRetournerContratsClient() {
        // Arrange
        List<Contrat> contrats = Arrays.asList(new Contrat());
        when(contratRepository.findByClientIdOrderByDateDebutDesc(1L)).thenReturn(contrats);
        
        // Act
        List<Contrat> resultat = contratService.obtenirContratsParClient(1L);
        
        // Assert
        assertEquals(1, resultat.size());
    }
    
    @Test
    void obtenirContratsParVehicule_devraitRetournerContratsVehicule() {
        // Arrange
        List<Contrat> contrats = Arrays.asList(new Contrat());
        when(contratRepository.findByVehiculeIdOrderByDateDebutDesc(1L)).thenReturn(contrats);
        
        // Act
        List<Contrat> resultat = contratService.obtenirContratsParVehicule(1L);
        
        // Assert
        assertEquals(1, resultat.size());
    }
    
    @Test
    void obtenirContratsParEtat_devraitRetournerContratsEtat() {
        // Arrange
        List<Contrat> contrats = Arrays.asList(new Contrat());
        when(contratRepository.findByEtat(EtatContrat.EN_COURS)).thenReturn(contrats);
        
        // Act
        List<Contrat> resultat = contratService.obtenirContratsParEtat(EtatContrat.EN_COURS);
        
        // Assert
        assertEquals(1, resultat.size());
    }
    
    @Test
    void obtenirContratsActifs_devraitRetournerContratsEnCoursEtEnRetard() {
        // Arrange
        Contrat contratEnCours = new Contrat();
        contratEnCours.setEtat(EtatContrat.EN_COURS);
        
        List<Contrat> contratsActifs = Arrays.asList(contratEnCours);
        when(contratRepository.findContratsActifs()).thenReturn(contratsActifs);
        
        // Act
        List<Contrat> resultat = contratService.obtenirContratsActifs();
        
        // Assert
        assertEquals(1, resultat.size());
    }
}
