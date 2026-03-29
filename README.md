# GainTrack

GainTrack je web aplikacija za praćenje fitness aktivnosti i treninga. Korisnicima omogućuje bilježenje treninga, upravljanje vježbama, pregled povijesti treninga, praćenje osobnih rekorda i statistike napretka.

## Product Goal

Cilj projekta GainTrack je razviti jednostavnu i preglednu web aplikaciju koja korisnicima omogućuje evidentiranje treninga, praćenje napretka i pregled statistika kako bi lakše planirali i unaprijedili svoje rezultate.

## Tehnologije

### Backend
- Java 25
- Spring Boot 4.0.5
- Postgres 18

### Frontend
- MVC with Thymeleaf


## Upute za razvojne inženjere

### Requirements

- Java 25
- Docker

### Razvoj

- Pokretanje i zaustavljanje baze (iz project root foldera!):
  - `docker compose up -d`
  - `docker compose down`

- Pokretanje aplikacije: `./mvnw spring-boot:run`

- Pokretanje testova: `./mvnw test`

- Brisanje build outputa: `./mvnw clean`

- Buildanje artefakta (finalne .jar datoteke): `./mvnw verify`


## Uloge
- Product Owner: Aleksander Radovan
- Scrum Master: Alen Bogadi
- Developeri: Marko Peša, Antonela Miletić
