package eu.su.mas.dedaleEtu.mas.agents.dummies;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.ReceiveTreasureTankerBehaviour;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.*;
import eu.su.mas.dedaleEtu.mas.behaviours.SemiRandomWalkBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;
import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.State;
import eu.su.mas.dedaleEtu.mas.knowledge.Treasure;


/**
 * Dummy Tanker agent. It does nothing more than printing what it observes every 10s and receiving the treasures from other agents. 
 * <p>
 * Note that this last behaviour is hidden, every tanker agent automatically possess it.
 * 
 * @author hc
 *
 */
public class DummyTankerAgent extends AbstractDedaleAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1784844593772918359L;

	private MapRepresentation myMap;

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
		lb.add(new RandomTankerBehaviour(this, myMap));
		lb.add(new SemiRandomWalkBehaviour(this, myMap));
		
		addBehaviour(new startMyBehaviours(this,lb));
		
		System.out.println("the  agent "+this.getLocalName()+ " is started");

	}

	/**
	 * This method is automatically called after doDelete()
	 */
	protected void takeDown(){

	}
}


/**************************************
 * 
 * 
 * 				BEHAVIOUR
 * 
 * 
 **************************************/

class RandomTankerBehaviour extends TickerBehaviour{
	/**
	 * When an agent choose to migrate all its components should be serializable
	 *  
	 */
	private static final long serialVersionUID = 9088209402507795289L;

	private MapRepresentation myMap;

	private boolean done = true;

	private boolean reported = false;


	public RandomTankerBehaviour (final AbstractDedaleAgent myagent, MapRepresentation myMap) {

		super(myagent, 5000);
		this.myMap = myMap;
	}

	public int intentionalMovement = 0;

	@Override
	public void onTick() {
		

		ACLMessage message = this.myAgent.receive();

        if (message != null) {
            if(message.getProtocol().equals("MoveTanker")) {
                String nodeId = message.getContent();
				System.out.println(this.myAgent.getLocalName()+" - Received request to move to node "+nodeId);

				ACLMessage msg=new ACLMessage(ACLMessage.REQUEST);

				msg.setSender(this.myAgent.getAID());
				msg.setProtocol("GetPathToTreasure");

				Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
				String conversationId = "GetPathToTreasure-" + System.currentTimeMillis();

				if (myPosition!=null && myPosition.getLocationId()!=""){
					msg.setContent(myPosition.getLocationId()+";"+nodeId);

					msg.addReceiver(new AID("Explorer 1",AID.ISLOCALNAME));
					msg.addReceiver(new AID("Explorer 2",AID.ISLOCALNAME));
					msg.addReceiver(new AID("Explorer 3",AID.ISLOCALNAME));
					msg.addReceiver(new AID("Manager 1",AID.ISLOCALNAME));
					// msg.addReceiver(new AID("m3",AID.ISLOCALNAME));
					// msg.addReceiver(new AID("m4",AID.ISLOCALNAME));
					// msg.addReceiver(new AID("m5",AID.ISLOCALNAME));									

					// Set the conversation ID of the request
					msg.setConversationId(conversationId);
					((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				}

				System.out.println(this.myAgent.getLocalName()+" - Waiting for path to node "+nodeId);
				ACLMessage pathMsg = this.myAgent.blockingReceive(MessageTemplate.MatchConversationId(conversationId));
				
				System.out.println(this.myAgent.getLocalName()+" - Received path to node "+nodeId);
				if(pathMsg == null){
					System.out.println(this.myAgent.getLocalName()+" - Path to treasure is null");
				}else if(pathMsg.getProtocol().equals("PathToTreasure")) {
					String pathString = pathMsg.getContent();
					if(pathString != null) {
						List<String> path = Arrays.asList(pathString.split(";"));
						if(path.size() > 1) {
							for(String node : path) {
								if(node.equals(nodeId)) {
									break;
								}
								List<Couple<Location,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();
								Location nextNode = null;
								for(Couple<Location, List<Couple<Observation,Integer>>> c: lobs) {
									if(c.getLeft().getLocationId().equals(node)) {
										nextNode = c.getLeft();
									}
								}
								if (nextNode != null) {
									while(!((AbstractDedaleAgent)this.myAgent).moveTo(nextNode)) {
										intentionalMovement += 1;
										this.myAgent.doWait(100);
									}
									this.myAgent.doWait(200);
								}		
							}
							System.out.println(this.myAgent.getLocalName()+" - Reached agent at node "+nodeId);
						}
					}
				}
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
					System.out.println("TANKER-MOVE BEHAVIOUR");
					System.out.println("Intentional movements for agent " + myAgent.getName() + " = " + intentionalMovement);
					System.out.println("####################");
					reported = true;
				}
			}

		}
	}
}