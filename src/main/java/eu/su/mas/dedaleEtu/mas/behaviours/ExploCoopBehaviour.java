package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.gsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.knowledge.State;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.Treasure;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapBehaviour;


import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;


/**
 * <pre>
 * This behaviour allows an agent to explore the environment and learn the associated topological map.
 * The algorithm is a pseudo - DFS computationally consuming because its not optimised at all.
 * 
 * When all the nodes around him are visited, the agent randomly select an open node and go there to restart its dfs. 
 * This (non optimal) behaviour is done until all nodes are explored. 
 * 
 * Warning, this behaviour does not save the content of visited nodes, only the topology.
 * Warning, the sub-behaviour ShareMap periodically share the whole map
 * </pre>
 * @author hc
 *
 */
public class ExploCoopBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	private boolean finished = false;
	private boolean explored = false;
	private Location prevLoc = null;

	/**
	 * Current knowledge of the agent regarding the environment
	 */
	private MapRepresentation myMap;

	private List<String> list_agentNames;
	private int count = 0;

	public int move = 0;

	private boolean done = true;

	private boolean reported = false;


/**
 * 
 * @param myagent reference to the agent we are adding this behaviour to
 * @param myMap known map of the world the agent is living in
 * @param agentNames name of the agents to share the map with
 */
	public ExploCoopBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap,List<String> agentNames) {
		super(myagent);
		this.myMap=myMap;
		this.list_agentNames=agentNames;
		
		
	}

	@Override
	public void action() {

		if(this.myMap==null) {
			this.myMap= MapRepresentation.getInstance();
		}

		//0) Retrieve the current position
		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if (myPosition!=null){
			move += 1;
			//List of observable from the agent's current position
			List<Couple<Location,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition

			/**
			 * Just added here to let you see what the agent is doing, otherwise he will be too quick
			 */
			try {
				this.myAgent.doWait(200);
			} catch (Exception e) {
				e.printStackTrace();
			}

			//1) remove the current node from openlist and add it to closedNodes.
			this.myMap.addNode(myPosition.getLocationId(), MapAttribute.closed);

			//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
			String nextNodeId=null;
			Iterator<Couple<Location, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
			while(iter.hasNext()){
				Location accessibleNode=iter.next().getLeft();
				boolean isNewNode=this.myMap.addNewNode(accessibleNode.getLocationId());
				//the node may exist, but not necessarily the edge
				if (myPosition.getLocationId()!=accessibleNode.getLocationId()) {
					this.myMap.addEdge(myPosition.getLocationId(), accessibleNode.getLocationId());
					if (nextNodeId==null && isNewNode) nextNodeId=accessibleNode.getLocationId();
				}
			}

			//2.5) Check if there is a treasure in the current node and add it to treasure list if it exists
			unlockTreasure(myPosition, lobs);
			

			//3) while openNodes is not empty, continues.
			if(!explored) {
				if(count > 0) {
					moveRandom(myPosition);
				}else {
					if (!this.myMap.hasOpenNode()){
						//Explo finished
						explored=true;
						System.out.println(this.myAgent.getLocalName()+" - Exploration completed succesfully.");
					}else{
						//4) select next move.
						//4.1 If there exist one open node directly reachable, go for it,
						//	 otherwise choose one from the openNode list, compute the shortestPath and go for it
						if (nextNodeId==null){
							//no directly accessible openNode
							//chose one, compute the path and take the first step.
							try {
								nextNodeId=this.myMap.getShortestPathToClosestOpenNode(myPosition.getLocationId()).get(0);
								if(!((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(nextNodeId))) {
									move += 1;
									count += 3;
								}
							}catch(Exception e) {
								moveRandom(myPosition);
							}
						}else {
							//System.out.println("nextNode notNUll - "+this.myAgent.getLocalName()+"-- list= "+this.myMap.getOpenNodes()+"\n -- nextNode: "+nextNode);
						}
					}		
				}
			}else {
				if(!moveTowardsTreasure(myPosition)) {
					moveRandom(myPosition);
				}
			}
			if(prevLoc == myPosition) {
				System.out.println(this.myAgent.getLocalName()+" - Stuck at "+myPosition.getLocationId()+", moving randomly");
				count += 3;
				moveRandom(myPosition);
			}
			count -= 1;
			prevLoc = myPosition;
		}

		done = true;

		List<Treasure> allTreasures = this.myMap.getTreasures();

		if(allTreasures.size() == 10) {
			for (Treasure treasure : allTreasures) {
				done = done && ((treasure.getState() == State.COLLECTED));
			}
		} else {
			done = false;
		}

		if(done == true) {
			if (reported == false) {
				System.out.println("####################");
				System.out.println("EXPLORER BEHAVIOUR");
				System.out.println("Movements for agent " + myAgent.getName() + " = " + move);
				System.out.println("####################");
				reported = true;
			}
		}
	}

	public void unlockTreasure(Location myPosition, List<Couple<Location,List<Couple<Observation,Integer>>>> lobs) {
		List<Couple<Observation,Integer>> lObservations= lobs.get(0).getRight();
		for(Couple<Observation,Integer> o:lObservations){
			switch (o.getLeft()) {
			case DIAMOND:case GOLD:
				Boolean added = this.myMap.addTreasure(myPosition.getLocationId(), o.getRight(), o.getLeft());
				if(added) {
					System.out.println(this.myAgent.getLocalName()+" - New treasure ("+ o.getLeft()+" - "+o.getRight()+") found at "+myPosition.getLocationId());
				}
				if(!this.myMap.checkUnlocked(myPosition.getLocationId())) {
					Boolean success = ((AbstractDedaleAgent) this.myAgent).openLock(o.getLeft());
					if(success) {
						this.myMap.unlockTreasure(myPosition.getLocationId());
						System.out.println(this.myAgent.getLocalName()+" - Opened lock ("+ o.getLeft()+" - "+o.getRight()+") at "+myPosition.getLocationId());
					}else{
						System.out.println(this.myAgent.getLocalName()+" - Failed to open lock ("+ o.getLeft()+" - "+o.getRight()+") at "+myPosition.getLocationId());
					}
				}
				break;
			default:
				break;
			}
		}
	}

	public boolean moveTowardsTreasure(Location myPosition) {
		if(myPosition != null && myPosition.getLocationId()!="") {
			List<Treasure> treasures = this.myMap.getTreasures();
			List<String> minPath = null;
			int minPathSize = Integer.MAX_VALUE;
			for(Treasure t : treasures) {
				if(t.getState() == State.LOCKED) {
					List<String> path = this.myMap.getShortestPath(myPosition.getLocationId(), t.getId());
					if(path != null && path.size() > 0 && path.size() < minPathSize) {
						minPathSize = path.size();
						minPath = path;
					}
				}
			}
			if(minPath != null && minPathSize > 0) {
				((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(minPath.get(0)));
				return true;
			}
		}
		return false;
	}


	private void moveRandom(Location myPosition) {
		if (myPosition!=null && myPosition.getLocationId()!=""){
			List<Couple<Location,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();

			//Random move from the current position
			int moveId;
			do{
				Random r= new Random();
				moveId = 1 + r.nextInt(lobs.size()-1);
			}while(lobs.get(moveId).getLeft() == prevLoc && prevLoc != null);

			((AbstractDedaleAgent)this.myAgent).moveTo(lobs.get(moveId).getLeft());
		}
	}

	@Override
	public boolean done() {
		return finished;
	}

}
