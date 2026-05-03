-- =============================================================================
-- SIRA — Constraints de negocio y triggers de integridad
-- Ejecutar después de 01_schema.sql (o después de que Spring genere el esquema).
-- =============================================================================

-- -----------------------------------------------------------------------------
-- CHECK constraints — valores permitidos y rangos
-- -----------------------------------------------------------------------------

ALTER TABLE usuario
    ADD CONSTRAINT chk_usuario_rol
        CHECK (rol IN ('ADMIN', 'MAESTRO', 'ALUMNO'));

ALTER TABLE materia
    ADD CONSTRAINT chk_materia_total_unidades
        CHECK (total_unidades >= 1);

ALTER TABLE grupo
    ADD CONSTRAINT chk_grupo_estado_evaluacion
        CHECK (estado_evaluacion IN ('ABIERTO', 'CERRADO')),
    ADD CONSTRAINT chk_grupo_cal_minima_rango
        CHECK (calificacion_minima_aprobatoria BETWEEN 0 AND 100),
    ADD CONSTRAINT chk_grupo_cal_maxima_rango
        CHECK (calificacion_maxima BETWEEN 0 AND 100),
    ADD CONSTRAINT chk_grupo_cal_minima_menor_maxima
        CHECK (calificacion_minima_aprobatoria < calificacion_maxima);

ALTER TABLE inscripcion
    ADD CONSTRAINT chk_inscripcion_estado_academico
        CHECK (estado_academico IN ('PENDIENTE', 'APROBADO', 'REPROBADO')),
    ADD CONSTRAINT chk_inscripcion_cal_calculada_rango
        CHECK (calificacion_final_calculada IS NULL OR calificacion_final_calculada BETWEEN 0 AND 100),
    ADD CONSTRAINT chk_inscripcion_cal_override_rango
        CHECK (calificacion_final_override IS NULL OR calificacion_final_override BETWEEN 0 AND 100),
    ADD CONSTRAINT chk_inscripcion_override_justificacion
        CHECK (calificacion_final_override IS NULL OR (override_justificacion IS NOT NULL AND override_justificacion <> ''));

ALTER TABLE actividad_grupo
    ADD CONSTRAINT chk_actgrp_ponderacion_positiva
        CHECK (ponderacion > 0),
    ADD CONSTRAINT chk_actgrp_ponderacion_rango
        CHECK (ponderacion <= 100),
    ADD CONSTRAINT chk_actgrp_nombre_o_catalogo
        CHECK (actividad_catalogo_id IS NOT NULL OR (nombre IS NOT NULL AND nombre <> ''));

ALTER TABLE resultado
    ADD CONSTRAINT chk_resultado_calificacion_rango
        CHECK (calificacion IS NULL OR calificacion BETWEEN 0 AND 100);

ALTER TABLE estado_unidad
    ADD CONSTRAINT chk_estadounidad_estado
        CHECK (estado IN ('ABIERTA', 'CERRADA'));

ALTER TABLE bonus
    ADD CONSTRAINT chk_bonus_tipo
        CHECK (tipo IN ('unidad', 'materia')),
    ADD CONSTRAINT chk_bonus_puntos_positivos
        CHECK (puntos > 0),
    ADD CONSTRAINT chk_bonus_unidad_requerida_si_tipo_unidad
        CHECK (tipo <> 'unidad' OR unidad_id IS NOT NULL);

-- -----------------------------------------------------------------------------
-- Trigger 1: Bloquear inscripción en grupo cerrado
-- -----------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION fn_bloquear_inscripcion_grupo_cerrado()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF (SELECT estado_evaluacion FROM grupo WHERE id = NEW.grupo_id) = 'CERRADO' THEN
        RAISE EXCEPTION 'No se puede inscribir un alumno en un grupo con acta cerrada.';
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_bloquear_inscripcion_grupo_cerrado
    BEFORE INSERT ON inscripcion
    FOR EACH ROW EXECUTE FUNCTION fn_bloquear_inscripcion_grupo_cerrado();

-- -----------------------------------------------------------------------------
-- Trigger 2: Bloquear eliminación de inscripción en grupo cerrado
-- -----------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION fn_bloquear_eliminar_inscripcion_cerrada()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF (SELECT estado_evaluacion FROM grupo WHERE id = OLD.grupo_id) = 'CERRADO' THEN
        RAISE EXCEPTION 'No se puede eliminar una inscripción de un grupo con acta cerrada.';
    END IF;
    RETURN OLD;
END;
$$;

