package com.BFB.automobile.presentation.mapper;

import com.BFB.automobile.data.Client;
import com.BFB.automobile.presentation.dto.ClientDTO;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir entre Client (entité JPA) et ClientDTO (représentation API)
 * 
 * DESIGN PATTERN GoF UTILISÉ :
 * 
 * ADAPTER PATTERN : Ce mapper adapte les objets d'un format à un autre.
 * - toDTO() : adapte Client (format interne/base de données) vers ClientDTO (format API/externe)
 * - toEntity() : adapte ClientDTO (format API) vers Client (format interne)
 * 
 * Avantages de ce pattern :
 * - Découple la structure interne (entités JPA) de l'API REST
 * - Évite les références circulaires lors de la sérialisation JSON
 * - Permet de masquer des champs sensibles (ex: ne pas exposer certains champs internes)
 * - Facilite l'évolution indépendante de la base de données et de l'API
 */
@Component
public class ClientMapper {
    
    public ClientDTO toDTO(Client client) {
        if (client == null) return null;
        
        ClientDTO dto = new ClientDTO();
        dto.setId(client.getId());
        dto.setNom(client.getNom());
        dto.setPrenom(client.getPrenom());
        dto.setDateNaissance(client.getDateNaissance());
        dto.setNumeroPermis(client.getNumeroPermis());
        dto.setAdresse(client.getAdresse());
        dto.setActif(client.getActif());
        dto.setDateCreation(client.getDateCreation());
        return dto;
    }
    
    public Client toEntity(ClientDTO dto) {
        if (dto == null) return null;
        
        Client client = new Client();
        client.setId(dto.getId());
        client.setNom(dto.getNom());
        client.setPrenom(dto.getPrenom());
        client.setDateNaissance(dto.getDateNaissance());
        client.setNumeroPermis(dto.getNumeroPermis());
        client.setAdresse(dto.getAdresse());
        if (dto.getActif() != null) {
            client.setActif(dto.getActif());
        }
        return client;
    }
}
