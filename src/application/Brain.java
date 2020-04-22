package application;

import java.util.Random;

/**
 * Brain class
 * Used to store a neural network for an Agent
 */
public class Brain {
	MLAttachment [][] attachments = new MLAttachment[3][18];
	MLNode [][] nodes;
	Agent agent;
	
	public double getActivationValue(double forwardValue) { // sigmoid equation
		return Main.sigmoid(forwardValue);
	}
	
	public void sweepForwardProp() {
		for (int rowIndex = 1; rowIndex < nodes.length; rowIndex++) {
			for (int id = 0; id < nodes[rowIndex].length; id++) {
				//MLNode[] rowBefore = nodes[rowIndex-1];
				MLNode thisNode = nodes[rowIndex][id];
				double myForwardValue = 0;
				MLAttachment[] attachmentRowBefore = attachments[rowIndex-1];
				for (int i = 0; i < attachmentRowBefore.length; i++) {
					MLAttachment a = attachmentRowBefore[i];
					if (a!=null) {
						MLNode goingTo = a.getFrontConnection();
						if (goingTo.equals(thisNode)) {
							MLNode comingFrom = a.getBackConnection();
							myForwardValue+=(a.getWeight() * comingFrom.getActivation());
						}
					}
				}
				double myActivation = getActivationValue(myForwardValue);
				thisNode.setActivation(myActivation);
				thisNode.setForward(myForwardValue);
			}
		}
	}
	
	public void printBrainContents() {
		System.out.println("Neurons:");
		System.out.println("===========================");
		for (MLNode [] row : nodes) {
			for (MLNode node : row) {
				System.out.println(node.getName() + ": F=" + node.getForward() + ", A=" + node.getActivation());
			}
		}
		System.out.println("Weights:");
		System.out.println("===========================");
		for (int rowIndex = 1; rowIndex < nodes.length; rowIndex++) {
			for (MLNode node : nodes[rowIndex]) {
				System.out.print(node.getName() + ": ");
				int nEntries = 0;
				for (MLAttachment a : attachments[rowIndex-1]) {
					if (a!=null&&a.getFrontConnection()!=null&&a.getFrontConnection().equals(node)) {
						System.out.print("(" + a.getBackConnection().getName() + ")=" + a.getWeight()+" ");
						if (++nEntries > 6) {
							System.out.println();
						}
					}
				}
				System.out.println();
			}
		}
	}
	
	public void tick() {
		
	}
	
	public Brain(Agent a, Random rand, double [][] weights) {
		agent = a;
		MLNode inputDirectionToFoodX = new MLNode("inputDirectionToFoodX");
		MLNode inputDirectionToFoodY = new MLNode("inputDirectionToFoodY");
		MLNode inputTimeOfDay = new MLNode("inputTimeOfDay");
		MLNode inputDirToBaseX = new MLNode("inputDirToBaseX");
		MLNode inputDirToBaseY = new MLNode("inputDirToBaseY");
		MLNode inputFoodAte = new MLNode("inputFoodAte");
		MLNode nearestFriendX = new MLNode("nearestFriendX");
		MLNode nearestFriendY = new MLNode("nearestFriendY");
		MLNode nearestEnemyX = new MLNode("nearestEnemyX");
		MLNode nearestEnemyY = new MLNode("nearestEnemyY");
		MLNode nearestEnemySize = new MLNode("nearestEnemySize");
		MLNode lookingAtTerrain = new MLNode("lookingAtTerrain");
		MLNode hidden1 = new MLNode("hidden1");
		MLNode hidden2 = new MLNode("hidden2");
		MLNode hidden3 = new MLNode("hidden3");
		MLNode hidden4 = new MLNode("hidden4");
		MLNode hidden5 = new MLNode("hidden5");
		MLNode hidden6 = new MLNode("hidden6");
		MLNode hidden7 = new MLNode("hidden7");
		
		MLNode outputDirectionLeft = new MLNode("outputDirectionLeft");
		MLNode outputDirectionRight = new MLNode("outputDirectionRight");
		MLNode outputDirectionUp = new MLNode("outputDirectionUp");
		MLNode outputDirectionDown = new MLNode("outputDirectionDown");
		MLNode outputAction = new MLNode("outputAction");
		
		MLNode[][] nodes = {{nearestFriendX,nearestFriendY,inputFoodAte,inputDirectionToFoodX,inputDirectionToFoodY,inputTimeOfDay,inputDirToBaseX,inputDirToBaseY,nearestEnemyX,nearestEnemyY,nearestEnemySize,lookingAtTerrain},
				{hidden1,hidden2,hidden3,hidden4,hidden5,hidden6,hidden7},
				//{hidden8,hidden9,hidden10,hidden11,hidden12,hidden13,hidden14,hidden15},
				{outputDirectionLeft,outputDirectionRight,outputDirectionUp,outputDirectionDown,outputAction}};
		this.nodes = nodes;
		int largestRow = 0;
		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i].length > largestRow) {
				largestRow = nodes[i].length;
			}
		}
		attachments = new MLAttachment[nodes.length+1][(int)Math.pow(largestRow, 2)];
		for (int rowIndex = 1; rowIndex < nodes.length; rowIndex++) {
			int id = 0;
			for (int attachmentIndex = 0; attachmentIndex < nodes[rowIndex].length; attachmentIndex++) {
				if(nodes[rowIndex][attachmentIndex]!=null) {
					for (int attachmentBeforeIndex = 0; attachmentBeforeIndex < nodes[rowIndex-1].length; attachmentBeforeIndex++) {
						if (nodes[rowIndex-1][attachmentBeforeIndex]!=null) {
							MLAttachment attachment = new MLAttachment(nodes[rowIndex-1][attachmentBeforeIndex],nodes[rowIndex][attachmentIndex],rowIndex-1,id,weights!=null ? weights[rowIndex-1][id] : (new Random()).nextDouble()*2 - 1);
							attachments[rowIndex-1][id]=attachment;
							id++;
						}
					}
				}
			}
		}
		
	}
	
}
