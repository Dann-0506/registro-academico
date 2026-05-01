# Plan de Migración: SIRA Desktop → Web

**Fecha:** 2026-05-01  
**Estado:** En planeación  
**Proyecto:** Sistema Institucional de Registro Académico (SIRA)

---

## Contexto

SIRA es una aplicación de escritorio JavaFX con arquitectura MVC+DAO, PostgreSQL y lógica de negocio completa (servicios, DAOs, modelos, tests). El objetivo es migrarla a una arquitectura web moderna:

- **Backend:** Spring Boot REST API (ya inicializado en `backend/`)
- **Frontend:** React + TypeScript (directorio `frontend/` reservado)

**Lo que se reutiliza:** toda la lógica de negocio (servicios, modelos, cálculo de calificaciones, BCrypt, OpenCSV, iTextPDF).  
**Lo que se reemplaza:** JavaFX UI, SessionManagerUtil (singleton → JWT), JDBC/HikariCP manual (→ Spring Data JPA), RBAC en cliente (→ RBAC en servidor).

---

## Arquitectura objetivo

```
┌─────────────────────────────────────────────────────────┐
│                    Cliente (Browser)                    │
│              React + TypeScript + shadcn/ui             │
└──────────────────────────┬──────────────────────────────┘
                           │ HTTPS + JWT
┌──────────────────────────▼──────────────────────────────┐
│               Backend (Spring Boot 4.0)                 │
│   REST API · Spring Security · Spring Data JPA          │
└──────────────────────────┬──────────────────────────────┘
                           │ JDBC
┌──────────────────────────▼──────────────────────────────┐
│                    PostgreSQL 14+                       │
│              (mismo esquema, sin cambios)               │
└─────────────────────────────────────────────────────────┘
```

---

## Fases de migración

### Fase 1 — Backend REST API
**Duración estimada:** 3–4 semanas  
**Directorio:** `backend/`

#### 1.1 Entidades JPA
Migrar los 15 POJOs de `com.academico.model` a entidades JPA en `com.sira.model`.

| POJO original | Entidad JPA |
|---|---|
| `Usuario` | `@Entity Usuario` |
| `Maestro` | `@Entity Maestro` (hereda / `@OneToOne Usuario`) |
| `Alumno` | `@Entity Alumno` (`@OneToOne Usuario`) |
| `Materia` | `@Entity Materia` |
| `Unidad` | `@Entity Unidad` (`@ManyToOne Materia`) |
| `Grupo` | `@Entity Grupo` (`@ManyToOne Materia, Maestro`) |
| `Inscripcion` | `@Entity Inscripcion` (`@ManyToOne Alumno, Grupo`) |
| `ActividadGrupo` | `@Entity ActividadGrupo` (`@ManyToOne Grupo, Unidad`) |
| `Resultado` | `@Entity Resultado` (`@ManyToOne Inscripcion, ActividadGrupo`) |
| `Bonus` | `@Entity Bonus` (`@ManyToOne Inscripcion, Unidad`) |
| `EstadoUnidad` | `@Entity EstadoUnidad` (`@ManyToOne Grupo, Unidad`) |
| `Configuracion` | `@Entity Configuracion` |
| `CalificacionFinal` | DTO (no tabla propia) |
| `ResultadoUnidad` | DTO (no tabla propia) |
| `Respaldo` | DTO (no entidad JPA) |

> El esquema SQL no cambia. `spring.jpa.hibernate.ddl-auto=validate` en producción.

#### 1.2 Repositorios Spring Data
Reemplazar los 13 DAOs (JDBC puro) por interfaces `JpaRepository`. Las consultas complejas se migran como `@Query` JPQL.

```
UsuarioRepository       AlumnoRepository        MaestroRepository
MateriaRepository       UnidadRepository        GrupoRepository
InscripcionRepository   ResultadoRepository     ActividadGrupoRepository
BonusRepository         EstadoUnidadRepository  ConfiguracionRepository
AdminRepository (→ métodos en otros repos)
```

#### 1.3 Servicios Spring
Migrar los 19 servicios a `@Service` beans. La lógica de negocio se reutiliza íntegra; solo cambian las inyecciones de DAOs por repositorios.

```
AuthService             CalificacionService     ReporteService
CargaDatosService       EstructuraAcademicaService  AnalisisService
ExportadorPdfService
+ 12 servicios individuales por entidad
```

