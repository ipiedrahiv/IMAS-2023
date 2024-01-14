package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * This example behaviour try to send a hello message (every 3s maximum) to agents Collect2 Collect1
 * @author hc
 *
 */
public class MessageManagingBehaviour extends TickerBehaviour{


	/**
     *
     */
    private static final long serialVersionUID = 1L;
    private MapRepresentation myMap;

	/**
	 * @param myagent the agent who posses the behaviour
	 *  
	 */
	public MessageManagingBehaviour (final Agent myagent, MapRepresentation myMap) {
		super(myagent, 200);
		this.myMap=myMap;
	}

    @Override
    public void onTick() {

        if(this.myMap==null) {
            this.myMap= MapRepresentation.getInstance();
        }

        ACLMessage msg = this.myAgent.receive();

        if (msg != null) {
            if(msg.getProtocol().equals("UpdateTreasure")) {
                // Update the treasure state to unlocked
                String treasureId = msg.getContent();
                this.myMap.updateTreasure(treasureId);

                ACLMessage confirmation = new ACLMessage(ACLMessage.INFORM);
                confirmation.setSender(this.myAgent.getAID());
                confirmation.setProtocol("TreasureUpdated");
                confirmation.setContent(treasureId);
                confirmation.addReceiver(new AID(msg.getSender().getLocalName(), AID.ISLOCALNAME));
                ((AbstractDedaleAgent)this.myAgent).sendMessage(confirmation);

            }else if(msg.getProtocol().equals("GetPathToTreasure")) {
                String content = msg.getContent();
                String[] ids = content.split(";");
                if(ids[0] != null && ids[1] != null){
                    List<String> path = this.myMap.getShortestPath(ids[0], ids[1]);

                    ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
                    reply.setSender(this.myAgent.getAID());
                    reply.setProtocol("PathToTreasure");

                    Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

                    if (myPosition!=null && myPosition.getLocationId()!=""){
                        if(path != null) {
                            reply.setContent(String.join(";", path));
                        }else{
                            reply.setContent(null);
                        }
                        
                        reply.addReceiver(new AID(msg.getSender().getLocalName(), AID.ISLOCALNAME));
                        reply.setConversationId(msg.getConversationId());
                        // System.out.println("Agent "+this.myAgent.getLocalName()+ " sent path to treasure to "+msg.getSender().getLocalName());
                        ((AbstractDedaleAgent)this.myAgent).sendMessage(reply);
                    }
                }else {
                    ((AbstractDedaleAgent)this.myAgent).sendMessage(null);
                }
            }else if(msg.getProtocol().equals("HelpCollector")) {
                //Retrieve id from content and send message to a tanker to go to the collector
                String agentId = msg.getContent();
                ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
                reply.setSender(this.myAgent.getAID());
                reply.setProtocol("MoveTanker");
                reply.setContent(agentId);
                
                reply.addReceiver(new AID("Tanker 1", AID.ISLOCALNAME));
                reply.addReceiver(new AID("Tanker 2", AID.ISLOCALNAME));

                ((AbstractDedaleAgent)this.myAgent).sendMessage(reply);
                
            }
        }
    }
}