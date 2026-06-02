# Plataforma de Reservas de Consultorios Médicos Universitarios

API REST para la gestión de citas médicas universitarias, desarrollada con Java 21, Spring Boot, PostgreSQL y pruebas automatizadas con JUnit 5, Mockito y Testcontainers.

---

## Stack tecnológico

| Tecnología | Versión |
|---|---|
| Java | 21 |
| Spring Boot | 3.4.2 |
| PostgreSQL | 16 |
| Hibernate / JPA | — |
| JWT (jjwt) | 0.11.5 |
| Lombok | — |
| MapStruct | 1.6.3 |
| Testcontainers | 1.x |
| Maven | 3.9+ |

---

## Requisitos previos

- Java 21
- Docker (para PostgreSQL y Testcontainers)
- Maven 3.9+

---

## Estructura del proyecto

```
src/
├── main/
│   └── java/unimag/plataformamedicos/
│       ├── api/
│       │   ├── controllers/          # Controladores REST
│       │   ├── dtos/                 # DTOs agrupados por entidad
│       │   │   ├── query/            # Records internos para queries JPQL
│       │   │   ├── AppointmentDtos.java
│       │   │   ├── DoctorDtos.java
│       │   │   ├── PatientDtos.java
│       │   │   ├── OfficeDtos.java
│       │   │   ├── SpecialtyDtos.java
│       │   │   ├── AppointmentTypeDtos.java
│       │   │   ├── DoctorScheduleDtos.java
│       │   │   └── ReportDtos.java
│       │   └── mappers/              # Mappers estáticos entidad <-> DTO
│       ├── domine/
│       │   ├── entities/             # Entidades JPA
│       │   └── repositories/         # Repositorios Spring Data JPA
│       ├── enums/                    # Enums de dominio
│       ├── exception/                # Excepciones personalizadas
│       └── service/
│           ├── interfaces/           # Interfaces de service
│           └── impl/                 # Implementaciones de service
└── test/
    └── java/unimag/plataformamedicos/
        ├── api/controllers/          # Tests de controladores con Spring MVC
        ├── domine/repositories/      # Tests de integración con Testcontainers
        └── service/                  # Tests unitarios con Mockito
```

---

## Base de datos

### Diagrama Entidad-Relación

```mermaid
erDiagram
	direction TB
	PATIENT {
		UUID id PK
		string name
		string documentNumber
		string email
		string phone
		enum status
	}
	DOCTOR {
		UUID id PK
		string name
		string licenceNumber
		string email
		boolean active
		UUID specialty_id FK
	}
	SPECIALTY {
		UUID id PK
		string name
		string description
	}
	OFFICE {
		UUID id PK
		string name
		string location
		enum status
	}
	APPOINTMENT_TYPE {
		UUID id PK
		string name
		string description
		integer durationMinutes
	}
	DOCTOR_SCHEDULE {
		UUID id PK
		UUID doctor_id FK
		enum dayOfWeek
		time startTime
		time endTime
	}
	APPOINTMENT {
		UUID id PK
		UUID patient_id FK
		UUID doctor_id FK
		UUID office_id FK
		UUID appointment_type_id FK
		datetime startAt
		datetime endAt
		enum status
		string cancellationReason
		string observations
		datetime createdAt
		datetime updatedAt
	}
	PATIENT ||--o{ APPOINTMENT : "has"
	DOCTOR ||--o{ APPOINTMENT : "attends"
	OFFICE ||--o{ APPOINTMENT : "hosts"
	APPOINTMENT_TYPE ||--o{ APPOINTMENT : "defines"
	DOCTOR ||--o{ DOCTOR_SCHEDULE : "has"
	SPECIALTY ||--o{ DOCTOR : "classifies"
```

### Enums

| Enum | Valores |
|---|---|
| `AppointmentStatus` | `SCHEDULED`, `CONFIRMED`, `COMPLETED`, `CANCELLED`, `NO_SHOW` |
| `PatientStatus` | `ACTIVE`, `INACTIVE` |
| `OfficeStatus` | `AVAILABLE`, `INACTIVE` |

### Relaciones

- `Specialty` 1 → * `Doctor`
- `Doctor` 1 → * `DoctorSchedule`
- `Doctor` 1 → * `Appointment`
- `Patient` 1 → * `Appointment`
- `Office` 1 → * `Appointment`
- `AppointmentType` 1 → * `Appointment`

---

## Reglas de negocio

### Creación de citas

- No se puede crear una cita en una fecha y hora pasada.
- El paciente debe existir y estar en estado `ACTIVE`.
- El doctor debe existir y estar activo (`active = true`).
- El consultorio debe existir y estar en estado `AVAILABLE`.
- La cita debe quedar dentro del horario laboral configurado para el doctor en ese día de la semana.
- El campo `endAt` **no lo manda el cliente** — lo calcula el service usando `startAt + durationMinutes` del tipo de cita.
- No puede existir traslape de horario para el doctor en el mismo rango temporal.
- No puede existir traslape de horario para el consultorio en el mismo rango temporal.
- Un paciente no puede tener dos citas activas que se crucen en el tiempo.
- Toda cita nueva se crea con estado inicial `SCHEDULED`.

