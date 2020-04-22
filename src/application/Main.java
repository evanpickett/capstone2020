package application;
	
import java.util.ArrayList;
import java.util.Random;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;


public class Main extends Application {
	
	Scene scene;
	static BorderPane root;
	Random rand = new Random();
	
	public static final int NUM_TEAMS = 4;
	
	public static final double DAY_LENGTH = 30*1E9;
	private static final double MINUTE_LENGTH = DAY_LENGTH/1440;
	private static final double WEIGHT_LEARN_RATE = 0.1;
	private static final double WEIGHT_MUTATION_CHANCE = 0.25;
	private static final double GENE_TWEAK_RATE = 0.1;
	private static final double GENE_MUTATION_CHANCE = 0.25;
	private static final int MAX_TEAM_SIZE = 50;
	private static final int BASE_TEAM_SIZE = 25;
	private static final int MIN_TEAM_SIZE = 20;
	private static final double FOOD_FACTOR = 2.00;
	public static ArrayList<Food> foodArray = new ArrayList<Food>(10);
	public static ArrayList<Seed> seedArray = new ArrayList<Seed>(10);
	public static ArrayList<Agent> agentArray = new ArrayList<Agent>(5);
	public static ArrayList<Shape> brainDrawingArray = new ArrayList<Shape>(20);
	public static final int DIMENSION_X = 1000;
	public static final int DIMENSION_Y = 1000;
	public static final int TERRAIN_RESOLUTION = 10;
	
	public static final Color [] colors = {
			new Color(1, 0, 0, 1),
			new Color(0, 1, 0, 1),
			new Color(0, 0, 1, 1),
			new Color(1, 0, 1, 1),
			
			
			/*Color.CORAL,
			Color.CYAN,
			Color.BROWN,
			Color.HOTPINK*/
	};
	
	public static final String [] teamNames = {
			"Red",
			"Green",
			"Blue",
			"Purple"
	};
	
	public static int [] teamPopulations = new int[NUM_TEAMS];
	
	public static Brain [] bestBrains = new Brain[NUM_TEAMS];
	public static double [] bestBrainValues = new double [NUM_TEAMS];
	public static int [] bestBrainTickers = new int [NUM_TEAMS];
	
	private static final double SCALE_FACTOR_X = DIMENSION_X*.1;
	private static final double SCALE_FACTOR_Y = DIMENSION_Y*.1;
	
	public static DoublePair [] basePositions = {
		new DoublePair(SCALE_FACTOR_X,SCALE_FACTOR_Y),
		new DoublePair(DIMENSION_X - SCALE_FACTOR_X,DIMENSION_Y - SCALE_FACTOR_Y),
		new DoublePair(DIMENSION_X - SCALE_FACTOR_X,SCALE_FACTOR_Y),
		new DoublePair(SCALE_FACTOR_X,DIMENSION_Y - SCALE_FACTOR_Y),
	};
	
	double [][] baseAttributes = new double[NUM_TEAMS][5];
	
	static double [] terrainThresholds = {
			-1.5,
			-1,
			0,
			0.6,
			0.9,
			Integer.MAX_VALUE
	};
	static String [] terrainNames = {
			"Water",
			"Water",
			"Grass",
			"Grass",
			"Mountain",
			"Mountain"
	};
	static Color [] terrainColors = {
			Color.DARKBLUE,
			Color.LIGHTBLUE,
			Color.GREEN,
			Color.DARKGREEN,
			Color.LIGHTSLATEGRAY,
			Color.WHITE
	};
	static TerrainGenerator noise;
	public static boolean ready = false;
	
	public static Food[] getFoodArray() {
		return foodArray.toArray(new Food[0]);
	}
	public static void eatFood(Food f) {
		//boolean changed = foodArray.remove(f);
		foodArray.remove(f);
		//if (changed) { System.out.println("food eaten!"); }
	}
	
	public static void plantSeed(double x, double y) {
		Seed s = new Seed(root, x, y);
		seedArray.add(s);
	}
	public static void removeSeed(Seed s) {
		seedArray.remove(s);
	}
	
