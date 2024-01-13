package eu.su.mas.dedaleEtu.mas.agents.dummies;

import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dataStructures.tuple.Couple;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;

/**
 * This dummy collector moves randomly, tries all its methods at each time step, store the treasure that match is treasureType 
 * in its backpack and intends to empty its backPack in the Tanker agent. @see {@link SemiRandomWalkExchangeBehaviour}
 * 
 * @author hc
 *
 */
public class DummyCollectorAgent extends AbstractDedaleAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1784844593772918359L;



	/**
	 * This method is automatically called when "agent".start() is executed.
	 * Consider that Agent is launched for the first time. 
	 * 			1) set the agent attributes 
	 *	 		2) add the behaviours
	 *          
	 */
	protected void setup(){

		super.setup();

		List<Behaviour> lb=new ArrayList<Behaviour>();
		lb.add(new SemiRandomWalkExchangeBehaviour(this));

		addBehaviour(new startMyBehaviours(this,lb));

		System.out.println("the  agent "+this.getLocalName()+ " is started");

	}



	/**
	 * This method is automatically called after doDelete()
	 */
	protected void takeDown(){

	}


	/**************************************
	 * 
	 * 
	 * 				BEHAVIOUR
	 * 
	 * 
	 **************************************/


	/**
	 * This behaviour is triggerable every 600ms.
	 *  It tries all the API methods at each time step, store the treasure that match the entity treasureType in its backpack and intends to 
	 *  empty its backPack in the Tanker agent (if he is in reach)
	 *<p>
	 *
	 *  Rmq : This behaviour is in the same class as the DummyCollectorAgent for clarity reasons. You should prefer to save your behaviours in the behaviours package, and all the behaviours referring to a given protocol in the same class    
	 *	
	 *	@author hc
	 */
	class SemiRandomWalkExchangeBehaviour extends TickerBehaviour{
		/**
		 * When an agent choose to move
		 *  
		 */
		private static final long serialVersionUID = 9088209402507795289L;
		private Location prevLoc = null;

		public SemiRandomWalkExchangeBehaviour (final AbstractDedaleAgent myagent) {
			super(myagent, 500);
			//super(myagent);
		}

		@Override
		public void onTick() {
			//Example to retrieve the current position
			Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

			if (myPosition!=null && myPosition.getLocationId()!=""){
				List<Couple<Location,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();
				List<Couple<Observation,Integer>> lObservations= lobs.get(0).getRight();

				//example related to the use of the backpack for the treasure hunt
				Boolean b=false;
				for(Couple<Observation,Integer> o:lObservations){
					switch (o.getLeft()) {
					case DIAMOND:case GOLD:
						Boolean success = ((AbstractDedaleAgent) this.myAgent).openLock(o.getLeft());
						if(success) {
							System.out.println(this.myAgent.getLocalName()+" - Opened lock at "+myPosition.getLocationId());
							if(o.getLeft()==((AbstractDedaleAgent) this.myAgent).getMyTreasureType()) {
								int amount = ((AbstractDedaleAgent) this.myAgent).pick();
								if(amount > 0) {
									System.out.println(this.myAgent.getLocalName()+" - Grabbed "+amount+" "+o.getLeft()+" from a total of "+o.getRight()+" at "+myPosition.getLocationId());
									System.out.println(this.myAgent.getLocalName()+" - My current backpack free space is:"+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
									b=true;
								}
							}	
						}	
						break;
					default:
						break;
					}
				}

				//Trying to store everything in the tanker
				if(b || (((AbstractDedaleAgent)this.myAgent).getBackPackFreeSpace().get(0).getRight() == 0)) {
					System.out.println(this.myAgent.getLocalName()+" - Trying to store treasure in a tanker");
					Boolean success = ((AbstractDedaleAgent)this.myAgent).emptyMyBackPack("t1");
					if(success) {
						System.out.println(this.myAgent.getLocalName()+" - Stored treasure in tanker t1");
					}else{
						success = ((AbstractDedaleAgent)this.myAgent).emptyMyBackPack("t2");
						if(success) {
							System.out.println(this.myAgent.getLocalName()+" - Stored treasure in tanker t2");
						}else{
							System.out.println(this.myAgent.getLocalName()+" - Failed to store treasure in tanker");
						}
					}
				}

				//Only move if the treasure has been collected completely
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
			}
		}
	}
}
