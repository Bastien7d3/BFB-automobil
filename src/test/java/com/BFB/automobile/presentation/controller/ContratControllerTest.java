package com.BFB.automobile.presentation.controller;

import com.BFB.automobile.business.exception.BusinessException;
import com.BFB.automobile.business.service.ContratService;
import com.BFB.automobile.data.*;
import com.BFB.automobile.presentation.dto.ContratDTO;
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

@WebMvcTest(ContratController.class)
class ContratControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private ContratService contratService;
    
    @MockBean
    private com.BFB.automobile.presentation.mapper.ContratMapper contratMapper;
    
    private Contrat contrat;
    private ContratDTO contratDTO;
    
    @BeforeEach
    void setUp() {
        Client client = new Client();
        client.setId(1L);
        
        Vehicule vehicule = new Vehicule();
        vehicule.setId(1L);
        
        contrat = Contrat.builder()
                .client(client)
                .vehicule(vehicule)
                .dateDebut(LocalDate.now().plusDays(5))
                .dateFin(LocalDate.now().plusDays(10))
                .etat(EtatContrat.EN_ATTENTE)
                .build();
        contrat.setId(1L);
        
        contratDTO = new ContratDTO();
        contratDTO.setClientId(1L);
        contratDTO.setVehiculeId(1L);
        contratDTO.setDateDebut(LocalDate.now().plusDays(5));
        contratDTO.setDateFin(LocalDate.now().plusDays(10));
    }
    
    @Test
    void creerContrat_devraitRetourner201() throws Exception {
        when(contratService.creerContrat(any(Contrat.class))).thenReturn(contrat);
        
        mockMvc.perform(post("/api/contrats")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contratDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }
    
    @Test
    void creerContrat_devraitRetourner400_siConflitDates() throws Exception {
        when(contratService.creerContrat(any(Contrat.class)))
                .thenThrow(new BusinessException("VEHICULE_DEJA_LOUE", "Conflit"));
        
        mockMvc.perform(post("/api/contrats")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contratDTO)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void listerContrats_devraitRetournerListe() throws Exception {
        when(contratService.obtenirTousLesContrats()).thenReturn(Arrays.asList(contrat));
        
        mockMvc.perform(get("/api/contrats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }
    
    @Test
    void obtenirContrat_devraitRetourner200() throws Exception {
        when(contratService.obtenirContratParId(1L)).thenReturn(contrat);
        
        mockMvc.perform(get("/api/contrats/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
    
    @Test
    void demarrerContrat_devraitRetourner204() throws Exception {
        mockMvc.perform(patch("/api/contrats/1/demarrer"))
                .andExpect(status().isNoContent());
    }
    
    @Test
    void annulerContrat_devraitRetourner204() throws Exception {
        mockMvc.perform(patch("/api/contrats/1/annuler"))
                .andExpect(status().isNoContent());
    }
}
