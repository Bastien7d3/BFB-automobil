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
        clientActif = Client.builder()
                .nom("Dupont")
                .prenom("Jean")
                .dateNaissance(LocalDate.of(1990, 5, 15))
                .numeroPermis("123456789")
                .adresse("10 rue de la Paix")
                .actif(true)
                .build();
        clientActif.setId(1L);
        
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
        
        contratValide = Contrat.builder()
                .client(clientActif)
                .vehicule(vehiculeDisponible)
                .dateDebut(LocalDate.now().plusDays(5))
                .dateFin(LocalDate.now().plusDays(10))
                .etat(EtatContrat.EN_ATTENTE)
                .build();
    }
    
    @Test
    void creerContrat_devraitReussir_avecDonneesValides() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(clientActif));
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehiculeDisponible));
        when(contratRepository.findContratsConflictuels(anyLong(), any(), any()))
            .thenReturn(Arrays.asList());
        when(contratRepository.save(any(Contrat.class))).thenReturn(contratValide);
        
        Contrat resultat = contratService.creerContrat(contratValide);
        
        assertNotNull(resultat);
        verify(contratRepository, times(1)).save(contratValide);
    }
    
    @Test
    void creerContrat_devraitDemarrerAutomatiquement_siDateDebutAujourdhui() {
        contratValide.setDateDebut(LocalDate.now());
        contratValide.setDateFin(LocalDate.now().plusDays(5));
        
        when(clientRepository.findById(1L)).thenReturn(Optional.of(clientActif));
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehiculeDisponible));
        when(contratRepository.findContratsConflictuels(anyLong(), any(), any()))
            .thenReturn(Arrays.asList());
        when(contratRepository.save(any(Contrat.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(vehiculeRepository.save(any(Vehicule.class))).thenReturn(vehiculeDisponible);
        
        Contrat resultat = contratService.creerContrat(contratValide);
        
        assertEquals(EtatContrat.EN_COURS, resultat.getEtat());
        assertEquals(EtatVehicule.EN_LOCATION, vehiculeDisponible.getEtat());
    }
    
    @Test
    void creerContrat_devraitLeverException_siClientInexistant() {
        when(clientRepository.findById(1L)).thenReturn(Optional.empty());
        
        BusinessException exception = assertThrows(BusinessException.class,
            () -> contratService.creerContrat(contratValide));
        
        assertEquals("CLIENT_NON_TROUVE", exception.getCode());
    }
    
    @Test
    void creerContrat_devraitLeverException_siClientInactif() {
        clientActif.setActif(false);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(clientActif));
        
        BusinessException exception = assertThrows(BusinessException.class,
            () -> contratService.creerContrat(contratValide));
        
        assertEquals("CLIENT_INACTIF", exception.getCode());
    }
    
    @Test
    void creerContrat_devraitLeverException_siVehiculeInexistant() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(clientActif));
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.empty());
        
        BusinessException exception = assertThrows(BusinessException.class,
            () -> contratService.creerContrat(contratValide));
        
        assertEquals("VEHICULE_NON_TROUVE", exception.getCode());
    }
    
    @Test
    void creerContrat_devraitLeverException_siVehiculeEnPanne() {
        vehiculeDisponible.setEtat(EtatVehicule.EN_PANNE);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(clientActif));
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehiculeDisponible));
        
        BusinessException exception = assertThrows(BusinessException.class,
            () -> contratService.creerContrat(contratValide));
        
        assertEquals("VEHICULE_EN_PANNE", exception.getCode());
    }
    
    @Test
    void creerContrat_devraitLeverException_siDateFinAvantDateDebut() {
        contratValide.setDateDebut(LocalDate.now().plusDays(10));
        contratValide.setDateFin(LocalDate.now().plusDays(5));
        
        when(clientRepository.findById(1L)).thenReturn(Optional.of(clientActif));
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehiculeDisponible));
        
        BusinessException exception = assertThrows(BusinessException.class,
            () -> contratService.creerContrat(contratValide));
        
        assertEquals("DATES_INCOHERENTES", exception.getCode());
    }
    
    @Test
    void creerContrat_devraitLeverException_siDateDebutDansLePasse() {
        contratValide.setDateDebut(LocalDate.now().minusDays(5));
        contratValide.setDateFin(LocalDate.now().plusDays(5));
        
        when(clientRepository.findById(1L)).thenReturn(Optional.of(clientActif));
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehiculeDisponible));
        
        BusinessException exception = assertThrows(BusinessException.class,
            () -> contratService.creerContrat(contratValide));
        
        assertEquals("DATE_DEBUT_PASSEE", exception.getCode());
    }
    
    @Test
    void creerContrat_devraitLeverException_siConflitAvecAutreContrat() {
        Contrat contratExistant = new Contrat();
        contratExistant.setId(999L);
        
        when(clientRepository.findById(1L)).thenReturn(Optional.of(clientActif));
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehiculeDisponible));
        when(contratRepository.findContratsConflictuels(anyLong(), any(), any()))
            .thenReturn(Arrays.asList(contratExistant));
        
        BusinessException exception = assertThrows(BusinessException.class,
            () -> contratService.creerContrat(contratValide));
        
        assertEquals("VEHICULE_DEJA_LOUE", exception.getCode());
    }
    
    @Test
    void annulerContrat_devraitReussir_siContratEnAttente() {
        Contrat contrat = new Contrat();
        contrat.setId(1L);
        contrat.setEtat(EtatContrat.EN_ATTENTE);
        
        Vehicule vehicule = new Vehicule();
        vehicule.setEtat(EtatVehicule.DISPONIBLE);
        contrat.setVehicule(vehicule);
        
        when(contratRepository.findById(1L)).thenReturn(Optional.of(contrat));
        when(contratRepository.save(any(Contrat.class))).thenReturn(contrat);
        
        contratService.annulerContrat(1L, "Annulation test");
        
        assertEquals(EtatContrat.ANNULE, contrat.getEtat());
        verify(contratRepository, times(1)).save(contrat);
    }
    
    @Test
    void terminerContrat_devraitReussir_siContratEnCours() {
        Contrat contrat = new Contrat();
        contrat.setId(1L);
        contrat.setEtat(EtatContrat.EN_COURS);
        contrat.setVehicule(vehiculeDisponible);
        vehiculeDisponible.setEtat(EtatVehicule.EN_LOCATION);
        
        when(contratRepository.findById(1L)).thenReturn(Optional.of(contrat));
        when(contratRepository.save(any(Contrat.class))).thenReturn(contrat);
        when(vehiculeRepository.save(any(Vehicule.class))).thenReturn(vehiculeDisponible);
        
        contratService.terminerContrat(1L);
        
        assertEquals(EtatContrat.TERMINE, contrat.getEtat());
        assertEquals(EtatVehicule.DISPONIBLE, vehiculeDisponible.getEtat());
    }
    
}