-- =============================================================================
-- SIRA — Esquema de base de datos
-- Ejecutar sobre una base de datos PostgreSQL vacía.
-- Nota: Spring Boot puede generar este esquema automáticamente a partir de las
--       entidades JPA. Este archivo sirve como referencia y para recreaciones
--       manuales controladas.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Usuarios y perfiles
-- -----------------------------------------------------------------------------

CREATE TABLE usuario (
    id                      SERIAL          PRIMARY KEY,
    nombre                  VARCHAR(150)    NOT NULL,
    email                   VARCHAR(150)    NOT NULL,
    password_hash           VARCHAR(255)    NOT NULL,
    rol                     VARCHAR(20)     NOT NULL,
    activo                  BOOLEAN         NOT NULL DEFAULT TRUE,
    requiere_cambio_password BOOLEAN        NOT NULL DEFAULT FALSE,
    creado_en               TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_usuario_email UNIQUE (email)
);

CREATE TABLE administrador (
    id              SERIAL      PRIMARY KEY,
    usuario_id      INTEGER     NOT NULL,
    num_empleado    VARCHAR(20) NOT NULL,

    CONSTRAINT uq_admin_usuario     UNIQUE (usuario_id),
    CONSTRAINT uq_admin_empleado    UNIQUE (num_empleado),
    CONSTRAINT fk_admin_usuario     FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE
);

CREATE TABLE maestro (
    id              SERIAL      PRIMARY KEY,
    usuario_id      INTEGER     NOT NULL,
    num_empleado    VARCHAR(20) NOT NULL,

    CONSTRAINT uq_maestro_usuario   UNIQUE (usuario_id),
    CONSTRAINT uq_maestro_empleado  UNIQUE (num_empleado),
    CONSTRAINT fk_maestro_usuario   FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE
);

CREATE TABLE alumno (
    id              SERIAL      PRIMARY KEY,
    usuario_id      INTEGER     NOT NULL,
    matricula       VARCHAR(20) NOT NULL,

    CONSTRAINT uq_alumno_usuario    UNIQUE (usuario_id),
    CONSTRAINT uq_alumno_matricula  UNIQUE (matricula),
    CONSTRAINT fk_alumno_usuario    FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE
);

-- -----------------------------------------------------------------------------
-- Catálogo académico
-- -----------------------------------------------------------------------------

CREATE TABLE materia (
    id              SERIAL          PRIMARY KEY,
    clave           VARCHAR(20)     NOT NULL,
    nombre          VARCHAR(150)    NOT NULL,
    total_unidades  INTEGER         NOT NULL,

    CONSTRAINT uq_materia_clave UNIQUE (clave)
);

CREATE TABLE unidad (
    id          SERIAL          PRIMARY KEY,
    materia_id  INTEGER         NOT NULL,
    numero      INTEGER         NOT NULL,
    nombre      VARCHAR(100)    NOT NULL,

    CONSTRAINT uq_unidad_materia_numero UNIQUE (materia_id, numero),
    CONSTRAINT fk_unidad_materia        FOREIGN KEY (materia_id) REFERENCES materia (id) ON DELETE CASCADE
);

CREATE TABLE actividad_catalogo (
    id          SERIAL          PRIMARY KEY,
    nombre      VARCHAR(150)    NOT NULL,
    descripcion TEXT,
    activo      BOOLEAN         NOT NULL DEFAULT TRUE,

    CONSTRAINT uq_actividad_catalogo_nombre UNIQUE (nombre)
);

-- -----------------------------------------------------------------------------
-- Grupos e inscripciones
-- -----------------------------------------------------------------------------

CREATE TABLE grupo (
    id                              SERIAL          PRIMARY KEY,
    materia_id                      INTEGER         NOT NULL,
    maestro_id                      INTEGER         NOT NULL,
    clave                           VARCHAR(20)     NOT NULL,
    semestre                        VARCHAR(50)     NOT NULL,
    activo                          BOOLEAN         NOT NULL DEFAULT TRUE,
    estado_evaluacion               VARCHAR(20)     NOT NULL DEFAULT 'ABIERTO',
    calificacion_minima_aprobatoria NUMERIC(5,2)    NOT NULL DEFAULT 70.00,
    calificacion_maxima             NUMERIC(5,2)    NOT NULL DEFAULT 100.00,

    CONSTRAINT uq_grupo_clave_materia_semestre  UNIQUE (clave, materia_id, semestre),
    CONSTRAINT fk_grupo_materia                 FOREIGN KEY (materia_id)  REFERENCES materia  (id),
    CONSTRAINT fk_grupo_maestro                 FOREIGN KEY (maestro_id)  REFERENCES maestro  (id)
);

