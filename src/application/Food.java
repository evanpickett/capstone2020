package application;

import java.util.Random;

import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Food extends Entity {
	
	public Circle c;
	private BorderPane root;
	
	public Food(BorderPane root,double centerX,double centerY) {
		c = new Circle(centerX,centerY,2,Color.YELLOW);
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
		root.getChildren().remove(c);
		Main.eatFood(this);
	}
	
	public DoublePair getPosition() {
		double posX = c.getCenterX();
		double posY = c.getCenterY();
		return new DoublePair(posX,posY);
	}
	
	@Override
	public void step(long deltaTime, long time, double gameSpeed) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void create(Random rand) {
		// TODO Auto-generated method stub
		
	}

}