	public static double sigmoid(double val) {
		return 2.0/(1+Math.pow(Math.E, -val)) - 1;
	}
	
	
	public void rewardAndResetAgents() {
		ready = false;
		System.out.println("====================");
		ArrayList<Agent> newAgents = new ArrayList<Agent>(5);
		double topScore = 0;
		Brain bestBrain = null;
		for (int i = 0; i < NUM_TEAMS; i++) {
			double totalScore = 0;
			double bestScore = 0;
			int numBreeding = 0;
			int numDied = 0;
			int totalNum = 0;
			double urgencyMultiplier = 1;
			for (Agent a : agentArray) {
				a.reportScore();
				if (a!=null&&a.getTeam() == i) {
					
					
					totalScore+=a.getScore();
					if (!a.getAlive())
						numDied++;
					else if (a.getScore() > 0)
						numBreeding++;
					if (a.getScore() > bestScore) {
						//previousBest = bestAgent;
						bestScore = a.getScore();
					}
					if (a.getScore() > bestBrainValues[i]) {
						bestBrainValues[i]=a.getScore();
						bestBrains[i]=a.getBrain();
						bestBrainTickers[i]=5;
					}
					if (a.getScore() > topScore) {
						topScore = a.getScore();
						bestBrain = a.getBrain();
					}
					totalNum++;
				}
			}
			if (numBreeding < 2) {
				urgencyMultiplier=5;
			}else if (numBreeding < 5) {
				urgencyMultiplier=2;
			}else if (numBreeding < 20) {
				urgencyMultiplier=1;
			}else if (numBreeding > 30) {
				urgencyMultiplier = 0.1;
			}else if (numBreeding > 25) {
				urgencyMultiplier = 0.5;
			}
			for (Agent a : agentArray) {
				if (a!=null&&a.getTeam() == i) {
					a.normalizedScore = a.getScore()/totalScore;
				}
			}
			ArrayList<Agent> normalizedAgents = new ArrayList<Agent>(totalNum);
			for (Agent a : agentArray) {
				if (a!=null&&a.getTeam() == i) {
					boolean inserted = false;
					for (int x = 0; x < normalizedAgents.size(); x++) {
						if (normalizedAgents.get(x)!=null&&a.normalizedScore < normalizedAgents.get(x).normalizedScore) {
							normalizedAgents.add(x, a);
							inserted = true;
							break;
						}
					}
					if (!inserted) {
						normalizedAgents.add(a);
					}
				}
			}
			double runningSum = 0;
			for (Agent a : normalizedAgents) {
				if (a!=null) {
					runningSum+=a.normalizedScore;
					a.accumulatedScore = runningSum;
				}
			}
			teamPopulations[i] = (int) Math.max(Math.min(teamPopulations[i] - numDied + numBreeding/2,MAX_TEAM_SIZE),MIN_TEAM_SIZE);
			ArrayList<Brain> brainList = new ArrayList<Brain>(5);
			if (bestScore > 0) {
				
				for (int x = 0; x < teamPopulations[i]; x++) {
					double selector = rand.nextDouble();
					for (Agent a : normalizedAgents) {
						if (a!=null) {
							if (a.accumulatedScore >= selector) {
								//System.out.println(selector + ": " + a.accumulatedScore);
								brainList.add(a.getBrain());
								break;
							}
						}
					}
				}
			}
			System.out.println("Team " + teamNames[i] + " got " + totalScore + " points");
			double [][] brainWeights = null;
			
			for (int x = 0; x < agentArray.size(); x++) {
				Agent a = agentArray.get(x);
				if (a!=null&&a.getTeam()==i) {
					root.getChildren().remove(a.c);
					a=null;
				}
			}
			if (brainList.size()<=0&&bestBrains[i]!=null) {
				if (--bestBrainTickers[i] <= 0) {
					bestBrains[i]=null;
				}
				
			}
			
			for (int id = 0; id < teamPopulations[i]; id++) {
				
				if (baseAttributes[i][0]==0) {
					baseAttributes[i][0]=rand.nextDouble()*5+5;
					baseAttributes[i][1]=rand.nextDouble()*2;
					baseAttributes[i][2]=Math.max(rand.nextDouble(), .1);
					baseAttributes[i][3]=Math.max(rand.nextDouble(), .1);
					baseAttributes[i][4]=Math.max(rand.nextDouble(), .1);
				}
				double attributeSize = baseAttributes[i][0];
				double attributeEatRatio = baseAttributes[i][1];
				double attributeSpeed = baseAttributes[i][2];
				double attributeMountainSpeed = baseAttributes[i][3];
				double attributeWaterSpeed = baseAttributes[i][4];
				int modificationModifier = 1;
				if (brainList.size()<=id&&bestBrains[i]!=null) {
					//brainList.add(bestBrains[i]);
					//modificationModifier = 25;
				}
				if (brainList.size()>0) {
					brainWeights = new double[brainList.get(0).attachments.length][brainList.get(0).attachments[0].length];
					for (int rowIndex = 0; rowIndex < brainWeights.length; rowIndex++) {
						for (int attachmentIndex = 0; attachmentIndex < brainWeights[rowIndex].length; attachmentIndex++) {
							if (brainList.get(id).attachments[rowIndex][attachmentIndex] != null) {
								brainWeights[rowIndex][attachmentIndex] = brainList.get(id).attachments[rowIndex][attachmentIndex].getWeight();
							}
							//brainWeights[rowIndex][attachmentIndex] = brainWeights[rowIndex][attachmentIndex] + brainWeights[rowIndex][attachmentIndex] * (rand.nextDouble()-.5) * LEARN_RATE;
						}
					}
					//brainList.remove(0);
					//myWeights = new double[brainWeights.length][brainWeights[0].length];
					for (int rowIndex = 0; rowIndex < brainWeights.length; rowIndex++) {
						for (int attachmentIndex = 0; attachmentIndex < brainWeights[rowIndex].length; attachmentIndex++) {
							if (rand.nextDouble() <= WEIGHT_MUTATION_CHANCE * modificationModifier * urgencyMultiplier)
								brainWeights[rowIndex][attachmentIndex] = brainWeights[rowIndex][attachmentIndex] + brainWeights[rowIndex][attachmentIndex] * (rand.nextDouble()*2-1) * WEIGHT_LEARN_RATE * modificationModifier * urgencyMultiplier;
						}
					}
					Agent prev = brainList.get(id).agent;
					attributeSize = prev.getSize();
					attributeEatRatio = prev.getEatRatio();
					attributeSpeed = prev.getMoveSpeed();
					attributeMountainSpeed = prev.getMountainMoveSpeed();
					attributeWaterSpeed = prev.getWaterMoveSpeed();
					if (rand.nextDouble()<=GENE_MUTATION_CHANCE * modificationModifier * urgencyMultiplier) 
						attributeSize = Math.max(attributeSize + (rand.nextDouble()*2-1) * GENE_TWEAK_RATE * modificationModifier * urgencyMultiplier,2);// minimum size is 2
					if (rand.nextDouble()<=GENE_MUTATION_CHANCE * modificationModifier * urgencyMultiplier) 
						attributeEatRatio = Math.max(attributeEatRatio + (rand.nextDouble()*2-1) * GENE_TWEAK_RATE * modificationModifier * urgencyMultiplier,0);// minimum eat ratio is 0
					if (rand.nextDouble()<=GENE_MUTATION_CHANCE * modificationModifier * urgencyMultiplier) 
						attributeMountainSpeed = Math.max(attributeMountainSpeed + (rand.nextDouble()*2-1) * GENE_TWEAK_RATE*.25 * modificationModifier * urgencyMultiplier,.1);// minimum speed is 0.1
					if (rand.nextDouble()<=GENE_MUTATION_CHANCE * modificationModifier * urgencyMultiplier) 
						attributeWaterSpeed = Math.max(attributeWaterSpeed + (rand.nextDouble()*2-1) * GENE_TWEAK_RATE*.25 * modificationModifier * urgencyMultiplier,.1);// minimum speed is 0.1
				}
				
				
				
				Agent newAgent = new Agent(colors[i], root, basePositions[i].first + (rand.nextDouble()-.5)*SCALE_FACTOR_X, basePositions[i].second + (rand.nextDouble()-.5)*SCALE_FACTOR_Y, rand, i, attributeSize, attributeSpeed, attributeMountainSpeed, attributeWaterSpeed, attributeEatRatio, brainWeights);
				newAgents.add(newAgent);
				newAgent.c.setOnMouseClicked(new EventHandler<MouseEvent>() {

					@Override
					public void handle(MouseEvent arg0) {
						drawBrain(newAgent.getBrain());
					}
					
				});
			}
			
		}
		
		if (bestBrain!=null) {
			drawBrain(bestBrain);
		}
		agentArray = newAgents;
		ready = true;
		
	}
	
