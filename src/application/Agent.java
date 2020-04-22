package application;

import java.util.Random;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Agent extends Entity {
	
	public Circle c;
	private int team = -1;
	private Color color;
	private double score;
	private Brain brain;
	public double normalizedScore = 0;
	public double accumulatedScore = 0;
	
	private boolean isAlive = true;
	private double energy = 100;
	private double moveCost = 10; //energy loss per tick = (moveSpeed*moveCost)/10
	private double teamFactor = 0; // between 0 and 1, used for reward calculations. Higher team factor = rewards are based on average food for whole team, not just an individual
	private double nutritionFact = 1;
	//[Speed + = moveCost +]
	//[Size + = moveCost +]
	//[EatRatio + = nutritionFact -]
	
	private double moveSpeed = .5;
	private double size = 10;
	private double eatRatio = 0;
	private double mountainMoveMod = .5;
	private double waterMoveMod = .5;
	
	private double foodGot = 0;
	
	private DoublePair moveDirection = new DoublePair(0,0);
	
	public Agent(Color color, BorderPane root,double centerX,double centerY, Random rand, int team, double size, double moveSpeed, double eatRatio, double mountainMod, double waterMod, double [][] weights) {
		this.team = team;
		c = new Circle(centerX,centerY,size,color);
		root.getChildren().add(c);
		this.color = color;
		create(rand,weights);
		
		this.moveSpeed = moveSpeed;
		this.eatRatio = eatRatio;
		this.size = size;
		this.mountainMoveMod = mountainMod;
		this.waterMoveMod = waterMod;
		
		moveCost = moveSpeed*size;
		nutritionFact = 5 / Math.pow(Math.E, eatRatio);
		
		c.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent arg0) {
				brain.printBrainContents();
				System.out.println("Food: " + foodGot);
				System.out.println("Energy: " + energy);
			}
			
		});
	}
	
	@Override
	public void move(double x, double y) {
		c.setTranslateX(c.getTranslateX()+x);
		c.setTranslateY(c.getTranslateY()+y);
		
		DoublePair pos = getPosition();
		double correctionX = 0;
		double correctionY = 0;
		if (pos.first < 0) {
			correctionX = -pos.first;
		}else if (pos.first > Main.DIMENSION_X) {
			correctionX = Main.DIMENSION_X-pos.first;
		}
		
		if (pos.second < 0) {
			correctionY = -pos.second;
		}else if (pos.second > Main.DIMENSION_Y) {
			correctionY = Main.DIMENSION_Y-pos.second;
		}
		
		if (correctionX != 0 || correctionY != 0) {
			move(correctionX,correctionY);
		}
		
	}
	
	public double getFoodValue(double nutrition) {
		return nutrition*nutritionFact;
	}
	
	public double getFoodValue(Agent a) {
		return a.getFoodGot();
	}
	
	public double reportScore() {
		//System.out.println("Calculating score...");
		score+=foodGot;//*20;//TODO score from team using teamFactor
		DoublePair baseDirection = getDirectionToBase(getPosition());
		if (baseDirection.getMagnitude() <= 50) {
			score*=2;
		}else {
			//score/=baseDirection.getMagnitude();
			//score-=baseDirection.getMagnitude();
			//die();
		}
		if (foodGot == 0) {
			//score-=15;
		}
		//System.out.println("Score = " + score);
		return score;
	}
	
	public String getTerrain(double x, double y) {
		return Main.getTerrainFromPoint((int)x, (int)y);
	}
	
	@Override
	public void die() {
		if (!isAlive) { return;}
		Color current = (Color) c.getFill();
		c.setFill(new Color(Math.min(current.getRed()+.2,1),Math.min(current.getGreen()+.2,1),Math.min(current.getBlue()+.2,1),.2));
		isAlive = false;
		//c.setDisable(true); //may be unnecessary
	}
	
	@Override
	public void step(long deltaTime, long time, double gameSpeed) {
		if (energy <= 0||!isAlive) { if (isAlive) {die();} return; }
		
		Food[] food = Main.getFoodArray();
		DoublePair myPos = getPosition();
		DoublePair foodDirection = getDirectionToFood(myPos,food);
		DoublePair baseDirection = getDirectionToBase(myPos);
		//if (baseDirection.getMagnitude() > 100) {
			energy-=(moveCost*(deltaTime/1E9)*gameSpeed);
		//}
		DoublePair nearestFriendDirection = getDirectionToNearestFriend(myPos);
		Agent nearestEnemy = getNearestEnemy(myPos);
		DoublePair nearestEnemyDirection = nearestEnemy!=null ? new DoublePair(nearestEnemy.getPosition().first-myPos.first,nearestEnemy.getPosition().second-myPos.second) : new DoublePair(0,0);
		foodDirection.divide(foodDirection.getMagnitude());
		baseDirection.divide(baseDirection.getMagnitude());
		nearestFriendDirection.divide(nearestFriendDirection.getMagnitude());
		double timeSetting = time/Main.DAY_LENGTH;
		int endNodes = brain.nodes.length-1;
		brain.nodes[0][0].setActivation(nearestFriendDirection.first);
		brain.nodes[0][1].setActivation(nearestFriendDirection.second);
		brain.nodes[0][1].setActivation(foodGot);
		brain.nodes[0][2].setActivation(foodDirection.first);
		brain.nodes[0][3].setActivation(foodDirection.second);
		brain.nodes[0][4].setActivation(timeSetting);
		brain.nodes[0][5].setActivation(baseDirection.first);
		brain.nodes[0][6].setActivation(baseDirection.second);
		brain.nodes[0][7].setActivation(nearestEnemyDirection.first);
		brain.nodes[0][8].setActivation(nearestEnemyDirection.second);
		brain.nodes[0][9].setActivation(nearestEnemy!=null ? nearestEnemy.getSize() : 10);
		double terrainValue = 0;
		String lookingTerrain = getTerrain(myPos.first + moveDirection.first, myPos.second + moveDirection.second);
		switch(lookingTerrain) {
		case "Water":
			terrainValue = -1;
			break;
		case "Mountain":
			terrainValue = 1;
			break;
		default:
			terrainValue = 0;
		}
		brain.nodes[0][10].setActivation(terrainValue);
		brain.sweepForwardProp();
		String terrain = getTerrain(myPos.first,myPos.second);//the terrain we are on is different than the terrain we are looking at
		double moveLeft = brain.nodes[endNodes][0].getActivation();
		double moveRight = brain.nodes[endNodes][1].getActivation();
		double moveUp = brain.nodes[endNodes][2].getActivation();
		double moveDown = brain.nodes[endNodes][3].getActivation();
		double action = brain.nodes[endNodes][4].getActivation();
		
		double moveX = 0;
		double moveY = 0;
		/*if (moveLeft > moveRight)
			moveX = -1;
		else if (moveRight > moveLeft)
			moveX = 1;
		if (moveUp > moveDown)
			moveY = -1;
		else if (moveDown > moveUp)
			moveY = 1;*/
		moveX = moveRight + moveLeft;
		moveY = moveUp + moveDown;
		
		DoublePair moveDirection = new DoublePair(moveX,moveY);
		moveDirection.divide(moveDirection.getMagnitude());//make this a unit vector
		moveDirection.multiply(moveSpeed);
		moveDirection.multiply(gameSpeed);
		if (terrain.equals("Water")) {
			moveDirection.multiply(waterMoveMod);
			energy-=((Math.max(0,mountainMoveMod-1)/5)*(deltaTime/1E9)*gameSpeed);
		}else if (terrain.equals("Mountain")) {
			moveDirection.multiply(mountainMoveMod);
			energy-=((Math.max(0,waterMoveMod-1)/5)*(deltaTime/1E9)*gameSpeed);
		}else {
			energy-=((Math.max(0,waterMoveMod-1)/5 + Math.max(0,mountainMoveMod-1)/5)*(deltaTime/1E9)*gameSpeed);
		}
		
		move(moveDirection.first,moveDirection.second);
		if (action > .7) {
			if (foodGot >= 1) {
				foodGot--;
				
				Main.plantSeed(myPos.first, myPos.second);
			}
			
		}
	}
	
	public DoublePair getPosition() {
		double posX = c.getCenterX()+c.getTranslateX();
		double posY = c.getCenterY()+c.getTranslateY();
		return new DoublePair(posX,posY);
	}
	
	public DoublePair getDirectionToFood(DoublePair myPos, Food[] foodArray) {//can incorporate eating into this I guess
		//Food nearest;
		double dist = Double.MAX_VALUE;
		DoublePair nearestDirection = null;
		for (int i = 0; i < foodArray.length; i++) {
			Food f = foodArray[i];
			DoublePair theirPosition = f.getPosition();
			double thisDist = Math.sqrt(Math.pow(theirPosition.first-myPos.first, 2) + Math.pow(theirPosition.second-myPos.second, 2));
			if (thisDist < size) {
				thisDist = Double.MAX_VALUE;
				f.die();
				foodGot+=1;//energy/100;
				energy+=getFoodValue(2/size);//size of food is two, if we are smaller than food, it should give more energy
			}
			if (thisDist < dist) {
				//nearest = f;
				dist = thisDist;
				nearestDirection = new DoublePair(theirPosition.first-myPos.first,theirPosition.second-myPos.second);
			}
		}
		return nearestDirection != null ? nearestDirection : new DoublePair(0,0);
	}
	
	public DoublePair getDirectionToBase(DoublePair myPos) {
		DoublePair basePosition = new DoublePair(c.getCenterX(),c.getCenterY());
		
		return new DoublePair(basePosition.first-myPos.first,basePosition.second-myPos.second);
	}
	
	public DoublePair getDirectionToNearestFriend(DoublePair myPos) {
		double dist = Integer.MAX_VALUE;
		DoublePair closestPos = new DoublePair(0,0);
		for (Agent a : Main.agentArray) {
			if (!a.equals(this)&&a.getTeam()==team) {
				DoublePair theirPos = a.getPosition();
				double d = myPos.getDistance(theirPos);
				if (d < dist) {
					dist = d;
					closestPos = theirPos;
				}
			}
		}
		return new DoublePair(closestPos.first-myPos.first,closestPos.second-myPos.second);
	}
	public Agent getNearestEnemy(DoublePair myPos) {
		double dist = Integer.MAX_VALUE;
		Agent closest = null;
		for (Agent a : Main.agentArray) {
			if (a.getTeam()!=team&&a.getAlive()) {
				DoublePair theirPos = a.getPosition();
				double d = myPos.getDistance(theirPos);
				
				if (d < size && size*eatRatio > a.getSize()) {
					a.die();
					foodGot+=1;
					energy+=getFoodValue(a.getFoodGot());
				}else if (d < dist) {
					dist = d;
				}
			}
		}
		return closest;
	}
	
	public Brain getBrain() {
		return brain;
	}
	
	public int getTeam() {
		return team;
	}
	
	public double getSize() {
		return size;
	}
	
	public Color getColor() {
		return color;
	}
	
	public double getScore() {
		return score;
	}
	public double getFoodGot() {
		return foodGot;
	}
	public double getTeamFactor() {
		return teamFactor;
	}
	public double getEatRatio() {
		return eatRatio;
	}
	public double getMoveCost() {
		return moveCost;
	}
	public double getMoveSpeed() {
		return moveSpeed;
	}
	public double getMountainMoveSpeed() {
		return mountainMoveMod;
	}
	public double getWaterMoveSpeed() {
		return waterMoveMod;
	}
	public double getNutritionFactor() {
		return nutritionFact;
	}
	public void setScore(double newScore) {
		score = newScore;
	}
	public void updateScore(double deltaScore) {
		score+=deltaScore;
	}
	public boolean getAlive() {
		return isAlive;
	}
	public void create(Random rand, double [][] weights) {
		brain = new Brain(this, rand, weights);
	}

	@Override
	public void create(Random rand) {
		// TODO Auto-generated method stub
		
	}
	
}
