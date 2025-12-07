package com.BFB.automobile.business.service;

import com.BFB.automobile.business.exception.BusinessException;
import com.BFB.automobile.data.EtatVehicule;
import com.BFB.automobile.data.Vehicule;
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

@ExtendWith(MockitoExtension.class)
class VehiculeServiceTest {
    
    @Mock
    private VehiculeRepository vehiculeRepository;
    
    @InjectMocks
    private VehiculeService vehiculeService;
    
    private Vehicule vehiculeValide;
    
    @BeforeEach
    void setUp() {
        vehiculeValide = Vehicule.builder()
                .marque("Peugeot")
                .modele("308")
                .motorisation("Diesel")
                .couleur("Blanc")
                .immatriculation("AA-123-BB")
                .dateAcquisition(LocalDate.of(2020, 1, 15))
                .etat(EtatVehicule.DISPONIBLE)
                .build();
    }
    
    @Test
    void creerVehicule_devraitReussir_avecDonneesValides() {
        when(vehiculeRepository.existsByImmatriculation(anyString())).thenReturn(false);
        when(vehiculeRepository.save(any(Vehicule.class))).thenReturn(vehiculeValide);
        
        Vehicule resultat = vehiculeService.creerVehicule(vehiculeValide);
        
        assertNotNull(resultat);
        verify(vehiculeRepository, times(1)).save(vehiculeValide);
    }
    
    @Test
    void creerVehicule_devraitLeverException_siImmatriculationExiste() {
        when(vehiculeRepository.existsByImmatriculation(vehiculeValide.getImmatriculation()))
            .thenReturn(true);
        
        BusinessException exception = assertThrows(BusinessException.class,
            () -> vehiculeService.creerVehicule(vehiculeValide));
        
        assertEquals("IMMATRICULATION_EXISTE", exception.getCode());
    }
    
    @Test
    void mettreAJourVehicule_devraitReussir_avecDonneesValides() {
        Vehicule vehiculeExistant = new Vehicule();
        vehiculeExistant.setId(1L);
        vehiculeExistant.setImmatriculation("AA-123-BB");
        
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehiculeExistant));
        when(vehiculeRepository.save(any(Vehicule.class))).thenReturn(vehiculeExistant);
        
        Vehicule modifications = new Vehicule();
        modifications.setCouleur("Rouge");
        
        Vehicule resultat = vehiculeService.mettreAJourVehicule(1L, modifications);
        
        assertNotNull(resultat);
        verify(vehiculeRepository, times(1)).save(vehiculeExistant);
    }
    
    @Test
    void changerEtatVehicule_devraitModifierEtat() {
        Vehicule vehicule = new Vehicule();
        vehicule.setId(1L);
        vehicule.setEtat(EtatVehicule.DISPONIBLE);
        
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule));
        when(vehiculeRepository.save(any(Vehicule.class))).thenReturn(vehicule);
        
        vehiculeService.changerEtatVehicule(1L, EtatVehicule.EN_LOCATION);
        
        assertEquals(EtatVehicule.EN_LOCATION, vehicule.getEtat());
        verify(vehiculeRepository, times(1)).save(vehicule);
    }
    
    @Test
    void rechercherVehicules_devraitRetournerResultatsRecherche() {
        when(vehiculeRepository.searchByMarqueAndModele("Peugeot", "308"))
            .thenReturn(Arrays.asList(vehiculeValide));
        
        List<Vehicule> results = vehiculeService.rechercherVehicules("Peugeot", "308");
        
        assertEquals(1, results.size());
        verify(vehiculeRepository, times(1)).searchByMarqueAndModele("Peugeot", "308");
    }
}