CREATE TABLE inscripcion (
    id                          SERIAL          PRIMARY KEY,
    alumno_id                   INTEGER         NOT NULL,
    grupo_id                    INTEGER         NOT NULL,
    fecha                       DATE            NOT NULL,
    calificacion_final_calculada    NUMERIC(5,2),
    estado_academico            VARCHAR(20)     NOT NULL DEFAULT 'PENDIENTE',
    calificacion_final_override NUMERIC(5,2),
    override_justificacion      TEXT,

    CONSTRAINT uq_inscripcion_alumno_grupo  UNIQUE (alumno_id, grupo_id),
    CONSTRAINT fk_inscripcion_alumno        FOREIGN KEY (alumno_id)  REFERENCES alumno  (id),
    CONSTRAINT fk_inscripcion_grupo         FOREIGN KEY (grupo_id)   REFERENCES grupo   (id)
);

CREATE TABLE actividad_grupo (
    id                      SERIAL          PRIMARY KEY,
    grupo_id                INTEGER         NOT NULL,
    unidad_id               INTEGER         NOT NULL,
    actividad_catalogo_id   INTEGER,
    etiqueta                VARCHAR(100),
    nombre                  VARCHAR(150),
    ponderacion             NUMERIC(5,2)    NOT NULL,
    creado_en               TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_actgrp_grupo      FOREIGN KEY (grupo_id)              REFERENCES grupo              (id) ON DELETE CASCADE,
    CONSTRAINT fk_actgrp_unidad     FOREIGN KEY (unidad_id)             REFERENCES unidad             (id),
    CONSTRAINT fk_actgrp_catalogo   FOREIGN KEY (actividad_catalogo_id) REFERENCES actividad_catalogo (id)
);

CREATE TABLE resultado (
    id                  SERIAL          PRIMARY KEY,
    inscripcion_id      INTEGER         NOT NULL,
    actividad_grupo_id  INTEGER         NOT NULL,
    calificacion        NUMERIC(5,2),
    modificado_en       TIMESTAMP,

    CONSTRAINT uq_resultado_insc_activ  UNIQUE (inscripcion_id, actividad_grupo_id),
    CONSTRAINT fk_resultado_inscripcion FOREIGN KEY (inscripcion_id)     REFERENCES inscripcion    (id) ON DELETE CASCADE,
    CONSTRAINT fk_resultado_actividad   FOREIGN KEY (actividad_grupo_id) REFERENCES actividad_grupo (id) ON DELETE CASCADE
);

CREATE TABLE estado_unidad (
    id              SERIAL      PRIMARY KEY,
    grupo_id        INTEGER     NOT NULL,
    unidad_id       INTEGER     NOT NULL,
    estado          VARCHAR(20) NOT NULL DEFAULT 'ABIERTA',
    actualizado_en  TIMESTAMP,

    CONSTRAINT uq_estado_unidad_grupo_unidad    UNIQUE (grupo_id, unidad_id),
    CONSTRAINT fk_estadounidad_grupo            FOREIGN KEY (grupo_id)  REFERENCES grupo  (id) ON DELETE CASCADE,
    CONSTRAINT fk_estadounidad_unidad           FOREIGN KEY (unidad_id) REFERENCES unidad (id)
);

CREATE TABLE bonus (
    id              SERIAL          PRIMARY KEY,
    inscripcion_id  INTEGER         NOT NULL,
    unidad_id       INTEGER,
    tipo            VARCHAR(10)     NOT NULL,
    puntos          NUMERIC(5,2)    NOT NULL,
    justificacion   TEXT,
    otorgado_en     TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_bonus_inscripcion FOREIGN KEY (inscripcion_id) REFERENCES inscripcion (id) ON DELETE CASCADE,
    CONSTRAINT fk_bonus_unidad      FOREIGN KEY (unidad_id)      REFERENCES unidad      (id)
);

-- -----------------------------------------------------------------------------
-- Configuración del sistema
-- -----------------------------------------------------------------------------

CREATE TABLE configuracion (
    clave       VARCHAR(50)     PRIMARY KEY,
    valor       VARCHAR(100),
    descripcion TEXT
);

-- -----------------------------------------------------------------------------
-- Índices de rendimiento
-- -----------------------------------------------------------------------------

CREATE INDEX idx_inscripcion_grupo     ON inscripcion      (grupo_id);
CREATE INDEX idx_inscripcion_alumno    ON inscripcion      (alumno_id);
CREATE INDEX idx_resultado_inscripcion ON resultado         (inscripcion_id);
CREATE INDEX idx_resultado_actividad   ON resultado         (actividad_grupo_id);
CREATE INDEX idx_actgrp_grupo          ON actividad_grupo   (grupo_id);
CREATE INDEX idx_actgrp_unidad         ON actividad_grupo   (unidad_id);
CREATE INDEX idx_estadounidad_grupo    ON estado_unidad     (grupo_id);
CREATE INDEX idx_bonus_inscripcion     ON bonus             (inscripcion_id);
CREATE INDEX idx_grupo_maestro         ON grupo             (maestro_id);
CREATE INDEX idx_grupo_materia         ON grupo             (materia_id);
