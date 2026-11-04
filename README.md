# Dyuksha

**AI-native emergency response command center for coordinating police, fire services, hospitals, ambulances, and rescue teams.**

Dyuksha is a Spring Boot application. The frontend is served by the backend using Thymeleaf, so no separate frontend server is required.

## Quick start

### Windows PowerShell

```powershell
.\mvnw.cmd spring-boot:run
```

### macOS/Linux

```bash
chmod +x mvnw
./mvnw spring-boot:run
```

Open **http://localhost:8080** in your browser.

## Requirements

- Java 21 or later
- Docker (optional)
- Maven is not required because the project includes the Maven Wrapper

Check Java:

```bash
java -version
```

## Application URLs

| URL | Purpose |
|---|---|
| `/` | Command center dashboard |
| `/emergencies` | Emergency management |
| `/resources` | Resource management |
| `/allocation` | Resource allocation |
| `/h2-console` | H2 database console for local development |

## Configuration

Configuration is stored in `backend/src/main/resources/application.properties`.

The application automatically uses Render's assigned port:

```properties
server.port=${PORT:8080}
```

If `PORT` is not set, it uses port `8080`.

| Variable | Default | Description |
|---|---:|---|
| `PORT` | `8080` | HTTP port used by Spring Boot and Render |
| `DB_USERNAME` | `dyuksha` | H2 database username |
| `DB_PASSWORD` | empty | H2 database password |

Example:

```powershell
$env:PORT=9090
.\mvnw.cmd spring-boot:run
```

```bash
PORT=9090 ./mvnw spring-boot:run
```

## Database

Dyuksha uses a file-based H2 database:

```text
jdbc:h2:file:./data/dyuksha-db;AUTO_SERVER=TRUE
```

For the local H2 console, use:

| Field | Value |
|---|---|
| JDBC URL | `jdbc:h2:file:./data/dyuksha-db` |
| User | `dyuksha`, or the value of `DB_USERNAME` |
| Password | The value of `DB_PASSWORD`, empty by default |

The local database is stored in `data/`, which is ignored by Git. Render's filesystem is temporary unless a persistent disk is configured, so configure persistent storage if production data must survive redeployments.

## Docker

Build the image:

```bash
docker build -t dyuksha .
```

Run it:

```bash
docker run --rm -p 8080:8080 dyuksha
```

Run it on another port:

```bash
docker run --rm -e PORT=9090 -p 9090:9090 dyuksha
```

The root `Dockerfile` builds the application with Maven and runs it on the `PORT` environment variable.

## Deploying to Render

### Docker deployment

1. Create a new **Web Service** in Render.
2. Connect this repository.
3. Select **Docker** as the runtime.
4. Deploy.

Render provides `PORT` automatically, and the application reads it from `server.port=${PORT:8080}`.

### Maven deployment

Use these settings if deploying without Docker:

| Setting | Value |
|---|---|
| Runtime | Java |
| Build command | `./mvnw clean package -DskipTests` |
| Start command | `java -jar target/dyuksha-0.1.0-SNAPSHOT.jar` |

## Build, test, and run the JAR

### Windows PowerShell

```powershell
.\mvnw.cmd clean package
.\mvnw.cmd test
java -jar target\dyuksha-0.1.0-SNAPSHOT.jar
```

### macOS/Linux

```bash
./mvnw clean package
./mvnw test
java -jar target/dyuksha-0.1.0-SNAPSHOT.jar
```

## Project structure

```text
dyuksha/
├── backend/
│   └── src/
│       ├── main/java/com/dyuksha/       # Controllers, services, models, repositories
│       ├── main/resources/              # Spring configuration
│       └── test/java/com/dyuksha/       # Backend tests
├── frontend/
│   ├── templates/                       # Thymeleaf HTML pages and fragments
│   └── static/                          # CSS and JavaScript
├── Dockerfile                           # Multi-stage production image
├── pom.xml                              # Maven build configuration
├── mvnw / mvnw.cmd                      # Maven Wrapper scripts
├── data/                                # Local H2 data, ignored by Git
└── README.md
```

The Maven configuration maps `backend/src` and `frontend/` into the standard Spring Boot build, so the existing Maven and Docker commands continue to work after the folder organization.

## Technology

- Java 21
- Spring Boot 3.3.5
- Spring MVC and Thymeleaf
- Spring Data JPA
- H2
- Maven
- Leaflet for the live operational map

## License

Proprietary — all rights reserved.
