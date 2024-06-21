package firstPart;

import java.util.Random;
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
                    .println("Current balance: " + AccountBalance.accountBalance + " - "
                            + (isDeposit ? "Deposit" : "Draft")
                            + " " + amount + " - " + Thread.currentThread().getName() + " is executing...;");
            if (isDeposit) {
                AccountBalance.accountBalance += amount;
            } else {
                if (AccountBalance.accountBalance < amount) {
                    System.out.println("Draft amount exceeds the balance");
                } else {
                    AccountBalance.accountBalance -= amount;
                }
            }
            semaphore.release();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

public class AccountBalance {
    public static int accountBalance = 1000;

    public static void main(String[] args) {
        Semaphore semaphore = new Semaphore(1);

        Random rand = new Random();

        for (int i = 0; i < rand.nextInt(20); i++) {
            int action = rand.nextInt(2);
            int amount = rand.nextInt(200);
            Person thread = new Person(semaphore, amount, action == 0);
            thread.start();
        }

        Person depositThread1 = new Person(semaphore, 100, true);
        Person depositThread2 = new Person(semaphore, 200, true);
        Person draftThread1 = new Person(semaphore, 300, false);
        Person draftThread2 = new Person(semaphore, 400, false);

        depositThread1.start();
        depositThread2.start();
        draftThread1.start();
        draftThread2.start();

        System.out.println("Final balance: " + accountBalance);
    }
}
