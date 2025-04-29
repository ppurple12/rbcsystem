package com.rbcsystem.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import com.rbcsystem.demo.persistence.repositories.gra.TaskRepository;
import com.rbcsystem.demo.persistence.entities.AgentEntity;
import com.rbcsystem.demo.persistence.entities.TicketEntity;
import com.rbcsystem.demo.persistence.entities.gra.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.transaction.Transactional;


@Service
@Transactional
public class GRAService {

    @Autowired
    private QualificationService qualificationService;

    @Autowired
    private TaskRepository taskRepository;

       //  puts the pieces all together
       public Map<Integer, Integer> GRAABD_WS(int[] L, List<AgentEntity> agents) {
        // Important initializations
        int m = agents.size();
        int n = qualificationService.getAbilitiesByAgentId(agents.get(0).getId()).length;
        double weight = 0.5;
    
        // Ensure the sizes are correct
        double[] busyWeights = agentBusyness(agents);
        double[][] Q = makeQMatrix(agents, m, n);
        
        // Debugging: print busy weights
        System.out.println("Busy Weights: " + Arrays.toString(busyWeights));
    
        // Debugging: print Q matrix
        System.out.println("Initial Q Matrix:");
        for (double[] row : Q) {
            System.out.println(Arrays.toString(row));
        }
    
        // Iterate over weights to find the optimal assignment
        for (double w = weight; w < 1.1; w += 0.1) {
            System.out.println("Trying weight: " + w);
            
            // Create modified Q matrix based on current weight
            double[][] Qp = makeQPrime_Vector(m, n, Q, busyWeights, w);
            
            // Debugging: print modified Q matrix
            System.out.println("Modified Q Matrix with weight " + w + ":");
            for (double[] row : Qp) {
                System.out.println(Arrays.toString(row));
            }
    
            // Perform optimization
            Map<Integer, Integer> assignments = optimizeAssignments(m, n, Qp, L, busyWeights, w);
            
            // Check if valid assignments were found
            if (assignments != null) {
                System.out.println("Found valid assignments: " + assignments);
                return assignments;
            }
        }
    
        // If no valid assignments found
        System.out.println("No valid assignments found for any weight.");
        return null;
    }
    //  Stacked Group Role Assignment with Agent Busyness' Degree in Weighted Sum Form
    //  to handle the assignment of multiple tickets at a time 
    //  essentially the same as GRAABD_WS but uses a potentialBusynessWeight attribute
    //  to account for the previous tickets assigned in the queue and sQ as the Q matrix
    //  where every qualifications value represents the agents' fitness to the task
    public List<int[]> SGRAABD_WS(List<int[]> Ls, List<List<Double>> agentFitness, List<AgentEntity> agents, List<TicketEntity> tickets, int[][] agentIndex){

        //important intitalizations
       
        int m = tickets.size();
        int n = agents.size();
        double weight = 0.5;
        List<int[]> stackedAssignment = new  ArrayList<int[]>();
        double[] potentialBusynessWeight = new double[n];
        //MAKE SURE STUFF IS RIGHT SIZE HERE
        //at every iteration, checks how potentially busy an agent can be
        double[] busyWeights = new double[n];
        // at each iteration, takes agent qualifications into account to return a fitness 
        // matrix, where fitness is their ability at the specified roles.
        double[][] Q = makeFitnessMatrix(agentFitness, m, n);
        
            /*
            For logging:
            double[][] Qp = makeQPrime_Vector(m, n, Q, busyWeights, w);
            for (int i = 0; i < m; i++){
                String srtr = "";
                for (int j = 0; j < n; j++){
                    srtr += Qp[i][j];
                    srtr += " ";
                }
                System.out.println(srtr);
            }
            */
        for (int i = 0; i < Q.length; i++){  
            int[] L = Ls.get(i);  
            int roleAmt = 0;
            for (int j = 0; j < L.length; j++){
                roleAmt += L[j];
            }
            busyWeights = stackedAgentBusyness(agents, potentialBusynessWeight);

            for (double w = weight; w<1.1; w += 0.1){           
                int[] assignments = stackedOptimizeAssignments(n,  Q[i], roleAmt, busyWeights, w, L, agentIndex);
            
                if(assignments != null){
                    //increments potential weights of agents who were optimal for specific task
                    potentialBusynessWeight = incrementPotentialWeight(assignments, busyWeights, tickets, i);
                    stackedAssignment.add(assignments);
                    break;
                } 
                if(assignments == null && w >= 1){
                    // indicator for handling non-optimal assignment
                   assignments = new int[] {-1};
                   System.out.println("MADE IT HERE");
                   stackedAssignment.add(assignments);
                }
            
            }        
        }

    
    return stackedAssignment;
    
    }

