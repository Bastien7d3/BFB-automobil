package com.BFB.automobile.presentation.controller;

import com.BFB.automobile.business.exception.BusinessException;
import com.BFB.automobile.business.service.ContratService;
import com.BFB.automobile.data.*;
import com.BFB.automobile.presentation.dto.ContratDTO;
import com.BFB.automobile.presentation.mapper.ContratMapper;
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
 * Tests du contrôleur ContratController
 */
@WebMvcTest(ContratController.class)
class ContratControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private ContratService contratService;
    
    @MockBean
    private ContratMapper contratMapper;
    
    private Contrat contrat;
    private ContratDTO contratDTO;
    private Client client;
    private Vehicule vehicule;
    
    @BeforeEach
    void setUp() {
        client = new Client();
        client.setId(1L);
        client.setNom("Dupont");
        client.setPrenom("Jean");
        
        vehicule = new Vehicule();
        vehicule.setId(1L);
        vehicule.setMarque("Peugeot");
        vehicule.setModele("308");
        vehicule.setImmatriculation("AB-123-CD");
        
        contrat = new Contrat();
        contrat.setId(1L);
        contrat.setClient(client);
        contrat.setVehicule(vehicule);
        contrat.setDateDebut(LocalDate.now().plusDays(5));
        contrat.setDateFin(LocalDate.now().plusDays(10));
        contrat.setEtat(EtatContrat.EN_ATTENTE);
        
        contratDTO = new ContratDTO();
        contratDTO.setId(1L);
        contratDTO.setClientId(1L);
        contratDTO.setVehiculeId(1L);
        contratDTO.setDateDebut(LocalDate.now().plusDays(5));
        contratDTO.setDateFin(LocalDate.now().plusDays(10));
        contratDTO.setEtat(EtatContrat.EN_ATTENTE);
    }
    
    // ========== Tests GET /api/contrats ==========
    
    @Test
    void obtenirTousLesContrats_devraitRetourner200AvecListeContrats() throws Exception {
        // Arrange
        List<Contrat> contrats = Arrays.asList(contrat);
        when(contratService.obtenirTousLesContrats()).thenReturn(contrats);
        when(contratMapper.toDTO(any(Contrat.class))).thenReturn(contratDTO);
        
        // Act & Assert
        mockMvc.perform(get("/api/contrats"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].etat").value("EN_ATTENTE"));
        
        verify(contratService, times(1)).obtenirTousLesContrats();
    }
    
    @Test
    void obtenirContratsActifs_devraitRetourner200AvecSeulementActifs() throws Exception {
        // Arrange
        List<Contrat> contrats = Arrays.asList(contrat);
        when(contratService.obtenirContratsParEtat(EtatContrat.EN_COURS)).thenReturn(contrats);
        when(contratMapper.toDTO(contrat)).thenReturn(contratDTO);
        
        // Act & Assert
        mockMvc.perform(get("/api/contrats?etat=EN_COURS"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
        
        verify(contratService, times(1)).obtenirContratsParEtat(EtatContrat.EN_COURS);
        verify(contratService, never()).obtenirTousLesContrats();
    }
    
    // ========== Tests GET /api/contrats/{id} ==========
    
    @Test
    void obtenirContratParId_devraitRetourner200AvecContrat() throws Exception {
        // Arrange
        when(contratService.obtenirContratParId(1L)).thenReturn(contrat);
        when(contratMapper.toDTO(contrat)).thenReturn(contratDTO);
        
        // Act & Assert
        mockMvc.perform(get("/api/contrats/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.etat").value("EN_ATTENTE"));
        
        verify(contratService, times(1)).obtenirContratParId(1L);
    }
    
    @Test
    void obtenirContratParId_devraitRetourner404SiContratNonTrouve() throws Exception {
        // Arrange
        when(contratService.obtenirContratParId(999L))
            .thenThrow(new BusinessException("CONTRAT_NON_TROUVE", "Contrat non trouvé"));
        
        // Act & Assert
        mockMvc.perform(get("/api/contrats/999"))
            .andExpect(status().isBadRequest());
    }
    
    // ========== Tests POST /api/contrats ==========
    
    @Test
    void creerContrat_devraitRetourner201AvecContrat() throws Exception {
        // Arrange
        when(contratMapper.toEntity(any(ContratDTO.class))).thenReturn(contrat);
        when(contratService.creerContrat(any(Contrat.class))).thenReturn(contrat);
        when(contratMapper.toDTO(any(Contrat.class))).thenReturn(contratDTO);
        
        // Act & Assert
        mockMvc.perform(post("/api/contrats")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contratDTO)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.etat").value("EN_ATTENTE"));
        
        verify(contratService, times(1)).creerContrat(any(Contrat.class));
    }
    
    @Test
    void creerContrat_devraitRetourner400SiVehiculeDejaLoue() throws Exception {
        // Arrange
        when(contratMapper.toEntity(any(ContratDTO.class))).thenReturn(contrat);
        when(contratService.creerContrat(any(Contrat.class)))
            .thenThrow(new BusinessException("VEHICULE_DEJA_LOUE", "Véhicule déjà loué sur cette période"));
        
        // Act & Assert
        mockMvc.perform(post("/api/contrats")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contratDTO)))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    void creerContrat_devraitRetourner400SiDatesIncoherentes() throws Exception {
        // Arrange
        when(contratMapper.toEntity(any(ContratDTO.class))).thenReturn(contrat);
        when(contratService.creerContrat(any(Contrat.class)))
            .thenThrow(new BusinessException("DATES_INCOHERENTES", "Date de fin avant date de début"));
        
        // Act & Assert
        mockMvc.perform(post("/api/contrats")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contratDTO)))
            .andExpect(status().isBadRequest());
    }
    
    // ========== Tests PUT /api/contrats/{id} ==========
    
    @Test
    void mettreAJourContrat_devraitRetourner200AvecContratModifie() throws Exception {
        // Arrange
        when(contratMapper.toEntity(any(ContratDTO.class))).thenReturn(contrat);
        when(contratService.mettreAJourContrat(anyLong(), any(Contrat.class))).thenReturn(contrat);
        when(contratMapper.toDTO(any(Contrat.class))).thenReturn(contratDTO);
        
        // Act & Assert
        mockMvc.perform(put("/api/contrats/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contratDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
        
        verify(contratService, times(1)).mettreAJourContrat(eq(1L), any(Contrat.class));
    }
    
    @Test
    void mettreAJourContrat_devraitRetourner400SiContratNonModifiable() throws Exception {
        // Arrange
        when(contratMapper.toEntity(any(ContratDTO.class))).thenReturn(contrat);
        when(contratService.mettreAJourContrat(anyLong(), any(Contrat.class)))
            .thenThrow(new BusinessException("CONTRAT_NON_MODIFIABLE", "Contrat déjà démarré"));
        
        // Act & Assert
        mockMvc.perform(put("/api/contrats/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contratDTO)))
            .andExpect(status().isBadRequest());
    }
    
    // ========== Tests PATCH /api/contrats/{id}/annuler ==========
    
    @Test
    void annulerContrat_devraitRetourner200AvecContratAnnule() throws Exception {
        // Arrange
        contrat.setEtat(EtatContrat.ANNULE);
        contratDTO.setEtat(EtatContrat.ANNULE);
        
        when(contratService.annulerContrat(1L, "Annulation client")).thenReturn(contrat);
        when(contratMapper.toDTO(contrat)).thenReturn(contratDTO);
        
        // Act & Assert
        mockMvc.perform(patch("/api/contrats/1/annuler")
                .param("motif", "Annulation client"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.etat").value("ANNULE"));
        
        verify(contratService, times(1)).annulerContrat(1L, "Annulation client");
    }
    
    @Test
    void annulerContrat_devraitRetourner400SiContratNonAnnulable() throws Exception {
        // Arrange
        when(contratService.annulerContrat(anyLong(), anyString()))
            .thenThrow(new BusinessException("CONTRAT_NON_ANNULABLE", "Contrat déjà annulé"));
        
        // Act & Assert
        mockMvc.perform(patch("/api/contrats/1/annuler")
                .param("motif", "Test"))
            .andExpect(status().isBadRequest());
    }
    
    // ========== Tests PATCH /api/contrats/{id}/terminer ==========
    
    @Test
    void terminerContrat_devraitRetourner200AvecContratTermine() throws Exception {
        // Arrange
        contrat.setEtat(EtatContrat.TERMINE);
        contratDTO.setEtat(EtatContrat.TERMINE);
        
        when(contratService.terminerContrat(1L)).thenReturn(contrat);
        when(contratMapper.toDTO(contrat)).thenReturn(contratDTO);
        
        // Act & Assert
        mockMvc.perform(patch("/api/contrats/1/terminer"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.etat").value("TERMINE"));
        
        verify(contratService, times(1)).terminerContrat(1L);
    }
    
    @Test
    void terminerContrat_devraitRetourner400SiContratNonTerminable() throws Exception {
        // Arrange
        when(contratService.terminerContrat(1L))
            .thenThrow(new BusinessException("CONTRAT_NON_TERMINABLE", "Contrat pas en cours"));
        
        // Act & Assert
        mockMvc.perform(patch("/api/contrats/1/terminer"))
            .andExpect(status().isBadRequest());
    }
    
    // ========== Tests GET /api/contrats/client/{clientId} ==========
    
    @Test
    void obtenirContratsParClient_devraitRetourner200AvecContrats() throws Exception {
        // Arrange
        List<Contrat> contrats = Arrays.asList(contrat);
        when(contratService.obtenirContratsParClient(1L)).thenReturn(contrats);
        when(contratMapper.toDTO(any(Contrat.class))).thenReturn(contratDTO);
        
        // Act & Assert
        mockMvc.perform(get("/api/contrats/client/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].clientId").value(1));
        
        verify(contratService, times(1)).obtenirContratsParClient(1L);
    }
    
    // ========== Tests GET /api/contrats/vehicule/{vehiculeId} ==========
    
    @Test
    void obtenirContratsParVehicule_devraitRetourner200AvecContrats() throws Exception {
        // Arrange
        List<Contrat> contrats = Arrays.asList(contrat);
        when(contratService.obtenirContratsParVehicule(1L)).thenReturn(contrats);
        when(contratMapper.toDTO(any(Contrat.class))).thenReturn(contratDTO);
        
        // Act & Assert
        mockMvc.perform(get("/api/contrats/vehicule/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].vehiculeId").value(1));
        
        verify(contratService, times(1)).obtenirContratsParVehicule(1L);
    }
    
}
