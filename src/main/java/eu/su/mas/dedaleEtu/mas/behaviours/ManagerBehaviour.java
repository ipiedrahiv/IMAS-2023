package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.Date;
import java.util.List;
import java.util.Map;

import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
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
		super(myagent, 3000);
		this.myMap=myMap;
	}

	@Override
	public void onTick() {

		if(this.myMap==null) {
			this.myMap= MapRepresentation.getInstance();
		}

		List<Treasure> unlockedTreasures = this.myMap.getUnlockedTreasures();

		for (Treasure treasure : unlockedTreasures) {
			if (!unlockedTreasures.isEmpty()) {
				String[] responders = {"c1", "c2", "c3", "c4"};
				
				// Fill the CFP message
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				for (int i = 0; i < responders.length; ++i) {
					cfp.addReceiver(new AID(responders[i], AID.ISLOCALNAME));
				}
				cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
				cfp.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
				cfp.setContent(treasure.toString());
				
				this.myAgent.addBehaviour(new DedaleContractNetInitiator(this.myAgent, cfp));
			}
			break;
		}

	}
}