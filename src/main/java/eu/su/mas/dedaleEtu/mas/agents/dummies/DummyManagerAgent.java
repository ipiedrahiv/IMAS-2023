package eu.su.mas.dedaleEtu.mas.agents.dummies;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.*;
import eu.su.mas.dedaleEtu.mas.behaviours.ManagerBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.MessageManagingBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SemiRandomWalkBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.protocols.DedaleContractNetInitiator;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;


/**
 * This example class start a Dummy agent that will manage the other according to its type:
 * <ol>
 * <li>
 * <li>
 * </ol>
 * @author hc
 *
 */

public class DummyManagerAgent extends AbstractDedaleAgent{

	private static final long serialVersionUID = -2991562876411096907L;
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

		//get the parameters given into the object[]
		final Object[] args = getArguments();
		System.out.println("Arg given by the user to "+this.getLocalName()+": "+args[2]);
		
		//use them as parameters for your behaviours is you want
		
		List<Behaviour> lb=new ArrayList<Behaviour>();
		
		/************************************************
		 * 
		 * ADD the inititial behaviours of the Dummy Moving Agent here
		 * 
		 ************************************************/
		lb.add(new ManagerBehaviour(this, myMap));
		lb.add(new SemiRandomWalkBehaviour(this));
		lb.add(new MessageManagingBehaviour(this, this.myMap));
		
		/***
		 * MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
		 */
		
		addBehaviour(new startMyBehaviours(this,lb));
	}


	/**
	 * This method is automatically called after doDelete()
	 */
	protected void takeDown(){
		super.takeDown();
	}
	
	/**
	 * This method is automatically called before migration. 
	 * You can add here all the saving you need
	 */
	protected void beforeMove(){
		super.beforeMove();
	}
	
	/**
	 * This method is automatically called after migration to reload. 
	 * You can add here all the info regarding the state you want your agent to restart from 
	 * 
	 */
	protected void afterMove(){
		super.afterMove();
	}

}

