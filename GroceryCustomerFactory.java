import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class GroceryCustomerFactory extends Thread{
	public int queue_length;
	public int num_cashiers;
	public Customer [][] lineup;
	public Semaphore [] queue_guard;
	public int [] adder;
	public int SimulationTime;
	public Clock SimulationClock;
	public Lock lock;
	public Condition clockTicked;
	public Condition CustomerIsHere;
	public QueueStats stats;
	
	public GroceryCustomerFactory(int num_cashiers) {
		this.num_cashiers = num_cashiers;
		this.adder = new int[num_cashiers];
	}
	
	
	public void run() {
		try {
			lock.lockInterruptibly();
		} catch (InterruptedException e1) {
		}

		while(SimulationClock.getTick() < SimulationTime) {
			int waitingDuration = getRandomNumber(20, 60);
			int StartWaitingForNewCustomer = SimulationClock.getTick();
			
			while(SimulationClock.getTick() - StartWaitingForNewCustomer < waitingDuration) {
				try {
					clockTicked.await();
				} catch (InterruptedException e) {	
				}
			}
			
			int  shortestLineup = getMaxIndex(queue_guard);
			stats.newCustomerArrived();
		    try {
				if(queue_guard[shortestLineup].tryAcquire()){
					lineup[shortestLineup][adder[shortestLineup]].arrival_time = SimulationClock.getTick();
					lineup[shortestLineup][adder[shortestLineup]].serving_duration = getRandomNumber(60, 300);
					
					if(++adder[shortestLineup] == queue_length) {
						adder[shortestLineup] = 0;
					}
					CustomerIsHere.signal();
				}
				else {
					boolean gotIn = false;
					int StartWaitingForQueue = SimulationClock.getTick();
					int waitingDurationForQueue = 10;
					
					while(SimulationClock.getTick() - StartWaitingForQueue < waitingDurationForQueue) {
						
						clockTicked.await();
						if(queue_guard[shortestLineup].tryAcquire()) {
							gotIn = true;
							lineup[shortestLineup][adder[shortestLineup]].arrival_time = StartWaitingForQueue;
							lineup[shortestLineup][adder[shortestLineup]].serving_duration = getRandomNumber(60, 300);
							
							if(++adder[shortestLineup] == queue_length) {
								adder[shortestLineup] = 0;
							}
							CustomerIsHere.signal();
							break;
						}
					}
					if(!gotIn) {
						stats.CustomerTurnedAway();
					}

				}
			} catch (InterruptedException e) {
			}
		}
		lock.unlock();

	}

	 public int getMaxIndex(Semaphore[] inputArray){ 
		 int maxValue = inputArray[0].availablePermits(); 
		 int maxIndex = 0;
		 
		 for(int i=0; i < inputArray.length; i++){ 
			
			 if(inputArray[i].availablePermits() > maxValue){ 
				 maxIndex = i; 
				 maxValue = inputArray[i].availablePermits();
			 } 
			 else if (inputArray[i].availablePermits() == maxValue) {
				 int [] randomArray = {maxIndex, i};
				 Random rand = new Random();
				 int int_random = rand.nextInt(2);
				 maxIndex = randomArray[int_random];
			 }
		    	  
		}	 
	    return maxIndex;
	} 
	 
	public int getRandomNumber(int min, int max) {
		return (int) ((Math.random() * (max - min)) + min);
	}
	 
}