     //  Stacked Group Role Assignment with Agent Busyness' Degree in Weighted Sum Form and Agent Constrinctiond(La array)
    //  to handle the assignment of multiple tickets at a time 
    //  essentially the same as GRAABD_WS but uses a La vector
    //  to account for the previous tickets assigned in the queue and sQ as the Q matrix
    //  where every qualifications value represents the agents' fitness to the task
    public List<int[]> SGRAABD_WSAC(List<int[]> Ls, List<List<Double>> agentFitness, List<AgentEntity> agents, List<TicketEntity> tickets) {

        // Important initializations
        int m = tickets.size();
        int n = agents.size();
        int ticketLimit = 3;
        int[] La = new int[n];
        double weight = 0.5;
        List<int[]> stackedAssignment = new ArrayList<>();
        double[] busyWeights = agentBusyness(agents);
    
        // Create fitness matrix
        double[][] Q = makeFitnessMatrix(agentFitness, m, n);
    
        for (int i = 0; i < Q.length; i++) {
            int[] L = Ls.get(i);
            int roleAmt = 0;
            for (int j = 0; j < L.length; j++) {
                roleAmt += L[j];
            }
    
            boolean assignmentMade = false;
            for (double w = weight; w <= 1.1; w += 0.1) {
                int[] assignments = stackedOptimizeAssignmentsWithAgentConstraint(n, Q[i], roleAmt, busyWeights, w, ticketLimit, La);
    
                if (assignments != null) {
                    // If an assignment is made, add the ticket to their ticket amount
                    int maxedOutAgents = 0;
                    for (int j = 0; j < n; j++) {
                        if (assignments[j] == 1) {
                            La[j]++;
                        }
                        if (La[j] >= ticketLimit) {
                            maxedOutAgents++;
                        }
                        
                    }
                    if (maxedOutAgents == n) {
                        ticketLimit += 3;
                    }
                    stackedAssignment.add(assignments);
                    assignmentMade = true;
                    break;
                }
    
                // Check if the role amount is bigger than agents with less than max tickets
                int maxedOutAgents = 0;
                for (int j = 0; j < n; j++) {
                    if (La[j] >= ticketLimit) {
                        maxedOutAgents++;
                    }
                }
                if ((n - maxedOutAgents) < roleAmt) { // Check if the number of available agents is smaller than required roles
                    ticketLimit += 3;
                }
            }
    
            if (!assignmentMade) {
              
                return null;
            }
        }
    
       
        return stackedAssignment;
    }



    //checks busyness of agents and normalizes tht totals
    public double[] agentBusyness(List<AgentEntity> agents){
        double maxVal = Double.MIN_VALUE;
        double minVal = Double.MAX_VALUE;
        double[] weightVector = new double[agents.size()];

        //handles NaN situations
        if (weightVector.length == 1){
            return new double[]{1};
        }

        for (int i = 0; i < agents.size(); i++) {
            AgentEntity agent = agents.get(i);
            System.out.println(agent.getId());
            weightVector[i] = agent.calculateBusyness();

            if (weightVector[i] < minVal) {
                minVal = weightVector[i];
            }
            if (weightVector[i] > maxVal) {
                maxVal = weightVector[i];
            }
        }

        double minRange = 0.1;
        double maxRange = 0.9;
        for (int i = 0; i < agents.size(); i++) {
            weightVector[i] = minRange + (weightVector[i] - minVal) * (maxRange - minRange) / (maxVal - minVal);

            //if they have no tickets
            if (Double.isNaN(weightVector[i])) {
                weightVector[i] = 0.001;
            }
        }
        return weightVector;
    }