#### 1.4 Autenticación y seguridad (Spring Security + JWT)
Reemplazar `SessionManagerUtil` (singleton global, patrón desktop) por autenticación stateless.

**Dependencias a agregar:**
- `spring-boot-starter-security`
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson` (io.jsonwebtoken)

**Componentes nuevos:**
- `JwtUtil` — generación y validación de tokens
- `JwtFilter` — filtro de Spring Security que extrae y valida el JWT en cada request
- `SecurityConfig` — configuración de cadena de filtros, rutas públicas vs. protegidas
- `UserDetailsServiceImpl` — carga usuario desde BD para Spring Security

**Flujo:**
```
POST /api/auth/login
  → AuthService.login(email, password)
  → BCrypt.verify()
  → JwtUtil.generateToken(usuario)
  → { token, rol, nombre }

Requests posteriores:
  Header: Authorization: Bearer <token>
  → JwtFilter extrae y valida token
  → SecurityContext cargado con usuario y roles
```

**RBAC server-side** (crítico: en desktop el control estaba en el cliente):
- `@PreAuthorize("hasRole('ADMIN')")` en endpoints y métodos de servicio
- Roles mapeados: `ROLE_ADMIN`, `ROLE_MAESTRO`, `ROLE_ALUMNO`

#### 1.5 Controladores REST
Un `@RestController` por dominio funcional:

| Controlador | Endpoints principales | Roles |
|---|---|---|
| `AuthController` | `POST /login`, `POST /logout` | Público |
| `AlumnosController` | CRUD `/api/alumnos` | ADMIN |
| `MaestrosController` | CRUD `/api/maestros` | ADMIN |
| `MateriasController` | CRUD `/api/materias` | ADMIN |
| `GruposController` | CRUD `/api/grupos` | ADMIN |
| `InscripcionesController` | CRUD `/api/inscripciones` | ADMIN |
| `ConfiguracionController` | `GET/PUT /api/configuracion` | ADMIN |
| `ActividadesController` | CRUD `/api/grupos/{id}/actividades` | MAESTRO |
| `CalificacionesController` | `GET/POST /api/grupos/{id}/calificaciones` | MAESTRO |
| `BonusController` | `POST /api/grupos/{id}/bonus` | MAESTRO |
| `ReportesController` | `GET /api/grupos/{id}/reporte/pdf` | MAESTRO, ADMIN |
| `CargaController` | `POST /api/carga/csv` | ADMIN |
| `RespaldosController` | `GET/POST /api/respaldos` | ADMIN |
| `AlumnoController` | `GET /api/mis-cursos`, `/api/mis-calificaciones` | ALUMNO |

#### 1.6 Funcionalidades auxiliares
- **PDF:** `iTextPDF` funciona en Spring Boot sin cambios → endpoint devuelve `application/pdf`
- **CSV:** `OpenCSV` funciona en Spring Boot → endpoint acepta `multipart/form-data`
- **Respaldos:** `pg_dump` / `pg_restore` vía `ProcessBuilder` → endpoint admin

#### 1.7 Configuración y CORS
- `application.properties` con perfiles: `dev`, `prod`
- `CorsConfig` que permite origin del frontend en desarrollo (`http://localhost:5173`)
- Variables de entorno via `spring-dotenv` (ya configurado)

---

### Fase 2 — Frontend React
**Duración estimada:** 4–5 semanas  
**Directorio:** `frontend/`

#### Stack tecnológico
| Herramienta | Propósito |
|---|---|
| React 19 + TypeScript | Framework UI |
| Vite | Build tool |
| React Router v7 | Navegación SPA |
| shadcn/ui + Tailwind CSS | Componentes UI |
| TanStack Query | Fetching y caché de datos |
| React Hook Form + Zod | Formularios y validación |
| Axios | Cliente HTTP |

#### Estructura de vistas (espejo de los 20 FXML)
```
src/
  pages/
    auth/         Login.tsx
    admin/
      Dashboard.tsx       Alumnos.tsx       Maestros.tsx
      Materias.tsx        Grupos.tsx        Inscripciones.tsx
      Configuracion.tsx   Respaldos.tsx     CargaCSV.tsx
    maestro/
      Dashboard.tsx       MisGrupos.tsx     Actividades.tsx
      Calificaciones.tsx  Bonus.tsx         Analisis.tsx
    alumno/
      Dashboard.tsx       MisCursos.tsx     MisCalificaciones.tsx
  components/
    layout/       Sidebar.tsx, Header.tsx, PrivateRoute.tsx
    ui/           (shadcn/ui components)
    tables/       DataTable.tsx (reutilizable)
    forms/        (formularios por entidad)
  hooks/
    useAuth.ts    usePagination.ts
  services/
    api.ts        auth.ts     alumnos.ts    ...
  store/
    authStore.ts  (Zustand o Context API)
```

