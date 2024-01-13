package eu.su.mas.dedaleEtu.mas.knowledge;

import java.io.Serializable;

import eu.su.mas.dedale.env.Observation;

public class Treasure implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    
    private String id;
    private int maxAmount;
    private State state;
    private Observation type;

    public Treasure(String id, int amount, State state, Observation type) {
        this.id = id;
        this.maxAmount = amount;
        this.state = state;
        this.type = type;
    }

    public Treasure(String treasure) {
        String[] treasureSplit = treasure.split(";");
        System.out.println(treasure);
        this.id = treasureSplit[0];

        if (treasureSplit[1].equals("Gold")) {
            this.type = Observation.GOLD;
        } else if (treasureSplit[1].equals("Diamond")){
            this.type = Observation.DIAMOND;
        }
        
        this.maxAmount = Integer.parseInt(treasureSplit[2]);

        if(treasureSplit[3].equals("Locked")) {
            this.state = State.LOCKED;
        }else if(treasureSplit[3].equals("Unlocked")) {
            this.state = State.UNLOCKED;
        }
    }

    public String getId() {
        return id;
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    public State getState() {
        return state;
    }

    public void setState(State s) {
        this.state = s;
    }

    public Observation getType() {
        return type;
    }

    public boolean equals(Treasure t) {
        return this.id.equals(t.getId());
    }

    public String toString() {
        return this.id + ";" + this.type + ";" + this.maxAmount + ";" + this.state;
    }
}