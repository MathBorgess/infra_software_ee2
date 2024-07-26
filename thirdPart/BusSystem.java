import java.util.concurrent.*;

// Classe que representa um passageiro que tenta embarcar no ônibus
class Passenger implements Runnable {
    private final int id; // Identificador único do passageiro
    private final BusStop busStop; // Referência ao ponto de ônibus

    // Construtor que inicializa o ID do passageiro e o ponto de ônibus
    public Passenger(int id, BusStop busStop) {
        this.id = id;
        this.busStop = busStop;
    }

    // Método run que será executado quando a thread for iniciada
    @Override
    public void run() {
        try {
            // Passageiro chega ao ponto de ônibus e espera para embarcar
            busStop.arriveAtBusStop(this);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Interrompe a thread se ocorrer uma interrupção
        }
    }

    // Método para obter o ID do passageiro
    public int getId() {
        return id;
    }
}

// Classe que representa o ponto de ônibus onde os passageiros esperam
class BusStop {
    private static final int BUS_CAPACITY = 50; // Capacidade do ônibus
    private final Semaphore seats = new Semaphore(BUS_CAPACITY); // Semáforo para gerenciar os assentos disponíveis no ônibus
    private final Semaphore boarding = new Semaphore(0); // Semáforo para gerenciar o embarque dos passageiros
    private final ConcurrentLinkedQueue<Passenger> waitingPassengers = new ConcurrentLinkedQueue<>(); // Fila concorrente de passageiros esperando

    // Método chamado pelo passageiro quando chega ao ponto de ônibus
    public void arriveAtBusStop(Passenger passenger) throws InterruptedException {
        waitingPassengers.add(passenger); // Adiciona o passageiro à fila de espera
        System.out.println("Passageiro " + passenger.getId() + " está esperando pelo ônibus.");
        boarding.acquire(); // Espera até que o semáforo permita tentar embarcar
        tryBoarding(passenger); // Tenta embarcar o passageiro
    }

    // Método que simula a chegada do ônibus ao ponto
    public void busArrives() throws InterruptedException {
        System.out.println("Ônibus chega ao ponto.");
        if (waitingPassengers.isEmpty()) {
            System.out.println("Nenhum passageiro no ponto. Ônibus parte imediatamente.");
            return; // Se não houver passageiros esperando, o ônibus parte imediatamente
        }

        seats.release(BUS_CAPACITY - seats.availablePermits()); // Reseta os assentos disponíveis
        System.out.println("Ônibus está embarcando passageiros.");

        while (seats.availablePermits() > 0 && !waitingPassengers.isEmpty()) {
            Passenger passenger = waitingPassengers.poll(); // Pega o próximo passageiro da fila de espera
            if (passenger != null) {
                boarding.release(); // Sinaliza que um passageiro pode tentar embarcar
                Thread.sleep(10); // Pequeno atraso para simular o tempo de embarque
            }
        }

        System.out.println("Ônibus parte do ponto.");
    }

    // Método que tenta embarcar um passageiro no ônibus
    private void tryBoarding(Passenger passenger) {
        if (seats.tryAcquire()) { // Tenta adquirir um assento no ônibus
            System.out.println("Passageiro " + passenger.getId() + " embarcou no ônibus.");
        } else {
            System.out.println("Passageiro " + passenger.getId() + " tem que esperar pelo próximo ônibus.");
            waitingPassengers.add(passenger); // Adiciona de volta à fila de espera se não houver assentos disponíveis
        }
    }
}

// Classe principal que executa a simulação
public class BusSystem {
    public static void main(String[] args) {
        BusStop busStop = new BusStop(); // Cria um ponto de ônibus
        ExecutorService passengerService = Executors.newCachedThreadPool(); // Cria um pool de threads para gerenciar os passageiros

        // Simula a chegada dos passageiros ao ponto de ônibus
        for (int i = 1; i <= 100; i++) {
            passengerService.submit(new Passenger(i, busStop)); // Submete um novo passageiro ao pool de threads
        }

        // Cria uma tarefa para simular a chegada do ônibus periodicamente
        Runnable busArrival = () -> {
            try {
                while (true) {
                    busStop.busArrives(); // Ônibus chega ao ponto
                    Thread.sleep(3000); // Espera 3 segundos antes do próximo ônibus chegar
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Interrompe a thread se ocorrer uma interrupção
            }
        };

        Thread busThread = new Thread(busArrival); // Cria uma nova thread para a chegada do ônibus
        busThread.start(); // Inicia a thread do ônibus

        // Desliga o serviço de passageiros após um certo período de tempo
        try {
            Thread.sleep(60000); // Executa a simulação por 1 minuto
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Interrompe a thread se ocorrer uma interrupção
        }
        passengerService.shutdownNow(); // Encerra o pool de threads dos passageiros
        busThread.interrupt(); // Interrompe a thread do ônibus
    }
}
