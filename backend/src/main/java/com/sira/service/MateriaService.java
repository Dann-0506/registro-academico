package com.sira.service;

import com.sira.model.Materia;
import com.sira.model.Unidad;
import com.sira.repository.MateriaRepository;
import com.sira.repository.UnidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class MateriaService {

    @Autowired private MateriaRepository materiaRepository;
    @Autowired private UnidadRepository unidadRepository;

    @Transactional(readOnly = true)
    public List<Materia> listarTodas() {
        return materiaRepository.findAllByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public Materia buscarPorId(Integer id) {
        return materiaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Materia no encontrada con id: " + id));
    }

    @Transactional(readOnly = true)
    public Materia buscarPorClave(String clave) {
        return materiaRepository.findByClave(clave)
                .orElseThrow(() -> new NoSuchElementException("Materia no encontrada con clave: " + clave));
    }

    @Transactional
    public Materia crear(String clave, String nombre, int totalUnidades, List<String> nombresUnidades) {
        validarCampos(clave, nombre, totalUnidades);
        if (materiaRepository.existsByClave(clave)) {
            throw new IllegalStateException("La clave de materia '" + clave + "' ya existe.");
        }
        Materia materia = materiaRepository.save(new Materia(clave.trim().toUpperCase(), nombre.trim(), totalUnidades));

        for (int i = 1; i <= totalUnidades; i++) {
            String nombreU = (nombresUnidades != null && nombresUnidades.size() >= i && !nombresUnidades.get(i - 1).isBlank())
                    ? nombresUnidades.get(i - 1).trim()
                    : "Unidad " + i;
            unidadRepository.save(new Unidad(materia, i, nombreU));
        }
        return materiaRepository.findById(materia.getId()).orElseThrow();
    }

    @Transactional
    public Materia actualizar(Integer id, String nombre) {
        Materia materia = buscarPorId(id);
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la materia es obligatorio.");
        }
        materia.setNombre(nombre.trim());
        return materiaRepository.save(materia);
    }

    @Transactional
    public void eliminar(Integer id) {
        Materia materia = buscarPorId(id);
        materiaRepository.delete(materia);
    }

    private void validarCampos(String clave, String nombre, int totalUnidades) {
        if (clave == null || clave.isBlank()) {
            throw new IllegalArgumentException("La clave de la materia es obligatoria.");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la materia es obligatorio.");
        }
        if (totalUnidades <= 0 || totalUnidades > 15) {
            throw new IllegalArgumentException("El total de unidades debe estar entre 1 y 15.");
        }
    }
}
