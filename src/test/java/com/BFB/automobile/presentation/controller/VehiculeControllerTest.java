package com.BFB.automobile.presentation.controller;

import com.BFB.automobile.business.exception.BusinessException;
import com.BFB.automobile.business.service.VehiculeService;
import com.BFB.automobile.data.EtatVehicule;
import com.BFB.automobile.data.Vehicule;
import com.BFB.automobile.presentation.dto.VehiculeDTO;
import com.BFB.automobile.presentation.mapper.VehiculeMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests du contrôleur VehiculeController
 */
@WebMvcTest(VehiculeController.class)
class VehiculeControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private VehiculeService vehiculeService;
    
    @MockBean
    private VehiculeMapper vehiculeMapper;
    
    private Vehicule vehicule;
    private VehiculeDTO vehiculeDTO;
    
    @BeforeEach
    void setUp() {
        vehicule = new Vehicule();
        vehicule.setId(1L);
        vehicule.setMarque("Peugeot");
        vehicule.setModele("308");
        vehicule.setMotorisation("Diesel");
        vehicule.setCouleur("Blanc");
        vehicule.setImmatriculation("AB-123-CD");
        vehicule.setDateAcquisition(LocalDate.of(2020, 1, 15));
        vehicule.setEtat(EtatVehicule.DISPONIBLE);
        
        vehiculeDTO = new VehiculeDTO();
        vehiculeDTO.setId(1L);
        vehiculeDTO.setMarque("Peugeot");
        vehiculeDTO.setModele("308");
        vehiculeDTO.setMotorisation("Diesel");
        vehiculeDTO.setCouleur("Blanc");
        vehiculeDTO.setImmatriculation("AB-123-CD");
        vehiculeDTO.setDateAcquisition(LocalDate.of(2020, 1, 15));
        vehiculeDTO.setEtat(EtatVehicule.DISPONIBLE);
    }
    
    // ========== Tests GET /api/vehicules ==========
    
    @Test
    void obtenirTousLesVehicules_devraitRetourner200AvecListeVehicules() throws Exception {
        // Arrange
        List<Vehicule> vehicules = Arrays.asList(vehicule);
        when(vehiculeService.obtenirTousLesVehicules()).thenReturn(vehicules);
        when(vehiculeMapper.toDTO(any(Vehicule.class))).thenReturn(vehiculeDTO);
        
        // Act & Assert
        mockMvc.perform(get("/api/vehicules"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].marque").value("Peugeot"))
            .andExpect(jsonPath("$[0].modele").value("308"));
        
        verify(vehiculeService, times(1)).obtenirTousLesVehicules();
    }
    
    @Test
    void obtenirVehiculesDisponibles_devraitRetourner200AvecSeulementDisponibles() throws Exception {
        // Arrange
        List<Vehicule> vehicules = Arrays.asList(vehicule);
        when(vehiculeService.obtenirVehiculesParEtat(EtatVehicule.DISPONIBLE)).thenReturn(vehicules);
        when(vehiculeMapper.toDTO(vehicule)).thenReturn(vehiculeDTO);
        
        // Act & Assert
        mockMvc.perform(get("/api/vehicules?etat=DISPONIBLE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].etat").value("DISPONIBLE"));
        
        verify(vehiculeService, times(1)).obtenirVehiculesParEtat(EtatVehicule.DISPONIBLE);
        verify(vehiculeService, never()).obtenirTousLesVehicules();
    }
    
    // ========== Tests GET /api/vehicules/{id} ==========
    
    @Test
    void obtenirVehiculeParId_devraitRetourner200AvecVehicule() throws Exception {
        // Arrange
        when(vehiculeService.obtenirVehiculeParId(1L)).thenReturn(vehicule);
        when(vehiculeMapper.toDTO(vehicule)).thenReturn(vehiculeDTO);
        
        // Act & Assert
        mockMvc.perform(get("/api/vehicules/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.marque").value("Peugeot"));
        
        verify(vehiculeService, times(1)).obtenirVehiculeParId(1L);
    }
    
    @Test
    void obtenirVehiculeParId_devraitRetourner404SiVehiculeNonTrouve() throws Exception {
        // Arrange
        when(vehiculeService.obtenirVehiculeParId(999L))
            .thenThrow(new BusinessException("VEHICULE_NON_TROUVE", "Véhicule non trouvé"));
        
        // Act & Assert
        mockMvc.perform(get("/api/vehicules/999"))
            .andExpect(status().isBadRequest());
    }
    
    // ========== Tests POST /api/vehicules ==========
    
    @Test
    void creerVehicule_devraitRetourner201AvecVehicule() throws Exception {
        // Arrange
        when(vehiculeMapper.toEntity(any(VehiculeDTO.class))).thenReturn(vehicule);
        when(vehiculeService.creerVehicule(any(Vehicule.class))).thenReturn(vehicule);
        when(vehiculeMapper.toDTO(any(Vehicule.class))).thenReturn(vehiculeDTO);
        
        // Act & Assert
        mockMvc.perform(post("/api/vehicules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vehiculeDTO)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.marque").value("Peugeot"))
            .andExpect(jsonPath("$.immatriculation").value("AB-123-CD"));
        
        verify(vehiculeService, times(1)).creerVehicule(any(Vehicule.class));
    }
    
    @Test
    void creerVehicule_devraitRetourner400SiImmatriculationExiste() throws Exception {
        // Arrange
        when(vehiculeMapper.toEntity(any(VehiculeDTO.class))).thenReturn(vehicule);
        when(vehiculeService.creerVehicule(any(Vehicule.class)))
            .thenThrow(new BusinessException("IMMATRICULATION_EXISTE", "Immatriculation existe déjà"));
        
        // Act & Assert
        mockMvc.perform(post("/api/vehicules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vehiculeDTO)))
            .andExpect(status().isBadRequest());
    }
    
    // ========== Tests PUT /api/vehicules/{id} ==========
    
    @Test
    void mettreAJourVehicule_devraitRetourner200AvecVehiculeModifie() throws Exception {
        // Arrange
        when(vehiculeMapper.toEntity(any(VehiculeDTO.class))).thenReturn(vehicule);
        when(vehiculeService.mettreAJourVehicule(anyLong(), any(Vehicule.class))).thenReturn(vehicule);
        when(vehiculeMapper.toDTO(any(Vehicule.class))).thenReturn(vehiculeDTO);
        
        // Act & Assert
        mockMvc.perform(put("/api/vehicules/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vehiculeDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.marque").value("Peugeot"));
        
        verify(vehiculeService, times(1)).mettreAJourVehicule(eq(1L), any(Vehicule.class));
    }
    
    // ========== Tests PATCH /api/vehicules/{id}/etat ==========
    
    @Test
    void changerEtatVehicule_devraitRetourner200AvecNouvelEtat() throws Exception {
        // Arrange
        vehicule.setEtat(EtatVehicule.EN_PANNE);
        vehiculeDTO.setEtat(EtatVehicule.EN_PANNE);
        
        when(vehiculeService.changerEtatVehicule(1L, EtatVehicule.EN_PANNE)).thenReturn(vehicule);
        when(vehiculeMapper.toDTO(vehicule)).thenReturn(vehiculeDTO);
        
        // Act & Assert
        mockMvc.perform(patch("/api/vehicules/1/etat")
                .param("etat", "EN_PANNE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.etat").value("EN_PANNE"));
        
        verify(vehiculeService, times(1)).changerEtatVehicule(1L, EtatVehicule.EN_PANNE);
    }
    
    // ========== Tests DELETE /api/vehicules/{id} ==========
    
    @Test
    void supprimerVehicule_devraitRetourner204() throws Exception {
        // Arrange
        doNothing().when(vehiculeService).supprimerVehicule(1L);
        
        // Act & Assert
        mockMvc.perform(delete("/api/vehicules/1"))
            .andExpect(status().isNoContent());
        
        verify(vehiculeService, times(1)).supprimerVehicule(1L);
    }
    
    @Test
    void supprimerVehicule_devraitRetourner400SiVehiculeEnLocation() throws Exception {
        // Arrange
        doThrow(new BusinessException("VEHICULE_EN_LOCATION", "Véhicule en location"))
            .when(vehiculeService).supprimerVehicule(1L);
        
        // Act & Assert
        mockMvc.perform(delete("/api/vehicules/1"))
            .andExpect(status().isBadRequest());
    }
    
}
