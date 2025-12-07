package com.BFB.automobile.presentation.mapper;

import com.BFB.automobile.data.Vehicule;
import com.BFB.automobile.presentation.dto.VehiculeDTO;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir entre Vehicule (entité JPA) et VehiculeDTO (représentation API)
 */
@Component
public class VehiculeMapper {
    
    public VehiculeDTO toDTO(Vehicule vehicule) {
        if (vehicule == null) return null;
        
        VehiculeDTO dto = new VehiculeDTO();
        dto.setId(vehicule.getId());
        dto.setMarque(vehicule.getMarque());
        dto.setModele(vehicule.getModele());
        dto.setMotorisation(vehicule.getMotorisation());
        dto.setCouleur(vehicule.getCouleur());
        dto.setImmatriculation(vehicule.getImmatriculation());
        dto.setDateAcquisition(vehicule.getDateAcquisition());
        dto.setEtat(vehicule.getEtat());
        return dto;
    }
    
    public Vehicule toEntity(VehiculeDTO dto) {
        if (dto == null) return null;
        
        Vehicule vehicule = new Vehicule();
        vehicule.setId(dto.getId());
        vehicule.setMarque(dto.getMarque());
        vehicule.setModele(dto.getModele());
        vehicule.setMotorisation(dto.getMotorisation());
        vehicule.setCouleur(dto.getCouleur());
        vehicule.setImmatriculation(dto.getImmatriculation());
        vehicule.setDateAcquisition(dto.getDateAcquisition());
        if (dto.getEtat() != null) {
            vehicule.setEtat(dto.getEtat());
        }
        return vehicule;
    }
}
