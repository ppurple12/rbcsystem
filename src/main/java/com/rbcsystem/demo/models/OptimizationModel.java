package com.rbcsystem.demo.models;

import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

public class OptimizationModel {

    public static void main(String[] args) {
        // Create solver
        MPSolver solver = MPSolver.createSolver("GLOP");

        // Define variables, constraints, and objective
        MPVariable x = solver.makeNumVar(0.0, 1.0, "x");
        MPVariable y = solver.makeNumVar(0.0, 2.0, "y");

        MPConstraint constraint = solver.makeConstraint(0.0, 1.0);
        constraint.setCoefficient(x, 1);
        constraint.setCoefficient(y, 1);

        MPObjective objective = solver.objective();
        objective.setCoefficient(x, 1);
        objective.setCoefficient(y, 1);
        objective.setMaximization();

        // Solve the problem
        MPSolver.ResultStatus resultStatus = solver.solve();

        // Output the result
        if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
            System.out.println("Optimal solution found:");
            System.out.println("x = " + x.solutionValue());
            System.out.println("y = " + y.solutionValue());
        } else {
            System.out.println("The problem does not have an optimal solution.");
        }
    }
 
    
}

