# SIRA — Sistema Institucional de Registro Académico

Sistema web para gestionar el ciclo completo de evaluación académica: alumnos, maestros, materias, grupos, actividades, calificaciones y reportes.

## Arquitectura

```
SIRA/
├── backend/    Spring Boot 4 · REST API · Java 21
└── frontend/   React 19 · TypeScript · Vite
```

El backend expone una API REST protegida con JWT. El frontend consume esa API y renderiza las vistas según el rol del usuario autenticado.

## Stack

| Capa | Tecnología |
|---|---|
| API | Spring Boot 4 · Spring Security · Spring Data JPA |
| Base de datos | PostgreSQL 14+ |
| Autenticación | JWT (JJWT 0.12) · BCrypt |
| Frontend | React 19 · TypeScript · Vite · Tailwind CSS |
| Estado cliente | TanStack Query · Zustand |

## Roles

| Rol | Acceso |
|---|---|
| **Administrador** | Gestión completa: alumnos, maestros, materias, grupos, inscripciones, configuración, carga CSV y análisis. Puede reabrir grupos y cerrar definitivamente. |
| **Maestro** | Operación de sus grupos activos: actividades y rúbrica por unidad, registro de calificaciones, bonus, reporte y cierre de evaluación. |
| **Alumno** | Consulta de sus cursos y calificaciones. Solo lectura. |

## Flujo de evaluación

```
Grupo ABIERTO
  └─ Maestro configura actividades y ponderaciones por unidad
  └─ Maestro registra calificaciones (auto-guardado por campo)
  └─ Maestro cierra cada unidad (requiere calificaciones completas)
  └─ Maestro termina la evaluación (requiere todas las unidades cerradas)
Grupo CERRADO  ←──── Admin puede reabrir
  └─ Admin cierra definitivamente (irreversible)
Grupo FINALIZADO
```

## Requisitos

- Java 21+
- Maven 3.9+
- Node.js 20+
- PostgreSQL 14+

## Configuración

### 1. Clonar el repositorio

```bash
git clone <url-del-repositorio>
cd SIRA
```

### 2. Variables de entorno

Crea `backend/.env` (ya está en `.gitignore`):

```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=sira
DB_USER=postgres
DB_PASSWORD=tu_contraseña
DB_URL=jdbc:postgresql://localhost:5432/sira

JWT_SECRET=clave-secreta-de-al-menos-32-caracteres
JWT_EXPIRATION_MS=604800000

CORS_ALLOWED_ORIGINS=http://localhost:5173
```

### 3. Crear la base de datos

```bash
psql -U postgres -c "CREATE DATABASE sira;"
```

El esquema se crea automáticamente al arrancar el backend por primera vez (`ddl-auto=update`).

## Ejecución

### Backend

```bash
cd backend
mvn spring-boot:run
# API disponible en http://localhost:8080
```

### Frontend

```bash
cd frontend
npm install
npm run dev
# App disponible en http://localhost:5173
```

## Credenciales por defecto

| Campo | Valor |
|---|---|
| Email | `admin@escuela.edu` |
| Contraseña | `123456` |
| Rol | Administrador |

> Los usuarios nuevos reciben como contraseña temporal su número de empleado o matrícula, y se les solicita cambiarla en el primer inicio de sesión.

## Colaboradores

- Daniel Landero Arias
- Ximena Zaleta Hernández
