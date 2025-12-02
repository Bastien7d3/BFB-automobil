package com.BFB.automobile.presentation.controller;

import com.BFB.automobile.business.exception.BusinessException;
import com.BFB.automobile.business.service.VehiculeService;
import com.BFB.automobile.data.EtatVehicule;
import com.BFB.automobile.data.Vehicule;
import com.BFB.automobile.presentation.dto.VehiculeDTO;
import com.BFB.automobile.presentation.mapper.VehiculeMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour VehiculeController
 * 
 * JUSTIFICATION COUCHE PRESENTATION :
 * - Teste les endpoints spécifiques aux véhicules
 * - Valide la gestion des états de véhicules via API
 * - Vérifie les filtres (véhicules disponibles)
 */
@WebMvcTest(VehiculeController.class)
@DisplayName("VehiculeController - Tests d'intégration")
class VehiculeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VehiculeService vehiculeService;

    @MockBean
    private VehiculeMapper vehiculeMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private Vehicule vehicule;
    private VehiculeDTO vehiculeDTO;

    @BeforeEach
    void setUp() {
        vehicule = new Vehicule();
        vehicule.setId(1L);
        vehicule.setImmatriculation("AB-123-CD");
        vehicule.setMarque("Peugeot");
        vehicule.setModele("308");
        vehicule.setAnnee(2023);
        vehicule.setPrixJournalier(new BigDecimal("50.00"));
        vehicule.setEtat(EtatVehicule.DISPONIBLE);

        vehiculeDTO = new VehiculeDTO();
        vehiculeDTO.setId(1L);
        vehiculeDTO.setImmatriculation("AB-123-CD");
        vehiculeDTO.setMarque("Peugeot");
        vehiculeDTO.setModele("308");
        vehiculeDTO.setAnnee(2023);
        vehiculeDTO.setPrixJournalier(new BigDecimal("50.00"));
        vehiculeDTO.setEtat(EtatVehicule.DISPONIBLE);
    }

    @Test
    @DisplayName("GET /api/vehicules - Devrait retourner la liste des véhicules")
    void getAllVehicules_ShouldReturnVehiculesList() throws Exception {
        // Given
        List<Vehicule> vehicules = Arrays.asList(vehicule);
        when(vehiculeService.getAllVehicules()).thenReturn(vehicules);
        when(vehiculeMapper.toDTO(vehicule)).thenReturn(vehiculeDTO);

        // When & Then
        mockMvc.perform(get("/api/vehicules"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].immatriculation").value("AB-123-CD"))
                .andExpect(jsonPath("$[0].marque").value("Peugeot"));

        verify(vehiculeService).getAllVehicules();
    }

    @Test
    @DisplayName("GET /api/vehicules/disponibles - Devrait retourner les véhicules disponibles")
    void getVehiculesDisponibles_ShouldReturnAvailableVehicules() throws Exception {
        // Given
        List<Vehicule> vehiculesDisponibles = Arrays.asList(vehicule);
        when(vehiculeService.getVehiculesDisponibles()).thenReturn(vehiculesDisponibles);
        when(vehiculeMapper.toDTO(vehicule)).thenReturn(vehiculeDTO);

        // When & Then
        mockMvc.perform(get("/api/vehicules/disponibles"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].etat").value("DISPONIBLE"));

        verify(vehiculeService).getVehiculesDisponibles();
    }

    @Test
    @DisplayName("POST /api/vehicules - Devrait créer un nouveau véhicule")
    void createVehicule_ValidData_ShouldCreateVehicule() throws Exception {
        // Given
        when(vehiculeMapper.toEntity(any(VehiculeDTO.class))).thenReturn(vehicule);
        when(vehiculeService.createVehicule(any(Vehicule.class))).thenReturn(vehicule);
        when(vehiculeMapper.toDTO(vehicule)).thenReturn(vehiculeDTO);

        // When & Then
        mockMvc.perform(post("/api/vehicules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vehiculeDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.immatriculation").value("AB-123-CD"));

        verify(vehiculeService).createVehicule(any(Vehicule.class));
    }

    @Test
    @DisplayName("PUT /api/vehicules/{id}/etat - Devrait changer l'état d'un véhicule")
    void changerEtatVehicule_ValidEtat_ShouldUpdateEtat() throws Exception {
        // Given
        doNothing().when(vehiculeService).changerEtatVehicule(1L, EtatVehicule.PANNE);

        // When & Then
        mockMvc.perform(patch("/api/vehicules/1/etat")
                        .param("nouvelEtat", "PANNE"))
                .andExpect(status().isOk());

        verify(vehiculeService).changerEtatVehicule(1L, EtatVehicule.PANNE);
    }

    @Test
    @DisplayName("DELETE /api/vehicules/{id} - Devrait supprimer un véhicule")
    void deleteVehicule_ExistingVehicule_ShouldDeleteVehicule() throws Exception {
        // Given
        doNothing().when(vehiculeService).deleteVehicule(1L);

        // When & Then
        mockMvc.perform(delete("/api/vehicules/1"))
                .andExpect(status().isNoContent());

        verify(vehiculeService).deleteVehicule(1L);
    }

    @Test
    @DisplayName("GET /api/vehicules/{id} - Devrait retourner 404 pour un véhicule inexistant")
    void getVehiculeById_NonExistingVehicule_ShouldReturn404() throws Exception {
        // Given
        when(vehiculeService.getVehiculeById(99L))
                .thenThrow(new BusinessException("Véhicule non trouvé avec l'ID : 99"));

        // When & Then
        mockMvc.perform(get("/api/vehicules/99"))
                .andExpect(status().isNotFound());

        verify(vehiculeService).getVehiculeById(99L);
    }
}