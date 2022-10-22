package nuber.students;

import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;

import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The core Dispatch class that instantiates and manages everything for Nuber
 *
 * @author james
 *
 */
public class NuberDispatch {

	/**
	 * The maximum number of idle drivers that can be awaiting a booking
	 */
	private final int MAX_DRIVERS = 999;

	private boolean logEvents = false;

	private boolean isShutdown = false;

	private int bookingId = 0;
	private int driversAwaiting = 0;

	// public ConcurrentMap<Integer, Integer> nextBookingId = new
	// ConcurrentHashMap<Integer, Integer>();
	// private ConcurrentMap<Integer, Integer> driversAwaiting = new
	// ConcurrentHashMap<Integer, Integer>();

	/**
	 * Stores all the idealDrivers in an bloking Queue.
	 * If a thread tries to take the driver and the queue is empty then
	 * it must wait until an element is added in the queue.
	 */
	public BlockingQueue<Driver> idealDrivers = new LinkedBlockingQueue<>();

	/**
	 * Stores all the instances of the regions proided to the dispatch in a hashmap.
	 * The key is the name of the region which maps to its instance.
	 */
	public HashMap<String, NuberRegion> allRegions = new HashMap<>();

	/**
	 * Creates a new dispatch objects and instantiates the required regions and any
	 * other objects required.
	 * It should be able to handle a variable number of regions based on the HashMap
	 * provided.
	 *
	 * @param regionInfo Map of region names and the max simultaneous bookings they
	 *                   can handle
	 * @param logEvents  Whether logEvent should print out events passed to it
	 */
	public NuberDispatch(HashMap<String, Integer> regionInfo, boolean logEvents) {
		System.out.println("Creating Nuber Dispatch");
		this.logEvents = logEvents;
		System.out.println("Creating " + regionInfo.size() + " regions");
		regionInfo.forEach(
				(key, value) -> {
					System.out.println("Creating " + key + " region");
					NuberRegion region = new NuberRegion(this, key, value);
					allRegions.put(key, region);
				});
		System.out.println("Done creating " + regionInfo.size() + " regions");

	}

	/**
	 * Adds drivers to a queue of idle driver.
	 *
	 * Must be able to have drivers added from multiple threads.
	 *
	 * @param The driver to add to the queue.
	 * @return Returns true if driver was added to the queue
	 */
	public boolean addDriver(Driver newDriver) throws InterruptedException {
		if (idealDrivers.size() <= MAX_DRIVERS) {
			idealDrivers.put(newDriver);
			// System.out.println("Driver Added : " + newDriver.name);
			return true;
		}
		return false;

	}

	/**
	 * Gets a driver from the front of the queue
	 *
	 * Must be able to have drivers added from multiple threads.
	 *
	 * @return A driver that has been removed from the queue
	 */
	public Driver getDriver() throws NoSuchElementException, InterruptedException {
		Driver removedDriverFromQueue = idealDrivers.take();
		driversAwaiting--;
		return removedDriverFromQueue;

	}

	/**
	 * Prints out the string
	 * booking + ": " + message
	 * to the standard output only if the logEvents variable passed into the
	 * constructor was true
	 *
	 * @param booking The booking that's responsible for the event occurring
	 * @param message The message to show
	 */
	public synchronized void logEvent(Booking booking, String message) {

		if (!logEvents)
			return;

		System.out.println(booking + ": " + message);

	}

	/**
	 * Books a given passenger into a given Nuber region.
	 *
	 * Once a passenger is booked, the getBookingsAwaitingDriver() should be
	 * returning one higher.
	 *
	 * If the region has been asked to shutdown, the booking should be rejected,
	 * and
	 * null returned.
	 *
	 * @param passenger The passenger to book
	 * @param region    The region to book them into
	 * @return returns a Future<BookingResult> object
	 */
	public Future<BookingResult> bookPassenger(Passenger passenger, String region) {
		NuberRegion currentRegion = allRegions.get(region);
		Future<BookingResult> result = currentRegion.bookPassenger(passenger);
		if (!isShutdown) {

			driversAwaiting++;
		}
		return result;

	}

	/**
	 * Gets the number of non-completed bookings that are awaiting a driver from
	 * dispatch
	 *
	 * Once a driver is given to a booking, the value in this counter should be
	 * reduced by one
	 *
	 * @return Number of bookings awaiting driver, across ALL regions
	 */
	public int getBookingsAwaitingDriver() {
		return driversAwaiting;
	}

	/**
	 * This function provides the unique sequential booking id for every booking
	 * It is called when a booking is created.
	 * It returns current valye of bookingId variable and increments it for the next
	 * booking object.
	 */
	public synchronized int getBookingId() {
		return ++bookingId;
	}

	/**
	 * Tells all regions to finish existing bookings already allocated, and stop
	 * accepting new bookings
	 */
	public void shutdown() {
		isShutdown = true;
		allRegions.forEach(
				(key, value) -> {
					value.shutdown();
				});
	}

}
