package com.BFB.automobile.business.service;

import com.BFB.automobile.business.exception.BusinessException;
import com.BFB.automobile.data.Contrat;
import com.BFB.automobile.data.EtatContrat;
import com.BFB.automobile.data.EtatVehicule;
import com.BFB.automobile.data.Vehicule;
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
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour VehiculeService
 * Couvre toutes les règles métier :
 * - Unicité de l'immatriculation
 * - Gestion des états des véhicules
 * - Annulation automatique des contrats en attente si véhicule en panne
 */
@ExtendWith(MockitoExtension.class)
class VehiculeServiceTest {
    
    @Mock
    private VehiculeRepository vehiculeRepository;
    
    @Mock
    private ContratRepository contratRepository;
    
    @InjectMocks
    private VehiculeService vehiculeService;
    
    private Vehicule vehiculeValide;
    
    @BeforeEach
    void setUp() {
        vehiculeValide = new Vehicule();
        vehiculeValide.setMarque("Peugeot");
        vehiculeValide.setModele("308");
        vehiculeValide.setMotorisation("Diesel");
        vehiculeValide.setCouleur("Blanc");
        vehiculeValide.setImmatriculation("AB-123-CD");
        vehiculeValide.setDateAcquisition(LocalDate.of(2020, 1, 15));
        vehiculeValide.setEtat(EtatVehicule.DISPONIBLE);
    }
    
    // ========== Tests de création de véhicule ==========
    
    @Test
    void creerVehicule_devraitReussir_avecDonneesValides() {
        // Arrange
        when(vehiculeRepository.existsByImmatriculation(anyString())).thenReturn(false);
        when(vehiculeRepository.save(any(Vehicule.class))).thenReturn(vehiculeValide);
        
        // Act
        Vehicule resultat = vehiculeService.creerVehicule(vehiculeValide);
        
        // Assert
        assertNotNull(resultat);
        assertEquals("AB-123-CD", resultat.getImmatriculation());
        verify(vehiculeRepository, times(1)).save(vehiculeValide);
    }
    
    @Test
    void creerVehicule_devraitLeverException_siImmatriculationExiste() {
        // Arrange
        when(vehiculeRepository.existsByImmatriculation(vehiculeValide.getImmatriculation()))
            .thenReturn(true);
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> vehiculeService.creerVehicule(vehiculeValide));
        
