import java.util.Scanner;

public class TowerDefenseApp {

    private static final int POSICION_INICIAL = 0;
    private static final int POSICION_FINAL = 20; // ruta lineal de 0 a 20

    private static ListaSecuencialTorres torres = new ListaSecuencialTorres();
    private static ListaDobleEnemigos enemigos = new ListaDobleEnemigos();
    private static ListaCircularOleadas oleadas = new ListaCircularOleadas();

    private static int vidasJugador = 3;
    private static int turnoActual = 0;
    private static int siguienteIdTorre = 1;
    private static int siguienteIdEnemigo = 1;
    private static int siguienteIdOleada = 1;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        // El juego arranca vacio: nada se considera "registrado" hasta que el
        // jugador lo registre manualmente desde el menu (opciones 1 y 4).
        int opcion;
        do {
            mostrarMenu();
            opcion = leerEntero(sc, "Seleccione una opcion: ");
            switch (opcion) {
                case 1: registrarTorre(sc); break;
                case 2: torres.mostrarTorres(); break;
                case 3: eliminarTorre(sc); break;
                case 4: registrarOleada(sc); break;
                case 5: oleadas.mostrarOleadas(); break;
                case 6: iniciarSiguienteOleada(); break;
                case 7: avanzarTurno(); break;
                case 8: mostrarEnemigosActivos(); break;
                case 9: mostrarEstadoGeneral(); break;
                case 10: System.out.println("Saliendo del juego. Gracias por jugar."); break;
                default: System.out.println("Opcion invalida, intente de nuevo.");
            }
            if (vidasJugador <= 0) {
                System.out.println("\n>>> El jugador ha perdido todas sus vidas. FIN DEL JUEGO <<<");
                opcion = 10;
            }
        } while (opcion != 10);
        sc.close();
    }

    private static void mostrarMenu() {
        System.out.println("\n===== TOWER DEFENSE - MENU PRINCIPAL =====");
        System.out.println("1. Registrar torre defensiva");
        System.out.println("2. Mostrar torres registradas");
        System.out.println("3. Eliminar torre");
        System.out.println("4. Registrar oleada");
        System.out.println("5. Mostrar oleadas");
        System.out.println("6. Iniciar siguiente oleada");
        System.out.println("7. Avanzar turno");
        System.out.println("8. Mostrar enemigos activos");
        System.out.println("9. Mostrar estado general del juego");
        System.out.println("10. Salir");
    }

    // ---------- OPCIONES DE TORRES (Integrante 1) ----------
    private static void registrarTorre(Scanner sc) {
        System.out.println("--- Registrar nueva torre ---");
        String nombre = leerTexto(sc, "Nombre: ");
        String tipo = leerTexto(sc, "Tipo (Arquero/Canon/...): ");
        int posicion = leerEntero(sc, "Posicion (0-" + POSICION_FINAL + "): ");
        int danio = leerEntero(sc, "Danio: ");
        int rango = leerEntero(sc, "Rango: ");
        int costo = leerEntero(sc, "Costo: ");
        Torre t = new Torre(siguienteIdTorre, nombre, tipo, posicion, danio, rango, costo);
        if (torres.insertarTorre(t)) {
            System.out.println("Torre registrada con id " + siguienteIdTorre);
            siguienteIdTorre++;
        }
    }

    private static void eliminarTorre(Scanner sc) {
        if (torres.getCantidad() == 0) {
            System.out.println("No hay torres registradas todavia.");
            return;
        }
        int id = leerEntero(sc, "Id de la torre a eliminar: ");
        if (torres.eliminarTorrePorId(id)) {
            System.out.println("Torre eliminada correctamente.");
        } else {
            System.out.println("No se encontro una torre con ese id.");
        }
    }

    // ---------- OPCIONES DE OLEADAS (Integrante 3) ----------
    private static void registrarOleada(Scanner sc) {
        System.out.println("--- Registrar nueva oleada ---");
        int cantidadEnemigos = leerEntero(sc, "Cantidad de enemigos: ");
        String tipoEnemigo = leerTexto(sc, "Tipo de enemigo: ");
        int vidaBase = leerEntero(sc, "Vida base: ");
        int velocidadBase = leerEntero(sc, "Velocidad base: ");
        Oleada o = new Oleada(siguienteIdOleada, cantidadEnemigos, tipoEnemigo, vidaBase, velocidadBase);
        oleadas.registrarOleada(o);
        System.out.println("Oleada registrada con id " + siguienteIdOleada);
        siguienteIdOleada++;
    }

    private static void iniciarSiguienteOleada() {
        if (oleadas.estaVacia()) {
            System.out.println("No hay oleadas registradas.");
            return;
        }
        Oleada o = oleadas.avanzarSiguienteOleada();
        System.out.println("Iniciando oleada " + o.getIdOleada() + " (" + o.getTipoEnemigo() + ")");
        for (int i = 0; i < o.getCantidadEnemigos(); i++) {
            Enemigo e = new Enemigo(siguienteIdEnemigo, o.getTipoEnemigo(), o.getVidaBase(),
                    o.getVelocidadBase(), POSICION_INICIAL, 10);
            enemigos.insertarAlFinal(e);
            siguienteIdEnemigo++;
        }
        System.out.println("Se generaron " + o.getCantidadEnemigos() + " enemigos de la oleada " + o.getIdOleada());

        // Si ya se recorrio el circulo completo de oleadas, se reinicia (opcional/demostrativo)
        if (oleadas.getOleadaActual() == null) {
            oleadas.reiniciarCiclo();
        }
    }

    // ---------- OPCIONES DE ENEMIGOS (Integrante 2) ----------
    private static void mostrarEnemigosActivos() {
        System.out.println("Elija direccion de recorrido: 1) Adelante  2) Atras");
        enemigos.recorrerAdelante();
    }

    // ---------- LOGICA DE TURNO (Integrante 4 - integracion) ----------
    private static void avanzarTurno() {
        turnoActual++;
        System.out.println("\n=== Avanzando turno " + turnoActual + " ===");

        // 1. Mover todos los enemigos segun su velocidad
        enemigos.actualizarPosiciones();

        // 2 y 3. Verificar rango de cada torre y aplicar danio
        Torre[] arregloTorres = torres.getTorres();
        int totalTorres = torres.getCantidad();
        NodoEnemigo nodo = enemigos.getPrimero();
        while (nodo != null) {
            Enemigo e = nodo.getEnemigo();
            for (int i = 0; i < totalTorres; i++) {
                Torre t = arregloTorres[i];
                if (t.estaEnRango(e.getPosicion())) {
                    e.recibirDanio(t.getDanio());
                    System.out.println("Torre '" + t.getNombre() + "' ataca a enemigo id=" +
                            e.getId() + " (vida restante: " + e.getVida() + ")");
                }
            }
            nodo = nodo.getSiguiente();
        }

        // 4. Eliminar enemigos cuya vida llegue a 0
        NodoEnemigo actual = enemigos.getPrimero();
        while (actual != null) {
            NodoEnemigo siguiente = actual.getSiguiente();
            if (actual.getEnemigo().estaMuerto()) {
                System.out.println("Enemigo id=" + actual.getEnemigo().getId() + " destruido.");
                enemigos.eliminarEnemigo(actual.getEnemigo().getId());
            }
            actual = siguiente;
        }

        // 5. Descontar vidas si un enemigo alcanzo el final del camino
        actual = enemigos.getPrimero();
        while (actual != null) {
            NodoEnemigo siguiente = actual.getSiguiente();
            if (actual.getEnemigo().getPosicion() >= POSICION_FINAL) {
                vidasJugador--;
                System.out.println("Un enemigo (id=" + actual.getEnemigo().getId() +
                        ") llego a la base. Vidas restantes del jugador: " + vidasJugador);
                enemigos.eliminarEnemigo(actual.getEnemigo().getId());
            }
            actual = siguiente;
        }

        // 6. Resumen del turno
        System.out.println("--- Resumen turno " + turnoActual + " ---");
        System.out.println("Enemigos activos: " + enemigos.getCantidad());
        System.out.println("Vidas del jugador: " + vidasJugador);
    }

    private static void mostrarEstadoGeneral() {
        System.out.println("\n===== ESTADO GENERAL DEL JUEGO =====");
        System.out.println("Turno actual: " + turnoActual);
        System.out.println("Vidas del jugador: " + vidasJugador);
        System.out.println("Torres activas: " + torres.contarActivas());
        System.out.println("Enemigos activos: " + enemigos.getCantidad());
        System.out.println("Oleadas registradas: " + oleadas.getCantidad());
    }

    // ---------- UTILIDADES DE ENTRADA ----------
    private static int leerEntero(Scanner sc, String mensaje) {
        System.out.print(mensaje);
        while (!sc.hasNextInt()) {
            System.out.print("Ingrese un numero valido: ");
            sc.next();
        }
        int valor = sc.nextInt();
        sc.nextLine();
        return valor;
    }

    private static String leerTexto(Scanner sc, String mensaje) {
        System.out.print(mensaje);
        return sc.nextLine();
    }
}