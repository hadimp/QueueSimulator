import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BankQueue extends Thread{
	
	public int num_tellers;
	public int queue_length;
	public Semaphore teller;
	public Customer [] lineup;
	public Semaphore queue_guard;
	public int servingptr;
	public Clock SimulationClock;
	public QueueStats stats;
	public Lock lock;
	public Condition clockTicked;
	public Condition CustomerIsHere;
	public int SimulationTime;
	public Lock semlock = new ReentrantLock();

	public BankQueue(int num_tellers) {
		this.num_tellers = num_tellers;
		this.teller = new Semaphore(num_tellers);
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
				teller.acquire();
				semlock.lock();
				if(queue_guard.availablePermits() != queue_length) {
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
				if(servingptr == queue_length) {
					servingptr = 0;
				}
				teller.release();
			}
		}
		lock.unlock();
			
	}

	
}
