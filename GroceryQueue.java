import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class GroceryQueue extends Thread {
	
	public int queue_length;
	public Semaphore cashier;
	public Customer [] lineup;
	public Semaphore queue_guard;
	public long customerTime;
	public int servingptr;
	public int SimulationTime;
	public Clock SimulationClock;
	public Lock lock;
	public Condition clockTicked;
	public int lineNumber;
	public QueueStats stats;
	public Condition CustomerIsHere;
	public Lock semlock = new ReentrantLock();


	public GroceryQueue(int queue_length) {
		this.queue_length = queue_length;
		this.queue_guard = new Semaphore(queue_length);
		this.lineup = new Customer[queue_length];
		this.cashier = new Semaphore(1);
		this.servingptr = 0;
	}
	
	public void run(){
		try {
			lock.lockInterruptibly();
			CustomerIsHere.await();
		} catch (InterruptedException e1) {
		}
		while(SimulationClock.getTick() < SimulationTime) {

			try {
				cashier.acquire();
				semlock.lock();
				if(queue_guard.availablePermits() < queue_length) {
					queue_guard.release();
				}
				else {
					CustomerIsHere.await();
					queue_guard.release();
				}
				semlock.unlock();
				int startServing = SimulationClock.getTick();
				
				while(SimulationClock.getTick() - startServing < lineup[servingptr].serving_duration) {
					clockTicked.await();	
				}
	
				int timeNeededtoServe = SimulationClock.getTick() - lineup[servingptr].arrival_time;
				stats.CustomerServed(timeNeededtoServe);
			}
			catch (InterruptedException e) {
			}
			finally {
				if(++servingptr == queue_length) {
					servingptr = 0;
				}
				cashier.release();
			}
		}
		
		lock.unlock();
	}
	

	
}
