# COUCHE DATA - Explication des choix techniques

## Vue d'ensemble

La couche Data est responsable de la **persistance des données** et de l'**accès aux données**. Elle implémente le pattern **Repository** de Spring Data JPA pour abstraire complètement l'accès à la base de données.

---

## 1. Architecture et Organisation

### Structure des packages
```
com.BFB.automobile.data/
├── Client.java                 # Entité Client
├── Vehicule.java              # Entité Véhicule
├── Contrat.java               # Entité Contrat
├── EtatVehicule.java          # Énumération des états de véhicule
├── EtatContrat.java           # Énumération des états de contrat
└── repository/
    ├── ClientRepository.java
    ├── VehiculeRepository.java
    └── ContratRepository.java
```

---

## 2. Choix Technologiques Majeurs

### 2.1 JPA (Jakarta Persistence API) plutôt que MongoDB

**✅ POURQUOI CE CHOIX ?**

1. **Relations complexes** : Le domaine métier comporte des relations clairement définies :
   - Un Contrat lie UN Client à UN Véhicule (relation Many-to-One)
   - Ces relations sont bidirectionnelles et nécessitent une intégrité référentielle

2. **Contraintes d'unicité multiples** :
   - Client : unique par (nom + prénom + date de naissance) ET numéro de permis unique
   - Véhicule : unique par immatriculation
   - Ces contraintes sont nativement gérées par les bases relationnelles

3. **Requêtes complexes** :
   - Recherche de conflits de dates pour les locations
   - Agrégations et comptages par état
   - JPA/SQL est plus performant pour ces opérations que MongoDB

4. **Transactions ACID** :
   - Cohérence critique : un contrat ne peut exister sans client ni véhicule
   - Isolation nécessaire : éviter les doubles réservations
   - JPA garantit ces propriétés transactionnelles

**Comparaison MongoDB vs JPA pour ce projet :**

| Critère | MongoDB | JPA/SQL | Choix |
|---------|---------|---------|-------|
| Relations | Document imbriqués ou références manuelles | Relations natives (FK) | ✅ JPA |
| Contraintes d'unicité | Index simples uniquement | Contraintes composées | ✅ JPA |
| Intégrité référentielle | À gérer manuellement | Automatique | ✅ JPA |
| Requêtes temporelles | Possible mais complexe | Natif avec SQL | ✅ JPA |
| Scalabilité horizontale | ✅ Excellent | Limité | MongoDB |
| Courbe d'apprentissage | Plus simple | Standard industriel | ✅ JPA |

**Conclusion** : Pour un système de gestion de locations avec des relations fortes et des contraintes métier strictes, JPA est le choix optimal.

---

### 2.2 Base de données H2 (en mémoire)

**✅ POURQUOI CE CHOIX ?**

1. **Développement rapide** : Pas besoin d'installer MySQL/PostgreSQL
2. **Tests simplifiés** : Base réinitialisée à chaque démarrage
3. **Console web intégrée** : Visualisation facile des données via `/h2-console`
4. **Migration facile** : Le code JPA fonctionne avec n'importe quelle base SQL (PostgreSQL, MySQL, etc.)

**Configuration pour la production** :
```properties
# Remplacer H2 par PostgreSQL en production
spring.datasource.url=jdbc:postgresql://localhost:5432/bfb_automobile
spring.datasource.username=bfb_user
spring.datasource.password=secure_password
spring.jpa.hibernate.ddl-auto=validate
```

---

## 3. Patterns et Techniques Utilisés

### 3.1 Pattern Repository (Spring Data JPA)

**Qu'est-ce que c'est ?**
Le pattern Repository abstrait l'accès aux données. Au lieu d'écrire du SQL manuellement, Spring Data JPA génère automatiquement les implémentations.

**Exemple :**
```java
public interface ClientRepository extends JpaRepository<Client, Long> {
    // Méthode générée automatiquement par Spring Data
    Optional<Client> findByNumeroPermis(String numeroPermis);
    
    // Spring comprend : SELECT * FROM clients WHERE numero_permis = ?
}
```

**✅ AVANTAGES :**
- **Gain de temps** : Pas besoin d'écrire les requêtes CRUD de base
- **Sécurité** : Protection automatique contre les injections SQL
- **Type-safe** : Erreurs détectées à la compilation
- **Testabilité** : Facile à mocker dans les tests

### 3.2 Conventions de nommage Spring Data

Spring Data JPA utilise des **conventions de nommage** pour générer automatiquement les requêtes :

| Méthode | SQL généré |
|---------|------------|
| `findByNom(String nom)` | `SELECT * FROM clients WHERE nom = ?` |
| `findByNomAndPrenom(...)` | `WHERE nom = ? AND prenom = ?` |
| `findByAgeGreaterThan(int age)` | `WHERE age > ?` |
| `existsByNumeroPermis(...)` | `SELECT COUNT(*) > 0 FROM ...` |
| `countByEtat(EtatContrat etat)` | `SELECT COUNT(*) FROM ... WHERE etat = ?` |

**✅ POURQUOI ?** Lisibilité maximale + Génération automatique = Productivité

### 3.3 Requêtes personnalisées avec @Query

Pour les requêtes complexes, on utilise JPQL (Java Persistence Query Language) :

```java
@Query("SELECT c FROM Contrat c WHERE c.vehicule.id = :vehiculeId " +
       "AND c.etat NOT IN ('ANNULE', 'TERMINE') " +
       "AND ((c.dateDebut <= :dateFin AND c.dateFin >= :dateDebut))")
List<Contrat> findContratsConflictuels(
    @Param("vehiculeId") Long vehiculeId,
    @Param("dateDebut") LocalDate dateDebut,
    @Param("dateFin") LocalDate dateFin);
```

