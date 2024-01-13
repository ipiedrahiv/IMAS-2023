package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.Date;
import java.util.List;
import java.util.Map;

import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.State;
import eu.su.mas.dedaleEtu.mas.knowledge.Treasure;
import eu.su.mas.dedaleEtu.mas.protocols.DedaleContractNetInitiator;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

/**
 * This example behaviour try to send a hello message (every 3s maximum) to agents Collect2 Collect1
 * @author hc
 *
 */
public class ManagerBehaviour extends TickerBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2058134622078521998L;
	private MapRepresentation myMap;

	/**
	 * @param myagent the agent who posses the behaviour
	 *  
	 */
	public ManagerBehaviour (final Agent myagent, MapRepresentation myMap) {
		super(myagent, 10000);
		this.myMap=myMap;
	}

	@Override
	public void onTick() {

		if(this.myMap==null) {
			this.myMap= MapRepresentation.getInstance();
		}

		List<Treasure> unlockedTreasures = this.myMap.getUnlockedTreasures();
		boolean gold = false;
		boolean diamond = false;
		boolean proceed = true;

		if (!unlockedTreasures.isEmpty()) {
			for (Treasure treasure : unlockedTreasures) {
				treasure.setState(State.COVERED);
				if(treasure.getType() == Observation.GOLD && gold) {
					proceed = false;
				}else if(treasure.getType() == Observation.DIAMOND && diamond) {
					proceed = false;
				}

				if(proceed) {
					String[] responders;
					if (treasure.getType() == Observation.GOLD) {
						responders = new String[]{"Collector 1", "Collector 2"};
						gold = true;
					} else {
						responders = new String[]{"Collector 3", "Collector 4"};
						diamond = true;
					}
				
					// Fill the CFP message
					ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
					for (int i = 0; i < responders.length; ++i) {
						cfp.addReceiver(new AID(responders[i], AID.ISLOCALNAME));
					}
					cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
					cfp.setReplyByDate(new Date(System.currentTimeMillis() + 8000));
					cfp.setContent(treasure.toString());

					this.myAgent.addBehaviour(new DedaleContractNetInitiator(this.myAgent, cfp, this.myMap));
				}
				if(treasure.getState() == State.COVERED) {
					treasure.setState(State.UNLOCKED);
				}
				proceed = true;
			}
			
		}
	}
}