# 🚀 Guide d'Utilisation - API Frida Calculs

## Démarrage Rapide (5 minutes)

### 1. Compiler et lancer l'application

```bash
# Aller dans le répertoire du projet
cd frida_calculs_api

# Compiler le projet (skip tests pour aller plus vite)
mvn clean package -DskipTests

# Ou avec les tests
mvn clean package

# Lancer l'application
mvn spring-boot:run
```

**L'API démarre sur:** http://localhost:8080

### 2. Vérifier que l'API fonctionne

```bash
# Test rapide
curl http://localhost:8080/api/v1/heritage/status
```

**Résultat attendu:**
```
API Frida Calculs - v1.0.0 - Opérationnelle ✓
```

### 3. Tester le calcul d'héritage

```bash
curl -X POST http://localhost:8080/api/v1/heritage/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "sexeDefunt": "M",
    "conjointVivant": true,
    "pereVivant": false,
    "mereVivante": false,
    "nbFilles": 1,
    "nbGarcons": 1
  }'
```

---

## 📊 Accès à la Documentation

### Swagger UI (Interface Interactive)

Ouvrez dans votre navigateur:
```
http://localhost:8080/swagger-ui.html
```

**Fonctionnalités:**
- Tester directement les endpoints
- Voir les schémas de données
- Exemples de requêtes
- Codes de retour documentés

### API Docs JSON

```
http://localhost:8080/api-docs
```

Format OpenAPI 3.0 pour import dans Postman, Insomnia, etc.

---

## 🔍 Monitoring et Health Checks

### Vérifier l'état de l'API

```bash
curl http://localhost:8080/actuator/health
```

**Résultat:**
```json
{
  "status": "UP"
}
```

### Voir les métriques

```bash
curl http://localhost:8080/actuator/metrics
```

### Informations sur l'application

```bash
curl http://localhost:8080/actuator/info
```

---

## 🧪 Lancer les Tests

### Tous les tests

```bash
mvn test
```

### Tests unitaires uniquement

```bash
mvn test -Dtest=CalculPartsServiceTest
```

### Tests d'intégration

```bash
mvn test -Dtest=CalculsPartsControllerIntegrationTest
```

### Tests avec rapport de couverture

```bash
mvn clean verify
# Le rapport sera dans: target/site/jacoco/index.html
```

---

## 🐳 Utilisation avec Docker

### Build de l'image Docker

```bash
docker build -t frida-calculs-api:1.0.0 .
```

### Lancer le container

```bash
docker run -d \
  -p 8080:8080 \
  --name frida-api \
  frida-calculs-api:1.0.0
```

### Voir les logs

```bash
docker logs -f frida-api
```

### Arrêter le container

```bash
docker stop frida-api
docker rm frida-api
```

---

## 📝 Exemples de Requêtes

### Exemple 1: Défunt masculin avec épouse et 2 enfants

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

### Exemple 2: Défunte féminine avec parents

```bash
curl -X POST http://localhost:8080/api/v1/heritage/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "sexeDefunt": "F",
    "pereVivant": true,
    "mereVivante": true
  }'
```

### Exemple 3: Cas complexe avec tous les héritiers

```bash
curl -X POST http://localhost:8080/api/v1/heritage/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "sexeDefunt": "M",
    "conjointVivant": true,
    "pereVivant": true,
    "mereVivante": true,
    "nbFilles": 2,
    "nbGarcons": 1
  }'
```

---

## 🛠️ Configuration

### Modifier le port

**Fichier:** `src/main/resources/application.properties`

```properties
server.port=9090  # Changer 8080 par le port souhaité
```

### Modifier les origins CORS

```properties
cors.allowed-origins=http://localhost:4200,http://monapp.com
```

### Activer/Désactiver Swagger

```properties
springdoc.swagger-ui.enabled=false  # Pour désactiver
```

### Changer le niveau de logging

```properties
logging.level.com.med.frida_calculs_app=DEBUG  # ou INFO, WARN, ERROR
```

---

## 🔧 Problèmes Courants

### L'application ne démarre pas

