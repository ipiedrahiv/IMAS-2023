package eu.su.mas.dedaleEtu.mas.knowledge;

import java.io.Serializable;

public enum State implements Serializable {
   LOCKED("Locked"),
   UNLOCKED("Unlocked"),
   COLLECTED("Collected");

   private String name;

   private State(String name) {
      this.name = name;
   }

   public String toString() {
      return this.name;
   }

   public String getName() {
      return this.name;
   }
}

