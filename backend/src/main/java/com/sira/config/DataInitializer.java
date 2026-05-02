package com.sira.config;

import com.sira.model.Administrador;
import com.sira.model.Configuracion;
import com.sira.model.Usuario;
import com.sira.repository.AdministradorRepository;
import com.sira.repository.ConfiguracionRepository;
import com.sira.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements ApplicationRunner {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AdministradorRepository administradorRepository;
    @Autowired private ConfiguracionRepository configuracionRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        crearAdminPorDefecto();
        crearConfiguracionPorDefecto();
    }

    private void crearAdminPorDefecto() {
        if (usuarioRepository.existsByEmail("admin@escuela.edu")) return;

        Usuario usuario = usuarioRepository.save(new Usuario(
                "Administrador",
                "admin@escuela.edu",
                passwordEncoder.encode("123456"),
                "admin"
        ));
        administradorRepository.save(new Administrador(usuario, "ADMIN-001"));
    }

    private void crearConfiguracionPorDefecto() {
        if (!configuracionRepository.existsById("calificacion_minima_aprobatoria")) {
            configuracionRepository.save(new Configuracion(
                    "calificacion_minima_aprobatoria", "70",
                    "Calificación mínima para aprobar una materia"));
        }
        if (!configuracionRepository.existsById("calificacion_maxima")) {
            configuracionRepository.save(new Configuracion(
                    "calificacion_maxima", "100",
                    "Calificación máxima permitida en el sistema"));
        }
        if (!configuracionRepository.existsById("semestre_activo")) {
            int anio = java.time.LocalDate.now().getYear();
            int mes = java.time.LocalDate.now().getMonthValue();
            String periodo = mes <= 6 ? "1" : "2";
            configuracionRepository.save(new Configuracion(
                    "semestre_activo", anio + "-" + periodo,
                    "Semestre académico activo para el dashboard operativo"));
        }
    }
}
