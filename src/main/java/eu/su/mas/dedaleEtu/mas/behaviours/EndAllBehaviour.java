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


public class EndAllBehaviour extends TickerBehaviour {

    private static final long serialVersionUID = -2058134622078521998L;
    private MapRepresentation myMap;

    /**
     * @param myagent the agent who posses the behaviour
     *
     */
    public EndAllBehaviour (final Agent myagent, MapRepresentation myMap) {
        super(myagent, 500);
        this.myMap=myMap;
    }

    @Override
    public void onTick() {

        List<Treasure> unlockedTreasures = this.myMap.getTreasures();
        boolean done = true;

        for (Treasure treasure : unlockedTreasures) {
            if (!(treasure.getState() == State.COLLECTED)) {
                done = false;
                break;
            }
        }

        if (done) {
            System.out.println("FOUND ALL THE TREASURES");
            System.exit(0);
        }
    }
}
