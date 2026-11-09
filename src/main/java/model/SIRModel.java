package model;

public class SIRModel {
    private double infectionRadius = 15.0;
    private double infectionProbability = 0.3;  // 30% chance per tick when close
    private int    recoverydays = 300;          // ticks until recovery
    private double deathRate = 0.02;             // 2% of infected will die
    private int    populationSize = 300;
    private double movementSpeed = 1.5;

    // Getters
    public double getInfectionRadius()      { return infectionRadius; }
    public double getInfectionProbability() { return infectionProbability; }
    public int    getRecoveryTicks()        { return recoverydays; }
    public double getDeathRate()            { return deathRate; }
    public int    getPopulationSize()       { return populationSize; }
    public double getMovementSpeed()        { return movementSpeed; }

    // Setters (called by sliders later)
    public void setInfectionRadius(double v)      { infectionRadius = v; }
    public void setInfectionProbability(double v) { infectionProbability = v; }
    public void setRecoveryTicks(int v)           { recoverydays = v; }
    public void setDeathRate(double v)            { deathRate = v; }
    public void setMovementSpeed(double v)        { movementSpeed = v; }
}