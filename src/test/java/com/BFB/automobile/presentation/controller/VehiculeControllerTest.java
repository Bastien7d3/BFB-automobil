package com.BFB.automobile.presentation.controller;

import com.BFB.automobile.business.exception.BusinessException;
import com.BFB.automobile.business.service.VehiculeService;
import com.BFB.automobile.data.EtatVehicule;
import com.BFB.automobile.data.Vehicule;
import com.BFB.automobile.presentation.dto.VehiculeDTO;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VehiculeController.class)
class VehiculeControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private VehiculeService vehiculeService;
    
    @MockBean
    private com.BFB.automobile.presentation.mapper.VehiculeMapper vehiculeMapper;
    
    private Vehicule vehicule;
    private VehiculeDTO vehiculeDTO;
    
    @BeforeEach
    void setUp() {
        vehicule = Vehicule.builder()
                .marque("Peugeot")
                .modele("308")
                .motorisation("Diesel")
                .couleur("Blanc")
                .immatriculation("AA-123-BB")
                .dateAcquisition(LocalDate.of(2020, 1, 15))
                .etat(EtatVehicule.DISPONIBLE)
                .build();
        vehicule.setId(1L);
        
        vehiculeDTO = new VehiculeDTO();
        vehiculeDTO.setMarque("Peugeot");
        vehiculeDTO.setModele("308");
        vehiculeDTO.setMotorisation("Diesel");
        vehiculeDTO.setCouleur("Blanc");
        vehiculeDTO.setImmatriculation("AA-123-BB");
        vehiculeDTO.setDateAcquisition(LocalDate.of(2020, 1, 15));
    }
    
    @Test
    void creerVehicule_devraitRetourner201() throws Exception {
        when(vehiculeService.creerVehicule(any(Vehicule.class))).thenReturn(vehicule);
        
        mockMvc.perform(post("/api/vehicules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vehiculeDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }
    
    @Test
    void creerVehicule_devraitRetourner400_siImmatriculationExiste() throws Exception {
        when(vehiculeService.creerVehicule(any(Vehicule.class)))
                .thenThrow(new BusinessException("IMMATRICULATION_EXISTANTE", "Immatriculation existe"));
        
        mockMvc.perform(post("/api/vehicules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vehiculeDTO)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void listerVehicules_devraitRetournerListe() throws Exception {
        when(vehiculeService.rechercherVehicules(null, null)).thenReturn(Arrays.asList(vehicule));
        
        mockMvc.perform(get("/api/vehicules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }
    
    @Test
    void obtenirVehicule_devraitRetourner200() throws Exception {
        when(vehiculeService.obtenirVehiculeParId(1L)).thenReturn(vehicule);
        
        mockMvc.perform(get("/api/vehicules/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
    
    @Test
    void mettreAJourVehicule_devraitRetourner200() throws Exception {
        when(vehiculeService.mettreAJourVehicule(anyLong(), any(Vehicule.class))).thenReturn(vehicule);
        
        mockMvc.perform(put("/api/vehicules/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vehiculeDTO)))
                .andExpect(status().isOk());
    }
    
    @Test
    void changerEtat_devraitRetourner204() throws Exception {
        mockMvc.perform(patch("/api/vehicules/1/etat")
                .param("etat", "EN_PANNE"))
                .andExpect(status().isNoContent());
    }
}
