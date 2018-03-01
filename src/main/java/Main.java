import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

public class Main {
	public static void main(String[] args) throws IOException {
		run("a_example.in");
		run("b_should_be_easy.in");
		run("c_no_hurry.in");
		run("d_metropolis.in");
		run("e_high_bonus.in");
	}

	private static void run(String file) throws IOException {
		Scanner scanner = new Scanner(new FileInputStream(new File(file)));
		int rows = scanner.nextInt();
		int columns = scanner.nextInt();
		int nCars = scanner.nextInt();
		int nRides = scanner.nextInt();
		int bonus = scanner.nextInt();
		int steps = scanner.nextInt();

		List<Ride> rides = new ArrayList<>();

		List<Car> cars = new ArrayList<>();

		for (int i = 0; i < nRides; i++) {
			int a = scanner.nextInt();
			int b = scanner.nextInt();
			int x = scanner.nextInt();
			int y = scanner.nextInt();
			int earliestStart = scanner.nextInt();
			int latestFinish = scanner.nextInt();
			Ride ride = new Ride(i, new Point(a, b), new Point(x, y), earliestStart, latestFinish);
			rides.add(ride);
		}

		for (int i = 0; i < nCars; i++) {
			cars.add(new Car(new Point(0, 0)));
		}

		for (int i = 0; i < steps; i++) {
			int currentStep = i;
			for (Car car : cars) {
				if (car.isAvailable()) {
					PriorityQueue<Ride> priorityQueue = new PriorityQueue<>(new UtilityFunction(currentStep, bonus, car));
					for (Ride ride : rides) {
						int time = i +
								distance(car.currentPosition, ride.start) +
								distance(ride.start, ride.end);
						if (time < ride.latestFinish) {
							priorityQueue.add(ride);
						}
					}
					Ride ride = priorityQueue.poll();
					if (ride != null) {
						rides.remove(ride);
						car.currentRide = ride;
						car.state = CarState.GOING_TO_START;
					}
				}
			}

			for (Car car : cars) {
				switch (car.state) {
					case GOING_TO_START:
						if (car.currentPosition.equals(car.currentRide.start)) {
							if (i >= car.currentRide.earliestStart) {
								car.currentPosition = makeStep(car.currentPosition, car.currentRide.end);
								car.state = CarState.GOING_TO_FINISH;
							} else {
								// wait
							}
						} else {
							car.currentPosition = makeStep(car.currentPosition, car.currentRide.start);
						}
						break;
					case GOING_TO_FINISH:
						if (car.currentPosition.equals(car.currentRide.end)) {
							Ride ride = car.currentRide;
							car.history.add(ride.index);
							car.currentRide = null;
							car.state = CarState.AVAILABLE;
						} else {
							car.currentPosition = makeStep(car.currentPosition, car.currentRide.end);
						}
						break;
					case AVAILABLE:
						// Do nothing
						break;
				}
			}
		}

		File output = new File(file + ".txt");
		StringBuilder stringBuilder = new StringBuilder();
		for (Car car : cars) {
			stringBuilder
					.append(car.history.size())
					.append(" ")
					.append(join(" ", car.history))
					.append("\n");
			System.out.println(stringBuilder.toString());
			FileUtils.write(output, stringBuilder.toString(), Charset.defaultCharset());
		}
	}

	static int distance(Point a, Point b) {
		return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
	}

	static Point makeStep(Point start, Point end) {
		Point point = new Point(start.x, start.y);
		if (start.x > end.x) {
			point.x--;
		} else if (start.x < end.x) {
			point.x++;
		} else if (start.y > end.y) {
			point.y--;
		} else if (start.y < end.y) {
			point.y++;
		}
		return point;
	}

	public static String join(CharSequence delimiter, Iterable tokens) {
		StringBuilder sb = new StringBuilder();
		boolean firstTime = true;
		for (Object token : tokens) {
			if (firstTime) {
				firstTime = false;
			} else {
				sb.append(delimiter);
			}
			sb.append(token);
		}
		return sb.toString();
	}
}


class Car {
	Point currentPosition;
	Ride currentRide;
	List<Integer> history = new ArrayList<>();
	CarState state = CarState.AVAILABLE;

	public Car(Point currentPosition) {
		this.currentPosition = currentPosition;
	}

	boolean isAvailable() {
		return state == CarState.AVAILABLE;
	}

	void addRide(int index) {
		history.add(index);
	}
}

class Ride {
	int index;
	final Point start;
	final Point end;
	final int earliestStart;
	final int latestFinish;

	Ride(int index, Point start, Point end, int earliestStart, int latestFinish) {
		this.index = index;
		this.start = start;
		this.end = end;
		this.earliestStart = earliestStart;
		this.latestFinish = latestFinish;
	}
}