	public void placeFood() {
		double amt = 0;
		double displacement = 1;//5
		for (int n : teamPopulations) {
			amt+=n;
		}
		amt*=FOOD_FACTOR;
		for (int i = 0; i < amt; i++) {
			Food f = new Food(root,DIMENSION_X/displacement + rand.nextDouble()*(DIMENSION_X-DIMENSION_X/(displacement/2)),DIMENSION_Y/displacement + rand.nextDouble()*(DIMENSION_Y-DIMENSION_Y/(displacement/2)));
			foodArray.add(f);
		}
	}
	
	Color [] brainContentColors = {
			Color.LIMEGREEN,
			Color.BLACK,
			Color.BLUEVIOLET,
			Color.ORANGE,
			Color.CORNFLOWERBLUE,
			Color.GOLD,
			Color.AQUAMARINE,
			Color.PINK,
			Color.GREY,
			Color.BEIGE,
			Color.BLUE,
			Color.BROWN,
			Color.DARKORANGE,
			Color.CRIMSON
	};
	
	public void drawBrain(Brain b) {
		int paddingX = 150;
		int paddingY = 100;
		int offsetX = 150;
		int offsetY = 100;
		int nodeSize = 25;
		for (int i = 0; i < brainDrawingArray.size(); i++) {
			Shape s = brainDrawingArray.get(i);
			if (s!=null) {
				root.getChildren().remove(s);
				s=null;
			}
		}
		Text label = new Text(DIMENSION_X+offsetX, 30,"team: " + teamNames[b.agent.getTeam()] + " score: " + String.format("%.2f", b.agent.getScore()));
		label.setFont(new Font("Arial",30));
		Agent agent = b.agent;
		label.setFill(colors[agent.getTeam()]);
		root.getChildren().add(label);
		brainDrawingArray.add(label);
		//brainDrawingArray = new ArrayList
		for (int row = 0; row < b.nodes.length; row++) {
			for (int i = 0; i < b.nodes[row].length; i++) {
				MLNode node = b.nodes[row][i];
				Circle c = new Circle(DIMENSION_X+offsetX+paddingX*row,offsetY+paddingY*i,nodeSize);
				c.setFill(brainContentColors[i]);
				Text t = new Text(c.getCenterX()-node.getName().length()*3,c.getCenterY(),node.getName());
				t.setFill(Color.WHITE);
				root.getChildren().addAll(c,t);
				brainDrawingArray.add(c);
				brainDrawingArray.add(t);
				if (row != 0) {
					for (MLAttachment a : b.attachments[row-1]) {
						if (a!=null&&a.getFrontConnection()!=null&&a.getFrontConnection().equals(node)) {
							//System.out.print("(" + a.getBackConnection().getName() + ")=" + a.getWeight()+" ");
							for (int j = 0; j < b.nodes[row-1].length; j++) {
								MLNode other = b.nodes[row-1][j];
								if (other.getName().equals(a.getBackConnection().getName())) {
									Text t2 = new Text(DIMENSION_X+offsetX+paddingX*(row-1)+25,offsetY+paddingY*j+14*i - 25,String.format("%.2f",a.getWeight()));
									t2.setFill(c.getFill());
									t2.setFont(new Font("Arial",14));
									root.getChildren().add(t2);
									brainDrawingArray.add(t2);
								}
							}
						}
					}
				}
			}
		}
		
		double attributeOffsetX = DIMENSION_X+offsetX+paddingX*b.nodes.length;
		Text attributeSpeed = new Text(attributeOffsetX, offsetY,"Speed: " + String.format("%.2f", agent.getMoveSpeed()));
		attributeSpeed.setFill(Color.WHITE);
		attributeSpeed.setFont(new Font("Arial",20));
		Text attributeEatRatio = new Text(attributeOffsetX, offsetY+25,"Eat Ratio: " + String.format("%.2f", agent.getEatRatio()));
		attributeEatRatio.setFill(Color.WHITE);
		attributeEatRatio.setFont(new Font("Arial",20));
		Text attributeSize = new Text(attributeOffsetX, offsetY+50,"Size: " + String.format("%.2f", agent.getSize()));
		attributeSize.setFill(Color.WHITE);
		attributeSize.setFont(new Font("Arial",20));
		Text attributeMountainSpeed = new Text(attributeOffsetX, offsetY+75,"Mtn. Speed: " + String.format("%.2f", agent.getMountainMoveSpeed()));
		attributeMountainSpeed.setFill(Color.WHITE);
		attributeMountainSpeed.setFont(new Font("Arial",20));
		Text attributeWaterSpeed = new Text(attributeOffsetX, offsetY+100,"Wtr. Speed: " + String.format("%.2f", agent.getWaterMoveSpeed()));
		attributeWaterSpeed.setFill(Color.WHITE);
		attributeWaterSpeed.setFont(new Font("Arial",20));
		
		root.getChildren().addAll(attributeSpeed,attributeEatRatio,attributeSize,attributeMountainSpeed,attributeWaterSpeed);
		brainDrawingArray.add(attributeSpeed);
		brainDrawingArray.add(attributeSize);
		brainDrawingArray.add(attributeEatRatio);
		brainDrawingArray.add(attributeMountainSpeed);
		brainDrawingArray.add(attributeWaterSpeed);
	}
	
