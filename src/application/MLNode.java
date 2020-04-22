package application;

public class MLNode {
	
	private double forwardProp;
	private double activationFunc;
	private String name;
	
	public void forwardPropegate() {
		
	}
	
	public void activate() {
		
		
		
	}
	
	public String getName() {
		return name;
	}
	
	public MLNode(String name) {
		this.name = name;
	}
	public void setForward(double forwardProp) {
		this.forwardProp = forwardProp;
	}
	public double getForward() {
		return forwardProp;
	}
	public void setActivation(double activationFunc) {
		this.activationFunc = activationFunc;
	}
	public double getActivation() {
		return activationFunc;
	}
}
