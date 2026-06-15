package com.cuerposano.backend.data;

import com.cuerposano.backend.entities.*;
import com.cuerposano.backend.enums.EstadoAsistenciaClase;
import com.cuerposano.backend.enums.EstadoMembresiaSocio;
import com.cuerposano.backend.enums.MedioPago;
import com.cuerposano.backend.repositories.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Component
@Order(100)
public class DemoSeedData implements CommandLineRunner {

    private static final LocalDate DEMO_START = LocalDate.of(2025, 12, 13);
    private static final LocalDate DEMO_TODAY = LocalDate.of(2026, 6, 13);
    private static final String DEMO_MARKER_DNI = "30124863";

    @Value("${CUERPOSANO_DEMO_SEED_ENABLED:true}")
    private boolean enabled;

    @Value("${CUERPOSANO_DEMO_SEED_FORCE:false}")
    private boolean force;

    private final ActividadRepository actividadRepository;
    private final EntrenadorRepository entrenadorRepository;
    private final TipoMembresiaRepository tipoMembresiaRepository;
    private final SocioRepository socioRepository;
    private final UsuarioRepository usuarioRepository;
    private final HorarioRepository horarioRepository;
    private final PagoRepository pagoRepository;
    private final ComprobanteRepository comprobanteRepository;
    private final MembresiaSocioRepository membresiaSocioRepository;
    private final InscripcionClaseRepository inscripcionClaseRepository;
    private final AsistenciaSocioRepository asistenciaSocioRepository;
    private final AsistenciaClaseRepository asistenciaClaseRepository;
    private final AsistenciaEntrenadorRepository asistenciaEntrenadorRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    private final Random random = new Random(20260613L);

    public DemoSeedData(
            ActividadRepository actividadRepository,
            EntrenadorRepository entrenadorRepository,
            TipoMembresiaRepository tipoMembresiaRepository,
            SocioRepository socioRepository,
            UsuarioRepository usuarioRepository,
            HorarioRepository horarioRepository,
            PagoRepository pagoRepository,
            ComprobanteRepository comprobanteRepository,
            MembresiaSocioRepository membresiaSocioRepository,
            InscripcionClaseRepository inscripcionClaseRepository,
            AsistenciaSocioRepository asistenciaSocioRepository,
            AsistenciaClaseRepository asistenciaClaseRepository,
            AsistenciaEntrenadorRepository asistenciaEntrenadorRepository,
            AuditLogRepository auditLogRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.actividadRepository = actividadRepository;
        this.entrenadorRepository = entrenadorRepository;
        this.tipoMembresiaRepository = tipoMembresiaRepository;
        this.socioRepository = socioRepository;
        this.usuarioRepository = usuarioRepository;
        this.horarioRepository = horarioRepository;
        this.pagoRepository = pagoRepository;
        this.comprobanteRepository = comprobanteRepository;
        this.membresiaSocioRepository = membresiaSocioRepository;
        this.inscripcionClaseRepository = inscripcionClaseRepository;
        this.asistenciaSocioRepository = asistenciaSocioRepository;
        this.asistenciaClaseRepository = asistenciaClaseRepository;
        this.asistenciaEntrenadorRepository = asistenciaEntrenadorRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled) {
            return;
        }

        if (force) {
            limpiarDatosDemo();
        } else if (socioRepository.existsByDni(DEMO_MARKER_DNI)) {
            System.out.println("Seed demo Cuerpo Sano: ya existen datos demo. No se vuelve a cargar.");
            return;
        }

        System.out.println("Seed demo Cuerpo Sano: iniciando carga ultrarealista últimos 6 meses...");

        Map<String, Actividad> actividades = crearActividades();
        Map<String, TipoMembresia> planes = crearPlanes();
        Map<String, Entrenador> profesores = crearProfesores(actividades);

        crearUsuariosDemo(profesores);

        List<Socio> socios = crearSocios();
        List<Horario> horarios = crearAgendaClases(actividades, profesores);

        crearMembresiasYPagos(socios, planes);
        crearIngresosGimnasio(socios);
        Map<Horario, List<Socio>> inscripcionesPorHorario = crearInscripciones(horarios, socios);
        crearAsistenciasClases(inscripcionesPorHorario);