### Transiciones de estado

```
SCHEDULED ──► CONFIRMED ──► COMPLETED
    │               │
    └───────────────┴──► CANCELLED
                    │
                    └──► NO_SHOW
```

| Transición | Regla |
|---|---|
| `SCHEDULED → CONFIRMED` | Solo desde `SCHEDULED`. No se puede confirmar una cita cancelada, completada o marcada como `NO_SHOW`. |
| `SCHEDULED/CONFIRMED → CANCELLED` | Solo desde `SCHEDULED` o `CONFIRMED`. Requiere motivo de cancelación obligatorio. |
| `CONFIRMED → COMPLETED` | Solo desde `CONFIRMED`. La hora actual debe ser posterior al inicio programado. Permite registrar observaciones. |
| `CONFIRMED → NO_SHOW` | Solo desde `CONFIRMED`. No se puede marcar antes de la hora de inicio. |

### Disponibilidad y reportes

- La disponibilidad depende del horario laboral del doctor, de las citas existentes (`SCHEDULED` o `CONFIRMED`) y de la duración del tipo de cita.
- Los slots devueltos son únicamente bloques completos y libres — nunca aproximados.
- La ocupación de consultorios se calcula sumando los `durationMinutes` del tipo de cita de cada cita, no contando citas. Así una cita de 50 minutos pesa más que una de 20.
- La productividad de doctores se basa en el número de citas `COMPLETED`.
- Las inasistencias identifican pacientes con mayor cantidad de `NO_SHOW` en un período.

---

## Decisiones de diseño

### DTOs agrupados por clase contenedora

En vez de tener un archivo por cada DTO, los agrupamos en una clase por entidad usando records estáticos anidados:

```java
public class PatientDtos {
    public record CreatePatientRequest(...) {}
    public record UpdatePatientRequest(...) {}
    public record PatientResponse(...) {}
    public record PatientSummaryResponse(...) {}
}
```

Esto reduce el número de archivos y deja clara la relación entre los DTOs de una misma entidad.

### Summaries en AppointmentResponse

`AppointmentResponse` no anida los objetos completos de `Patient`, `Doctor`, etc., sino _summaries_ con solo los campos necesarios para mostrar una cita. Si el cliente necesita el detalle completo de un doctor hace `GET /api/doctors/{id}`.

### Mappers estáticos

Los mappers usan métodos estáticos en vez de inyección de dependencias. El método `patch()` aplica actualizaciones parciales — solo modifica los campos que llegan no nulos en el request.

### Sin update en Appointment

Decidimos no implementar un endpoint de update para `Appointment`. Si una cita necesita modificarse, se cancela con motivo y se crea una nueva. Esto garantiza trazabilidad completa del historial de citas.

### Sin update en AppointmentType

`AppointmentType` no tiene update para proteger la consistencia de los cálculos de `endAt`. Si se necesita una duración diferente se crea un nuevo tipo. Esto evita que citas ya agendadas queden con un `endAt` inconsistente.

### Separación de updates sensibles

Operaciones sobre campos únicos o sensibles tienen su propio DTO y endpoint:
- `UpdateDoctorLicenceRequest` → `PATCH /api/doctors/{id}/licence`
- `UpdatePatientDocumentRequest` → `PATCH /api/patients/{id}/document`

Así el service puede aplicar validaciones adicionales (verificar unicidad del nuevo valor) sin contaminar el update general.

### Cálculo de ocupación por minutos

El reporte de ocupación suma los `durationMinutes` del tipo de cita en vez de contar citas. El porcentaje se calcula en el service dividiendo los minutos ocupados entre los minutos totales del rango de fechas.

### Records internos para queries JPQL

Las queries de agregación devuelven records tipados en vez de `Object[]` para evitar casteos manuales:

```java
public record OfficeOccupancy(Office office, Long sumOccupiedMinutes) {}
public record DoctorAppointment(Doctor doctor, Long countCompletedAppointment) {}
public record PatientCountStatus(Patient patient, Long countNoShow) {}
public record SpecialtyStats(Specialty specialty, Long cancelled, Long noShow) {}
```

---

## Ejecución

```bash
docker compose up -d

# Compilar (saltando tests para no depender de Docker en CI)
mvn clean install -DskipTests

# Iniciar (requiere PostgreSQL corriendo)
mvn spring-boot:run
```

El servidor arranca en `http://localhost:8080`.

---

## Pruebas

```bash
# Todas las pruebas
mvn test

# Solo tests de integración (requiere Docker)
mvn test -Dtest="*IntegrationTest"

# Solo tests unitarios
mvn test -Dtest="*ServiceImplTest"
```

| Tipo | Cobertura |
|---|---|
| **Controller tests** | 45 tests — validación de endpoints, autenticación y autorización con Spring MVC |
| **Service tests** (unitarios) | 44 tests — lógica de negocio con Mockito |
| **Repository tests** (integración) | 32 tests — queries JPA con Testcontainers + PostgreSQL real |

### Tests de integración (Repository)