    //checks busyness of agents and normalizes tht totals
    public double[] stackedAgentBusyness(List<AgentEntity> agents, double[] potentialBusynessWeight){
        double maxVal = Double.MIN_VALUE;
        double minVal = Double.MAX_VALUE;
        double[] weightVector = new double[agents.size()];

        //handles NaN situations
        if (weightVector.length == 1){
            return new double[]{1};
        }

        for (int i = 0; i < agents.size(); i++) {
            double busyness = agents.get(i).calculateBusyness();
            double weightedSum = busyness + potentialBusynessWeight[i];
            weightVector[i] = weightedSum;
            if (weightVector[i] < minVal) {
                minVal = weightVector[i];
            }
            if (weightVector[i] > maxVal) {
                maxVal = weightVector[i];
            }
        }
        double minRange = 0.1;
        double maxRange = 0.9;
        for (int i = 0; i < agents.size(); i++) {
            weightVector[i] = minRange + (weightVector[i] - minVal) * (maxRange - minRange) / (maxVal - minVal);

            //if they have no tickets
            if (Double.isNaN(weightVector[i])) {
                weightVector[i] = 0.001;
            }
        }
        return weightVector;
    }

    public double[] incrementPotentialWeight(int[] assignments, double[] busyWeights, List<TicketEntity> tickets, int val) {
        
        // Ensure assignments and busyWeights have the same length to avoid ArrayIndexOutOfBoundsException
        if (assignments.length != busyWeights.length) {
            throw new IllegalArgumentException("Assignments and busyWeights must have the same length.");
        }
    
        for (int i = 0; i < assignments.length; i++) {
            if (assignments[i] == 1) {  // Only consider important agents
                Long taskId = tickets.get(val).getTaskId();
                Task task = taskRepository.findById(taskId);
                
                busyWeights[i] += (double) task.getEstimatedTime();
               
            }
           
          
        }
        return busyWeights;
    }

    
    //  simple matrix output for modelling
    public void showQMatrix(double[][] Q, int m, int n){
        System.out.println("Q Matrix:");
        for (int i = 0; i < m; i++){
            String str = "";
            for (int j = 0; j < n; j++){
                str += Q[i][j];
                str += " ";
            }
            System.out.println(str);
        }
    }

    public double[][] makeQMatrix(List<AgentEntity> agents, int m, int n){
        double[][] Q = new double[m][n];

        for (int i=0; i < m; i++){
            double[] abilities = qualificationService.getAbilitiesByAgentId(agents.get(i).getId());
            for (int j = 0; j < n ;j++){
                Q[i][j] = (double) abilities[j];
              
            }
        }
        return Q;
    }

    // uses Q and prepares it by using weight sum and busyness vector
    public double[][] makeQPrime_Vector(int m, int n, double[][] Q, double[] Vb, double w){
        double [][]Qp = new double [m][n];
			for (int i=0; i<m; i++) {
				for (int j=0; j<n; j++){
				    Qp[i][j] = ((w*Q[i][j]) - (1 - w) * Vb[i]);
                    Qp[i][j] = Math.round(Qp[i][j] * 100.0) / 100.0;
                }
            }

        return Qp;
    }

    //makes matrix for fitness to tasks using agent role qualifications and requirments for tasks
    public double[][] makeFitnessMatrix(List<List<Double>> agentFitness, int m, int n){
        double[][] Q = new double[m][n];

         // Iterate over each agent
        for (int i = 0; i < m; i++) {
            List<Double> fitness = agentFitness.get(i); // Get the fitness values for the current task

            // Iterate over each fitness value for the current task
            for (int j = 0; j < n; j++) {
                Q[i][j] = fitness.get(j); // Assign the fitness value to the corresponding cell in the matrix
                
            }
        }
        return Q;
    } 

