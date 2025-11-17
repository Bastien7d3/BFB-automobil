# BFB Automobile - Système de Gestion de Locations

Application Spring Boot pour la gestion des locations de véhicules automobiles.

## 📋 Vue d'ensemble

Ce projet implémente un système complet de gestion de locations automobiles avec :
- **Gestion des clients** : Création, modification, recherche de clients
- **Gestion des véhicules** : Suivi du parc automobile (disponible, en location, en panne)
- **Gestion des contrats** : Création, suivi, annulation de contrats de location
- **Automatisation** : Traitement automatique des retards et annulations

## 🏗️ Architecture

L'application suit une **architecture en couches** (3-tier architecture) :

```
┌─────────────────────────────────────┐
│   COUCHE PRÉSENTATION (API REST)    │  ← ClientController, VehiculeController, ContratController
│   DTOs, Mappers, Controllers        │
├─────────────────────────────────────┤
│   COUCHE BUSINESS (Logique Métier)  │  ← ClientService, VehiculeService, ContratService
│   Services, Règles métier           │
├─────────────────────────────────────┤
│   COUCHE DATA (Persistance)         │  ← Entités JPA, Repositories
│   Entités, Repositories             │
└─────────────────────────────────────┘
            ↓
    ┌──────────────┐
    │  Base H2     │
    └──────────────┘
```

### Documentation détaillée par couche :
- **[DATA_LAYER.md](DATA_LAYER.md)** : Explication de la couche de données
- **[BUSINESS_LAYER.md](BUSINESS_LAYER.md)** : Explication de la couche métier
- **[PRESENTATION_LAYER.md](PRESENTATION_LAYER.md)** : Explication de la couche présentation

## 🚀 Démarrage rapide

### Prérequis
- Java 17 ou supérieur
- Maven 3.6 ou supérieur

### Lancer l'application

**Option 1 : Avec Maven Wrapper (recommandé)**
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

**Option 2 : Avec Maven installé**
```bash
mvn spring-boot:run
```

**Option 3 : Via votre IDE**
- Ouvrir le projet dans IntelliJ IDEA / Eclipse / VS Code
- Exécuter la classe `AutomobileApplication.java`

### Accès à l'application
- **API REST** : http://localhost:8080/api
- **Console H2** : http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:bfb_automobile`
  - Username: `sa`
  - Password: (vide)

## 📚 API Endpoints

### Clients
```http
GET    /api/clients                    # Liste tous les clients
GET    /api/clients/{id}               # Détails d'un client
GET    /api/clients?nom=Dupont         # Recherche par nom
POST   /api/clients                    # Créer un client
PUT    /api/clients/{id}               # Modifier un client
DELETE /api/clients/{id}               # Désactiver un client
```

### Véhicules
```http
GET    /api/vehicules                  # Liste tous les véhicules
GET    /api/vehicules/disponibles      # Véhicules disponibles uniquement
GET    /api/vehicules/{id}             # Détails d'un véhicule
POST   /api/vehicules                  # Créer un véhicule
PUT    /api/vehicules/{id}             # Modifier un véhicule
PATCH  /api/vehicules/{id}/etat        # Changer l'état d'un véhicule
DELETE /api/vehicules/{id}             # Supprimer un véhicule
```

### Contrats
```http
GET    /api/contrats                   # Liste tous les contrats
GET    /api/contrats/actifs            # Contrats en cours/en attente
GET    /api/contrats/{id}              # Détails d'un contrat
GET    /api/contrats/client/{id}       # Contrats d'un client
GET    /api/contrats/vehicule/{id}     # Contrats d'un véhicule
POST   /api/contrats                   # Créer un contrat
PUT    /api/contrats/{id}              # Modifier un contrat
PATCH  /api/contrats/{id}/annuler      # Annuler un contrat
PATCH  /api/contrats/{id}/terminer     # Terminer un contrat
POST   /api/contrats/traiter-etats     # Traitement manuel des états
```

## 🧪 Exemples d'utilisation

### Créer un client
```bash
curl -X POST http://localhost:8080/api/clients \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Dupont",
    "prenom": "Jean",
    "dateNaissance": "1990-05-15",
    "numeroPermis": "123456789",
    "adresse": "10 rue de la Paix, 75001 Paris"
  }'
```

### Créer un véhicule
```bash
curl -X POST http://localhost:8080/api/vehicules \
  -H "Content-Type: application/json" \
  -d '{
    "marque": "Peugeot",
    "modele": "308",
    "motorisation": "1.5 BlueHDi 130ch",
    "couleur": "Gris",
    "immatriculation": "AB-123-CD",
    "dateAcquisition": "2023-01-15"
  }'
