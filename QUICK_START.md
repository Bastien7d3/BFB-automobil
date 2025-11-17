# Guide de Démarrage Rapide - BFB Automobile

## 🚀 Lancer l'application en 3 étapes

### 1. Vérifier les prérequis
```powershell
# Vérifier Java (besoin de version 17+)
java -version

# Si Java n'est pas installé, télécharger depuis :
# https://adoptium.net/
```

### 2. Lancer l'application
```powershell
# Depuis le répertoire du projet
.\mvnw.cmd spring-boot:run
```

**Attendre le message :**
```
Started AutomobileApplication in X.XXX seconds
```

### 3. Tester l'API

Ouvrir un navigateur ou utiliser cURL :

#### Voir tous les clients
```powershell
# Dans un autre terminal PowerShell
Invoke-RestMethod -Uri "http://localhost:8080/api/clients" | ConvertTo-Json
```

Ou dans un navigateur : http://localhost:8080/api/clients

#### Voir la console H2
http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:bfb_automobile`
- Username: `sa`
- Password: (laisser vide)

## 🧪 Tests rapides avec PowerShell

### Créer un client
```powershell
$body = @{
    nom = "Test"
    prenom = "Utilisateur"
    dateNaissance = "1995-05-15"
    numeroPermis = "TEST123456"
    adresse = "1 rue de Test, 75000 Paris"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/clients" `
    -Method Post `
    -Body $body `
    -ContentType "application/json"
```

### Créer un véhicule
```powershell
$body = @{
    marque = "Peugeot"
    modele = "3008"
    motorisation = "1.5 BlueHDi"
    couleur = "Noir"
    immatriculation = "TEST-001-AA"
    dateAcquisition = "2024-01-01"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/vehicules" `
    -Method Post `
    -Body $body `
    -ContentType "application/json"
```

### Lister les véhicules disponibles
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/vehicules/disponibles" | ConvertTo-Json
```

### Créer un contrat
```powershell
$body = @{
    dateDebut = "2024-12-01"
    dateFin = "2024-12-10"
    clientId = 1
    vehiculeId = 1
    commentaire = "Test de location"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/contrats" `
    -Method Post `
    -Body $body `
    -ContentType "application/json"
```

## 📊 Données de démonstration

L'application démarre avec des données pré-chargées :
- **5 clients** (Dupont, Martin, Bernard, Dubois, Robert)
- **7 véhicules** (Peugeot 308, Renault Clio, Citroën C3, etc.)
- **4 contrats** (différents états : terminé, en cours, en attente)

## 🛑 Arrêter l'application

Dans le terminal où l'application tourne :
```
Ctrl + C
```

## ❓ Problèmes courants

### Erreur "Port 8080 already in use"
```powershell
# Trouver le processus qui utilise le port 8080
Get-NetTCPConnection -LocalPort 8080

# Tuer le processus (remplacer PID par l'ID du processus)
Stop-Process -Id PID -Force
```

### Erreur "mvnw.cmd not found"
Vous devez être dans le répertoire du projet :
```powershell
cd "c:\Users\aq02263\OneDrive - Alliance\Documents\ecole\BFB-automobil"
```

### Java version incorrecte
Assurez-vous d'avoir Java 17 ou supérieur :
```powershell
java -version
# Devrait afficher : version "17.x.x" ou plus
```

## 📚 Documentation complète

- **README.md** : Vue d'ensemble et documentation complète
- **DATA_LAYER.md** : Explication de la couche de données
- **BUSINESS_LAYER.md** : Explication de la couche métier
- **PRESENTATION_LAYER.md** : Explication de la couche présentation

## 🎯 Scénarios de démonstration

### Scénario 1 : Création complète d'une location
1. Créer un client
2. Créer un véhicule
3. Créer un contrat liant le client et le véhicule
4. Vérifier que le véhicule passe à l'état EN_LOCATION

### Scénario 2 : Détection de conflit
1. Créer un contrat pour un véhicule sur une période
2. Essayer de créer un second contrat sur la même période
3. Observer l'erreur "VEHICULE_DEJA_LOUE"

### Scénario 3 : Véhicule en panne
1. Créer des contrats en attente pour un véhicule
2. Changer l'état du véhicule à EN_PANNE
3. Observer que les contrats en attente sont automatiquement annulés

### Scénario 4 : Recherche et filtrage
1. Rechercher des clients par nom : `/api/clients?nom=Dupont`
2. Lister uniquement les véhicules disponibles : `/api/vehicules/disponibles`
3. Voir les contrats d'un client : `/api/contrats/client/1`

## 🔧 Configuration rapide pour la présentation

### Afficher des logs plus clairs
Dans `application.properties`, mettre :
```properties
logging.level.com.BFB.automobile=INFO
logging.level.org.hibernate.SQL=INFO
```

### Désactiver le rechargement automatique de la base
Pour garder les données entre redémarrages :
```properties
spring.jpa.hibernate.ddl-auto=update
# au lieu de create-drop
```

---

**Prêt pour la démo ! 🚀**
