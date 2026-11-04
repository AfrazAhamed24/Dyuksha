# Dyuksha

**AI-Native Resource Allocation System for Multi-Agency Emergency Response**

Dyuksha coordinates police, fire services, hospitals, and rescue teams during emergencies. It is designed to eventually include AI-based resource allocation, real-time emergency monitoring, and flood detection.

This repository contains the Dyuksha emergency response platform.

---

## Technology Stack

| Layer        | Technology                  |
|--------------|-----------------------------|
| Language     | Java 21                     |
| Framework    | Spring Boot 3.3.5           |
| Build        | Maven 3.9 + Maven Wrapper   |
| Templating   | Thymeleaf                   |
| Persistence  | Spring Data JPA + H2        |
| Dev tools    | Spring Boot DevTools        |

---

## Prerequisites

- **Java 21** or later — [Adoptium](https://adoptium.net)
- **No Maven installation required** — the project includes the Maven wrapper (`mvnw` / `mvnw.cmd`)

Verify your Java version:

```bash
java -version
```

---

## Running Locally

### Unix / macOS

```bash
# Make the wrapper executable (first time only)
chmod +x mvnw

# Start the application
./mvnw spring-boot:run
```

### Windows (PowerShell)

```powershell
.\mvnw.cmd spring-boot:run
```

The application starts on **http://localhost:8080**.

---

## Building an Executable JAR

```bash
# Unix / macOS
./mvnw clean package

# Windows
.\mvnw.cmd clean package
```

The JAR is created at `target/dyuksha-0.1.0-SNAPSHOT.jar`.

Run it directly:

```bash
java -jar target/dyuksha-0.1.0-SNAPSHOT.jar
```

---

## Configuration

All settings are in `backend/src/main/resources/application.properties`.

Key settings can be overridden with environment variables:

| Environment Variable | Default   | Description               |
|----------------------|-----------|---------------------------|
| `PORT`               | `8080`    | HTTP port                 |
| `DB_USERNAME`        | `dyuksha` | H2 database username      |
| `DB_PASSWORD`        | *(empty)* | H2 database password      |

Example (Unix):

```bash
PORT=9090 ./mvnw spring-boot:run
```

---

## H2 Database Console

The embedded H2 console is available during development at:

**http://localhost:8080/h2-console**

| Field    | Value                                                   |
|----------|---------------------------------------------------------|
| JDBC URL | `jdbc:h2:file:./data/dyuksha-db`                        |
| Username | `dyuksha` (or value of `DB_USERNAME`)                   |
| Password | *(leave blank by default)*                              |

Database files are stored in the `data/` directory (excluded from Git).

---

## Project Structure

```
dyuksha/
├── backend/
│   └── src/
│       ├── main/java/com/dyuksha/       # Spring Boot backend
│       ├── main/resources/              # Backend configuration
│       └── test/java/com/dyuksha/       # Backend tests
├── frontend/
│   ├── templates/                       # Thymeleaf HTML templates
│   └── static/                          # CSS, JavaScript, and images
├── .mvn/wrapper/                         # Maven wrapper files
├── pom.xml
├── mvnw                                  # Unix wrapper
├── mvnw.cmd                              # Windows wrapper
├── .gitignore
└── README.md
```

---

## Development Phases

| Phase | Description                        | Status      |
|-------|------------------------------------|-------------|
| 1     | Project foundation & landing page  | ✅ Complete |
| 2     | Core domain models & dashboard     | Planned     |
| 3     | Resource allocation engine         | Planned     |
| 4     | AI agent integration               | Planned     |
| 5     | Flood detection & monitoring       | Planned     |

---

## License

Proprietary — all rights reserved.
