package model;

import java.util.Random;

public class Person {
    public int x, y;
    public InfectionState state;
    private int infectionTimer = 0;

     public Person (int x, int y){

             this.x = x;
         this.y = y;
         this.state = InfectionState.SUSCEPTIBLE;


     }

    }

