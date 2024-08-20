package com.rbcsystem.demo.models;

import java.util.ArrayList;

public class Agent {

    private double[] ability;
	private ArrayList<Ticket> tickets;

    //constructor
    public Agent(double[] ab){
        
        this.ability = ab;
		this.tickets = new ArrayList<Ticket>();
    }

    //getters and setters
    public double[] getAbility() {
        return ability;
    }

    public void setAbility(double[] ability) {
        this.ability = ability;
    }
	
	public void addTicket(Ticket ticket){
		tickets.add(ticket);
	}
	
	//returns a value of busyness depending on ticket amount, priority and estimated time
	//will be normalized when congregated with other agents
	//busyness will be measured by the following equation
	
	//  SumEstimatedTime += EstimatedTime * 1 + (priotity/5([0.2, 0.4, 0.6, 0.8, 1]))
	// then later put within [0, 1] with normmalization
	
	public double calculateBusyness() {
		double priority = 0;
		int estimatedTimeSum = 0;

		// Calculate the sum of product of priorities and estimated times
		for (Ticket ticket : tickets) {

			estimatedTimeSum += (ticket.getTime() * (1 + (priority/5)));
		}

		// returns estimatedTimeSum'
		return (estimatedTimeSum);
	}
}


