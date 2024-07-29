package thirdPart;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// Classe representando um passageiro de ônibus.
class BusPassager extends Thread {

    @Override
    public void run() {
        System.out.println("Passageiro " + this.getName() + " chegou ao ponto de ônibus.");
        try {
            while (true) {
                // Trava o lock para garantir acesso exclusivo ao bloco de código.
                BusProblem.lock.lock();
                if (BusProblem.BUS_STATE) {
                    // Se o ônibus está embarcando, o passageiro espera o próximo ônibus.
                    System.out.println("Passageiro " + this.getName() + " esperando o próximo ônibus.");
                    while (BusProblem.BUS_STATE) {
                        BusProblem.tryAgain.await();
                    }
                    BusProblem.lock.unlock();
                } else {
                    while (!BusProblem.BUS_STATE) {
                        // Passageiro entra na "bagunça" esperando pelo ônibus.
                        BusProblem.QUEUE_NUM++;
                        System.out.println("Passageiro " + this.getName() + " entrou na bagunça.");
                        BusProblem.readyToQueue.await();
                    }

                    // Tenta adquirir um assento no ônibus.
                    boolean hasEnter = BusProblem.seats.tryAcquire();
                    if (hasEnter) {
                        System.out.println("Passageiro " + this.getName() + " entrou no ônibus.");
                        break;
                    } else {
                        BusProblem.lock.unlock();
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // Destrava o lock ao final do bloco.
            BusProblem.lock.unlock();
        }

    }

}

public class BusProblem {
    // Número de passageiros na fila.
    public static int QUEUE_NUM = 0;
    // Constantes representando tempos de chegada e parada do ônibus.
    public static final int BUS_ARRIVAL_TIME = 100;
    public static final int BUS_STOP_TIME = 300;
    // Lock para garantir exclusividade no acesso a variáveis compartilhadas.
    public static final Lock lock = new ReentrantLock();
    // Condition para controlar a espera e sinalização entre threads.
    public static final Condition readyToQueue = lock.newCondition();
    public static final Condition tryAgain = lock.newCondition();
    // Estado do ônibus (FALSE = não chegou, TRUE = chegou).
    public static boolean BUS_STATE = false;

    // Semáforo para controlar o número de assentos no ônibus.
    public static final Semaphore seats = new Semaphore(50);

    public static void main(String[] args) {
        // Cria e inicia threads representando passageiros chegando ao ponto de ônibus.
        for (int i = 0; i < 60; i++) {
            new BusPassager().start();
        }

        // Simula a chegada e partida de ônibus.
        for (int i = 0; i < 4; i++) {
            System.out.println("Ônibus");
            try {
                Thread.sleep(100);
                // Trava o lock para manipular o estado do ônibus e a fila de passageiros.
                lock.lock();

                BUS_STATE = true;
                System.out.println("Ônibus chegou ao ponto.");
                if (BusProblem.QUEUE_NUM == 0) {
                    // Se não há passageiros, o ônibus parte imediatamente.
                    System.out.println("Nenhum passageiro no ponto. Ônibus parte imediatamente.");
                    BUS_STATE = false;
                } else {
                    // Sinaliza para todos os passageiros que o ônibus chegou.
                    readyToQueue.signalAll();
                    lock.unlock();
                    int newPassagers = 10;
                    Thread.sleep(BUS_ARRIVAL_TIME);

                    // Cria novas threads para simular novos passageiros chegando.
                    for (int j = 0; j < newPassagers; j++) {
                        new BusPassager().start();
                    }

                    // Trava o lock novamente para manipular o estado do ônibus e a fila de
                    // passageiros.
                    lock.lock();
                    BUS_STATE = false;
                    System.out.println("Ônibus partiu.");
                    tryAgain.signalAll();
                    BusProblem.QUEUE_NUM = 0;
                    lock.unlock();

                    Thread.sleep(BUS_STOP_TIME);

                    // Trava o lock para ajustar a quantidade de assentos disponíveis.
                    lock.lock();
                    seats.release(50 - seats.availablePermits());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                // Destrava o lock ao final do bloco.
                lock.unlock();
            }
        }
    }

}
