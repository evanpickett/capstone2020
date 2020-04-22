package application;

import java.util.Random;

import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Seed extends Entity {
	
	public Circle c;
	private BorderPane root;
	double growTime = 0;
	boolean dead = false;
	boolean plant = false;
	
	public Seed(BorderPane root,double centerX,double centerY) {
		c = new Circle(centerX,centerY,10,Color.GREENYELLOW);
		this.root = root;
		root.getChildren().add(c);
	}
	
	@Override
	public void move(double x, double y) {
		c.setTranslateX(x);
		c.setTranslateY(y);
	}

	@Override
	public void die() {
		dead = true;
		root.getChildren().remove(c);
	}

	@Override
	public void step(long deltaTime, long time, double gameSpeed) {
		growTime+=(deltaTime/1E9*gameSpeed);
		if (growTime > 10) {
			//die();
			dead = true;
			plant = true;
		}
	}

	@Override
	public void create(Random rand) {
		// TODO Auto-generated method stub

	}

}