        System.out.println("Seed demo Cuerpo Sano: carga finalizada.");
        System.out.println("Usuarios demo:");
        System.out.println("admin / admin123");
        System.out.println("recepcion-turno-manana / recepcion123");
        System.out.println("recepcion-turno-noche / recepcion123");
        System.out.println("profesor-sofia / profesor123");
        System.out.println("profesor-martin / profesor123");
        System.out.println("profesor-valentina / profesor123");
        System.out.println("profesor-diego / profesor123");
        System.out.println("profesor-carolina / profesor123");
    }

    private void limpiarDatosDemo() {
        System.out.println("Seed demo Cuerpo Sano: FORCE activo. Limpiando datos existentes...");

        auditLogRepository.deleteAllInBatch();
        asistenciaClaseRepository.deleteAllInBatch();
        asistenciaSocioRepository.deleteAllInBatch();
        asistenciaEntrenadorRepository.deleteAllInBatch();
        inscripcionClaseRepository.deleteAllInBatch();
        membresiaSocioRepository.deleteAllInBatch();
        comprobanteRepository.deleteAllInBatch();
        pagoRepository.deleteAllInBatch();
        horarioRepository.deleteAllInBatch();
        usuarioRepository.deleteAllInBatch();
        entrenadorRepository.deleteAllInBatch();
        tipoMembresiaRepository.deleteAllInBatch();
        socioRepository.deleteAllInBatch();
        actividadRepository.deleteAllInBatch();
    }

    private Map<String, Actividad> crearActividades() {
        List<ActividadSeed> seeds = List.of(
                new ActividadSeed("Funcional", "Entrenamiento integral orientado al movimiento, la coordinación y la mejora progresiva de la condición física.", 18),
                new ActividadSeed("HIIT", "Sesiones de alta intensidad organizadas en intervalos breves con recuperación controlada.", 14),
                new ActividadSeed("Spinning", "Trabajo cardiovascular sobre bicicleta fija con bloques de resistencia, cadencia y control técnico.", 16),
                new ActividadSeed("Crossfit", "Entrenamiento combinado de fuerza, potencia y acondicionamiento general con planificación por bloques.", 12),
                new ActividadSeed("Powerlifting", "Práctica enfocada en fuerza máxima, técnica y progresión sobre patrones principales de levantamiento.", 10)
        );

        Map<String, Actividad> result = new LinkedHashMap<>();

        for (ActividadSeed seed : seeds) {
            Actividad actividad = new Actividad();
            actividad.setNombre(seed.nombre());
            actividad.setDescripcion(seed.descripcion());
            actividad.setCupoMaximo(seed.cupo());
            actividad.setActiva(true);

            result.put(seed.nombre(), actividadRepository.save(actividad));
        }

        return result;
    }

    private Map<String, TipoMembresia> crearPlanes() {
        List<PlanSeed> seeds = List.of(
                new PlanSeed("Plan Mensual", 30, "Vigencia mensual para mantener una continuidad activa dentro del sistema.", new BigDecimal("41000.00")),
                new PlanSeed("Plan Trimestral", 90, "Vigencia trimestral pensada para sostener una planificación de mediano plazo.", new BigDecimal("115000.00")),
                new PlanSeed("Plan Semestral", 180, "Vigencia semestral orientada a procesos de seguimiento prolongados.", new BigDecimal("210000.00")),
                new PlanSeed("Plan Anual", 365, "Vigencia anual para continuidad extendida durante el ciclo completo.", new BigDecimal("380000.00"))
        );

        Map<String, TipoMembresia> result = new LinkedHashMap<>();

        for (PlanSeed seed : seeds) {
            TipoMembresia plan = new TipoMembresia();
            plan.setNombre(seed.nombre());
            plan.setDuracionDias(seed.duracionDias());
            plan.setDescripcion(seed.descripcion());
            plan.setPrecio(seed.precio2026());
            plan.setActiva(true);

            result.put(seed.nombre(), tipoMembresiaRepository.save(plan));
        }

        return result;
    }

    private Map<String, Entrenador> crearProfesores(Map<String, Actividad> actividades) {
        List<ProfesorSeed> seeds = List.of(
                new ProfesorSeed("Sofía", "Benítez", "28654719", "Funcional", "+54 9 11 5842-1930", "sofia.benitez@cuerposano.demo", "https://randomuser.me/api/portraits/women/19.jpg"),
                new ProfesorSeed("Martín", "Gutiérrez", "27489362", "HIIT", "+54 9 11 6128-4407", "martin.gutierrez@cuerposano.demo", "https://randomuser.me/api/portraits/men/41.jpg"),
                new ProfesorSeed("Valentina", "Serrano", "31842795", "Spinning", "+54 9 11 3791-7284", "valentina.serrano@cuerposano.demo", "https://randomuser.me/api/portraits/women/88.jpg"),
                new ProfesorSeed("Diego", "Mansilla", "25963841", "Crossfit", "+54 9 11 5376-9021", "diego.mansilla@cuerposano.demo", "https://randomuser.me/api/portraits/men/60.jpg"),
                new ProfesorSeed("Carolina", "Aguirre", "33751628", "Powerlifting", "+54 9 11 4982-6135", "carolina.aguirre@cuerposano.demo", "https://randomuser.me/api/portraits/women/5.jpg")
        );

        Map<String, Entrenador> result = new LinkedHashMap<>();

        for (ProfesorSeed seed : seeds) {
            Entrenador entrenador = new Entrenador();
            entrenador.setNombre(seed.nombre());
            entrenador.setApellido(seed.apellido());
            entrenador.setDni(seed.dni());
            entrenador.setEspecialidadId(actividades.get(seed.actividad()).getId());
            entrenador.setCertificado(true);
            entrenador.setTelefono(seed.telefono());
            entrenador.setEmail(seed.email());
            entrenador.setFotoUrl(seed.fotoUrl());
            entrenador.setActivo(true);

            Entrenador saved = entrenadorRepository.save(entrenador);
            result.put(seed.actividad(), saved);
        }

        return result;
    }

    private void crearUsuariosDemo(Map<String, Entrenador> profesores) {
        upsertUsuario("admin", "admin123", "Administrador", null);
        upsertUsuario("recepcion-turno-manana", "recepcion123", "Recepcionista", null);
        upsertUsuario("recepcion-turno-noche", "recepcion123", "Recepcionista", null);

        upsertUsuario("profesor-sofia", "profesor123", "Profesor", profesores.get("Funcional"));
        upsertUsuario("profesor-martin", "profesor123", "Profesor", profesores.get("HIIT"));
        upsertUsuario("profesor-valentina", "profesor123", "Profesor", profesores.get("Spinning"));
        upsertUsuario("profesor-diego", "profesor123", "Profesor", profesores.get("Crossfit"));
        upsertUsuario("profesor-carolina", "profesor123", "Profesor", profesores.get("Powerlifting"));
    }

    private void upsertUsuario(String username, String password, String rol, Entrenador entrenador) {
        Usuario usuario = usuarioRepository
                .findByNombreUsuario(username)
                .orElseGet(Usuario::new);

        usuario.setNombreUsuario(username);
        usuario.setPasswordHash(passwordEncoder.encode(password));
        usuario.setRol(rol);
        usuario.setActivo(true);
        usuario.setEntrenador(entrenador);

        usuarioRepository.save(usuario);
    }

    private List<Socio> crearSocios() {
        List<SocioSeed> seeds = List.of(
                new SocioSeed("Lucía", "Fernández", "30124863", LocalDate.of(1991, 4, 12), "Av. Rivadavia 14580, Ramos Mejía", "+54 9 11 5412-7784", "lucia.fernandez@gmail.com", "https://randomuser.me/api/portraits/women/44.jpg", LocalDate.of(2025, 12, 13), true),
                new SocioSeed("Mateo", "Gómez", "29271548", LocalDate.of(1988, 9, 7), "Arieta 3261, San Justo", "+54 9 11 6384-2190", "mateo.gomez@hotmail.com", "https://randomuser.me/api/portraits/men/46.jpg", LocalDate.of(2025, 12, 17), true),
                new SocioSeed("Camila", "Rodríguez", "33742819", LocalDate.of(1995, 6, 23), "Belgrano 742, Morón", "+54 9 11 5820-9146", "camila.rodriguez@gmail.com", "https://randomuser.me/api/portraits/women/65.jpg", LocalDate.of(2025, 12, 21), true),
                new SocioSeed("Nicolás", "Martínez", "31490827", LocalDate.of(1993, 11, 2), "San Martín 1085, Haedo", "+54 9 11 6049-3257", "nicolas.martinez@outlook.com", "https://randomuser.me/api/portraits/men/22.jpg", LocalDate.of(2025, 12, 28), true),
                new SocioSeed("Valentina", "López", "35281736", LocalDate.of(1998, 1, 19), "Alsina 2240, Lomas del Mirador", "+54 9 11 3275-9041", "valentina.lopez@gmail.com", "https://randomuser.me/api/portraits/women/32.jpg", LocalDate.of(2026, 1, 3), true),
                new SocioSeed("Tomás", "Silva", "38614920", LocalDate.of(2001, 8, 30), "Av. Illia 1982, San Justo", "+54 9 11 5591-7362", "tomas.silva@gmail.com", "https://randomuser.me/api/portraits/men/53.jpg", LocalDate.of(2026, 1, 8), true),
                new SocioSeed("Sofía", "Pereyra", "27943615", LocalDate.of(1985, 12, 4), "Brandsen 651, Ituzaingó", "+54 9 11 6480-1175", "sofia.pereyra@yahoo.com", "https://randomuser.me/api/portraits/women/68.jpg", LocalDate.of(2026, 1, 14), true),
                new SocioSeed("Agustín", "Torres", "36521847", LocalDate.of(1999, 3, 16), "Jujuy 1489, Morón", "+54 9 11 4258-3390", "agustin.torres@gmail.com", "https://randomuser.me/api/portraits/men/75.jpg", LocalDate.of(2026, 1, 20), true),
                new SocioSeed("Martina", "Castro", "42150963", LocalDate.of(2004, 5, 11), "Chile 920, San Justo", "+54 9 11 7084-2561", "martina.castro@gmail.com", "https://randomuser.me/api/portraits/women/12.jpg", LocalDate.of(2026, 1, 26), true),
                new SocioSeed("Santiago", "Molina", "29874135", LocalDate.of(1989, 7, 25), "Av. de Mayo 1165, Ramos Mejía", "+54 9 11 5716-8843", "santiago.molina@hotmail.com", "https://randomuser.me/api/portraits/men/10.jpg", LocalDate.of(2026, 2, 1), false),
                new SocioSeed("Julieta", "Herrera", "33284917", LocalDate.of(1994, 2, 8), "Colón 533, Castelar", "+54 9 11 6082-4197", "julieta.herrera@gmail.com", "https://randomuser.me/api/portraits/women/25.jpg", LocalDate.of(2026, 2, 7), true),
                new SocioSeed("Franco", "Navarro", "34502918", LocalDate.of(1996, 10, 18), "Pueyrredón 1873, Haedo", "+54 9 11 5139-6724", "franco.navarro@gmail.com", "https://randomuser.me/api/portraits/men/31.jpg", LocalDate.of(2026, 2, 13), true),
                new SocioSeed("Abril", "Suárez", "40268174", LocalDate.of(2002, 4, 5), "España 811, Morón", "+54 9 11 6348-9027", "abril.suarez@gmail.com", "https://randomuser.me/api/portraits/women/36.jpg", LocalDate.of(2026, 2, 19), true),
                new SocioSeed("Joaquín", "Romero", "28671394", LocalDate.of(1986, 6, 29), "Av. Mosconi 3380, Lomas del Mirador", "+54 9 11 5694-1832", "joaquin.romero@outlook.com", "https://randomuser.me/api/portraits/men/63.jpg", LocalDate.of(2026, 2, 25), true),
                new SocioSeed("Emilia", "Díaz", "37164208", LocalDate.of(2000, 9, 14), "Perón 2304, San Justo", "+54 9 11 7031-5846", "emilia.diaz@gmail.com", "https://randomuser.me/api/portraits/women/49.jpg", LocalDate.of(2026, 3, 3), true),
                new SocioSeed("Bruno", "Acosta", "31785640", LocalDate.of(1992, 1, 3), "Sarmiento 452, Ramos Mejía", "+54 9 11 4926-7135", "bruno.acosta@gmail.com", "https://randomuser.me/api/portraits/men/82.jpg", LocalDate.of(2026, 3, 9), true),
                new SocioSeed("Candela", "Rojas", "39420571", LocalDate.of(2001, 7, 9), "Lavalle 1590, Morón", "+54 9 11 6819-3405", "candela.rojas@hotmail.com", "https://randomuser.me/api/portraits/women/58.jpg", LocalDate.of(2026, 3, 15), true),
                new SocioSeed("Lautaro", "Vega", "27198436", LocalDate.of(1984, 11, 21), "Av. Crovara 2870, La Tablada", "+54 9 11 5772-0184", "lautaro.vega@gmail.com", "https://randomuser.me/api/portraits/men/14.jpg", LocalDate.of(2026, 3, 21), false),
                new SocioSeed("Malena", "Sosa", "30972815", LocalDate.of(1990, 8, 2), "Mendoza 721, Castelar", "+54 9 11 6441-5088", "malena.sosa@gmail.com", "https://randomuser.me/api/portraits/women/72.jpg", LocalDate.of(2026, 3, 27), true),
                new SocioSeed("Thiago", "Medina", "41893250", LocalDate.of(2003, 12, 17), "Av. Gaona 2051, Ramos Mejía", "+54 9 11 7076-9201", "thiago.medina@gmail.com", "https://randomuser.me/api/portraits/men/93.jpg", LocalDate.of(2026, 4, 2), true),
                new SocioSeed("Florencia", "Arias", "33567194", LocalDate.of(1995, 3, 22), "Almafuerte 1176, San Justo", "+54 9 11 5304-8891", "florencia.arias@gmail.com", "https://randomuser.me/api/portraits/women/8.jpg", LocalDate.of(2026, 4, 8), true),
                new SocioSeed("Facundo", "Cabrera", "36390824", LocalDate.of(1998, 10, 1), "Rondeau 915, Haedo", "+54 9 11 6710-3564", "facundo.cabrera@hotmail.com", "https://randomuser.me/api/portraits/men/57.jpg", LocalDate.of(2026, 4, 14), true),
                new SocioSeed("Victoria", "Núñez", "29176482", LocalDate.of(1987, 5, 26), "Av. San Martín 3301, Caseros", "+54 9 11 4620-7749", "victoria.nunez@gmail.com", "https://randomuser.me/api/portraits/women/91.jpg", LocalDate.of(2026, 4, 20), false),
                new SocioSeed("Ignacio", "Morales", "35270619", LocalDate.of(1997, 2, 13), "Urquiza 740, Morón", "+54 9 11 5157-2698", "ignacio.morales@gmail.com", "https://randomuser.me/api/portraits/men/18.jpg", LocalDate.of(2026, 4, 26), true),
                new SocioSeed("Delfina", "Ramos", "40723981", LocalDate.of(2002, 6, 6), "Entre Ríos 1320, San Justo", "+54 9 11 6840-1742", "delfina.ramos@gmail.com", "https://randomuser.me/api/portraits/women/76.jpg", LocalDate.of(2026, 5, 2), true),
                new SocioSeed("Lucas", "Benítez", "27854016", LocalDate.of(1985, 9, 20), "Av. Rivadavia 13920, Ramos Mejía", "+54 9 11 5536-8002", "lucas.benitez@gmail.com", "https://randomuser.me/api/portraits/men/70.jpg", LocalDate.of(2026, 5, 8), true),
                new SocioSeed("Rocío", "Ponce", "32681475", LocalDate.of(1993, 7, 31), "Monteagudo 586, Morón", "+54 9 11 6099-3618", "rocio.ponce@hotmail.com", "https://randomuser.me/api/portraits/women/29.jpg", LocalDate.of(2026, 5, 14), true),
                new SocioSeed("Manuel", "Ortega", "38427691", LocalDate.of(2000, 1, 28), "Perú 1477, Haedo", "+54 9 11 6477-0934", "manuel.ortega@gmail.com", "https://randomuser.me/api/portraits/men/28.jpg", LocalDate.of(2026, 5, 20), true),
                new SocioSeed("Micaela", "Luna", "35910482", LocalDate.of(1997, 11, 12), "Pringles 1028, Castelar", "+54 9 11 5201-6695", "micaela.luna@gmail.com", "https://randomuser.me/api/portraits/women/83.jpg", LocalDate.of(2026, 5, 26), true),
                new SocioSeed("Enzo", "Giménez", "41280536", LocalDate.of(2003, 4, 24), "Av. Brig. Juan Manuel de Rosas 3910, San Justo", "+54 9 11 6982-4420", "enzo.gimenez@gmail.com", "https://randomuser.me/api/portraits/men/86.jpg", LocalDate.of(2026, 6, 1), true)
        );

        List<Socio> result = new ArrayList<>();

        int index = 1;
        for (SocioSeed seed : seeds) {
            Socio socio = new Socio();
            socio.setNumeroSocio(String.format("CS-%05d", index));
            socio.setCodigoBarra("779" + seed.dni() + String.format("%02d", index));
            socio.setNombre(seed.nombre());
            socio.setApellido(seed.apellido());
            socio.setDni(seed.dni());
            socio.setFechaNacimiento(seed.fechaNacimiento());
            socio.setDireccion(seed.direccion());
            socio.setTelefono(seed.telefono());
            socio.setEmail(seed.email());
            socio.setFotoUrl(seed.fotoUrl());
            socio.setActivo(seed.activo());

            result.add(socioRepository.save(socio));
            index++;
        }

        return result;
    }

    private List<Horario> crearAgendaClases(
            Map<String, Actividad> actividades,
            Map<String, Entrenador> profesores
    ) {
        List<ClaseSeed> seeds = List.of(
                new ClaseSeed("Funcional", List.of(1, 3, 5), LocalTime.of(7, 0), LocalTime.of(8, 0)),
                new ClaseSeed("HIIT", List.of(2, 4), LocalTime.of(8, 0), LocalTime.of(8, 50)),
                new ClaseSeed("Spinning", List.of(1, 3, 6), LocalTime.of(18, 0), LocalTime.of(18, 50)),
                new ClaseSeed("Crossfit", List.of(2, 4, 6), LocalTime.of(19, 0), LocalTime.of(20, 0)),
                new ClaseSeed("Powerlifting", List.of(1, 3, 5), LocalTime.of(20, 0), LocalTime.of(21, 15))
        );

        List<Horario> result = new ArrayList<>();

        for (ClaseSeed seed : seeds) {
            for (Integer dia : seed.dias()) {
                Horario horario = new Horario();
                horario.setDiaSemana(dia);
                horario.setHoraInicio(seed.inicio());
                horario.setHoraFin(seed.fin());
                horario.setFechaDesde(DEMO_START);
                horario.setFechaHasta(LocalDate.of(2026, 12, 31));
                horario.setActividad(actividades.get(seed.actividad()));
                horario.setEntrenador(profesores.get(seed.actividad()));
                horario.setActivo(true);

                result.add(horarioRepository.save(horario));
            }
        }

        return result;
    }

    private void crearMembresiasYPagos(List<Socio> socios, Map<String, TipoMembresia> planes) {
        List<String> planRotacion = List.of(
                "Plan Mensual",
                "Plan Trimestral",
                "Plan Semestral",
                "Plan Anual"
        );

        int comprobanteSeq = 1;

        for (int i = 0; i < socios.size(); i++) {
            Socio socio = socios.get(i);
            LocalDate alta = socioAlta(socio);
            LocalDate fechaCursor = alta;
            LocalDate fechaLimite = Boolean.TRUE.equals(socio.getActivo())
                    ? DEMO_TODAY.plusDays(20 + (i % 18))
                    : alta.plusMonths(2 + (i % 3));

            String planNombre = planRotacion.get(i % planRotacion.size());
            TipoMembresia plan = planes.get(planNombre);

            while (!fechaCursor.isAfter(fechaLimite)) {
                LocalDate fechaFin = fechaCursor.plusDays(plan.getDuracionDias() - 1L);

                Pago pago = new Pago();
                pago.setSocio(socio);
                pago.setMonto(precioHistorico(planNombre, fechaCursor.getYear()));
                pago.setFechaPago(fechaCursor.atTime(horaPago(i)));
                pago.setMedioPago(medioPago(i + fechaCursor.getMonthValue()));
                pago.setObservacion("Pago correspondiente a " + planNombre + " - período " + fechaCursor + " al " + fechaFin);
                Pago pagoGuardado = pagoRepository.save(pago);

                Comprobante comprobante = new Comprobante();
                comprobante.setPago(pagoGuardado);
                comprobante.setNumero(String.format("CS-F%04d-%06d", fechaCursor.getYear(), comprobanteSeq++));
                comprobante.setFechaEmision(pagoGuardado.getFechaPago().plusMinutes(3));
                comprobante.setDetalle("Comprobante generado por pago de membresía.");
                comprobanteRepository.save(comprobante);

                MembresiaSocio membresia = new MembresiaSocio();
                membresia.setSocio(socio);
                membresia.setTipoMembresia(plan);
                membresia.setFechaInicio(fechaCursor);
                membresia.setFechaFin(fechaFin);
                membresia.setPago(pagoGuardado);

                if (!Boolean.TRUE.equals(socio.getActivo()) && fechaFin.isAfter(fechaLimite.minusDays(5))) {
                    membresia.setEstado(EstadoMembresiaSocio.CANCELADA);
                } else if (fechaFin.isBefore(DEMO_TODAY)) {
                    membresia.setEstado(EstadoMembresiaSocio.VENCIDA);
                } else {
                    membresia.setEstado(EstadoMembresiaSocio.ACTIVA);
                }

                membresiaSocioRepository.save(membresia);

                fechaCursor = fechaFin.plusDays(1 + random.nextInt(4));
            }
        }
    }

    private void crearIngresosGimnasio(List<Socio> socios) {
        for (Socio socio : socios) {
            LocalDate inicio = socioAlta(socio);
            LocalDate fin = Boolean.TRUE.equals(socio.getActivo())
                    ? DEMO_TODAY
                    : inicio.plusMonths(8 + random.nextInt(6));

            LocalDate cursor = LocalDate.of(inicio.getYear(), inicio.getMonth(), 1);

            while (!cursor.isAfter(fin)) {
                int visitasMes = Boolean.TRUE.equals(socio.getActivo())
                        ? 3 + random.nextInt(5)
                        : 1 + random.nextInt(2);

                for (int i = 0; i < visitasMes; i++) {
                    LocalDate fecha = randomDateInMonth(cursor.getYear(), cursor.getMonthValue());

                    if (fecha.isBefore(inicio) || fecha.isAfter(fin) || fecha.isAfter(DEMO_TODAY)) {
                        continue;
                    }

                    if (fecha.getDayOfWeek() == DayOfWeek.SUNDAY) {
                        continue;
                    }

                    AsistenciaSocio asistencia = new AsistenciaSocio();
                    asistencia.setSocio(socio);
                    asistencia.setFechaHora(fecha.atTime(horaIngreso()));
                    asistencia.setMetodoIngreso(metodoIngreso());

                    asistenciaSocioRepository.save(asistencia);
                }

                cursor = cursor.plusMonths(1);
            }
        }
    }

    private Map<Horario, List<Socio>> crearInscripciones(List<Horario> horarios, List<Socio> socios) {
        List<Socio> sociosActivos = socios.stream()
                .filter(s -> Boolean.TRUE.equals(s.getActivo()))
                .toList();

        Map<Horario, List<Socio>> result = new LinkedHashMap<>();

        for (Horario horario : horarios) {
            List<Socio> candidatos = new ArrayList<>(sociosActivos);
            Collections.shuffle(candidatos, random);

            int cupo = horario.getActividad().getCupoMaximo();
            int cantidad = Math.min(candidatos.size(), Math.max(5, Math.min(cupo - 2, 7 + random.nextInt(5))));

            List<Socio> seleccionados = candidatos.subList(0, cantidad);

            for (Socio socio : seleccionados) {
                if (inscripcionClaseRepository.existsBySocioIdAndHorarioId(socio.getId(), horario.getId())) {
                    continue;
                }

                InscripcionClase inscripcion = new InscripcionClase();
                inscripcion.setSocio(socio);
                inscripcion.setHorario(horario);
                inscripcion.setFechaInscripcion(fechaInscripcion(socioAlta(socio)).atTime(10, 15));
                inscripcion.setActiva(true);

                inscripcionClaseRepository.save(inscripcion);
            }

            result.put(horario, new ArrayList<>(seleccionados));
        }

        return result;
    }

    private void crearAsistenciasClases(Map<Horario, List<Socio>> inscripcionesPorHorario) {
        for (Map.Entry<Horario, List<Socio>> entry : inscripcionesPorHorario.entrySet()) {
            Horario horario = entry.getKey();
            List<Socio> alumnos = entry.getValue();

            for (LocalDate fecha : fechasClaseDemo(horario.getDiaSemana())) {
                for (Socio socio : alumnos) {
                    if (fecha.isBefore(socioAlta(socio)) || fecha.isAfter(DEMO_TODAY)) {
                        continue;
                    }

                    if (asistenciaClaseRepository
                            .findBySocioIdAndHorarioIdAndFechaClase(socio.getId(), horario.getId(), fecha)
                            .isPresent()) {
                        continue;
                    }

                    AsistenciaClase asistencia = new AsistenciaClase();
                    asistencia.setSocio(socio);
                    asistencia.setHorario(horario);
                    asistencia.setFechaClase(fecha);
                    asistencia.setFechaHora(fecha.atTime(horario.getHoraInicio()).plusMinutes(5 + random.nextInt(12)));
                    asistencia.setEstado(random.nextDouble() < 0.83 ? EstadoAsistenciaClase.PRESENTE : EstadoAsistenciaClase.AUSENTE);

                    asistenciaClaseRepository.save(asistencia);
                }
            }
        }
    }

    private List<LocalDate> fechasClaseDemo(Integer diaSemana) {
        List<LocalDate> fechas = new ArrayList<>();
        LocalDate cursor = DEMO_START;

        while (!cursor.isAfter(DEMO_TODAY)) {
            if (cursor.getDayOfWeek().getValue() == diaSemana) {
                if (random.nextDouble() < 0.24 || cursor.isAfter(DEMO_TODAY.minusMonths(2))) {
                    fechas.add(cursor);
                }
            }

            cursor = cursor.plusDays(1);
        }

        return fechas;
    }

    private LocalDate socioAlta(Socio socio) {
        String numero = socio.getNumeroSocio().replace("CS-", "");
        int index = Integer.parseInt(numero);
        List<LocalDate> altas = List.of(
                LocalDate.of(2025, 12, 13), LocalDate.of(2025, 12, 17), LocalDate.of(2025, 12, 21), LocalDate.of(2025, 12, 28), LocalDate.of(2026, 1, 3), LocalDate.of(2026, 1, 8), LocalDate.of(2026, 1, 14), LocalDate.of(2026, 1, 20), LocalDate.of(2026, 1, 26), LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 7), LocalDate.of(2026, 2, 13), LocalDate.of(2026, 2, 19), LocalDate.of(2026, 2, 25), LocalDate.of(2026, 3, 3), LocalDate.of(2026, 3, 9), LocalDate.of(2026, 3, 15), LocalDate.of(2026, 3, 21), LocalDate.of(2026, 3, 27), LocalDate.of(2026, 4, 2), LocalDate.of(2026, 4, 8), LocalDate.of(2026, 4, 14), LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 26), LocalDate.of(2026, 5, 2), LocalDate.of(2026, 5, 8), LocalDate.of(2026, 5, 14), LocalDate.of(2026, 5, 20), LocalDate.of(2026, 5, 26), LocalDate.of(2026, 6, 1)
        );

        return altas.get(index - 1);
    }

    private LocalDate fechaInscripcion(LocalDate altaSocio) {
        LocalDate fecha = altaSocio.plusDays(7 + random.nextInt(35));
        return fecha.isAfter(DEMO_TODAY) ? DEMO_TODAY.minusDays(20) : fecha;
    }

    private BigDecimal precioHistorico(String planNombre, int year) {
        Map<String, BigDecimal> p2024 = Map.of(
                "Plan Mensual", new BigDecimal("18000.00"),
                "Plan Trimestral", new BigDecimal("50000.00"),
                "Plan Semestral", new BigDecimal("90000.00"),
                "Plan Anual", new BigDecimal("160000.00")
        );

        Map<String, BigDecimal> p2025 = Map.of(
                "Plan Mensual", new BigDecimal("29000.00"),
                "Plan Trimestral", new BigDecimal("82000.00"),
                "Plan Semestral", new BigDecimal("150000.00"),
                "Plan Anual", new BigDecimal("270000.00")
        );

        Map<String, BigDecimal> p2026 = Map.of(
                "Plan Mensual", new BigDecimal("41000.00"),
                "Plan Trimestral", new BigDecimal("115000.00"),
                "Plan Semestral", new BigDecimal("210000.00"),
                "Plan Anual", new BigDecimal("380000.00")
        );

        if (year <= 2024) return p2024.get(planNombre);
        if (year == 2025) return p2025.get(planNombre);

        return p2026.get(planNombre);
    }

    private LocalTime horaPago(int seed) {
        int hour = List.of(9, 10, 11, 16, 17, 18, 19).get(Math.floorMod(seed, 7));
        int minute = List.of(5, 12, 20, 28, 35, 42, 50).get(Math.floorMod(seed, 7));
        return LocalTime.of(hour, minute);
    }

    private MedioPago medioPago(int index) {
        MedioPago[] medios = {
                MedioPago.EFECTIVO,
                MedioPago.DEBITO,
                MedioPago.CREDITO,
                MedioPago.TRANSFERENCIA,
                MedioPago.QR
        };

        return medios[Math.floorMod(index, medios.length)];
    }

    private LocalDate randomDateInMonth(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        int day = 1 + random.nextInt(ym.lengthOfMonth());
        return LocalDate.of(year, month, day);
    }

    private LocalTime horaIngreso() {
        List<LocalTime> horas = List.of(
                LocalTime.of(7, 10),
                LocalTime.of(8, 5),
                LocalTime.of(12, 20),
                LocalTime.of(17, 45),
                LocalTime.of(18, 30),
                LocalTime.of(19, 15),
                LocalTime.of(20, 10)
        );

        return horas.get(random.nextInt(horas.size())).plusMinutes(random.nextInt(8));
    }

    private String metodoIngreso() {
        List<String> metodos = List.of("Credencial", "QR", "Recepción");
        return metodos.get(random.nextInt(metodos.size()));
    }

    private record ActividadSeed(String nombre, String descripcion, Integer cupo) {}
    private record PlanSeed(String nombre, Integer duracionDias, String descripcion, BigDecimal precio2026) {}
    private record ProfesorSeed(String nombre, String apellido, String dni, String actividad, String telefono, String email, String fotoUrl) {}
    private record ClaseSeed(String actividad, List<Integer> dias, LocalTime inicio, LocalTime fin) {}

    private record SocioSeed(
            String nombre,
            String apellido,
            String dni,
            LocalDate fechaNacimiento,
            String direccion,
            String telefono,
            String email,
            String fotoUrl,
            LocalDate alta,
            Boolean activo
    ) {}
}
