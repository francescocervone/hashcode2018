import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
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
			Ride ride = new Ride(new Point(a, b), new Point(x, y), earliestStart, latestFinish);
			rides.add(ride);
		}

		for (int i = 0; i < nCars; i++) {
			cars.add(new Car(new Point(0, 0)));
		}


	}
}


class Car {
	Point currentPosition;

	public Car(Point currentPosition) {
		this.currentPosition = currentPosition;
	}
}

class Ride {
	final Point start;
	final Point end;
	final int earliestStart;
	final int latestFinish;

	Ride(Point start, Point end, int earliestStart, int latestFinish) {
		this.start = start;
		this.end = end;
		this.earliestStart = earliestStart;
		this.latestFinish = latestFinish;
	}
}

class Point {
	final int x;
	final int y;

	Point(int x, int y) {
		this.x = x;
		this.y = y;
	}
}
