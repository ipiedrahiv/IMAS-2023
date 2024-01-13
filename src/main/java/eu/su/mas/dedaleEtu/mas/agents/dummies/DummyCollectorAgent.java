package eu.su.mas.dedaleEtu.mas.agents.dummies;

import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.*;
import eu.su.mas.dedaleEtu.mas.knowledge.Treasure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import dataStructures.tuple.Couple;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

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
	private boolean onMission = false;



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


		MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
				MessageTemplate.MatchPerformative(ACLMessage.CFP) );

		addBehaviour(new ContractNetResponder(this, template) {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
				System.out.println("Agent "+getLocalName()+": CFP received from "+cfp.getSender().getName()+". Action is reach treasure: "+cfp.getContent());

				// Retrieve treasure from message
				Treasure t = new Treasure(cfp.getContent());

				// Check if the treasure is the same type as the agent
				if (((AbstractDedaleAgent) this.getAgent()).getMyTreasureType().equals(t.getType())) {

					// Evaluate the action (get length of path to treasure)
					List<String> path = evaluateAction(t);

					if(path != null) {
						// Obtain the length of the path
						int pathLen = path.size();
						System.out.println("Agent "+getLocalName()+": Proposing "+pathLen);

						// Send the length of the path as a proposal
						ACLMessage propose = cfp.createReply();
						propose.setPerformative(ACLMessage.PROPOSE);
						propose.setContent(String.valueOf(pathLen));
						return propose;
					}else{
						System.out.println("Agent "+getLocalName()+": Refuse");
						throw new RefuseException("evaluation-failed");
					}
				}else {
					System.out.println("Agent "+getLocalName()+": Refuse");
					throw new RefuseException("evaluation-failed");
				}
			}

			@Override
			protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose,ACLMessage accept) throws FailureException {
				System.out.println("Agent "+getLocalName()+": Proposal accepted");
				Treasure t = new Treasure(cfp.getContent());

				// Perform the action (move to treasure)
				if (performAction(evaluateAction(t))) {
					System.out.println("Agent "+getLocalName()+": Action successfully performed");
					ACLMessage inform = accept.createReply();
					inform.setPerformative(ACLMessage.INFORM);
					return inform;
				}else {
					System.out.println("Agent "+getLocalName()+": Action execution failed");
					throw new FailureException("unexpected-error");
				}	
			}

			protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
				System.out.println("Agent "+getLocalName()+": Proposal rejected");
			}
		} );
	}

	private Boolean performAction(List<String> path) {
		this.onMission = true;
		// Get observable nodes
		while(path.size() > 0) {
			List<Couple<Location,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this).observe();
			// Retrieve location from the list of observations whose id matches the first node in the path
			Location nextNode = null;
			for(Couple<Location, List<Couple<Observation,Integer>>> c: lobs) {
				if(c.getLeft().getLocationId().equals(path.get(0))) {
					nextNode = c.getLeft();
				}
			}
			if (nextNode != null) {
				// Move to the next node
				((AbstractDedaleAgent)this).moveTo(nextNode);
				path.remove(0);
			}else {
				this.onMission = false;
				return false;
			}
		}
		this.onMission = false;
		return true;
	}

	private List<String> evaluateAction(Treasure t) {
		System.out.println(this.getLocalName()+" - Asking for path to treasure "+t.getId());
		ACLMessage msg=new ACLMessage(ACLMessage.INFORM);

		msg.setSender(this.getAID());
		msg.setProtocol("GetPathToTreasure");

		Location myPosition=((AbstractDedaleAgent)this).getCurrentPosition();

		if (myPosition!=null && myPosition.getLocationId()!=""){
			msg.setContent(myPosition.getLocationId()+";"+t.getId());

			msg.addReceiver(new AID("e1",AID.ISLOCALNAME));
			msg.addReceiver(new AID("e2",AID.ISLOCALNAME));
			msg.addReceiver(new AID("e3",AID.ISLOCALNAME));
			msg.addReceiver(new AID("m1",AID.ISLOCALNAME));
			// msg.addReceiver(new AID("m2",AID.ISLOCALNAME));
			// msg.addReceiver(new AID("m3",AID.ISLOCALNAME));
			// msg.addReceiver(new AID("m4",AID.ISLOCALNAME));
			// msg.addReceiver(new AID("m5",AID.ISLOCALNAME));									

			((AbstractDedaleAgent)this).sendMessage(msg);
		}

		ACLMessage pathMsg = this.blockingReceive(MessageTemplate.MatchProtocol("PathToTreasure"));
		if(pathMsg != null) {
			String path = pathMsg.getContent();
			if(path != null) {
				String[] pathArray = path.split(";");
				if(pathArray.length > 1) {
					return Arrays.asList(pathArray);
				}
			}
		}
		return null;
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

			if (myPosition!=null && myPosition.getLocationId()!="") {
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

								if(amount == o.getRight()) {
									System.out.println(this.myAgent.getLocalName()+" - Treasure collected completely");
									ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
			
									msg.setSender(this.myAgent.getAID());
									msg.setProtocol("UpdateTreasure");

									if (myPosition!=null && myPosition.getLocationId()!=""){
										msg.setContent(myPosition.getLocationId());

										msg.addReceiver(new AID("e1",AID.ISLOCALNAME));
										msg.addReceiver(new AID("e2",AID.ISLOCALNAME));
										msg.addReceiver(new AID("e3",AID.ISLOCALNAME));
										msg.addReceiver(new AID("m1",AID.ISLOCALNAME));
										// msg.addReceiver(new AID("m2",AID.ISLOCALNAME));
										// msg.addReceiver(new AID("m3",AID.ISLOCALNAME));
										// msg.addReceiver(new AID("m4",AID.ISLOCALNAME));
										// msg.addReceiver(new AID("m5",AID.ISLOCALNAME));									

										((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
									}
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
