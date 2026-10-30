# JanDrishti

JanDrishti is a JavaFX desktop prototype for monitoring civic infrastructure issues across India. It gives officers a single view of issue volume, hotspots, workflow status, and citizen verification.

## Run

Requirements:

- JDK 21+
- Maven 3.9+
- MySQL 8+ for persistence (optional for the demo UI)

Run the demo dashboard with:

```bash
mvn javafx:run
```

The first version starts with realistic in-memory data so the interface can be reviewed without database credentials. The persistence boundary is `IssueRepository`, and `database/schema.sql` contains the MySQL schema for the next integration step.

## Product flow

Citizen report -> Area -> City -> District -> State -> India

Issue workflow: Reported -> Verified -> Assigned -> Resolved

## Structure

- `src/main/java/in/jandrishti/model`: domain records and status values
- `src/main/java/in/jandrishti/data`: repository contracts and demo data
- `src/main/java/in/jandrishti/ui`: JavaFX dashboard composition
- `src/main/resources/styles.css`: visual system
- `database/schema.sql`: MySQL tables and starter aggregation views
