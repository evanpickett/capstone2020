package application;

import java.util.Random;

public class MLAttachment {
	private MLNode connectionBack;
	private MLNode connectionFront;
	private double weight;
	private int rowNumber;
	private int id;
	
	public MLAttachment(MLNode connectionBack,MLNode connectionFront, int rowNumber, int id) {
		this.connectionBack = connectionBack;
		this.connectionFront = connectionFront;
		this.rowNumber = rowNumber;
		this.id = id;
		weight = (new Random()).nextDouble()*2 - 1;
	}
	public MLAttachment(MLNode connectionBack,MLNode connectionFront, int rowNumber, int id, double weight) {
		this.connectionBack = connectionBack;
		this.connectionFront = connectionFront;
		this.rowNumber = rowNumber;
		this.id = id;
		this.weight = weight;
	}
	public MLAttachment(MLNode connectionBack,MLNode connectionFront, int rowNumber, int id, double [][] weights) {
		this.connectionBack = connectionBack;
		this.connectionFront = connectionFront;
		this.rowNumber = rowNumber;
		this.id = id;
		this.weight = weights!=null ? weights[rowNumber][id] : new Random().nextDouble()*2-1;
	}
	
	public double getWeight() {
		return weight;
	}
	public void setWeight(double n) {
		weight = n;
	}
	
	public int getRow() {
		return rowNumber;
	}
	public int getId() {
		return id;
	}
	
	public MLNode getBackConnection() {
		return connectionBack;
	}
	
	public MLNode getFrontConnection() {
		return connectionFront;
	}
}