**✅ POURQUOI JPQL et pas SQL pur ?**
- Indépendant de la base de données
- Travaille avec des objets Java, pas des tables SQL
- Support de l'autocomplétion IDE

---

## 4. Modèle de Données - Décisions Architecturales

### 4.1 Entité Client

**Attributs ajoutés au-delà du cahier des charges :**
- `id` : Clé primaire technique (Long auto-incrémenté)
- `dateCreation` : Traçabilité (quand le client a été créé)
- `actif` : Soft delete (désactivation sans suppression)

**✅ POURQUOI ces ajouts ?**
1. **ID technique** : Sépare l'identité technique de l'identité métier
   - Permet de modifier nom/prénom sans casser les relations
   - Performance des jointures (Long vs String composite)

2. **Soft delete** : En production, on ne supprime JAMAIS vraiment un client
   - Conservation de l'historique des contrats
   - Conformité RGPD (archivage)
   - Possibilité de réactivation

**Contraintes d'unicité :**
```java
@UniqueConstraint(name = "uk_client_identity", 
                 columnNames = {"nom", "prenom", "date_naissance"})
@UniqueConstraint(name = "uk_client_permis", 
                 columnNames = {"numero_permis"})
```

**✅ POURQUOI au niveau base de données ?**
- Garantie d'unicité même en cas de concurrence (2 requêtes simultanées)
- Performance : index automatique sur ces colonnes
- Documentation : le schéma de base indique clairement les règles

### 4.2 Entité Véhicule

**Énumération EtatVehicule :**
```java
public enum EtatVehicule {
    DISPONIBLE, EN_LOCATION, EN_PANNE
}
```

**✅ POURQUOI une énumération ?**
- **Type-safe** : Impossible de mettre un état invalide
- **Maintenabilité** : Changement d'état centralisé
- **Performance** : Stocké comme String en base (lisible dans les requêtes SQL)

**Méthodes utiles ajoutées :**
```java
public boolean estDisponible() {
    return this.etat == EtatVehicule.DISPONIBLE;
}
```

**✅ POURQUOI ?** Encapsulation de la logique métier dans l'entité (Domain-Driven Design)

### 4.3 Entité Contrat

**Relations JPA :**
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "client_id", nullable = false)
private Client client;
```

**✅ POURQUOI FetchType.LAZY ?**
- **Performance** : Ne charge le client que si nécessaire
- **Évite les N+1 queries** : Chargement optimisé avec `JOIN FETCH` si besoin

**Index ajoutés :**
```java
@Index(name = "idx_contrat_dates", columnList = "date_debut, date_fin")
```

**✅ POURQUOI ?**
- Requêtes de recherche de conflits très fréquentes
- Index composite sur les dates = recherche ultra-rapide

**Méthodes métier :**
```java
public boolean chevauche(LocalDate debut, LocalDate fin) {
    return !(this.dateFin.isBefore(debut) || this.dateDebut.isAfter(fin));
}
```

**✅ POURQUOI ?** Logique de chevauchement encapsulée, testable unitairement

---

## 5. Gestion des Contraintes Métier

### Stratégie à deux niveaux :

1. **Niveau Base de Données** : Contraintes d'unicité
   - Dernier rempart contre les doublons
   - Protection contre les erreurs de code

2. **Niveau Application** : Validation dans les services
   - Retours d'erreurs explicites à l'utilisateur
   - Logique métier complexe (ex: âge minimum 18 ans)

**✅ POURQUOI les deux ?**
- **Défense en profondeur** : Double sécurité
- **Expérience utilisateur** : Messages d'erreur clairs
- **Performance** : Validation rapide côté application avant d'aller en base

---

## 6. Évolutions Futures Facilitées

### Migration vers une vraie base de données :
```properties
# Passer de H2 à PostgreSQL : 3 lignes à changer !
spring.datasource.url=jdbc:postgresql://...
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate
```

### Ajout d'audit automatique :
```java
@EntityListeners(AuditingEntityListener.class)
public class Client {
    @CreatedDate
    private LocalDateTime createdDate;
    
    @LastModifiedDate
    private LocalDateTime lastModifiedDate;
}
```

---

## 7. Résumé des Points Clés

| Décision | Justification |
|----------|---------------|
| **JPA au lieu de MongoDB** | Relations complexes, contraintes d'unicité, transactions ACID |
| **H2 en mémoire** | Développement rapide, migration facile vers prod |
| **Repository Pattern** | Abstraction, génération auto des requêtes, type-safety |
| **Enums pour les états** | Type-safety, maintenabilité, lisibilité |
| **Soft delete** | Conservation historique, conformité légale |
| **Index sur dates** | Performance des recherches de conflits |
| **FetchType.LAZY** | Optimisation des performances |
| **Contraintes DB + validation app** | Défense en profondeur, UX + sécurité |

---

## 8. Points d'Attention pour la Soutenance

**Questions probables :**

1. **"Pourquoi JPA et pas NoSQL ?"**
   → Relations fortes, contraintes strictes, ACID requis

2. **"H2 c'est pas pour la prod, non ?"**
   → Exact ! En prod : PostgreSQL. H2 = dev/test uniquement

3. **"Pourquoi des ID techniques auto-générés ?"**
   → Séparation identité technique/métier, performance, évolutivité

4. **"Comment gérez-vous les conflits de réservation ?"**
   → Index sur dates + requête JPQL optimisée + transaction isolée

5. **"Et la scalabilité ?"**
   → JPA scale verticalement. Si besoin horizontal : sharding ou cache Redis

---

**Préparé pour démontrer une maîtrise approfondie de l'architecture de données !** 🎯