	public void generateTerrainFromNoise(TerrainGenerator n, int sizeX, int sizeY,double resolution) {
		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {
				Rectangle terrain = new Rectangle(x*resolution,y*resolution,resolution,resolution);
				float point = n.getNoise(x, y);
				int terrainMarker = 0;
				for (int i = 0; i < terrainThresholds.length; i++) {
					if (point <= terrainThresholds[i]) {
						terrainMarker = i;
						break;
					}
				}
				terrain.setFill(terrainColors[terrainMarker]);
				root.getChildren().add(terrain);
			}
		}
	}
	
	public static String getTerrainFromPoint(int x, int y) {
		if (x >= DIMENSION_X)
			x = DIMENSION_X - 1;
		if (y >= DIMENSION_Y)
			y = DIMENSION_Y - 1;
		float point = noise.getNoise(x/TERRAIN_RESOLUTION, y/TERRAIN_RESOLUTION);
		for (int i = 0; i < terrainThresholds.length; i++) {
			if (point <= terrainThresholds[i]) {
				return terrainNames[i];
			}
		}
		return "NONE";
	}
	
	@Override
	public void start(Stage primaryStage) {
		try {
			//rand.setSeed(System.nanoTime());
			root = new BorderPane();
			scene = new Scene(root,DIMENSION_X,DIMENSION_Y+35);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			Rectangle secondBackground = new Rectangle(0,0,10000,10000);
			secondBackground.setFill(Color.DARKCYAN);
			Rectangle mainBackground = new Rectangle(0,0,1000,1000);
			mainBackground.setFill(Color.GREEN);
			root.getChildren().addAll(secondBackground,mainBackground);
			primaryStage.setScene(scene);
			primaryStage.show();
			
			//Agent e1 = new Agent(Color.RED,root,190,190,rand);
			//Circle c = e1.c;
			//root.getChildren().add(c);
			int mapSize = DIMENSION_X/TERRAIN_RESOLUTION;
			noise = new TerrainGenerator(rand, 1f, mapSize,mapSize);
			noise.init();
			generateTerrainFromNoise(noise,mapSize,mapSize,TERRAIN_RESOLUTION);
			Text label = new Text("Day 0: X");
			label.setFont(new Font("Arial",30));
			label.setFill(Color.GRAY);
			label.setTranslateX(0);
			label.setTranslateY(975);
			
			root.getChildren().add(label);
			
			for (int i = 0; i < NUM_TEAMS; i++) {
				teamPopulations[i]=BASE_TEAM_SIZE;
			}
			
			AnimTimerExt A = new AnimTimerExt() {
				
				@Override
				public void handle(long now) {
					
					if (day == -1) {
						day = 0;
						
						rewardAndResetAgents();
						placeFood();
					}
					if (lastTime == 0L){//wait for first frame
						lastTime = now;
					}else {
						long deltaTime = (now-lastTime);
						lastTime = now;
						if (!ready) { return; }
						//e1.move(0, 50*speed*deltaTime/1E9);
						time+=deltaTime*speed;
						//e1.step(deltaTime,time,speed);
						for (Agent e : agentArray) {
							if (e!=null) {
								e.step(deltaTime, time, speed);
							}
						}
						for (Seed s : seedArray) {
							if (s!=null&&!s.dead) {
								s.step(deltaTime, time, speed);
							}
						}
						double numMins = time/MINUTE_LENGTH;
						int hour = (int)Math.floor(numMins/60);
						int min = (int)numMins%60;
						int pop = 0;
						String popText = "";
						for (int i = 0; i < teamPopulations.length; i++) {
							int c = teamPopulations[i];
							popText += teamNames[i] + ": " + c + "|";
							pop+=c;
						}
						String minuteText = "" + min;
						minuteText = minuteText.length() > 1 ? minuteText : "0" + minuteText;
						label.setText("Global Pop.: " + pop + "|" + popText + " Day " + day + ": " + hour + ":" + minuteText);
						if (time > DAY_LENGTH) {
							//time-=DAY_LENGTH;
							day++;
							for (int i = 0; i < seedArray.size(); i++) {
								Seed s = seedArray.get(i);
								if (s != null) {
									root.getChildren().remove(s.c);
									seedArray.remove(i);
									s = null;
								}
							}
							for (int i = 0; i < foodArray.size(); i++) {
								Food f = foodArray.get(i);
								if (f != null) {
									root.getChildren().remove(f.c);
									foodArray.remove(i);
									f = null;
								}
							}
							rewardAndResetAgents();
							placeFood();
							time = 0;
							//e1.reportScore();
						}else {
							for (int i = 0; i < seedArray.size(); i++) {
								Seed s = seedArray.get(i);
								if (s != null && s.plant) {
									Food f = new Food(root,s.c.getCenterX()+rand.nextInt(3)-1,s.c.getCenterY()+rand.nextInt(3)-1);
									foodArray.add(f);
									Food f2 = new Food(root,s.c.getCenterX()+rand.nextInt(3)-1,s.c.getCenterY()+rand.nextInt(3)-1);
									foodArray.add(f2);
									root.getChildren().remove(s.c);
									seedArray.remove(s);
									s = null;
								}
							}
						}
					}
					
				}
				
			};
			A.start();
			
			Rectangle bottomBar = new Rectangle(0,1000,1000,35);
			bottomBar.setFill(Color.BLACK);
			Text speedText = new Text(0,1030,"Speed: %dx");
			speedText.setFont(new Font("Arial",30));
			speedText.setFill(Color.WHITE);
			ToggleGroup speedGroup = new ToggleGroup();
			RadioButton speed1 = new RadioButton();
			speed1.setTranslateX(250);
			speed1.setTranslateY(1020);
			speed1.setToggleGroup(speedGroup);
			Tooltip t1 = new Tooltip("1x Speed");
			t1.setFont(new Font("Arial",20));
			t1.setWrapText(true);
			speed1.setTooltip(t1);
			speed1.setOnAction((event) -> {
				A.setSpeed(1);
				speedText.setText("Speed: 1x");
			});
			RadioButton speed2 = new RadioButton();
			speed2.setTranslateX(300);
			speed2.setTranslateY(1020);
			speed2.setToggleGroup(speedGroup);
			Tooltip t2 = new Tooltip("2x Speed");
			t2.setFont(new Font("Arial",20));
			t2.setWrapText(true);
			speed2.setTooltip(t2);
			speed2.setOnAction((event) -> {
				A.setSpeed(2);
				speedText.setText("Speed: 2x");
			});
			RadioButton speed3 = new RadioButton();
			speed3.setTranslateX(350);
			speed3.setTranslateY(1020);
			speed3.setToggleGroup(speedGroup);
			Tooltip t3 = new Tooltip("3x Speed");
			t3.setFont(new Font("Arial",20));
			t3.setWrapText(true);
			speed3.setTooltip(t3);
			speed3.setOnAction((event) -> {
				A.setSpeed(3);
				speedText.setText("Speed: 3x");
			});
			RadioButton speed4 = new RadioButton();
			speed4.setTranslateX(400);
			speed4.setTranslateY(1020);
			speed4.setToggleGroup(speedGroup);
			Tooltip t4 = new Tooltip("4x Speed");
			t4.setFont(new Font("Arial",20));
			t4.setWrapText(true);
			speed4.setTooltip(t4);
			speed4.setOnAction((event) -> {
				A.setSpeed(4);
				speedText.setText("Speed: 4x");
			});
			RadioButton speed5 = new RadioButton();
			speed5.setTranslateX(450);
			speed5.setTranslateY(1020);
			speed5.setToggleGroup(speedGroup);
			Tooltip t5 = new Tooltip("5x Speed");
			t5.setFont(new Font("Arial",20));
			t5.setWrapText(true);
			speed5.setTooltip(t5);
			speed5.setOnAction((event) -> {
				A.setSpeed(5);
				speedText.setText("Speed: 5x");
			});
			RadioButton speed6 = new RadioButton();
			speed6.setTranslateX(500);
			speed6.setTranslateY(1020);
			speed6.setToggleGroup(speedGroup);
			Tooltip t6 = new Tooltip("6x Speed");
			t6.setFont(new Font("Arial",20));
			t6.setWrapText(true);
			speed6.setTooltip(t6);
			speed6.setOnAction((event) -> {
				A.setSpeed(6);
				speedText.setText("Speed: 6x");
			});
			RadioButton speed10 = new RadioButton();
			speed10.setTranslateX(550);
			speed10.setTranslateY(1020);
			speed10.setToggleGroup(speedGroup);
			Tooltip t10 = new Tooltip("10x Speed");
			t10.setFont(new Font("Arial",20));
			t10.setWrapText(true);
			speed10.setTooltip(t10);
			speed10.setOnAction((event) -> {
				A.setSpeed(10);
				speedText.setText("Speed: 10x");
			});
			RadioButton speedPause = new RadioButton();
			speedPause.setTranslateX(600);
			speedPause.setTranslateY(1020);
			speedPause.setToggleGroup(speedGroup);
			Tooltip tPause = new Tooltip("Pause");
			tPause.setFont(new Font("Arial",20));
			tPause.setWrapText(true);
			speedPause.setTooltip(tPause);
			speedPause.setOnAction((event) -> {
				A.setSpeed(0);
				speedText.setText("Speed: PAUSED");
			});
			root.getChildren().addAll(bottomBar,speedText,speed1,speed2,speed3,speed4,speed5,speed6,speed10,speedPause);
			speed1.fireEvent(new ActionEvent());
			speed1.setSelected(true);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	public static void main(String[] args) {
		launch(args);
		
	}
	
	
}

class AnimTimerExt extends AnimationTimer{
	long lastTime = 0L;
	int day = -1;
	long time = 0;
	double speed = 6.0;
	public void setSpeed(double newSpeed) {
		speed = newSpeed;
	}
	@Override
	public void handle(long now) {
		//Hacky-- this will be overwritten
	}
	
}