package application;

public class DoublePair {
	double first = 0;
	double second = 0;
	
	public DoublePair(double x, double y) {
		first = x;
		second = y;
	}
	
	public void divide(double divisor) {
		if (divisor == 0) { return; }
		first/=divisor;
		second/=divisor;
	}
	
	public void multiply(double factor) {
		first*=factor;
		second*=factor;
	}
	
	public double getMagnitude() {
		return Math.sqrt(Math.pow(first, 2)+Math.pow(second, 2));
	}
	
	public double getDistance(DoublePair a) {
		return new DoublePair(first-a.first,second-a.second).getMagnitude();
	}
}