#### Autenticación web
- Login → JWT almacenado en `httpOnly cookie` (recomendado) o `localStorage`
- `PrivateRoute` verifica rol antes de renderizar
- Axios interceptor agrega `Authorization: Bearer <token>` automáticamente
- Refresh automático de token antes de expirar

---

### Fase 3 — Paridad de funcionalidades
**Duración estimada:** 2 semanas

| Funcionalidad desktop | Equivalente web |
|---|---|
| Diálogo de archivo para PDF | Endpoint REST → `window.open()` o `<a download>` |
| Diálogo de archivo para CSV | `<input type="file">` → upload multipart |
| Respaldo local de BD | Descarga de archivo `.sql` desde endpoint |
| Restauración de BD | Upload de `.sql` → endpoint admin |
| Sesión persistente | JWT con refresh token (7 días) |
| Impresión de boleta | PDF generado server-side → visualización en browser |

---

### Fase 4 — Testing y despliegue
**Duración estimada:** 1–2 semanas

#### Tests backend
- Migrar los 6 tests existentes a `@SpringBootTest`
- Tests de integración con base de datos PostgreSQL de test (o Testcontainers)
- Tests de controladores con `MockMvc`

#### Tests frontend
- Tests de componentes con Vitest + Testing Library
- Tests E2E con Playwright para flujos críticos:
  - Login y redirección por rol
  - Registro de calificaciones
  - Generación de PDF

#### Despliegue
```yaml
# docker-compose.yml
services:
  db:       PostgreSQL 14
  backend:  Spring Boot JAR → puerto 8080
  frontend: Nginx sirviendo build de React → puerto 80
```

---

## Orden de implementación recomendado

```
Sprint 1 (sem 1-2):  Spring Security + JWT → login funcional vía REST
Sprint 2 (sem 3-4):  Entidades JPA + repositorios + servicios básicos
Sprint 3 (sem 5-6):  Controladores REST (CRUD admin) + CORS
Sprint 4 (sem 7-8):  Frontend: login + vistas admin completas
Sprint 5 (sem 9-10): Controladores de maestro (calificaciones, bonus)
Sprint 6 (sem 11-12): Frontend: vistas de maestro
Sprint 7 (sem 13):   PDF, CSV, respaldos
Sprint 8 (sem 14):   Frontend: vistas de alumno
Sprint 9 (sem 15):   Testing E2E + Docker + QA final
```

---

## Riesgos y decisiones críticas

| Riesgo | Mitigación |
|---|---|
| RBAC en cliente (actual) → servidor (web) | Todos los endpoints deben validar rol; usar `@PreAuthorize` sistemáticamente |
| Sesión global (`SessionManagerUtil`) es incompatible con stateless | Eliminar completamente; reemplazar por SecurityContext de Spring |
| Tests existentes usan mocks de DAO | Reescribir con Mockito sobre repositorios JPA o usar Testcontainers |
| `pg_dump` para respaldos requiere binario en servidor | Documentar como requisito de despliegue; alternativa: pg_dump vía Docker |
| Manejo de archivos (PDF, CSV) en servidor sin estado | Generar en memoria y devolver como stream; no guardar en disco |

---

## Estado de avance

- [x] Análisis de la app original (JavaFX)
- [x] Definición de arquitectura objetivo
- [x] Plan de migración documentado
- [x] **Fase 1:** Backend REST API ✅ completada
  - [x] S1: pom.xml + 12 entidades JPA
  - [x] S2: 12 repositorios JPA con @Query personalizadas
  - [x] S3: Spring Security + JWT + AuthController
  - [x] S4: Servicios y controladores CRUD del administrador (6 entidades)
  - [x] S5: CalificacionService, ReporteService, servicios y controladores del maestro
  - [x] S6: Vistas del alumno, PDF, CSV, respaldos, análisis, fix cierre de acta
- [ ] **Fase 2:** Frontend React
- [ ] **Fase 3:** Paridad de funcionalidades (cubierta en Fase 1)
- [ ] **Fase 4:** Testing y despliegue
