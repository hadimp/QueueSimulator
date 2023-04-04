import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MainProgram implements Runnable {
	
	public int num_tellers = 3;
	public int queue_length = 5;
	public int SimulationTime = 7200;
	public BankQueue BQ = new BankQueue(num_tellers);
	public Clock SimulationClock = new Clock();
	public int servingDuration;
	public Semaphore queue_guard = new Semaphore(queue_length);
	public Customer [] lineup = new Customer [queue_length];
	public CustomerFactory CustomerFactory = new CustomerFactory();
	public QueueStats Qstats = new QueueStats();
	public Lock QClockLock = new ReentrantLock();
	public Condition clockTicked = QClockLock.newCondition();
	public Condition CustomerIsHere = QClockLock.newCondition();
	public int num_cashiers = 3;
	public int GroceryQueue_length = 2;
	public GroceryCustomerFactory GCQ = new GroceryCustomerFactory(num_cashiers);
	public Customer [][] GroceryLineup = new Customer [num_cashiers][GroceryQueue_length];
	public GroceryQueue [] GQ = new GroceryQueue[num_cashiers];
	public Semaphore [] GroceryQueue_guard = new Semaphore[num_cashiers];

	public void run(){

		for (int i = 0; i < queue_length; i++) {
			lineup[i] = new Customer();
		}
		
		BQ.SimulationClock = SimulationClock;
		BQ.queue_length = queue_length;
		BQ.queue_guard = queue_guard;
		BQ.lineup = lineup;
		BQ.stats = Qstats;
		BQ.lock = QClockLock;
		BQ.clockTicked = clockTicked;
		BQ.CustomerIsHere = CustomerIsHere;
		BQ.SimulationTime = SimulationTime;
		
		CustomerFactory.SimulationClock = SimulationClock;
		CustomerFactory.queue_length = queue_length;
		CustomerFactory.lineup = lineup;
		CustomerFactory.queue_guard = queue_guard;
		CustomerFactory.stats = Qstats;
		CustomerFactory.lock = QClockLock;
		CustomerFactory.clockTicked = clockTicked;
		CustomerFactory.CustomerIsHere = CustomerIsHere;
		CustomerFactory.SimulationTime = SimulationTime;
		
		CustomerFactory.start();
		BQ.start();

		while(SimulationClock.getTick() < SimulationTime) {
			try {
				Thread.sleep(50);
				QClockLock.lockInterruptibly();
				SimulationClock.tick();
				clockTicked.signalAll();
			} catch (InterruptedException e) {
			}
			finally {
				QClockLock.unlock();
			}

		}
		
		int totalBankCustomersArrived = Qstats.getTotalCustomersArrived();
		int numBankCustomersTurnedAway = Qstats.getNumCustomersTurnedAway();
		int totalBankCustomerServeTime = Qstats.getTotalCustomerServeTime();
		int totalBankCustomersServed = Qstats.getTotalCustomersServed();
		float averageBankServeTime = ((float) totalBankCustomerServeTime)/totalBankCustomersServed;
		
		
		System.out.println("Total Number of Bank Customers Arrived: " + totalBankCustomersArrived);
		System.out.println("Number of Bank Customers Turned Away: " + numBankCustomersTurnedAway);
		System.out.println("Average Time to Serve a Bank Customer: " + averageBankServeTime + " seconds.");
		
		SimulationClock.reset();
		Qstats.resetStats();
		
		for (int i = 0; i < num_cashiers; i++) {
			GroceryQueue_guard[i] = new Semaphore(GroceryQueue_length);
			GQ[i] = new GroceryQueue(GroceryQueue_length);
			GQ[i].SimulationClock = SimulationClock;
			GQ[i].queue_length = GroceryQueue_length;
			GQ[i].queue_guard = GroceryQueue_guard[i];
			GQ[i].lock = QClockLock;
			GQ[i].clockTicked = clockTicked;
			GQ[i].CustomerIsHere = CustomerIsHere;
			GQ[i].SimulationTime = SimulationTime;
			GQ[i].stats = Qstats;
		}
		
		GCQ.lineup = new Customer[num_cashiers][GroceryQueue_length];
		
		for (int i = 0; i < num_cashiers; i++) {
			for(int j = 0; j < GroceryQueue_length; j++) {
				GroceryLineup[i][j] = new Customer();
				GCQ.lineup[i][j] = GroceryLineup[i][j];
				GQ[i].lineup[j] = GCQ.lineup[i][j];
			}
		}
		

				
		GCQ.SimulationClock = SimulationClock;
		GCQ.SimulationTime = SimulationTime;
		GCQ.queue_length = GroceryQueue_length;
		GCQ.queue_guard = GroceryQueue_guard;
		GCQ.stats = Qstats;
		GCQ.lock = QClockLock;
		GCQ.clockTicked = clockTicked;
		GCQ.CustomerIsHere = CustomerIsHere;
		GCQ.SimulationTime = SimulationTime;
		
		GCQ.start();
		for(int i = 0; i < num_cashiers; i++) {
			GQ[i].start();
		}
		
		while(SimulationClock.getTick() < SimulationTime) {
			try {
				Thread.sleep(50);
				QClockLock.lockInterruptibly();
				SimulationClock.tick();
				clockTicked.signalAll();
			} catch (InterruptedException e) {
			}
			finally {
				QClockLock.unlock();
			}

		}
		
		int totalGroceryCustomersArrived = Qstats.getTotalCustomersArrived();
		int numGroceryCustomersTurnedAway = Qstats.getNumCustomersTurnedAway();
		int totalGroceryCustomerServeTime = Qstats.getTotalCustomerServeTime();
		int totalGroceryCustomersServed = Qstats.getTotalCustomersServed();
		float averageGroceryServeTime = ((float) totalGroceryCustomerServeTime)/totalGroceryCustomersServed;
		
		System.out.println("\nTotal Number of Grocery Customers Arrived: " + totalGroceryCustomersArrived);
		System.out.println("Number of Grocery Customers Turned Away: " + numGroceryCustomersTurnedAway);
		System.out.println("Average Time to Serve a Grocery Customer: " + averageGroceryServeTime + " seconds.");
		
		
		
	}
}