    // Returns best options
    // Actual operation of the GRA Algorithm
    public Map<Integer, Integer> optimizeAssignments(int m, int n, double[][] Q, int[] L, double[] Vb, double weight) {
        Loader.loadNativeLibraries();
        MPSolver solver = MPSolver.createSolver("GLOP");
        if (solver == null) {
            return null;
        }

        //System.out.println("Number of agents: " + m);
       // System.out.println("Number of tasks: " + n);
        MPVariable[][] x = new MPVariable[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                x[i][j] = solver.makeBoolVar("x[" + i + "][" + j + "]");
            }
        }

        for (int i = 0; i < m; i++) {
            MPConstraint agentConstraint = solver.makeConstraint(0, 1, "AgentConstraint_" + i);
            for (int j = 0; j < n; j++) {
                agentConstraint.setCoefficient(x[i][j], 1);
                /*
                if (Q[i][j] < 0.1) {
                    // Ensure this assignment is not allowed by setting a constraint
                    MPConstraint qualificationConstraint = solver.makeConstraint(0, 0, "QualificationConstraint_" + i + "_" + j);
                    qualificationConstraint.setCoefficient(x[i][j], 1);
                }
                    */
            }
            //System.out.println("Agent constraint " + i + " added.");
        }

        for (int j = 0; j < n; j++) {
            MPConstraint taskConstraint = solver.makeConstraint(L[j], L[j], "TaskConstraint_" + j);
            for (int i = 0; i < m; i++) {
                taskConstraint.setCoefficient(x[i][j], 1);
            }
            //System.out.println("Task constraint " + j + " added.");
        }

