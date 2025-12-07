package com.BFB.automobile.business.service;

import com.BFB.automobile.business.exception.BusinessException;
import com.BFB.automobile.data.Client;
import com.BFB.automobile.data.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service métier pour la gestion des clients
 * Implémente les règles métier :
 * - Un client doit être unique (nom + prénom + date de naissance)
 * - Deux clients distincts ne peuvent pas avoir le même numéro de permis
 * 
 * DESIGN PATTERNS GoF UTILISÉS :
 * 
 * 1. FACADE PATTERN : Encapsule la logique métier complexe (validations,
 *    vérifications d'unicité, gestion des transactions) derrière une interface
 *    simple pour les contrôleurs.
 * 
 * 2. STRATEGY PATTERN : @Transactional utilise le Strategy Pattern pour gérer
 *    les transactions (begin/commit/rollback). Spring injecte dynamiquement
 *    la stratégie de gestion transactionnelle selon le contexte.
 *    - @Transactional = stratégie lecture-écriture
 *    - @Transactional(readOnly = true) = stratégie lecture optimisée
 * 
 */
@Service
@Transactional
public class ClientService {
    
    private final ClientRepository clientRepository;
    
    /**
     * Injection par constructeur recommandée (immutabilité + testabilité)
     */
    @Autowired
    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }
    
    /**
     * Crée un nouveau client après validation des règles métier
     */
    public Client creerClient(Client client) {
        // Règle : Un client doit être unique (nom + prénom + date de naissance)
        if (clientRepository.existsByNomAndPrenomAndDateNaissance(
                client.getNom(), 
                client.getPrenom(), 
                client.getDateNaissance())) {
            throw new BusinessException(
                "CLIENT_EXISTE_DEJA",
                "Un client avec ce nom, prénom et date de naissance existe déjà");
        }
        
        // Règle : Deux clients distincts ne peuvent pas avoir le même numéro de permis
        if (clientRepository.existsByNumeroPermis(client.getNumeroPermis())) {
            throw new BusinessException(
                "NUMERO_PERMIS_EXISTE",
                "Ce numéro de permis est déjà utilisé par un autre client");
        }
        
        // Validation de l'âge minimum (18 ans)
        if (client.getDateNaissance().isAfter(LocalDate.now().minusYears(18))) {
            throw new BusinessException(
                "AGE_INSUFFISANT",
                "Le client doit avoir au moins 18 ans pour louer un véhicule");
        }
        
        try {
            return clientRepository.save(client);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(
                "ERREUR_CREATION_CLIENT",
                "Impossible de créer le client : violation de contrainte d'unicité", e);
        }
    }
    
    /**
     * Met à jour un client existant
     */
    public Client mettreAJourClient(Long id, Client clientModifie) {
        Client clientExistant = clientRepository.findById(id)
            .orElseThrow(() -> new BusinessException(
                "CLIENT_NON_TROUVE",
                "Client avec l'ID " + id + " non trouvé"));
        
        // Vérifier si le nouveau numéro de permis n'est pas déjà utilisé par un autre client
        if (!clientExistant.getNumeroPermis().equals(clientModifie.getNumeroPermis())) {
            Optional<Client> clientAvecMemePermis = clientRepository.findByNumeroPermis(
                clientModifie.getNumeroPermis());
            if (clientAvecMemePermis.isPresent() && 
                !clientAvecMemePermis.get().getId().equals(id)) {
                throw new BusinessException(
                    "NUMERO_PERMIS_EXISTE",
                    "Ce numéro de permis est déjà utilisé par un autre client");
            }
        }
        
        // Mise à jour des champs modifiables
        clientExistant.setNom(clientModifie.getNom());
        clientExistant.setPrenom(clientModifie.getPrenom());
        clientExistant.setDateNaissance(clientModifie.getDateNaissance());
        clientExistant.setNumeroPermis(clientModifie.getNumeroPermis());
        clientExistant.setAdresse(clientModifie.getAdresse());
        
        try {
            return clientRepository.save(clientExistant);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(
                "ERREUR_MISE_A_JOUR_CLIENT",
                "Impossible de mettre à jour le client : violation de contrainte", e);
        }
    }
    
    /**
     * Désactive un client (soft delete)
     */
    public void desactiverClient(Long id) {
        Client client = clientRepository.findById(id)
            .orElseThrow(() -> new BusinessException(
                "CLIENT_NON_TROUVE",
                "Client avec l'ID " + id + " non trouvé"));
        
        client.setActif(false);
        clientRepository.save(client);
    }
    
    /**
     * Récupère tous les clients actifs
     */
    @Transactional(readOnly = true)
    public List<Client> obtenirTousLesClientsActifs() {
        return clientRepository.findByActifTrue();
    }
    
    /**
     * Récupère tous les clients
     */
    @Transactional(readOnly = true)
    public List<Client> obtenirTousLesClients() {
        return clientRepository.findAll();
    }
    
    /**
     * Récupère un client par son ID
     */
    @Transactional(readOnly = true)
    public Client obtenirClientParId(Long id) {
        return clientRepository.findById(id)
            .orElseThrow(() -> new BusinessException(
                "CLIENT_NON_TROUVE",
                "Client avec l'ID " + id + " non trouvé"));
    }
    
    /**
     * Recherche des clients par nom et/ou prénom
     */
    @Transactional(readOnly = true)
    public List<Client> rechercherClients(String nom, String prenom) {
        if (nom != null && prenom != null) {
            return clientRepository.searchByNomAndPrenom(nom, prenom);
        } else if (nom != null) {
            return clientRepository.findByNomContainingIgnoreCase(nom);
        } else if (prenom != null) {
            return clientRepository.findByPrenomContainingIgnoreCase(prenom);
        } else {
            return clientRepository.findAll();
        }
    }
    
    /**
     * Recherche un client par son numéro de permis
     */
    @Transactional(readOnly = true)
    public Optional<Client> rechercherParNumeroPermis(String numeroPermis) {
        return clientRepository.findByNumeroPermis(numeroPermis);
    }
}
