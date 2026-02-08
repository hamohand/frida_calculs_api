# ⚡ Quick Start - API Frida Calculs

## 🎯 En 3 minutes

### 1️⃣ Lancer l'application (30 secondes)

```bash
cd frida_calculs_api
mvn spring-boot:run
```

Attendez le message: `Started FridaCalculsAppApplication`

### 2️⃣ Tester l'API (30 secondes)

**Ouvrir Swagger UI dans votre navigateur:**
```
http://localhost:8080/swagger-ui.html
```

**Ou tester avec cURL:**
```bash
curl http://localhost:8080/api/v1/heritage/status
```

### 3️⃣ Faire votre premier calcul (1 minute)

**Dans Swagger UI:**
1. Cliquer sur `POST /api/v1/heritage/calculate`
2. Cliquer sur "Try it out"
3. Utiliser l'exemple par défaut ou modifier les valeurs
4. Cliquer sur "Execute"
5. Voir le résultat !

**Ou avec cURL:**
```bash
curl -X POST http://localhost:8080/api/v1/heritage/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "sexeDefunt": "M",
    "conjointVivant": true,
    "nbFilles": 1,
    "nbGarcons": 1
  }'
```

**Résultat attendu:**
```json
{
  "calculId": "...",
  "timestamp": "2025-12-21T...",
  "heritiers": [
    {
      "heritier": "conjoint",
      "part": { "numerateur": 3, "denominateur": 24 }
    },
    {
      "heritier": "fille",
      "part": { "numerateur": 7, "denominateur": 24 }
    },
    {
      "heritier": "garçon",
      "part": { "numerateur": 14, "denominateur": 24 }
    }
  ],
  "nombreHeritiers": 3,
  "calculComplet": true,
  "message": "Calcul des parts d'héritage effectué avec succès"
}
```

---

## 🔥 Commandes Essentielles

### Développement

```bash
# Lancer en mode dev
mvn spring-boot:run

# Lancer avec debug
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"

# Recompiler à chaud (DevTools actif)
# Juste sauvegarder les fichiers, le rechargement est automatique
```

### Build

```bash
# Compiler rapidement (sans tests)
mvn clean package -DskipTests

# Build complet avec tests
mvn clean package

# Compiler sans créer le JAR
mvn compile
```

### Tests

```bash
# Tous les tests
mvn test

# Tests spécifiques
mvn test -Dtest=CalculPartsServiceTest
mvn test -Dtest=CalculsPartsControllerIntegrationTest

# Skip tests
mvn install -DskipTests
```

### Docker

```bash
# Build image
docker build -t frida-calculs-api .

# Run container
docker run -d -p 8080:8080 --name frida-api frida-calculs-api

# Voir logs
docker logs -f frida-api

# Arrêter et supprimer
docker stop frida-api && docker rm frida-api
```

---

## 🎨 URLs Importantes

| URL | Description |
|-----|-------------|
| http://localhost:8080/swagger-ui.html | **Swagger UI** - Documentation interactive |
| http://localhost:8080/api-docs | **OpenAPI JSON** - Spec API |
| http://localhost:8080/api/v1/heritage/status | **Status** - Santé de l'API |
| http://localhost:8080/actuator/health | **Health Check** |
| http://localhost:8080/actuator/info | **Info** Application |

---

## 🧪 Exemples de Tests Rapides

### 1. Défunt masculin avec épouse et enfants

```json
{
  "sexeDefunt": "M",
  "conjointVivant": true,
  "nbFilles": 2,
  "nbGarcons": 1
}
```

### 2. Défunte féminine avec parents

```json
{
  "sexeDefunt": "F",
  "pereVivant": true,
  "mereVivante": true
}
```

### 3. Cas complexe

```json
{
  "sexeDefunt": "M",
  "conjointVivant": true,
  "pereVivant": true,
  "mereVivante": true,
  "nbFilles": 2,
  "nbGarcons": 1
}
```

### 4. Test d'erreur (sexe invalide)

```json
{
  "sexeDefunt": "X",
  "conjointVivant": true
}
```

**Résultat attendu:** Erreur 400 avec message clair

### 5. Test d'erreur (nombre négatif)

```json
{
  "sexeDefunt": "M",
  "nbFilles": -1
}
```

**Résultat attendu:** Erreur 400 avec détails de validation

---

## 📝 Cheat Sheet

### Arrêter l'application

```bash
# Si lancé avec mvn spring-boot:run
Ctrl+C

# Si lancé avec java -jar
ps aux | grep frida
kill [PID]
```

### Changer le port

**Fichier:** `src/main/resources/application.properties`
```properties
server.port=9090
```

**Ou via variable d'environnement:**
```bash
export SERVER_PORT=9090  # Linux/Mac
set SERVER_PORT=9090     # Windows
mvn spring-boot:run
```

### Activer le debug logging

**Fichier:** `application.properties`
```properties
logging.level.com.med.frida_calculs_app=DEBUG
```

**Ou temporairement:**
```bash
mvn spring-boot:run -Dlogging.level.com.med.frida_calculs_app=DEBUG
```

### Nettoyer complètement

```bash
mvn clean
rm -rf target/
rm -rf ~/.m2/repository/com/med/frida-calculs-api/
```

---

## 🆘 Problèmes Fréquents

### "Port 8080 already in use"

```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID [PID] /F

# Linux/Mac
lsof -i :8080
kill -9 [PID]

# Ou changer le port (voir ci-dessus)
```

### "JAVA_HOME not defined"

```bash
# Windows
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot

# Linux/Mac
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
```

### Application démarre mais ne répond pas

1. Vérifier que l'application a bien démarré:
   ```bash
   curl http://localhost:8080/actuator/health
   ```

2. Vérifier les logs pour des erreurs

3. Vérifier le pare-feu

### Tests échouent

```bash
# Skip temporairement
mvn package -DskipTests

# Voir détails
mvn test -X
```

---

## 📚 Documentation Complète

Pour plus de détails, consulter:

- 📖 [README.md](README.md) - Vue d'ensemble complète
- 📘 [GUIDE_UTILISATION.md](GUIDE_UTILISATION.md) - Guide utilisateur détaillé
- 📗 [API_DOCUMENTATION.md](API_DOCUMENTATION.md) - Référence API complète
- 📙 [AMELIORATIONS.md](AMELIORATIONS.md) - Liste des améliorations
- 📕 [VERIFICATION_BUILD.md](VERIFICATION_BUILD.md) - Guide de build/déploiement

---

## ✅ Checklist Première Utilisation

- [ ] Java 21 installé (`java -version`)
- [ ] Maven 3.9+ installé (`mvn -version`)
- [ ] Projet téléchargé/cloné
- [ ] Dans le bon répertoire (`cd frida_calculs_api`)
- [ ] Application lancée (`mvn spring-boot:run`)
- [ ] Message "Started FridaCalculsAppApplication" visible
- [ ] Status OK (`curl http://localhost:8080/api/v1/heritage/status`)
- [ ] Swagger UI accessible (http://localhost:8080/swagger-ui.html)
- [ ] Premier calcul testé ✨

---

<div align="center">

**🎉 Vous êtes prêt ! Bonne utilisation ! 🎉**

[📖 Documentation](README.md) | [🐛 Support](GUIDE_UTILISATION.md)

</div>