**Vérifier:**
1. Java 21 est installé
   ```bash
   java -version
   ```

2. Le port 8080 n'est pas déjà utilisé
   ```bash
   # Windows
   netstat -ano | findstr :8080

   # Linux/Mac
   lsof -i :8080
   ```

3. Maven est correctement configuré
   ```bash
   mvn -version
   ```

### Erreur de compilation

```bash
# Nettoyer et recompiler
mvn clean install -U
```

### Les tests échouent

```bash
# Voir les détails
mvn test -X

# Ignorer les tests temporairement
mvn clean install -DskipTests
```

---

## 📱 Intégration avec un Frontend

### React/Vue/Angular

```javascript
// Exemple avec Fetch API
const calculateHeritage = async (familyData) => {
  const response = await fetch('http://localhost:8080/api/v1/heritage/calculate', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(familyData)
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message);
  }

  return await response.json();
};

// Utilisation
try {
  const result = await calculateHeritage({
    sexeDefunt: 'M',
    conjointVivant: true,
    nbFilles: 2,
    nbGarcons: 1
  });

  console.log('Résultat:', result);
  console.log('Nombre d\'héritiers:', result.nombreHeritiers);
  console.log('Calcul complet:', result.calculComplet);
} catch (error) {
  console.error('Erreur:', error.message);
}
```

---

## 📦 Import dans Postman

1. Télécharger la spec OpenAPI:
   ```
   http://localhost:8080/api-docs
   ```

2. Dans Postman:
   - Import → Link
   - Coller l'URL ci-dessus
   - Importer

Tous les endpoints seront disponibles automatiquement !

---

## 📊 Interpréter les Résultats

### Lecture des fractions

```json
{
  "heritier": "fille",
  "part": {
    "numerateur": 7,
    "denominateur": 24
  }
}
```

**Signification:** La fille reçoit 7/24 de l'héritage total.

### Calcul du montant réel

Si l'héritage total = 240 000€:

```
Part de la fille = (7 / 24) × 240 000€ = 70 000€
```

### Part restante

```json
{
  "partRestante": {
    "numerateur": 0,
    "denominateur": 24
  },
  "calculComplet": true
}
```

**`calculComplet: true`** = Tout l'héritage a été distribué
**`calculComplet: false`** = Il reste une part non distribuée

---

## 📖 Documentation Complète

Pour plus de détails, consultez:

1. **API_DOCUMENTATION.md** - Documentation complète de l'API
2. **AMELIORATIONS.md** - Liste des améliorations apportées
3. **Swagger UI** - http://localhost:8080/swagger-ui.html

---

## 🆘 Support

### Logs de l'application

```bash
# Logs en temps réel
tail -f logs/spring.log

# Avec Maven
mvn spring-boot:run
# Les logs s'affichent dans la console
```

### Debug

Activer le mode debug dans `application.properties`:

```properties
logging.level.com.med.frida_calculs_app=DEBUG
logging.level.org.springframework.web=DEBUG
```

---

## 🎓 Pour aller plus loin

### Ajouter de nouveaux cas de figure

1. Modifier `Heritiers.java` pour ajouter les règles
2. Mettre à jour `CalculPartsService.java`
3. Ajouter des tests dans `CalculPartsServiceTest.java`
4. Documenter dans Swagger

### Déploiement en production

Voir la section "Production" dans API_DOCUMENTATION.md

### Contribuer

1. Fork le projet
2. Créer une branche feature
3. Commit les changements
4. Push et créer une Pull Request

---

## ✅ Checklist de Vérification

Avant de considérer l'API prête:

- [ ] L'API démarre sans erreur
- [ ] Swagger UI est accessible
- [ ] Les tests passent (mvn test)
- [ ] Le endpoint /status fonctionne
- [ ] Un calcul basique fonctionne
- [ ] Les erreurs de validation fonctionnent
- [ ] La documentation est à jour
- [ ] Docker build fonctionne (optionnel)

---

**Version:** 1.0.0
**Dernière mise à jour:** Décembre 2025

Bonne utilisation ! 🎉
