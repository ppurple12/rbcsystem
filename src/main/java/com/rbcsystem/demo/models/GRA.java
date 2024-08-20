package  com.rbcsystem.demo.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import java.awt.Color;

public class GRA {

        // Any class that would find itself in a non-entity class (controller classes) will be found here for 
	// simplicity within the modelling

     //checks busyness of agents and normalizes tht totals
     public double[] agentBusyness(List<Agent> agents){
        double maxVal = Double.MIN_VALUE;
        double minVal = Double.MAX_VALUE;
        double[] weightVector = new double[agents.size()];

        //handles NaN situations
        if (weightVector.length == 1){
            return new double[]{1};
        }

        for (int i = 0; i < agents.size(); i++) {
            weightVector[i] = agents.get(i).calculateBusyness();

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
    public double[] stackedAgentBusyness(Agent[] agents, double[] potentialBusynessWeight){
        double maxVal = Double.MIN_VALUE;
        double minVal = Double.MAX_VALUE;
        double[] weightVector = new double[agents.length];

        //handles NaN situations
        if (weightVector.length == 1){
            return new double[]{1};
        }

        for (int i = 0; i < agents.length; i++) {
            double busyness = agents[i].calculateBusyness();
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
        for (int i = 0; i < agents.length; i++) {
            weightVector[i] = minRange + (weightVector[i] - minVal) * (maxRange - minRange) / (maxVal - minVal);

            //if they have no tickets
            if (Double.isNaN(weightVector[i])) {
                weightVector[i] = 0.001;
            }
        }
        return weightVector;
    }

     // Returns best options
    // Actual operation of the GRA Algorithm
    public double optimizeAssignments(int m, int n, double[][] Q, int[] L, double[] Vb, double weight) {
        Loader.loadNativeLibraries();
        MPSolver solver = MPSolver.createSolver("GLOP");
        if (solver == null) {
            return 0.0;
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
                if (Q[i][j] < 0.1) {
                    // Ensure this assignment is not allowed by setting a constraint
                    MPConstraint qualificationConstraint = solver.makeConstraint(0, 0, "QualificationConstraint_" + i + "_" + j);
                    qualificationConstraint.setCoefficient(x[i][j], 1);
                }
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
            //System.out.println("Group score: " + groupScore);
            return groupScore;
        } else {
            return 0.0;
        }
    }

     public double[] incrementPotentialWeight(int[] assignments, double[] busyWeights, int val) {
        
        // Ensure assignments and busyWeights have the same length to avoid ArrayIndexOutOfBoundsException
        if (assignments.length != busyWeights.length) {
            throw new IllegalArgumentException("Assignments and busyWeights must have the same length.");
        }
        Random random = new Random();
        for (int i = 0; i < assignments.length; i++) {
            if (assignments[i] == 1) {  // Only consider important agents
                
                busyWeights[i] += random.nextDouble() * 2.75 + 0.25;// for simplicity, asusme each task takes one hour
               
            }
           
          
        }
        return busyWeights;
    }
    public List<List<Double>> getAgentFitness(int[][] Ls, Agent[] agents){
        List<List<Double>> agentQualifications = new ArrayList<>();
        //loops through every role in every active ticket to see fitness to do specific ticket
        for (int[] L : Ls){ 
            List<Double> quals = new ArrayList<>();
            for (int i = 0; i < agents.length; i++ ) {
                Agent agent = agents[i];
                int amount = 0;
                double skill = 0;
                double[] qualificationsArray = agent.getAbility();
                for (int j=0; j < L.length; j++){
                    if (L[j] > 0 && qualificationsArray[j] > 0.0){ //  checks if role is relevant to task and if agent is qualified
                        skill += (qualificationsArray[j] * L[j]); //  adds qualification of specific role depending on amount
                        amount += L[j];
                    }
                }
               
                skill/=amount;
                skill = Math.round(skill * 100.0) / 100.0;
                quals.add(skill);
                
                
            }
            agentQualifications.add(quals);
           
        }
        return agentQualifications;
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

    public double[][] makeQMatrix(List<Agent> agents, int m, int n){
        double[][] Q = new double[m][n];

        for (int i=0; i < m; i++){
            double[] abilities = agents.get(i).getAbility();
            for (int j = 0; j < n ;j++){
                Q[i][j] = (double) abilities[j];
              
            }
        }
        return Q;
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

    //  uses Q and prepares it for algorithm optimization by using busyness weight vector
    //  and qualification threshold to alter qualifications according to workload
    //  returns Q' with each value rounded to closest hundreth
    public double[][] makeQPrime_nonVector(int m, int n, double[][] Q, double[] Vb, double threshold){
        double [][]Qp = new double [m][n];
			for (int i=0; i<m; i++) {
				for (int j=0; j<n; j++){
				    Qp[i][j]=Q[i][j]*(1-Vb[i]);
                    if (Qp[i][j] < threshold){
                        Qp[i][j] = -(m*m);
                    }
                    Qp[i][j] = Math.round(Qp[i][j] * 100.0) / 100.0;
                }
            }

        return Qp;
    }

    // uses Q and prepares it by using weight sum and busyness vector
    public double[][] makeQPrime_Vector(int m, int n, double[][] Q, double[] Vb, double w, double threshold){
        double [][]Qp = new double [m][n];
			for (int i=0; i<m; i++) {
				for (int j=0; j<n; j++){
				    Qp[i][j] = ((w*Q[i][j]) - (1 - w) * Vb[i]);
                    if (Qp[i][j] < threshold){
                        Qp[i][j] = -(m*m);
                    }
                    Qp[i][j] = Math.round(Qp[i][j] * 100.0) / 100.0;
                }
            }

        return Qp;
    }
	
    //  another essentially identical method but specifiec for large amounts of assignment at a time
    //  takes L[] and finds amount of fitting agents for amount of roles. Roles will be dispersed afterwards
    public int[] stackedOptimizeAssignments(int m, double[] Qrow, int roleAmt, double[] Vb, double weight) {
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
                MPConstraint qualificationConstraint = solver.makeConstraint(0, 0, "QualificationConstraint_" + i + "_");
                qualificationConstraint.setCoefficient(x[i], 1);
            }
        }

        // Constraint: Each task must be assigned to exactly 'roleAmt' agents
        MPConstraint taskConstraint = solver.makeConstraint(roleAmt, roleAmt, "TaskConstraint");
        for (int i = 0; i < m; i++) {
            taskConstraint.setCoefficient(x[i], 1);
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
                }
            }
            return assignments;
        } else {
            return null;
        }
    }

    public double GRAABD_WS(int[] L, List<Agent> agents){

        //important intitalizations
        int m = agents.size();
        int n = agents.get(0).getAbility().length;
        double weight = 0.5;
        //MAKE SURE STUFF IS RIGHT SIZE HERE
        double[] busyWeights = agentBusyness(agents);
        double[][] Q = makeQMatrix(agents, m, n);
       /*
        For Debugging
        String str = "";
        for (int j = 0; j < busyWeights.length; j++){
            str += busyWeights[j];
            str += " ";
        }
        System.out.println(str + "done");
        */
    
        for (double w = weight; w<1.1; w += 0.1){
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

            //showQMatrix(Q, m, n);
            double score = optimizeAssignments(m, n, Q, L, busyWeights, w);
            
                return score;
            
    
        }
        return 0.0;
       
    }

     //  Stacked Group Role Assignment with Agent Busyness' Degree in Weighted Sum Form
    //  to handle the assignment of multiple tickets at a time 
    //  essentially the same as GRAABD_WS but uses a potentialBusynessWeight attribute
    //  to account for the previous tickets assigned in the queue and sQ as the Q matrix
    //  where every qualifications value represents the agents' fitness to the task
    public List<int[]> SGRAABD_WS(int[][] Ls, List<List<Double>> agentFitness, Agent[] agents, int ticketAmt){

        //important intitalizations
       
        int m = ticketAmt;
        int n = agents.length;
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
            int[] L = Ls[i];  
            int roleAmt = 0;
            for (int j = 0; j < L.length; j++){
                roleAmt += L[j];
            }
            busyWeights = stackedAgentBusyness(agents, potentialBusynessWeight);

            for (double w = weight; w<1.1; w += 0.1){           
                int[] assignments = stackedOptimizeAssignments(n,  Q[i], roleAmt, busyWeights, w);
            
                if(assignments != null){
                    //increments potential weights of agents who were optimal for specific task
                    potentialBusynessWeight = incrementPotentialWeight(assignments, busyWeights, i);
                    stackedAssignment.add(assignments);
                    break;
                }

                if(assignments == null && w >= 1){
                    return null;
                }
            }        
        }

    
        return stackedAssignment;
    
    }

