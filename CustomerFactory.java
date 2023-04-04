import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class CustomerFactory extends Thread{
	public Semaphore queue_guard;
	public Customer [] lineup;
	public Clock SimulationClock;
	public int addptr = 0;
	public int queue_length;
	public QueueStats stats;
	public Lock lock;
	public Condition clockTicked;
	public Condition CustomerIsHere;
	public int SimulationTime;


	public void run(){
		try {
			lock.lockInterruptibly();
		} catch (InterruptedException e1) {
		}
		while(SimulationClock.getTick() < SimulationTime) {
			try {
				int waitingDuration = getRandomNumber(20, 60);
				int StartWaitingForNewCustomer = SimulationClock.getTick();
				
				while(SimulationClock.getTick() - StartWaitingForNewCustomer < waitingDuration) {
					try {
						clockTicked.await();
					} catch (InterruptedException e) {	
					}
				}

				stats.newCustomerArrived();
				if(queue_guard.tryAcquire()) {
	
					lineup[addptr].arrival_time = SimulationClock.getTick();
					int servingDuration = getRandomNumber(60, 300);
					lineup[addptr].serving_duration = servingDuration;
						
					if(++addptr == queue_length) {
						addptr = 0;
					}
					CustomerIsHere.signalAll();
					}
				else {
					stats.CustomerTurnedAway();
				}
			}
			finally {
				
			}
		}
		
		lock.unlock();

			
	}
	
		
	public int getRandomNumber(int min, int max) {
	    return (int) ((Math.random() * (max - min)) + min);
	}
}
