# GitHub Repository Info API

Aplikacja Spring Boot, która pobiera publiczne (nieforkowane) repozytoria użytkownika GitHub oraz zwraca ich branże z SHA ostatniego commita.

Funkcjonalność

- Pobieranie publicznych repozytoriów danego użytkownika z GitHub (pomijając forki)
- Dla każdego repozytorium zwracane są:
  - nazwa brancha
  - SHA ostatniego commita
- Obsługa błędów:
  - 404 – użytkownik nie istnieje
  - 500 – problem z komunikacją z API GitHuba

Technologie

- Java 17
- Spring Boot
- REST API
- RestTemplate
- Lombok

Endpointy REST

GET `/api/github/users/{username}/repositories`

**Opis:** Zwraca listę publicznych repozytoriów użytkownika GitHub wraz z branżami.

Przykład żądania:
```http
GET /api/github/users/octocat/repositories
```

Przykład odpowiedzi:
```json
[
  {
    "repositoryName": "Hello-World",
    "ownerLogin": "octocat",
    "branches": [
      {
        "name": "main",
        "lastCommitSha": "f5a0e9d..."
      }
    ]
  }
]
```
Błędy:

- `404 Not Found`
```json
{
  "status": 404,
  "message": "GitHub user not found: someUnknownUser"
}
```

- `500 Internal Server Error`
```json
{
  "status": 500,
  "message": "Error communicating with GitHub API"
}
```
Konfiguracja

W pliku `application.properties` (lub `.yml`) należy skonfigurować adresy endpointów API GitHuba lub ich mocka:

```properties
github.api.url=https://api.github.com/users/{username}/repos
github.api.branches-url=https://api.github.com/repos/{owner}/{repo}/branches
```
Uruchamianie aplikacji

```bash
./mvnw spring-boot:run
```

Aplikacja będzie dostępna pod adresem: [http://localhost:8080](http://localhost:8080)

Testowanie


## 🧪 Przykład testowania lokalnego z WireMock

Można uruchomić mocka na porcie `9561` i użyć adresów:

```properties
github.api.url=http://localhost:9561/users/{username}/repos
github.api.branches-url=http://localhost:9561/repos/{owner}/{repo}/branches
```

---

Struktura katalogów

```
.
├── client              # Komunikacja z GitHub API
├── controller          # REST kontrolery
├── dto                 # DTO do odpowiedzi
├── exception           # Obsługa błędów
├── service             # Warstwa biznesowa
├── AppApiApplication   # Klasa główna
```
