package thirdPart;

import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// Enumeração para representar os gêneros das pessoas.
enum Gender {
    NONE(0), MALE(1), FEMALE(2);

    private final int value;

    Gender(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        switch (this) {
            case MALE -> {
                return "masculino";
            }
            case FEMALE -> {
                return "feminino";
            }
            default -> {
                return "não definido";
            }
        }
    }
}

// Classe representando uma pessoa que usará o banheiro.
class BathroomPerson extends Thread {
    private final Gender gender;
    private final int timeOnBathroom;

    public BathroomPerson(Gender personGender, int time) {
        this.gender = personGender;
        this.timeOnBathroom = time;
    }

    public String name() {
        return this.getName() + " do sexo " + this.gender.toString();
    }

    @Override
    public void run() {
        System.out.println(this.name()
                + " tenta entrar no banheiro que só tem " + UnisexBathroom.currentGender.toString() + "s dentro.");

        try {
            // Adquire o semáforo para controlar o acesso ao banheiro.
            UnisexBathroom.enterBathroom.acquire();
            // Trava o lock para garantir acesso exclusivo ao bloco de código.
            UnisexBathroom.lock.lock();
            // Espera enquanto o banheiro não está disponível para o gênero atual ou a
            // capacidade está cheia.
            while ((UnisexBathroom.currentGender != this.gender || UnisexBathroom.sameGenderCapacity > 2)
                    && UnisexBathroom.currentGender != Gender.NONE) {
                UnisexBathroom.sameGender.await();
            }
            if (UnisexBathroom.currentGender == Gender.NONE) {
                UnisexBathroom.currentGender = this.gender;
                System.out.println(this.name() + " trocando para " + this.gender.toString() +
                        " e ficará "
                        + this.timeOnBathroom + " ms.");
            } else if (UnisexBathroom.currentGender == this.gender) {
                System.out.println(this.name() + " entrou no banheiro e ficará "
                        + this.timeOnBathroom + " ms, com mais " + (2 -
                                UnisexBathroom.sameGenderCapacity)
                        + " pessoas.");
            }
            // Incrementa a capacidade de pessoas do mesmo gênero no banheiro.
            UnisexBathroom.sameGenderCapacity++;
            // Libera o semáforo para permitir que outras threads tentem entrar.
            UnisexBathroom.enterBathroom.release();
        } catch (Exception e) {
            // TODO: handle exception
        } finally {
            // Destrava o lock ao final do bloco.
            UnisexBathroom.lock.unlock();
        }
        try {
            // Simula o tempo que a pessoa passa no banheiro.
            Thread.sleep(this.timeOnBathroom);

            // Trava o lock para manipular a capacidade e o gênero atual do banheiro.
            UnisexBathroom.lock.lock();
            System.out.println(this.name() + " saiu no banheiro.");
            UnisexBathroom.sameGenderCapacity--;
            if (UnisexBathroom.sameGenderCapacity == 0) {
                System.out.println("ESVAZEOU");

                UnisexBathroom.currentGender = Gender.NONE;
            }
            // Sinaliza todas as threads esperando que o banheiro esvaziou, que pela
            // definição do semáforo, será apenas uma
            UnisexBathroom.sameGender.signalAll();
        } catch (Exception e) {
            // TODO: handle exception
        } finally {
            // Destrava o lock ao final do bloco.
            UnisexBathroom.lock.unlock();
        }
    }
}

public class UnisexBathroom {
    // Gênero atual das pessoas no banheiro.
    public static Gender currentGender = Gender.NONE;
    // Capacidade de pessoas do mesmo gênero no banheiro.
    public static int sameGenderCapacity = 0;
    // Semáforo para controlar a entrada no banheiro.
    public static Semaphore enterBathroom = new Semaphore(1);
    // Lock para garantir exclusividade no acesso a variáveis compartilhadas.
    static final Lock lock = new ReentrantLock();
    // Condition para controlar a espera e sinalização entre threads.
    static final Condition sameGender = UnisexBathroom.lock.newCondition();

    public static void main(String[] args) throws InterruptedException {
        Random rand = new Random();
        // Cria e inicia threads representando pessoas tentando usar o banheiro.
        for (int i = 0; i < 100; i++) {
            BathroomPerson person = new BathroomPerson(Gender.values()[rand.nextInt(2) + 1], 100 * rand.nextInt(7));
            person.start();
            Thread.sleep(100);
        }
    }
}
