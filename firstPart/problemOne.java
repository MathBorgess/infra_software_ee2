package firstPart;

import java.util.concurrent.Semaphore;

//Problema 1: Sistema Bancário
// O banco criou um sistema para compartilhar uma mesma conta entre os membros de uma família.
// Onde é possível fazer depósitos e saques, que podem acontecer simultaneamente.
// Deseja-se manter a uniformidade do saldo da conta, que por sua vez é utilizada por mais de um membro da família.

// Sua tarefa é criar duas operações (saque e depósito) que alteram uma variável chamada saldo e garantir que um
// cliente não consiga sacar mais do que a conta tem de saldo. 

// Observações:
// Lembre-se de simular várias pessoas (threads) sacando e depositando simultaneamente na mesma conta.
// Crie uma thread para cada pessoa que deseja fazer uma operação.

class Person extends Thread {
    final Semaphore semaphore;
    final int amount;
    final boolean isDeposit;

    public Person(Semaphore semaphore, int amount, boolean isDeposit) {
        this.semaphore = semaphore;
        this.amount = amount;
        this.isDeposit = isDeposit;
    }

    @Override
    public void run() {
        try {
            semaphore.acquire();
            System.out
                    .println("Current balance: " + problemOne.accountBalance + " - " + (isDeposit ? "Deposit" : "Draft")
                            + " " + amount + " - " + Thread.currentThread().getName() + " is executing...;");
            if (isDeposit) {
                problemOne.accountBalance += amount;
            } else {
                if (problemOne.accountBalance < amount) {
                    System.out.println("Draft amount exceeds the balance");
                } else {
                    problemOne.accountBalance -= amount;
                }
            }
            semaphore.release();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

public class problemOne {
    public static int accountBalance = 1000;

    public static void main(String[] args) {
        Semaphore semaphore = new Semaphore(1);

        Person depositThread1 = new Person(semaphore, 100, true);
        Person depositThread2 = new Person(semaphore, 200, true);
        Person draftThread1 = new Person(semaphore, 300, false);
        Person draftThread2 = new Person(semaphore, 400, false);
        Person draftThread3 = new Person(semaphore, 700, false);
        Person draftThread4 = new Person(semaphore, 700, false);
        Person draftThread5 = new Person(semaphore, 700, false);

        depositThread1.start();
        depositThread2.start();
        draftThread1.start();
        draftThread2.start();
        draftThread3.start();
        draftThread4.start();
        draftThread5.start();

        try {
            depositThread1.join();
            depositThread2.join();
            draftThread1.join();
            draftThread2.join();
            draftThread3.join();
            draftThread4.join();
            draftThread5.join();
        } catch (InterruptedException e) {
            System.err.println(e);
        }

        System.out.println("Final balance: " + accountBalance);
    }
}
