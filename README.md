# Plataforma de Gestión Médica — Backend

API REST para la gestión de citas médicas, doctores, pacientes y consultorios en una clínica universitaria.

---

## Tecnologías

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
- Docker (para levantar PostgreSQL)
- Maven 3.9+

---

## Base de datos

Levantar PostgreSQL con Docker:

```bash
docker run -d \
  --name plataformamedicos-db \
  -e POSTGRES_DB=plataformamedicos \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16
```

Detener:

```bash
docker stop plataformamedicos-db
docker rm plataformamedicos-db
```

---

## Ejecución

```bash
# Compilar (salteando tests)
mvn clean install -DskipTests

# Iniciar (requiere PostgreSQL corriendo)
mvn spring-boot:run
```

El servidor arranca en `http://localhost:8080`.

También se puede abrir el proyecto en IntelliJ IDEA y ejecutar la clase `PlataformamedicosApplication`.

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