CREATE TRIGGER trg_bloquear_eliminar_inscripcion_cerrada
    BEFORE DELETE ON inscripcion
    FOR EACH ROW EXECUTE FUNCTION fn_bloquear_eliminar_inscripcion_cerrada();

-- -----------------------------------------------------------------------------
-- Trigger 3: Bloquear modificación de resultados en grupo cerrado
-- -----------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION fn_bloquear_resultado_grupo_cerrado()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE
    v_grupo_id INTEGER;
BEGIN
    SELECT ag.grupo_id INTO v_grupo_id
    FROM actividad_grupo ag
    WHERE ag.id = COALESCE(NEW.actividad_grupo_id, OLD.actividad_grupo_id);

    IF (SELECT estado_evaluacion FROM grupo WHERE id = v_grupo_id) = 'CERRADO' THEN
        RAISE EXCEPTION 'No se pueden modificar calificaciones de un grupo con acta cerrada.';
    END IF;
    RETURN COALESCE(NEW, OLD);
END;
$$;

CREATE TRIGGER trg_bloquear_resultado_insert_grupo_cerrado
    BEFORE INSERT ON resultado
    FOR EACH ROW EXECUTE FUNCTION fn_bloquear_resultado_grupo_cerrado();

CREATE TRIGGER trg_bloquear_resultado_update_grupo_cerrado
    BEFORE UPDATE ON resultado
    FOR EACH ROW EXECUTE FUNCTION fn_bloquear_resultado_grupo_cerrado();

CREATE TRIGGER trg_bloquear_resultado_delete_grupo_cerrado
    BEFORE DELETE ON resultado
    FOR EACH ROW EXECUTE FUNCTION fn_bloquear_resultado_grupo_cerrado();

-- -----------------------------------------------------------------------------
-- Trigger 4: Validar que unidad_id de actividad_grupo pertenece a la materia del grupo
-- -----------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION fn_validar_unidad_pertenece_a_materia_grupo()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE
    v_materia_grupo  INTEGER;
    v_materia_unidad INTEGER;
BEGIN
    SELECT materia_id INTO v_materia_grupo  FROM grupo  WHERE id = NEW.grupo_id;
    SELECT materia_id INTO v_materia_unidad FROM unidad WHERE id = NEW.unidad_id;

    IF v_materia_grupo <> v_materia_unidad THEN
        RAISE EXCEPTION 'La unidad (id=%) no pertenece a la materia del grupo (id=%).', NEW.unidad_id, NEW.grupo_id;
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_validar_unidad_actividad_grupo
    BEFORE INSERT OR UPDATE ON actividad_grupo
    FOR EACH ROW EXECUTE FUNCTION fn_validar_unidad_pertenece_a_materia_grupo();

-- -----------------------------------------------------------------------------
-- Trigger 5: Validar que unidad_id de estado_unidad pertenece a la materia del grupo
-- -----------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION fn_validar_unidad_pertenece_a_materia_estadounidad()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE
    v_materia_grupo  INTEGER;
    v_materia_unidad INTEGER;
BEGIN
    SELECT materia_id INTO v_materia_grupo  FROM grupo  WHERE id = NEW.grupo_id;
    SELECT materia_id INTO v_materia_unidad FROM unidad WHERE id = NEW.unidad_id;

    IF v_materia_grupo <> v_materia_unidad THEN
        RAISE EXCEPTION 'La unidad (id=%) no pertenece a la materia del grupo (id=%).', NEW.unidad_id, NEW.grupo_id;
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_validar_unidad_estado_unidad
    BEFORE INSERT OR UPDATE ON estado_unidad
    FOR EACH ROW EXECUTE FUNCTION fn_validar_unidad_pertenece_a_materia_estadounidad();

-- -----------------------------------------------------------------------------
-- Trigger 6: Validar que resultado.calificacion no supera calificacion_maxima del grupo
-- -----------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION fn_validar_calificacion_maxima()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE
    v_cal_maxima NUMERIC(5,2);
BEGIN
    IF NEW.calificacion IS NULL THEN
        RETURN NEW;
    END IF;

    SELECT g.calificacion_maxima INTO v_cal_maxima
    FROM grupo g
    JOIN inscripcion i ON i.grupo_id = g.id
    WHERE i.id = NEW.inscripcion_id;

    IF NEW.calificacion > v_cal_maxima THEN
        RAISE EXCEPTION 'La calificación (%) supera el máximo permitido para este grupo (%).', NEW.calificacion, v_cal_maxima;
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_validar_calificacion_maxima
    BEFORE INSERT OR UPDATE ON resultado
    FOR EACH ROW EXECUTE FUNCTION fn_validar_calificacion_maxima();