        assertEquals("IMMATRICULATION_EXISTE", exception.getCode());
        verify(vehiculeRepository, never()).save(any());
    }
    
    // ========== Tests de mise à jour ==========
    
    @Test
    void mettreAJourVehicule_devraitReussir_avecDonneesValides() {
        // Arrange
        Vehicule vehiculeExistant = new Vehicule();
        vehiculeExistant.setId(1L);
        vehiculeExistant.setImmatriculation("AB-123-CD");
        
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehiculeExistant));
        when(vehiculeRepository.save(any(Vehicule.class))).thenReturn(vehiculeExistant);
        
        Vehicule modifications = new Vehicule();
        modifications.setMarque("Renault");
        modifications.setModele("Clio");
        modifications.setMotorisation("Essence");
        modifications.setCouleur("Rouge");
        modifications.setImmatriculation("AB-123-CD"); // Même immatriculation
        modifications.setDateAcquisition(LocalDate.of(2021, 6, 1));
        
        // Act
        Vehicule resultat = vehiculeService.mettreAJourVehicule(1L, modifications);
        
        // Assert
        assertNotNull(resultat);
        assertEquals("Renault", resultat.getMarque());
        verify(vehiculeRepository, times(1)).save(vehiculeExistant);
    }
    
    @Test
    void mettreAJourVehicule_devraitLeverException_siVehiculeNonTrouve() {
        // Arrange
        when(vehiculeRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(BusinessException.class,
            () -> vehiculeService.mettreAJourVehicule(999L, vehiculeValide));
        
        verify(vehiculeRepository, never()).save(any());
    }
    
    @Test
    void mettreAJourVehicule_devraitLeverException_siNouvelleImmatriculationExiste() {
        // Arrange
        Vehicule vehiculeExistant = new Vehicule();
        vehiculeExistant.setId(1L);
        vehiculeExistant.setImmatriculation("AB-123-CD");
        
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehiculeExistant));
        when(vehiculeRepository.existsByImmatriculation("XY-789-ZZ")).thenReturn(true);
        
        Vehicule modifications = new Vehicule();
        modifications.setImmatriculation("XY-789-ZZ"); // Nouvelle immatriculation déjà utilisée
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> vehiculeService.mettreAJourVehicule(1L, modifications));
        
        assertEquals("IMMATRICULATION_EXISTE", exception.getCode());
        verify(vehiculeRepository, never()).save(any());
    }
    
    // ========== Tests de changement d'état ==========
    
    @Test
    void changerEtatVehicule_devraitReussir_versENLOCATION() {
        // Arrange
        Vehicule vehicule = new Vehicule();
        vehicule.setId(1L);
        vehicule.setEtat(EtatVehicule.DISPONIBLE);
        
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule));
        when(vehiculeRepository.save(any(Vehicule.class))).thenReturn(vehicule);
        
        // Act
        Vehicule resultat = vehiculeService.changerEtatVehicule(1L, EtatVehicule.EN_LOCATION);
        
        // Assert
        assertEquals(EtatVehicule.EN_LOCATION, resultat.getEtat());
        verify(vehiculeRepository, times(1)).save(vehicule);
    }
    
    @Test
    void changerEtatVehicule_devraitAnnulerContratsEnAttente_siPassageEnPanne() {
        // Arrange
        Vehicule vehicule = new Vehicule();
        vehicule.setId(1L);
        vehicule.setEtat(EtatVehicule.DISPONIBLE);
        
        Contrat contrat1 = new Contrat();
        contrat1.setId(10L);
        contrat1.setEtat(EtatContrat.EN_ATTENTE);
        
        Contrat contrat2 = new Contrat();
        contrat2.setId(11L);
        contrat2.setEtat(EtatContrat.EN_ATTENTE);
        
        List<Contrat> contratsEnAttente = Arrays.asList(contrat1, contrat2);
        
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule));
        when(vehiculeRepository.save(any(Vehicule.class))).thenReturn(vehicule);
        when(contratRepository.findContratsEnAttenteByVehicule(1L)).thenReturn(contratsEnAttente);
        when(contratRepository.save(any(Contrat.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Vehicule resultat = vehiculeService.changerEtatVehicule(1L, EtatVehicule.EN_PANNE);
        
        // Assert
        assertEquals(EtatVehicule.EN_PANNE, resultat.getEtat());
        verify(contratRepository, times(2)).save(any(Contrat.class));
        assertEquals(EtatContrat.ANNULE, contrat1.getEtat());
        assertEquals(EtatContrat.ANNULE, contrat2.getEtat());
        assertTrue(contrat1.getCommentaire().contains("en panne"));
    }
    
    @Test
    void changerEtatVehicule_devraitLeverException_siVehiculeNonTrouve() {
        // Arrange
        when(vehiculeRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(BusinessException.class,
            () -> vehiculeService.changerEtatVehicule(999L, EtatVehicule.EN_PANNE));
    }
    
    // ========== Tests de récupération ==========
    
    @Test
    void obtenirTousLesVehicules_devraitRetournerListeComplete() {
        // Arrange
        List<Vehicule> vehicules = Arrays.asList(new Vehicule(), new Vehicule());
        when(vehiculeRepository.findAll()).thenReturn(vehicules);
        
        // Act
        List<Vehicule> resultat = vehiculeService.obtenirTousLesVehicules();
        
        // Assert
        assertEquals(2, resultat.size());
        verify(vehiculeRepository, times(1)).findAll();
    }
    
    @Test
    void obtenirVehiculesDisponibles_devraitRetournerSeulementDisponibles() {
        // Arrange
        Vehicule vehicule1 = new Vehicule();
        vehicule1.setEtat(EtatVehicule.DISPONIBLE);
        vehicule1.setMarque("Peugeot");
        
        List<Vehicule> vehiculesDisponibles = Arrays.asList(vehicule1);
        when(vehiculeRepository.findByEtatOrderByMarqueAscModeleAsc(EtatVehicule.DISPONIBLE))
            .thenReturn(vehiculesDisponibles);
        
        // Act
        List<Vehicule> resultat = vehiculeService.obtenirVehiculesDisponibles();
        
        // Assert
        assertEquals(1, resultat.size());
        assertEquals(EtatVehicule.DISPONIBLE, resultat.get(0).getEtat());
    }
    
    @Test
    void obtenirVehiculeParId_devraitRetournerVehicule_siExiste() {
        // Arrange
        Vehicule vehicule = new Vehicule();
        vehicule.setId(1L);
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule));
        
        // Act
        Vehicule resultat = vehiculeService.obtenirVehiculeParId(1L);
        
        // Assert
        assertNotNull(resultat);
        assertEquals(1L, resultat.getId());
    }
    
    @Test
    void obtenirVehiculeParId_devraitLeverException_siVehiculeNonTrouve() {
        // Arrange
        when(vehiculeRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> vehiculeService.obtenirVehiculeParId(999L));
        
        assertEquals("VEHICULE_NON_TROUVE", exception.getCode());
    }
    
    // ========== Tests de recherche ==========
    
    @Test
    void rechercherVehicules_devraitRechercher_parMarqueEtModele() {
        // Arrange
        List<Vehicule> vehicules = Arrays.asList(new Vehicule());
        when(vehiculeRepository.searchByMarqueAndModele("Peugeot", "308")).thenReturn(vehicules);
        
        // Act
        List<Vehicule> resultat = vehiculeService.rechercherVehicules("Peugeot", "308");
        
        // Assert
        assertEquals(1, resultat.size());
        verify(vehiculeRepository, times(1)).searchByMarqueAndModele("Peugeot", "308");
    }
    
    @Test
    void rechercherVehicules_devraitRechercher_parMarqueSeule() {
        // Arrange
        List<Vehicule> vehicules = Arrays.asList(new Vehicule());
        when(vehiculeRepository.findByMarqueContainingIgnoreCase("Peugeot")).thenReturn(vehicules);
        
        // Act
        List<Vehicule> resultat = vehiculeService.rechercherVehicules("Peugeot", null);
        
        // Assert
        assertEquals(1, resultat.size());
        verify(vehiculeRepository, times(1)).findByMarqueContainingIgnoreCase("Peugeot");
    }
    
    @Test
    void rechercherVehicules_devraitRetournerTous_siAucunCritere() {
        // Arrange
        List<Vehicule> vehicules = Arrays.asList(new Vehicule(), new Vehicule());
        when(vehiculeRepository.findAll()).thenReturn(vehicules);
        
        // Act
        List<Vehicule> resultat = vehiculeService.rechercherVehicules(null, null);
        
        // Assert
        assertEquals(2, resultat.size());
        verify(vehiculeRepository, times(1)).findAll();
    }
    
    // ========== Tests de suppression ==========
    
    @Test
    void supprimerVehicule_devraitReussir_siAucunContratActif() {
        // Arrange
        Vehicule vehicule = new Vehicule();
        vehicule.setId(1L);
        
        Contrat contratTermine = new Contrat();
        contratTermine.setEtat(EtatContrat.TERMINE);
        
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule));
        when(contratRepository.findByVehicule(vehicule)).thenReturn(Arrays.asList(contratTermine));
        
        // Act
        vehiculeService.supprimerVehicule(1L);
        
        // Assert
        verify(vehiculeRepository, times(1)).delete(vehicule);
    }
    
    @Test
    void supprimerVehicule_devraitLeverException_siContratActif() {
        // Arrange
        Vehicule vehicule = new Vehicule();
        vehicule.setId(1L);
        
        Contrat contratActif = new Contrat();
        contratActif.setEtat(EtatContrat.EN_COURS);
        
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule));
        when(contratRepository.findByVehicule(vehicule)).thenReturn(Arrays.asList(contratActif));
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> vehiculeService.supprimerVehicule(1L));
        
        assertEquals("VEHICULE_EN_LOCATION", exception.getCode());
        verify(vehiculeRepository, never()).delete(any());
    }
}
