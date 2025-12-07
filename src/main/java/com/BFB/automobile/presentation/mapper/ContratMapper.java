package com.BFB.automobile.presentation.mapper;

import com.BFB.automobile.data.Client;
import com.BFB.automobile.data.Contrat;
import com.BFB.automobile.data.Vehicule;
import com.BFB.automobile.presentation.dto.ContratDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir entre Contrat (entité JPA) et ContratDTO (représentation API)

 */
@Component
public class ContratMapper {
    
    @Autowired
    private ClientMapper clientMapper;
    
    @Autowired
    private VehiculeMapper vehiculeMapper;
    
    public ContratDTO toDTO(Contrat contrat) {
        if (contrat == null) return null;
        
        ContratDTO dto = new ContratDTO();
        dto.setId(contrat.getId());
        dto.setDateDebut(contrat.getDateDebut());
        dto.setDateFin(contrat.getDateFin());
        dto.setEtat(contrat.getEtat());
        dto.setCommentaire(contrat.getCommentaire());
        dto.setDateCreation(contrat.getDateCreation());
        dto.setDateModification(contrat.getDateModification());
        
        if (contrat.getClient() != null) {
            dto.setClientId(contrat.getClient().getId());
            dto.setClient(clientMapper.toDTO(contrat.getClient()));
        }
        
        if (contrat.getVehicule() != null) {
            dto.setVehiculeId(contrat.getVehicule().getId());
            dto.setVehicule(vehiculeMapper.toDTO(contrat.getVehicule()));
        }
        
        return dto;
    }
    
    public Contrat toEntity(ContratDTO dto) {
        if (dto == null) return null;
        
        Contrat contrat = new Contrat();
        contrat.setId(dto.getId());
        contrat.setDateDebut(dto.getDateDebut());
        contrat.setDateFin(dto.getDateFin());
        contrat.setCommentaire(dto.getCommentaire());
        
        if (dto.getEtat() != null) {
            contrat.setEtat(dto.getEtat());
        }
        
        // Créer des entités minimalistes avec juste l'ID
        // Le service se chargera de récupérer les entités complètes
        if (dto.getClientId() != null) {
            Client client = new Client();
            client.setId(dto.getClientId());
            contrat.setClient(client);
        }
        
        if (dto.getVehiculeId() != null) {
            Vehicule vehicule = new Vehicule();
            vehicule.setId(dto.getVehiculeId());
            contrat.setVehicule(vehicule);
        }
        
        return contrat;
    }
}