```

### Créer un contrat de location
```bash
curl -X POST http://localhost:8080/api/contrats \
  -H "Content-Type: application/json" \
  -d '{
    "dateDebut": "2024-12-01",
    "dateFin": "2024-12-10",
    "clientId": 1,
    "vehiculeId": 1,
    "commentaire": "Location pour voyage professionnel"
  }'
```

## 🔒 Règles Métier Implémentées

### Clients
- ✅ Un client doit être unique (nom + prénom + date de naissance)
- ✅ Deux clients ne peuvent pas avoir le même numéro de permis
- ✅ Âge minimum : 18 ans

### Véhicules
- ✅ Un véhicule doit être unique (par immatriculation)
- ✅ Les véhicules en panne ne peuvent pas être loués
- ✅ Si un véhicule passe en panne, ses contrats en attente sont annulés

### Contrats
- ✅ Un véhicule ne peut être loué qu'une fois sur une période donnée
- ✅ Un client peut louer plusieurs véhicules simultanément
- ✅ Si retour en retard, le contrat passe automatiquement à "EN_RETARD"
- ✅ Si un retard bloque le contrat suivant, celui-ci est annulé
- ✅ Démarrage automatique des contrats à leur date de début

## ⚙️ Configuration

### Base de données

**Développement (H2 en mémoire) :**
```properties
spring.datasource.url=jdbc:h2:mem:bfb_automobile
spring.jpa.hibernate.ddl-auto=create-drop
```

**Production (PostgreSQL) :**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/bfb_automobile
spring.datasource.username=bfb_user
spring.datasource.password=votre_mot_de_passe
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

### Tâches planifiées

Les traitements automatiques s'exécutent **chaque jour à minuit** :
- Démarrage des contrats
- Détection des retards
- Annulation des contrats bloqués

Pour modifier la fréquence :
```java
@Scheduled(cron = "0 0 0 * * *")  // Expression cron
```

## 🧪 Tests

### Tester avec cURL

Voir les exemples ci-dessus dans "Exemples d'utilisation"

### Tester avec Postman

1. Importer la collection (à créer) dans Postman
2. Variables d'environnement :
   - `baseUrl`: `http://localhost:8080`

### Tests automatisés

```bash
mvn test
```

## 📊 Modèle de données

### Entités principales

**Client**
- id, nom, prénom, dateNaissance, numeroPermis, adresse, actif

**Véhicule**
- id, marque, modele, motorisation, couleur, immatriculation, dateAcquisition, etat

**Contrat**
- id, dateDebut, dateFin, etat, client, vehicule, commentaire

### États des véhicules
- `DISPONIBLE` : Prêt à être loué
- `EN_LOCATION` : Actuellement loué
- `EN_PANNE` : Non disponible

### États des contrats
- `EN_ATTENTE` : Réservation confirmée, pas encore démarré
- `EN_COURS` : Location active
- `TERMINE` : Location terminée normalement
- `EN_RETARD` : Dépassement de la date de fin
- `ANNULE` : Contrat annulé

## 🛠️ Technologies utilisées

- **Spring Boot 3.5.7** : Framework principal
- **Spring Data JPA** : Persistance des données
- **H2 Database** : Base de données en mémoire (dev)
- **Jakarta Validation** : Validation des données
- **Maven** : Gestion des dépendances

## 📖 Pour la soutenance

### Points clés à présenter :

1. **Architecture en couches** : Séparation claire des responsabilités
2. **Patterns utilisés** :
   - Repository Pattern (Spring Data)
   - Service Layer Pattern
   - DTO Pattern
   - Mapper Pattern
3. **Règles métier** : Toutes implémentées avec justification
4. **Gestion des transactions** : @Transactional pour la cohérence
5. **Automatisation** : Tâches planifiées pour les traitements quotidiens
6. **API REST** : Standards RESTful respectés

### Démonstration suggérée :

1. Créer des clients
2. Créer des véhicules
3. Créer des contrats de location
4. Montrer la détection de conflits (double réservation)
5. Mettre un véhicule en panne → observer l'annulation des contrats
6. Consulter la console H2 pour voir les données

## 🔮 Évolutions futures possibles

- [ ] Authentification et autorisation (Spring Security)
- [ ] Documentation API (Swagger/OpenAPI)
- [ ] Gestion des tarifs et facturation
- [ ] Notifications email/SMS
- [ ] Historique des modifications (audit)
- [ ] Génération de rapports
- [ ] Interface web (React/Angular)
- [ ] Application mobile

## 👥 Auteur

Projet réalisé dans le cadre du cours BFB-automobile

## 📄 Licence

Ce projet est sous licence MIT - voir le fichier [LICENSE](LICENSE) pour plus de détails.

---

**Bonne chance pour la soutenance du 08/12 ! 🎯**
