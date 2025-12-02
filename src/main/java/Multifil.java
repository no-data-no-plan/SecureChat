import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Classe Multifil - FASE 1 RA2
 * Aquesta classe implementa les 3 fites de la Fase 1:
 * - Fita 1: 3 fils funcionant en paral·lel amb logs de temps
 * - Fita 2: MessageBuffer - cua segura per a missatges
 * - Fita 3: ExecutorService amb clients simulats
 */
public class Multifil {

    //   ##################################
    //  ###  CLASSE DE COLORS ANSI     ###
    // ##################################

    /**
     * Classe amb constants per als colors ANSI de la terminal
     * Permet colorir la sortida per fer-la més llegible
     */
    static class Colors {
        // Reset - torna al color per defecte
        public static final String RESET = "\u001B[0m";

        // Colors bàsics
        public static final String NEGRO = "\u001B[30m";
        public static final String ROJO = "\u001B[31m";
        public static final String VERDE = "\u001B[32m";
        public static final String AMARILLO = "\u001B[33m";
        public static final String AZUL = "\u001B[34m";
        public static final String MAGENTA = "\u001B[35m";
        public static final String CYAN = "\u001B[36m";
        public static final String BLANCO = "\u001B[37m";

        // Colors brillants (més visibles)
        public static final String ROJO_BRILLANTE = "\u001B[91m";
        public static final String VERDE_BRILLANTE = "\u001B[92m";
        public static final String AMARILLO_BRILLANTE = "\u001B[93m";
        public static final String AZUL_BRILLANTE = "\u001B[94m";
        public static final String MAGENTA_BRILLANTE = "\u001B[95m";
        public static final String CYAN_BRILLANTE = "\u001B[96m";

        // Estils
        public static final String NEGRETA = "\u001B[1m";

        /**
         * Colorea un text amb el color especificat
         * @param text Text a colorar
         * @param color Color ANSI
         * @return Text colorat
         */
        public static String colorear(String text, String color) {
            return color + text + RESET;
        }
    }

    //   ##############################################################
    //  ###  FITA 2: MESSAGE BUFFER - CUA SEGURA PER A MISSATGES  ####
    // ##############################################################

    /**
     * Implementa una cua thread-safe per emmagatzemar missatges
     * Utilitza synchronized per evitar condicions de carrera
     */
    static class MessageBuffer {
        private Queue<String> cua;
        private final int capacitatMaxima;

        /**
         * Constructor del MessageBuffer
         * @param capacitat Capacitat màxima de la cua
         */
        public MessageBuffer(int capacitat) {
            this.cua = new LinkedList<>();
            this.capacitatMaxima = capacitat;
            log("MessageBuffer creat amb capacitat: " + capacitat);
        }

        /**
         * Afegeix un missatge a la cua de forma segura
         * Si la cua està plena, espera fins que hi hagi espai
         * @param missatge El missatge a afegir
         */
        public synchronized void afegirMissatge(String missatge) {
            // Esperar mentre la cua estigui plena
            while (cua.size() >= capacitatMaxima) {
                try {
                    log("Cua plena! Esperant espai...");
                    wait(); // Espera fins que algú tregui un missatge
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log("Error en esperar: " + e.getMessage());
                }
            }

            // Afegir el missatge
            cua.add(missatge);
            log("Missatge afegit: '" + missatge + "' | Total cua: " + cua.size());

            // Notificar que hi ha un nou missatge disponible
            notifyAll();
        }

        /**
         * Treu un missatge de la cua de forma segura
         * Si la cua està buida, espera fins que hi hagi missatges
         * @return El missatge tret de la cua
         */
        public synchronized String treureMissatge() {
            // Esperar mentre la cua estigui buida
            while (cua.isEmpty()) {
                try {
                    log("Cua buida! Esperant missatges...");
                    wait(); // Espera fins que algú afegeixi un missatge
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log("Error en esperar: " + e.getMessage());
                    return null;
                }
            }

            // Treure el missatge
            String missatge = cua.poll();
            log("Missatge tret: '" + missatge + "' | Restants: " + cua.size());

            // Notificar que hi ha espai disponible
            notifyAll();

            return missatge;
        }

        /**
         * Retorna la quantitat de missatges a la cua
         * @return Nombre de missatges
         */
        public synchronized int getMidaCua() {
            return cua.size();
        }
    }

    //   #######################################
    //  ###  UTILITATS - LOG AMB TIMESTAMP  ###
    // #######################################