class Point {
	int x;
	int y;

	Point(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Point point = (Point) o;
		return x == point.x &&
				y == point.y;
	}

	@Override
	public int hashCode() {

		return Objects.hash(x, y);
	}
}

enum CarState {
	AVAILABLE,
	GOING_TO_START,
	GOING_TO_FINISH
}

class LengthComparator implements Comparator<Ride> {

	@Override
	public int compare(Ride ride1, Ride ride2) {
		int ride1Length = Main.distance(ride1.start, ride1.end);
		int ride2Length = Main.distance(ride2.start, ride2.end);

		return ride2Length - ride1Length;
	}
}

class LengthAndBonusComparator implements Comparator<Ride> {
	private final int currentStep;
	private final int bonus;
	private Car car;

	public LengthAndBonusComparator(int currentStep, int bonus, Car car) {
		this.currentStep = currentStep;
		this.bonus = bonus;
		this.car = car;
	}

	@Override
	public int compare(Ride ride1, Ride ride2) {
		int ride1Length = Main.distance(ride1.start, ride1.end);
		int ride2Length = Main.distance(ride2.start, ride2.end);

		int ride1StartTime = Main.distance(car.currentPosition, ride1.start);
		int bonus1 = (currentStep + ride1StartTime < ride1.earliestStart) ? bonus : 0;
		int ride2StartTime = Main.distance(car.currentPosition, ride2.start);
		int bonus2 = (currentStep + ride2StartTime < ride2.earliestStart) ? bonus : 0;

		return (ride2Length + bonus2) - (ride1Length + bonus1);
	}
}

class SmallestRideComparator implements Comparator<Ride> {
	private Car car;

	public SmallestRideComparator(Car car) {
		this.car = car;
	}

	@Override
	public int compare(Ride ride1, Ride ride2) {
		int ride1Length = Main.distance(ride1.start, ride1.end);
		int ride2Length = Main.distance(ride2.start, ride2.end);

		int ride1StartTime = Main.distance(car.currentPosition, ride1.start);
		int ride2StartTime = Main.distance(car.currentPosition, ride2.start);

		return (ride1Length + ride1StartTime) - (ride2Length + ride2StartTime);
	}
}

class SmallestRideAndBonusComparator implements Comparator<Ride> {
	private final int currentStep;
	private final int bonus;
	private Car car;

	public SmallestRideAndBonusComparator(int currentStep, int bonus, Car car) {
		this.currentStep = currentStep;
		this.bonus = bonus;
		this.car = car;
	}

	@Override
	public int compare(Ride ride1, Ride ride2) {
		int ride1Length = Main.distance(ride1.start, ride1.end);
		int ride2Length = Main.distance(ride2.start, ride2.end);

		int ride1StartTime = Main.distance(car.currentPosition, ride1.start);
		int bonus1 = (currentStep + ride1StartTime < ride1.earliestStart) ? bonus : 0;
		int ride2StartTime = Main.distance(car.currentPosition, ride2.start);
		int bonus2 = (currentStep + ride2StartTime < ride2.earliestStart) ? bonus : 0;

		return (ride1Length + ride1StartTime - bonus1) - (ride2Length + ride2StartTime - bonus2);
	}
}

class UtilityFunction implements Comparator<Ride> {
	private final int currentStep;
	private final int bonus;
	private final Car car;

	public UtilityFunction(int currentStep, int bonus, Car car) {
		this.currentStep = currentStep;
		this.bonus = bonus;
		this.car = car;
	}

	@Override
	public int compare(Ride ride1, Ride ride2) {
		int ride1Length = Main.distance(ride1.start, ride1.end);
		int ride2Length = Main.distance(ride2.start, ride2.end);

		int ride1StartTime = Main.distance(car.currentPosition, ride1.start);
		int bonus1 = (currentStep + ride1StartTime < ride1.earliestStart) ? bonus : 0;
		int ride2StartTime = Main.distance(car.currentPosition, ride2.start);
		int bonus2 = (currentStep + ride2StartTime < ride2.earliestStart) ? bonus : 0;

		int wait1 = 0;
		if (currentStep + ride1StartTime < ride1.earliestStart) {
			wait1 = ride1.earliestStart - (currentStep + ride1StartTime);
		}

		int wait2 = 0;
		if (currentStep + ride2StartTime < ride2.earliestStart) {
			wait2 = ride2.earliestStart - (currentStep + ride2StartTime);
		}

		int u1 = ride1Length + bonus1 - wait1;
		int u2 = ride2Length + bonus2 - wait2;

		return u2 - u1;
	}
}