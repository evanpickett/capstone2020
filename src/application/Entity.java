package application;

import java.util.Random;

public abstract class Entity {

	
	public abstract void move(double x, double y);
	public abstract void die();
	public abstract void step(long deltaTime, long time, double gameSpeed);
	public abstract void create(Random rand);
	
}