    /**
     * Assigna un color consistent a cada fil segons el seu nom
     * @param nomFil Nom del fil
     * @return Color ANSI assignat al fil
     */
    private static String obtenirColorPerFil(String nomFil) {
        // Array de colors disponibles per als fils
        String[] colorsDisponibles = {
                Colors.AZUL_BRILLANTE,
                Colors.VERDE_BRILLANTE,
                Colors.AMARILLO_BRILLANTE,
                Colors.MAGENTA_BRILLANTE,
                Colors.CYAN_BRILLANTE,
                Colors.ROJO_BRILLANTE
        };

        // Usar el hashCode del nom per assignar un color consistent
        int index = Math.abs(nomFil.hashCode()) % colorsDisponibles.length;
        return colorsDisponibles[index];
    }

    /**
     * Mètode per imprimir logs amb timestamp, nom del fil i color automàtic
     * Cada fil té assignat automàticament un color segons el seu nom
     * @param missatge El missatge a mostrar
     */
    private static void log(String missatge) {
        LocalDateTime ara = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        String timestamp = ara.format(formatter);
        String nomFil = Thread.currentThread().getName();

        // Obtenir el color assignat a aquest fil
        String colorFil = obtenirColorPerFil(nomFil);

        // Aplicar color al missatge
        String missatgeColorat = Colors.colorear(missatge, colorFil);
        System.out.println("[" + timestamp + "] [" + nomFil + "] " + missatgeColorat);
    }

    /**
     * Imprimeix un separador visual a la consola
     * @param titol Títol del separador
     */
    private static void imprimirSeparador(String titol) {
        System.out.println("\n" + Colors.colorear("=".repeat(70), Colors.CYAN_BRILLANTE));
        System.out.println(Colors.colorear("  " + titol, Colors.CYAN_BRILLANTE + Colors.NEGRETA));
        System.out.println(Colors.colorear("=".repeat(70), Colors.CYAN_BRILLANTE) + "\n");
    }

    //   ################################################
    //  ###  FITA 1: 3 FILS FUNCIONANT EN PARAL·LEL  ###
    // ################################################

    /**
     * Demostra 3 fils executant-se en paral·lel
     * Cada fil imprimeix logs amb timestamps i colors diferents
     */
    public static void demostrarFilsBasics() {
        imprimirSeparador("FITA 1: 3 FILS EN PARAL·LEL");

        // Crear 3 fils - cada un tindrà el seu color automàtic
        Thread fil1 = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                log("Fil 1 - Iteració " + i);
                try {
                    Thread.sleep(500); // Simular treball
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            log("Fil 1 - FINALITZAT");
        }, "Fil-1");

