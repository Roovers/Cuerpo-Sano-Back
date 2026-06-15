package com.cuerposano.backend.services;

import com.cuerposano.backend.dto.PermisoResponse;
import com.cuerposano.backend.dto.RolPermisosResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class PermisosService {

    public static final String ROL_ADMINISTRADOR = "Administrador";
    public static final String ROL_PROFESOR = "Profesor";
    public static final String ROL_RECEPCIONISTA = "Recepcionista";

    public List<RolPermisosResponse> listarRolesPermisos() {
        return List.of(
                administrador(),
                recepcionista(),
                profesor()
        );
    }

    public RolPermisosResponse obtenerPorRol(String rol) {
        String normalizado = normalizarRol(rol);

        return listarRolesPermisos()
                .stream()
                .filter(item -> item.getRol().equalsIgnoreCase(normalizado))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rol no encontrado"));
    }

    public String normalizarRol(String rol) {
        if (rol == null || rol.isBlank()) {
            return ROL_RECEPCIONISTA;
        }

        String valor = rol.trim();
        String lower = valor.toLowerCase(Locale.ROOT);

        if (lower.equals("administrador") || lower.equals("admin")) {
            return ROL_ADMINISTRADOR;
        }

        if (lower.equals("profesor") || lower.equals("entrenador")) {
            return ROL_PROFESOR;
        }

        if (lower.equals("recepcionista") || lower.equals("recepcion")) {
            return ROL_RECEPCIONISTA;
        }

        return valor;
    }

    public List<String> codigosPaginas(String rol) {
        return obtenerPorRol(rol).getPaginas().stream().map(PermisoResponse::getCodigo).toList();
    }

    public List<String> codigosAcciones(String rol) {
        return obtenerPorRol(rol).getAcciones().stream().map(PermisoResponse::getCodigo).toList();
    }

    public List<String> codigosCards(String rol) {
        return obtenerPorRol(rol).getCards().stream().map(PermisoResponse::getCodigo).toList();
    }

    private RolPermisosResponse administrador() {
        return new RolPermisosResponse(
                ROL_ADMINISTRADOR,
                "Control total del sistema: operación, administración, seguridad, usuarios, auditoría, pagos, reportes y configuración.",
                List.of(
                        page("dashboard", "Dashboard", "Vista general completa del gimnasio"),
                        page("socios", "Socios", "Gestión completa de socios"),
                        page("membresias", "Membresías", "Tipos, asignación y activación"),
                        page("pagos", "Pagos", "Registro y consulta de pagos"),
                        page("actividades", "Actividades", "Gestión de actividades"),
                        page("entrenadores", "Profesores", "Gestión de profesores"),
                        page("clases", "Clases", "Gestión de horarios, clases y calendario"),
                        page("asistencias", "Asistencias", "Ingresos, inscripciones y asistencia a clases"),
                        page("reportes", "Reportes", "Reportes operativos y ejecutivos"),
                        page("configuracion", "Configuración", "Usuarios, roles, auditoría y seguridad")
                ),
                List.of(
                        action("dashboard.ver_completo", "Ver dashboard completo", "Acceso a indicadores operativos y financieros"),
                        action("socios.ver", "Consultar socios", "Consulta de socios"),
                        action("socios.crear", "Crear socios", "Alta de socios"),
                        action("socios.editar", "Editar socios", "Modificación de socios"),
                        action("socios.eliminar", "Dar de baja socios", "Baja lógica de socios"),
                        action("membresias.ver", "Ver membresías", "Consulta de membresías"),
                        action("membresias.gestionar", "Gestionar membresías", "Crear y editar tipos de membresía"),
                        action("membresias.asignar", "Asignar membresía", "Asignación de membresías a socios"),
                        action("membresias.activar", "Activar membresía", "Activación de membresías"),
                        action("pagos.ver", "Ver pagos", "Consulta de historial de pagos"),
                        action("pagos.crear", "Registrar pagos", "Registro de pagos y comprobantes"),
                        action("actividades.ver", "Ver actividades", "Consulta de actividades"),
                        action("actividades.crear", "Crear actividades", "Alta de actividades"),
                        action("actividades.editar", "Editar actividades", "Modificación de actividades"),
                        action("actividades.desactivar", "Desactivar actividades", "Baja lógica de actividades"),
                        action("entrenadores.ver", "Ver profesores", "Consulta de profesores"),
                        action("entrenadores.crear", "Crear profesores", "Alta de profesores"),
                        action("entrenadores.editar", "Editar profesores", "Modificación de profesores"),
                        action("entrenadores.desactivar", "Desactivar profesores", "Baja lógica de profesores"),
                        action("clases.ver", "Ver clases", "Consulta de horarios, clases y calendario"),
                        action("clases.crear", "Crear clases", "Alta de horarios y clases"),
                        action("clases.editar", "Editar clases", "Modificación de horarios y clases"),
                        action("clases.desactivar", "Desactivar clases", "Baja lógica de horarios y clases"),
                        action("asistencias.ver", "Consultar asistencias", "Consulta de asistencias"),
                        action("asistencias_gimnasio.registrar", "Registrar ingreso al gimnasio", "Registro de ingresos generales"),
                        action("inscripciones.crear", "Inscribir socio a clase", "Inscripción a clases"),
                        action("inscripciones.cancelar", "Cancelar inscripción", "Cancelación de inscripción"),
                        action("asistencias_clases.registrar", "Registrar asistencia a clases", "Presentes y ausentes de clases"),
                        action("reportes.ver", "Ver reportes", "Consulta y exportación de reportes"),
                        action("reportes_clases.ver", "Ver reportes de clases", "Consulta de reportes de clases"),
                        action("usuarios.gestionar", "Gestionar usuarios", "Crear, editar y desactivar usuarios"),
                        action("auditoria.ver", "Ver auditoría", "Consulta de logs del sistema"),
                        action("configuracion.ver", "Ver configuración", "Acceso a configuración del sistema")
                ),
                List.of(
                        card("dashboard.finanzas", "Finanzas", "Indicadores financieros del dashboard"),
                        card("dashboard.recaudacion", "Recaudación", "Recaudación mensual y evolución"),
                        card("dashboard.pagos", "Pagos", "Pagos del día y comprobantes"),
                        card("dashboard.socios", "Socios", "Socios activos y evolución"),
                        card("dashboard.vencimientos", "Vencimientos", "Membresías por vencer"),
                        card("dashboard.clases", "Clases", "Clases programadas"),
                        card("dashboard.horarios", "Horarios", "Agenda y calendario"),
                        card("dashboard.asistencias", "Asistencias", "Movimiento diario de ingresos"),
                        card("dashboard.asistencia_clases", "Asistencia a clases", "Presentes y ausentes"),
                        card("dashboard.actividades", "Actividades", "Actividades y popularidad"),
                        card("dashboard.alumnos", "Alumnos", "Alumnos inscriptos")
                ),
                List.of()
        );
    }

    private RolPermisosResponse recepcionista() {
        return new RolPermisosResponse(
                ROL_RECEPCIONISTA,
                "Rol operativo de recepción: socios, membresías, pagos, actividades, clases, ingresos, inscripciones y asistencias. Sin configuración ni auditoría.",
                List.of(
                        page("dashboard", "Dashboard", "Dashboard operativo de recepción"),
                        page("socios", "Socios", "Gestión operativa de socios"),
                        page("membresias", "Membresías", "Consulta, asignación y activación de membresías"),
                        page("pagos", "Pagos", "Registro y consulta de pagos"),
                        page("actividades", "Actividades", "Gestión operativa de actividades"),
                        page("clases", "Clases", "Consulta, creación y edición de horarios y clases"),
                        page("asistencias", "Asistencias", "Ingresos al gimnasio, inscripciones y consulta de asistencias")
                ),
                List.of(
                        action("dashboard.ver_recepcion", "Ver dashboard de recepción", "Indicadores operativos y pagos de recepción"),
                        action("socios.ver", "Consultar socios", "Buscar y consultar socios"),
                        action("socios.crear", "Crear socios", "Alta de socios"),
                        action("socios.editar", "Editar socios", "Modificación de socios"),
                        action("socios.eliminar", "Dar de baja socios", "Baja lógica de socios"),
                        action("membresias.ver", "Ver membresías", "Consulta de membresías"),
                        action("membresias.asignar", "Asignar membresía", "Asignar membresías a socios"),
                        action("membresias.activar", "Activar membresía", "Activar membresía de un socio"),
                        action("pagos.ver", "Ver pagos", "Consulta de pagos registrados"),
                        action("pagos.crear", "Registrar pagos", "Registrar pagos y comprobantes"),
                        action("actividades.ver", "Ver actividades", "Consulta de actividades"),
                        action("actividades.crear", "Crear actividades", "Alta de actividades"),
                        action("actividades.editar", "Editar actividades", "Modificación de actividades"),
                        action("actividades.desactivar", "Desactivar actividades", "Baja lógica de actividades"),
                        action("clases.ver", "Ver clases", "Consulta de horarios, clases y calendario"),
                        action("clases.crear", "Crear clases", "Alta de horarios y clases"),
                        action("clases.editar", "Editar clases", "Modificación de horarios y clases"),
                        action("clases.desactivar", "Desactivar clases", "Baja lógica de horarios y clases"),
                        action("asistencias.ver", "Consultar asistencias", "Consulta de asistencias"),
                        action("asistencias_gimnasio.registrar", "Registrar ingreso al gimnasio", "Registrar ingreso general del socio"),
                        action("inscripciones.crear", "Inscribir socio a clase", "Inscripción de socios a clases"),
                        action("inscripciones.cancelar", "Cancelar inscripción", "Cancelación de inscripción")
                ),
                List.of(
                        card("dashboard.socios", "Socios", "Socios activos y estado operativo"),
                        card("dashboard.vencimientos", "Vencimientos", "Membresías por vencer"),
                        card("dashboard.ingresos_gimnasio", "Ingresos del día", "Ingresos registrados al gimnasio"),
                        card("dashboard.clases", "Clases de hoy", "Clases programadas"),
                        card("dashboard.horarios", "Horarios", "Agenda operativa"),
                        card("dashboard.inscripciones", "Inscripciones", "Inscripciones recientes"),
                        card("dashboard.pagos", "Pagos", "Pagos registrados")
                ),
                List.of(
                        "No accede a Configuración.",
                        "No gestiona usuarios ni roles.",
                        "No ve auditoría.",
                        "No gestiona profesores.",
                        "No accede a reportes ejecutivos."
                )
        );
    }

    private RolPermisosResponse profesor() {
        return new RolPermisosResponse(
                ROL_PROFESOR,
                "Rol deportivo: consulta de socios, profesores, actividades, calendario, inscripciones y asistencia a clases. Sin pagos, membresías ni ingresos al gimnasio.",
                List.of(
                        page("dashboard", "Dashboard", "Dashboard de actividades, horarios y asistencias"),
                        page("socios", "Socios", "Consulta de socios"),
                        page("actividades", "Actividades", "Consulta, creación y edición de actividades"),
                        page("entrenadores", "Profesores", "Consulta de profesores"),
                        page("clases", "Clases", "Consulta de horarios, clases y calendario"),
                        page("asistencias", "Asistencias", "Inscripciones y asistencia a clases"),
                        page("reportes", "Reportes", "Reportes de asistencia a clases")
                ),
                List.of(
                        action("dashboard.ver_profesor", "Ver dashboard de profesor", "Indicadores de actividades, horarios y asistencias"),
                        action("socios.ver", "Consultar socios", "Consulta de datos básicos del socio"),
                        action("actividades.ver", "Ver actividades", "Consulta de actividades"),
                        action("actividades.crear", "Crear actividades", "Alta de actividades"),
                        action("actividades.editar", "Editar actividades", "Modificación de actividades"),
                        action("entrenadores.ver", "Consultar profesores", "Consulta de profesores"),
                        action("clases.ver", "Ver clases", "Consulta de horarios, clases y calendario"),
                        action("asistencias.ver", "Consultar asistencias", "Consulta de asistencias de clases"),
                        action("inscripciones.crear", "Inscribir socio a clase", "Inscripción de socios a clases"),
                        action("inscripciones.cancelar", "Cancelar inscripción", "Cancelación de inscripción a clase"),
                        action("asistencias_clases.registrar", "Registrar asistencia a clases", "Marcar presentes y ausentes"),
                        action("reportes_clases.ver", "Ver reportes de clases", "Consulta de asistencia a clases")
                ),
                List.of(
                        card("dashboard.actividades", "Actividades", "Actividades disponibles"),
                        card("dashboard.clases", "Clases", "Clases programadas"),
                        card("dashboard.horarios", "Horarios", "Calendario y agenda"),
                        card("dashboard.asistencia_clases", "Asistencia a clases", "Presentes y ausentes"),
                        card("dashboard.alumnos", "Alumnos", "Alumnos inscriptos")
                ),
                List.of(
                        "No accede a Configuración.",
                        "No accede a pagos ni métricas financieras.",
                        "No gestiona membresías.",
                        "No registra ingresos al gimnasio.",
                        "No crea, edita ni desactiva socios.",
                        "No gestiona profesores.",
                        "No crea, edita ni desactiva clases/horarios generales."
                )
        );
    }

    private PermisoResponse page(String codigo, String nombre, String descripcion) {
        return new PermisoResponse(codigo, nombre, descripcion, "PÁGINA");
    }

    private PermisoResponse action(String codigo, String nombre, String descripcion) {
        return new PermisoResponse(codigo, nombre, descripcion, "ACCIÓN");
    }

    private PermisoResponse card(String codigo, String nombre, String descripcion) {
        return new PermisoResponse(codigo, nombre, descripcion, "CARD");
    }
}