| Test | Qué cubre |
|---|---|
| `AppointmentRepositoryIntegrationTest` | Traslapes, ocupación, ranking de doctores, ranking de inasistencias, cancelados por especialidad |
| `PatientRepositoryIntegrationTest` | Búsqueda por estado |
| `DoctorRepositoryIntegrationTest` | Búsqueda por especialidad y estado activo |
| `DoctorScheduleRepositoryIntegrationTest` | Búsqueda por doctor y día de la semana |
| `OfficeRepositoryIntegrationTest` | Búsqueda por estado |

### Tests unitarios (Service)

| Test | Qué cubre |
|---|---|
| `AppointmentServiceImplTest` | Todas las reglas de creación, validaciones de estado, transiciones |
| `AvailabilityServiceImplTest` | Generación de slots, exclusión de ocupados, slots completos únicamente |
| `DoctorScheduleServiceImplTest` | Creación y consulta de horarios |
| `ReportServiceImplTest` | Cálculo de ocupación con porcentaje, productividad, ranking de inasistencias |

---

## Roles disponibles

| Rol | Descripción |
|---|---|
| `ADMIN` | Acceso completo a todos los módulos |
| `DOCTOR` | Gestión de citas propias, disponibilidad |
| `RECEPTIONIST` | Gestión de pacientes, citas, disponibilidad |

---

## Endpoints principales

### Auth
| Método | Ruta | Descripción | Acceso |
|---|---|---|---|
| POST | `/api/auth/login` | Iniciar sesión | Público |
| POST | `/api/auth/register` | Registrar usuario | ADMIN |

### Pacientes
| GET | `/api/patients` | Listar pacientes | Autenticado |
| GET | `/api/patients/{id}` | Obtener paciente | Autenticado |
| GET | `/api/patients/search` | Buscar por documento | Autenticado |
| POST | `/api/patients` | Crear paciente | Autenticado |
| PUT | `/api/patients/{id}` | Actualizar paciente | Autenticado |

### Doctores
| GET | `/api/doctors` | Listar doctores | Autenticado |
| GET | `/api/doctors/{id}` | Obtener doctor | Autenticado |
| GET | `/api/doctors/search` | Buscar por licencia | Autenticado |
| POST | `/api/doctors` | Crear doctor | ADMIN |
| PUT | `/api/doctors/{id}` | Actualizar doctor | ADMIN |

### Especialidades
| GET | `/api/specialties` | Listar especialidades | Autenticado |
| POST | `/api/specialties` | Crear especialidad | ADMIN |
| PUT | `/api/specialties/{id}` | Actualizar especialidad | ADMIN |
| PATCH | `/api/specialties/{id}/desactivar` | Desactivar especialidad | ADMIN |

### Tipos de cita
| GET | `/api/appointment-types` | Listar tipos | Autenticado |
| POST | `/api/appointment-types` | Crear tipo | ADMIN |
| PUT | `/api/appointment-types/{id}` | Actualizar tipo | ADMIN |
| PATCH | `/api/appointment-types/{id}/desactivar` | Desactivar tipo | ADMIN |

### Consultorios
| GET | `/api/offices` | Listar consultorios | Autenticado |
| POST | `/api/offices` | Crear consultorio | ADMIN |
| PUT | `/api/offices/{id}` | Actualizar consultorio | ADMIN |

### Horarios de doctores
| GET | `/api/doctors/{id}/schedules` | Obtener horarios | Autenticado |
| POST | `/api/doctors/{id}/schedules` | Agregar horario | ADMIN |
| PUT | `/api/doctors/{id}/schedules/{sid}` | Actualizar horario | ADMIN |
| DELETE | `/api/doctors/{id}/schedules/{sid}` | Eliminar horario | ADMIN |

### Citas
| GET | `/api/appointments?status=all` | Listar citas (filtro por estado) | Autenticado |
| GET | `/api/appointments/{id}` | Obtener cita | Autenticado |
| POST | `/api/appointments` | Crear cita | ADMIN, RECEPTIONIST |
| PUT | `/api/appointments/{id}/confirm` | Confirmar cita | ADMIN, RECEPTIONIST |
| PUT | `/api/appointments/{id}/cancel` | Cancelar cita | ADMIN, RECEPTIONIST |
| PUT | `/api/appointments/{id}/complete` | Completar cita | ADMIN, DOCTOR |
| PUT | `/api/appointments/{id}/no-show` | Marcar inasistencia | ADMIN, DOCTOR |

### Disponibilidad
| GET | `/api/availability/doctors/{id}?date=&appointmentTypeId=` | Slots disponibles | Autenticado |

### Reportes
| GET | `/api/reports/office-occupancy?start=&end=` | Ocupación de consultorios | ADMIN |
| GET | `/api/reports/doctor-productivity` | Productividad de doctores | ADMIN |
| GET | `/api/reports/no-show-patients?start=&end=` | Pacientes con inasistencias | ADMIN |

### Usuarios
| GET | `/api/users/roles` | Listar roles disponibles | Autenticado |
| POST | `/api/users` | Crear usuario | ADMIN |

---

## Credenciales por defecto

- **Email:** `admin@admin.com`
- **Contraseña:** `Admin1234`
- **Rol:** ADMIN