        Thread fil2 = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                log("Fil 2 - Iteració " + i);
                try {
                    Thread.sleep(700); // Simular treball amb diferent velocitat
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            log("Fil 2 - FINALITZAT");
        }, "Fil-2");

        Thread fil3 = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                log("Fil 3 - Iteració " + i);
                try {
                    Thread.sleep(600); // Simular treball amb diferent velocitat
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            log("Fil 3 - FINALITZAT");
        }, "Fil-3");

        // Iniciar els 3 fils
        log("Iniciant els 3 fils...");
        fil1.start();
        fil2.start();
        fil3.start();

        // Esperar que tots els fils acabin
        try {
            fil1.join();
            fil2.join();
            fil3.join();
            log("Tots els fils han finalitzat correctament");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log("Error esperant els fils: " + e.getMessage());
        }
    }

    //   ###############################################
    //  ###  FITA 2: DEMOSTRACIÓ DE MESSAGE BUFFER  ###
    // ###############################################

    /**
     * Demostra el funcionament del MessageBuffer
     * Crea productors i consumidors que treballen amb la cua segura
     */
    public static void demostrarMessageBuffer() {
        imprimirSeparador("FITA 2: MESSAGE BUFFER - CUA SEGURA");

        // Crear el buffer compartit amb capacitat de 3 missatges
        MessageBuffer buffer = new MessageBuffer(3);

        // Fil PRODUCTOR - Afegeix missatges
        Thread productor = new Thread(() -> {
            for (int i = 1; i <= 7; i++) {
                String missatge = "Missatge_" + i;
                log("Productor intenta afegir: " + missatge);
                buffer.afegirMissatge(missatge);
                try {
                    Thread.sleep(400); // Simular temps entre missatges
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            log("Productor FINALITZAT");
        }, "Productor");

        // Fil CONSUMIDOR - Treu missatges
        Thread consumidor = new Thread(() -> {
            for (int i = 1; i <= 7; i++) {
                log("Consumidor intenta treure missatge");
                String missatge = buffer.treureMissatge();
                log("Consumidor ha rebut: " + missatge);
                try {
                    Thread.sleep(800); // Simular processament més lent
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            log("Consumidor FINALITZAT");
        }, "Consumidor");

        // Iniciar productor i consumidor
        log("Iniciant productor i consumidor...");
        productor.start();
        consumidor.start();

        // Esperar que acabin
        try {
            productor.join();
            consumidor.join();
            log("MessageBuffer funciona correctament!");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log("Error esperant fils: " + e.getMessage());
        }
    }

    //   #######################################################
    //  ###  FITA 3: EXECUTOR SERVICE AMB CLIENTS SIMULATS  ###
    // #######################################################

    /**
     * Classe interna que simula un client
     * Cada client té un ID i fa diverses operacions
     */
    static class ClientSimulat implements Runnable {
        private final int idClient;
        private final MessageBuffer buffer;

        public ClientSimulat(int idClient, MessageBuffer buffer) {
            this.idClient = idClient;
            this.buffer = buffer;
        }

        @Override
        public void run() {
            log("Client " + idClient + " connectat");

            try {
                // Simular que el client envia un missatge
                String missatge = "Missatge del client " + idClient;
                buffer.afegirMissatge(missatge);

                // Simular alguna operació
                Thread.sleep(500 + (idClient * 100));

                // El client rep una resposta
                log("Client " + idClient + " esperant resposta...");
                Thread.sleep(300);

                log("Client " + idClient + " desconnectat");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log("Client " + idClient + " interromput");
            }
        }
    }

    /**
     * Demostra l'ús d'ExecutorService per gestionar múltiples clients
     * Aquesta és la base per al servidor escalable
     */
    public static void demostrarExecutorService() {
        imprimirSeparador("FITA 3: EXECUTOR SERVICE - MOTOR MULTIFIL");

        // Crear un MessageBuffer per als clients
        MessageBuffer buffer = new MessageBuffer(10);

        // Crear un ExecutorService amb un pool de 5 fils
        // Això permet gestionar múltiples clients simultàniament
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        log("Servidor iniciat amb pool de 5 fils");

        // Crear un fil que processa missatges del buffer
        Thread procesadorMissatges = new Thread(() -> {
            for (int i = 0; i < 8; i++) {
                String missatge = buffer.treureMissatge();
                log("Servidor processant: " + missatge);
                try {
                    Thread.sleep(400); // Simular temps de processament
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "Processador-Servidor");

        procesadorMissatges.start();

        // Simular 8 clients que es connecten
        log("Simulant connexió de 8 clients...\n");
        for (int i = 1; i <= 8; i++) {
            ClientSimulat client = new ClientSimulat(i, buffer);
            executorService.submit(client); // Assignar client a un fil del pool

            try {
                Thread.sleep(200); // Temps entre connexions de clients
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Tancar l'ExecutorService de forma ordenada
        log("\nTancant servidor...");
        executorService.shutdown();

        try {
            // Esperar que tots els clients acabin (màxim 30 segons)
            if (executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                log("Tots els clients han estat processats correctament");
            } else {
                log("Timeout: alguns clients encara s'estan processant");
                executorService.shutdownNow();
            }

            // Esperar el processador de missatges
            procesadorMissatges.join();
            log("Servidor tancat correctament");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log("Error tancant el servidor: " + e.getMessage());
            executorService.shutdownNow();
        }
    }

    //   #######################################
    //  ###  MAIN - EXECUTA TOTES LES FITES  ###
    // #######################################

    /**
     * Mètode principal que executa totes les demostracions
     * Executa les 3 fites de la Fase 1 seqüencialment
     */
    public static void main(String[] args) {

        try {
            // Executar Fita 1: Fils bàsics
            demostrarFilsBasics();
            Thread.sleep(1000); // Pausa entre demostracions

            // Executar Fita 2: MessageBuffer
            demostrarMessageBuffer();
            Thread.sleep(1000); // Pausa entre demostracions

            // Executar Fita 3: ExecutorService
            demostrarExecutorService();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log("Error en l'execució: " + e.getMessage());
        }
    }
}