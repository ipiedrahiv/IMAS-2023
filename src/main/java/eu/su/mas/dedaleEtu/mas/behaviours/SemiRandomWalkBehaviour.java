package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.TickerBehaviour;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.State;
import eu.su.mas.dedaleEtu.mas.knowledge.Treasure;

/**************************************
 * 
 * 
 * 				BEHAVIOUR RandomWalk : Illustrates how an agent can interact with, and move in, the environment
 * 
 * 
 **************************************/

public class SemiRandomWalkBehaviour extends TickerBehaviour{
	/**
	 * When an agent choose to move
	 *
	 */
	private static final long serialVersionUID = 9088209402507795289L;
	private Location prevLoc = null;

	private MapRepresentation myMap;

	private int semiRandomMoves = 0;


	private boolean done = true;

	private boolean reported = false;

	public SemiRandomWalkBehaviour (final AbstractDedaleAgent myagent, MapRepresentation myMap) {
		super(myagent, 200);
		this.myMap = myMap;
		//super(myagent);
	}

	@Override
	public void onTick() {
		semiRandomMoves += 1;
		//Example to retrieve the current position
		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if (myPosition!=null && myPosition.getLocationId()!=""){
			List<Couple<Location,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
			// System.out.println(this.myAgent.getLocalName()+" -- list of observables: "+lobs);

			//Random move from the current position
			int moveId;
			if(prevLoc == null) {
				prevLoc = myPosition;
			}
			do{
				Random r= new Random();
				moveId = 1 + r.nextInt(lobs.size()-1);
			}while(lobs.get(moveId).getLeft().equals(prevLoc) && prevLoc != null);
			prevLoc = myPosition;

			((AbstractDedaleAgent)this.myAgent).moveTo(lobs.get(moveId).getLeft());

			//The move action (if any) should be the last action of your behaviour
			((AbstractDedaleAgent)this.myAgent).moveTo(lobs.get(moveId).getLeft());
		}

		if(this.myMap==null) {
			this.myMap= MapRepresentation.getInstance();
		}

		done = true;

		List<Treasure> allTreasures = this.myMap.getTreasures();

		if(allTreasures.size() == 1) {
			for (Treasure treasure : allTreasures) {
				done = done && ((treasure.getState() == State.COLLECTED));
			}
		} else {
			done = false;
		}

		if(done == true) {
			if (reported == false) {
				System.out.println("####################");
				System.out.println("SEMIRANDOM-WALK BEHAVIOUR");
				System.out.println("Intentional movements for agent " + myAgent.getName() + " = " + semiRandomMoves);
				System.out.println("####################");
				reported = true;
			}
		}

	}

}


//public class RandomWalkBehaviour extends TickerBehaviour{
//
//	/**
//	 * When an agent choose to move
//	 *
//	 */
//	private static final long serialVersionUID = 9088209402507795289L;
//
//	public RandomWalkBehaviour (final AbstractDedaleAgent myagent) {
//		super(myagent, 600);
//	}
//
//	@Override
//	public void onTick() {
//		//Example to retrieve the current position
//		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
//		System.out.println(this.myAgent.getLocalName()+" -- myCurrentPosition is: "+myPosition);
//		if (myPosition!=null){
//			//List of observable from the agent's current position
//			List<Couple<Location,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
//			System.out.println(this.myAgent.getLocalName()+" -- list of observables: "+lobs);
//
//			//Little pause to allow you to follow what is going on
//			try {
//				System.out.println("Press enter in the console to allow the agent "+this.myAgent.getLocalName() +" to execute its next move");
//				System.in.read();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//
//			//list of observations associated to the currentPosition
//			List<Couple<Observation,Integer>> lObservations= lobs.get(0).getRight();
//
//			//example related to the use of the backpack for the treasure hunt
//			Boolean b=false;
//			for(Couple<Observation,Integer> o:lObservations){
//				switch (o.getLeft()) {
//				case DIAMOND:case GOLD:
//					System.out.println(this.myAgent.getLocalName()+" - My treasure type is : "+((AbstractDedaleAgent) this.myAgent).getMyTreasureType());
//					System.out.println(this.myAgent.getLocalName()+" - My current backpack capacity is:"+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
//					System.out.println(this.myAgent.getLocalName()+" - Value of the treasure on the current position: "+o.getLeft() +": "+ o.getRight());
//					System.out.println(this.myAgent.getLocalName()+" - The agent grabbed :"+((AbstractDedaleAgent) this.myAgent).pick());
//					System.out.println(this.myAgent.getLocalName()+" - the remaining backpack capacity is: "+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
//					b=true;
//					break;
//				default:
//					break;
//				}
//			}
//
//			//If the agent picked (part of) the treasure
//			if (b){
//				List<Couple<Location,List<Couple<Observation,Integer>>>> lobs2=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
//				System.out.println(this.myAgent.getLocalName()+" - State of the observations after trying to pick something "+lobs2);
//			}
//
//			//Random move from the current position
//			Random r= new Random();
//			int moveId=1+r.nextInt(lobs.size()-1);//removing the current position from the list of target, not necessary as to stay is an action but allow quicker random move
//
//			//The move action (if any) should be the last action of your behaviour
//			((AbstractDedaleAgent)this.myAgent).moveTo(lobs.get(moveId).getLeft());
//		}
//
//	}
//
//}