	// beginning of simulation of assignment process
	public static void main(String args[]){
		//TO CONTROL SIMULATION 
        int agentAmount = 50;
        int ticketAmount = 200;
        GRA simulation = new GRA();
		//intializations for testing
        /*
		Role itSupport = new Role("IT Support", 1, 3);
        Role software = new Role("Software", 2, 3);
        Role hardware = new Role("Hardware", 3, 3);
        Role network = new Role("Network", 4, 2);
        Role database = new Role("Database", 5, 2);
        Role sysAdm = new Role("SysAdm", 6, 1);
        Role supervisor = new Role("Supervisor", 7, 0);
        Role hardwareSpec = new Role("Hardware Specialist", 8, 1);
		// Define clients
		Client client1 = new Client(1, "John Doe", "john@doe.com", "password", "Management", "CEO", 5);
		Client client2 = new Client(2, "Homer Simpson", "homer@simpson.com", "password", "Nuclear", "Safety Inspector", 1);
		Client client3 = new Client(3, "Bruce Wayne", "bruce@wayne.com", "password", "Finance", "Supervisor", 2);
		Client client4 = new Client(4, "Peter Parker", "peter@parker.com", "password", "Research", "Scientist", 3);
		Client client5 = new Client(5, "Tony Stark", "tony@stark.com", "password", "Engineering", "Engineer", 4);
        */
        Random random = new Random();

        Agent[] agents = new Agent[agentAmount]; 
        for (int i = 0; i < agentAmount; i++) {
            double[] abilities = new double[8];
            for (int j = 0; j < abilities.length; j++) {
                abilities[j] = random.nextDouble(); // Random ability score between 0 and 1
            }
            agents[i] = new Agent(abilities);
        }
       

        int nAgents = agents.length;
        int nRoles = agents[0].getAbility().length;
		
        int[][] LOptions = {
            {0, 0, 0, 1, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 1, 0, 0},
            {0, 1, 0, 0, 0, 0, 1, 0},
            {0, 0, 0, 1, 0, 0, 1, 0},
            {0, 1, 0, 0, 1, 0, 0, 0},
            {0, 0, 1, 0, 0, 0, 1, 0},
            {1, 0, 0, 0, 0, 0, 0, 1},
            {0, 1, 0, 0, 0, 1, 0, 0},
            {0, 0, 1, 0, 1, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 0, 1},
            {1, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 1, 0, 0, 1, 0, 0},
            {0, 0, 0, 1, 1, 0, 0, 0},
            {1, 0, 0, 0, 0, 0, 1, 0},
            {0, 0, 0, 0, 1, 0, 0, 1},
            {1, 0, 0, 1, 0, 0, 0, 0},
            {0, 1, 0, 0, 0, 1, 0, 0},
            {0, 0, 1, 0, 0, 0, 1, 0}
        };

        // create random ticket requirements for tickets
       
        

        //inits for simualtion
        long longestTime, totalTime;
        longestTime = totalTime = 0;
        double[] scorePoints = new double[ticketAmount];
        double[] timePoints = new double[ticketAmount];

        // Begin simulation
        for (int i = 0; i < 100; i++) {
            
            long startTime = System.nanoTime();
            int[][] Ls = new int[ticketAmount][];
            for (int j = 0; j < ticketAmount; j++) {
                int randVal = random.nextInt(LOptions.length);
                Ls[j] = LOptions[randVal];
            }
    
            //creates fitness vector for agents
           List<List<Double>> agentFitness = simulation.getAgentFitness(Ls, agents);

           
            List<int[]> assignment = simulation.SGRAABD_WS(Ls, agentFitness, agents, ticketAmount); 
            double avgScore = 0;
            //returns average group score for each run of simulation
            for (int j = 0; j < assignment.size(); j++){
                

                //  for each ticket(i) gets the agents picked and the L vector
                int[] agentChoice = assignment.get(j);
                
                int[] L = Ls[j];

                //for every task, get list of agents best fit
               List<Agent> agentsForTask = new ArrayList<Agent>();
                for (int k = 0;  k < agentChoice.length; k++){
                   
                    if (agentChoice[k] == 1){
                        //System.out.println("Size: " + k);
                        agentsForTask.add(agents[k]);
                    }
                    
                }

                //  calls GRAABD_WS method with agent list
                double group = simulation.GRAABD_WS(L, agentsForTask);
                avgScore += group;
            }

            //stat collection
            avgScore /= assignment.size(); 
            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            totalTime += duration;
            if (duration > longestTime) {
                longestTime = duration;
            }
            double durationMs = duration / 1_000_000.0;
            timePoints[i]= durationMs;
            scorePoints[i] = avgScore/10;
        }

        long averageTime = totalTime / ticketAmount;
        double longestTimeMs = longestTime / 1_000_000.0;
        double averageTimeMs = averageTime / 1_000_000.0;

        System.out.println("Longest time: " + longestTime + " nanoseconds (" + longestTimeMs + " milliseconds)");
        System.out.println("Average time: " + averageTime + " nanoseconds (" + averageTimeMs + " milliseconds)");
        javax.swing.SwingUtilities.invokeLater(() -> {
            Plot example = new Plot(new Color(255, 0, 0, 150), "Group Score", "Simulation iterations", "Average Group Score", scorePoints, -1, 99, 0.88, 0.92);
            example.setPlotParms(example);
        });
        javax.swing.SwingUtilities.invokeLater(() -> {
            Plot example1 = new Plot(new Color(0, 126, 126, 150), "Time to Complete", "Simulation iterations", "Duration (milliseconds)", timePoints, -1, 99, 0 , 1000);
            example1.setPlotParms(example1);
        });
    }

}
 
    
