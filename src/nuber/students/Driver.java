package nuber.students;

public class Driver extends Person {

	private Passenger passenger;

	public Driver(String driverName, int maxSleep) {
		super(driverName, maxSleep);

	}

	public int getTravelTime() {
		return (int) (Math.random() * maxSleep);
	}

	/**
	 * Stores the provided passenger as the driver's current passenger and then
	 * sleeps the thread for between 0-maxDelay milliseconds.
	 * 
	 * @param newPassenger Passenger to collect
	 * @throws InterruptedException
	 */
	public void pickUpPassenger(Passenger newPassenger) throws InterruptedException {
		passenger = newPassenger;
		int timeToGetToPassenger = getTravelTime();
		Thread.sleep(timeToGetToPassenger);
	}

	/**
	 * Sleeps the thread for the amount of time returned by the current
	 * passenger's getTravelTime() function
	 * 
	 * @throws InterruptedException
	 */
	public void driveToDestination() throws InterruptedException {
		int travelTimeForPassenger = passenger.getTravelTime();
		Thread.sleep(travelTimeForPassenger);
	
	}

}
