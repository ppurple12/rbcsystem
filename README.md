# Stacked GRAABD-WS: Optimal IT Ticket Assignment with Role-Based Collaboration

**Author**: Evan Wells  

## Abstract

Efficient assignment of IT tickets is essential in today's fast-paced digital environments. This project introduces the **Stacked GRAABD-WS** algorithm—an enhancement of the Group Role Assignment with Agents’ Busyness Degree in Weighted Sum form. Built on the **E-CARGO** object-oriented framework, GRAABD-WS models the relationship between IT tickets and technicians as **roles** and **agents**, respectively. 

The algorithm evaluates each technician’s suitability based on their current workload and technical capabilities, computing an **Agent Fitness** score to guide role assignments. By ranking candidates and balancing work distribution, Stacked GRAABD-WS ensures optimal task delegation. Using **Google OR-Tools**, the model successfully assigned 40 tickets to 11 agents in just **3.71 seconds**.

---

## Features

- Implements the **Stacked GRAABD-WS** algorithm using Python and Google OR-Tools
- Models technician-task relationships using E-CARGO principles
- Calculates Agent Fitness based on real-time workload and skill metrics
- Optimizes ticket assignment to improve resolution speed and team balance
- Demonstrates real-world feasibility through empirical testing

---

## Project Contributions

1. **Practical Solution**: Offers a scalable and efficient system for IT ticket distribution
2. **Algorithmic Modeling**: Applies formal E-CARGO-based definitions to task assignment
3. **Tooling**: Utilizes Google OR-Tools to solve a combinatorially complex problem
4. **Innovation**: First known application of GRAABD-WS in IT ticket management

---