        MPObjective objective = solver.objective();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                double weightedValue = ((weight * Q[i][j]) - ((1 - weight) * Vb[i]));
                objective.setCoefficient(x[i][j], weightedValue);
            }
        }
        objective.setMaximization();
       
        MPSolver.ResultStatus resultStatus = solver.solve();
        double groupScore = 0;
        int amount = 0;
        // Create a map to store the assignments
        Map<Integer, Integer> assignments = new HashMap<>();
        if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
            
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    if (x[i][j].solutionValue() > 0.5) {
                        groupScore += Q[i][j];
                        assignments.put(i, j);
                        amount++;
                        //System.out.println("Agent " + i + " is assigned to task " + j);
                    }
                }
            }
            groupScore /= amount;
            groupScore = (Math.round(groupScore * 100.0) / 100.0) * 10;
           // System.out.println("Group score: " + groupScore);
            return assignments;
        } else {
            return null;
        }
    }


    public int[] stackedOptimizeAssignments(int m, double[] Qrow, int roleAmt, double[] Vb, double weight, int[] L, int[][] agentIndex) {
        Loader.loadNativeLibraries();
        MPSolver solver = MPSolver.createSolver("CBC_MIXED_INTEGER_PROGRAMMING");
        if (solver == null) {
            return null;
        }
    
        MPVariable[] x = new MPVariable[m]; // Only need a single dimension array for one task
        for (int i = 0; i < m; i++) {
            x[i] = solver.makeBoolVar("x[" + i + "]");
        }
    
        // Constraint: At most one agent can be assigned to a task
        for (int i = 0; i < m; i++) {
            MPConstraint agentConstraint = solver.makeConstraint(0, 1, "AgentConstraint_" + i);
            agentConstraint.setCoefficient(x[i], 1);
            if (Qrow[i] < 0.1) {
                // Ensure this assignment is not allowed by setting a constraint
                MPConstraint qualificationConstraint = solver.makeConstraint(0, 0, "QualificationConstraint_" + i);
                qualificationConstraint.setCoefficient(x[i], 1);
            }
        }
    
        // Constraint: Each task must be assigned to exactly 'roleAmt' agents
        MPConstraint taskConstraint = solver.makeConstraint(roleAmt, roleAmt, "TaskConstraint");
        for (int i = 0; i < m; i++) {
            taskConstraint.setCoefficient(x[i], 1);
        }
    
        // Constraint: At least L[i] number of agents must have non-zero abilities for each role
        for (int roleIndex = 0; roleIndex < L.length; roleIndex++) {
            int requiredAgents = L[roleIndex];
            MPConstraint roleConstraint = solver.makeConstraint(requiredAgents, Double.POSITIVE_INFINITY, "RoleConstraint_" + roleIndex);
            for (int i = 0; i < m; i++) {
                if (agentIndex[i][roleIndex] > 0) {
                    roleConstraint.setCoefficient(x[i], 1);
                }
            }
        }
    
        // Objective function
        MPObjective objective = solver.objective();
        for (int i = 0; i < m; i++) {
            double weightedValue = (weight * Qrow[i]) - ((1 - weight) * Vb[i]);
            objective.setCoefficient(x[i], weightedValue);
        }
        objective.setMaximization();
    
        MPSolver.ResultStatus resultStatus = solver.solve();
        int[] assignments = new int[m];
        if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
            for (int i = 0; i < m; i++) {
                assignments[i] = (x[i].solutionValue() > 0.5) ? 1 : 0;
            }
            return assignments;
        } else {
            System.out.println("wahhhhh");
            return null;
        }
    }

    public int[] stackedOptimizeAssignmentsWithAgentConstraint(int m, double[] Qrow, int roleAmt, double[] Vb, double weight, int ticketLimit, int[] La) {
        Loader.loadNativeLibraries();
        MPSolver solver = MPSolver.createSolver("CBC_MIXED_INTEGER_PROGRAMMING");
        if (solver == null) {
            return null;
        }
    
        //System.out.println("Number of agents: " + m);
        //System.out.println("Role amount: " + roleAmt);
        MPVariable[] x = new MPVariable[m]; // Only need a single dimension array for one task
        for (int i = 0; i < m; i++) {
            x[i] = solver.makeBoolVar("x[" + i + "]");
        }
    
        // Constraint: At most one agent can be assigned to a task
        MPConstraint taskConstraint = solver.makeConstraint(roleAmt, roleAmt, "TaskConstraint");
        for (int i = 0; i < m; i++) {
            taskConstraint.setCoefficient(x[i], 1);
        }
    
        // Additional constraint: Number of tickets assigned to each agent must be less than or equal to ticketLimit
        for (int i = 0; i < m; i++) {
            int remainingTickets = ticketLimit - La[i];
            if (remainingTickets < 1) {
                MPConstraint ticketConstraint = solver.makeConstraint(0, 0, "TicketLimitConstraint_" + i);
                ticketConstraint.setCoefficient(x[i], 1);
                //System.out.println("Ticket limit constraint for agent " + i + " added: 0 (no more tasks).");
            } else {
                MPConstraint ticketConstraint = solver.makeConstraint(0, remainingTickets, "TicketLimitConstraint_" + i);
                ticketConstraint.setCoefficient(x[i], 1);
                //System.out.println("Ticket limit constraint for agent " + i + " added: " + remainingTickets);
            }
        }
    
        // Objective function
        MPObjective objective = solver.objective();
        for (int i = 0; i < m; i++) {
            double weightedValue = (weight * Qrow[i]) - ((1 - weight) * Vb[i]);
            objective.setCoefficient(x[i], weightedValue);
        }
        objective.setMaximization();
    
        MPSolver.ResultStatus resultStatus = solver.solve();
        int[] assignments = new int[m];
        if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
            for (int i = 0; i < m; i++) {
                assignments[i] = (x[i].solutionValue() > 0.5) ? 1 : 0;
                if (assignments[i] == 1) {
                    //System.out.println("Agent " + i + " is assigned.");
                }
            }
            return assignments;
        } else {
            return null;
        }
    }


}