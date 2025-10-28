# BFB-automobil

Projet Spring Boot minimal pour le TP d'écoconception.

## Prérequis

- Java 17 (le projet est configuré pour `<java.version>17</java.version>` dans le `pom.xml`).
- Git (optionnel)
- Une instance MongoDB si vous utilisez la persistance MongoDB (par défaut : `mongodb://localhost:27017`).

## Lancer le projet (Windows PowerShell)

Les commandes ci-dessous sont adaptées à Windows PowerShell depuis la racine du projet.

1) Exécuter en mode développement (rechargement automatique avec Spring DevTools) :

```powershell
.\mvnw.cmd spring-boot:run
```

2) Construire le jar puis l'exécuter :

```powershell
.\mvnw.cmd clean package
java -jar target\automobile-0.0.1-SNAPSHOT.jar
```

Remarques :
- Vous pouvez aussi utiliser `mvn` local si Maven est installé : `mvn spring-boot:run` ou `mvn clean package`.
- Si votre MongoDB n'est pas sur `localhost:27017`, configurez la propriété `spring.data.mongodb.uri` dans `src/main/resources/application.properties` ou via une variable d'environnement.

## Accès

L'application démarre par défaut sur :

- http://localhost:8080

## Dépannage rapide

- Si le port 8080 est déjà utilisé, changez `server.port` dans `application.properties`.
- Si le build échoue, vérifiez la version de Java (`java -version`) et que vous avez accès au dépôt Maven central.

---

Si vous voulez, je peux ajouter une section « tests » ou des instructions pour lancer l'application dans Docker.