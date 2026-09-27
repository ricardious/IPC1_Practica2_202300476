# UDrive

UDrive is a Java desktop application for managing routes and simulating trips
with up to three drivers at the same time. It implements IPC1 Practice 2 using
the original NetBeans interface from 2024, with its unfinished functionality
completed.

## Features

- Import routes from CSV files using `JFileChooser`.
- Edit the distance of an imported route.
- Choose an origin, destination, and one of nine vehicles.
- Assign one of three available drivers automatically.
- Start individual trips or all prepared trips at once.
- Simulate concurrent trips with duration proportional to distance.
- Track fuel consumption by vehicle type and refuel when needed.
- Finish at the destination or start the return trip manually.
- Review trip history with dates, route, vehicle, driver, distance, and fuel use.
- Save routes, trips, history, and fleet state automatically in a binary file.

## Requirements

- JDK 21 or newer.
- No separate Maven installation is needed; the repository includes Maven Wrapper.

## Run

On Linux or macOS:

```bash
./mvnw clean package
java -jar target/UDrive.jar
```

On Windows:

```powershell
mvnw.cmd clean package
java -jar target/UDrive.jar
```

The executable JAR includes its required dependencies.

## Test

```bash
./mvnw test
```

## CSV format

The first row may contain a header. Each route must have exactly three columns:

```csv
Inicio,Fin,Distancia
Ciudad de Guatemala,Antigua Guatemala,45
Antigua Guatemala,Escuintla,68
```

A ready-to-use file is available at [`examples/rutas.csv`](examples/rutas.csv).
Routes can be traveled in either direction.

## Persistence

The application saves its state through Java serialization in
`data/udrive-state.bin`. The directory is created when needed and is excluded
from version control. If the application closes during a trip, the trip resumes
from its saved position the next time it opens.

## Documentation

- [User manual](docs/Manual%20de%20Usuario.pdf)
- [Technical manual](docs/Manual%20T%C3%A9cnico.pdf)
- [Assignment brief](docs/Practica2_IPC1.pdf)
