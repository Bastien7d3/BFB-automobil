package com.BFB.automobile.presentation.controller;

import com.BFB.automobile.business.exception.BusinessException;
import com.BFB.automobile.business.service.ClientService;
import com.BFB.automobile.data.Client;
import com.BFB.automobile.presentation.dto.ClientDTO;
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

@WebMvcTest(ClientController.class)
class ClientControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private ClientService clientService;
    
    @MockBean
    private com.BFB.automobile.presentation.mapper.ClientMapper clientMapper;
    
    private Client client;
    private ClientDTO clientDTO;
    
    @BeforeEach
    void setUp() {
        client = Client.builder()
                .nom("Dupont")
                .prenom("Jean")
                .dateNaissance(LocalDate.of(1990, 5, 15))
                .numeroPermis("123456789")
                .adresse("10 rue de la Paix")
                .actif(true)
                .build();
        client.setId(1L);
        
        clientDTO = new ClientDTO();
        clientDTO.setNom("Dupont");
        clientDTO.setPrenom("Jean");
        clientDTO.setDateNaissance(LocalDate.of(1990, 5, 15));
        clientDTO.setNumeroPermis("123456789");
        clientDTO.setAdresse("10 rue de la Paix");
    }
    
    @Test
    void creerClient_devraitRetourner201() throws Exception {
        when(clientService.creerClient(any(Client.class))).thenReturn(client);
        
        mockMvc.perform(post("/api/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clientDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }
    
    @Test
    void creerClient_devraitRetourner400_siClientExiste() throws Exception {
        when(clientService.creerClient(any(Client.class)))
                .thenThrow(new BusinessException("CLIENT_EXISTE_DEJA", "Client existe"));
        
        mockMvc.perform(post("/api/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clientDTO)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void listerClients_devraitRetournerListe() throws Exception {
        when(clientService.rechercherClients(null, null)).thenReturn(Arrays.asList(client));
        
        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }
    
    @Test
    void obtenirClient_devraitRetourner200() throws Exception {
        when(clientService.obtenirClientParId(1L)).thenReturn(client);
        
        mockMvc.perform(get("/api/clients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
    
    @Test
    void mettreAJourClient_devraitRetourner200() throws Exception {
        when(clientService.mettreAJourClient(anyLong(), any(Client.class))).thenReturn(client);
        
        mockMvc.perform(put("/api/clients/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clientDTO)))
                .andExpect(status().isOk());
    }
    
    @Test
    void desactiverClient_devraitRetourner204() throws Exception {
        mockMvc.perform(delete("/api/clients/1"))
                .andExpect(status().isNoContent());
    }
}
