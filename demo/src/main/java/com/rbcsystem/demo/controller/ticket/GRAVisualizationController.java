package com.rbcsystem.demo.controller.ticket;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbcsystem.demo.persistence.entities.AgentEntity;
import com.rbcsystem.demo.persistence.entities.ClientEntity;
import com.rbcsystem.demo.persistence.entities.StatusTicket;
import com.rbcsystem.demo.persistence.entities.TicketEntity;
import com.rbcsystem.demo.persistence.entities.TicketMap;
import com.rbcsystem.demo.persistence.entities.enums.TicketCondition;
import com.rbcsystem.demo.persistence.entities.enums.TicketStatus;
import com.rbcsystem.demo.persistence.entities.UpdateLog;
import com.rbcsystem.demo.persistence.entities.gra.Role;
import com.rbcsystem.demo.services.AgentService;
import com.rbcsystem.demo.services.ClientService;
import com.rbcsystem.demo.services.GRAService;
import com.rbcsystem.demo.services.QualificationService;
import com.rbcsystem.demo.services.RoleService;
import com.rbcsystem.demo.services.StatusTicketService;
import com.rbcsystem.demo.services.TicketMapService;
import com.rbcsystem.demo.services.TicketService;
import com.rbcsystem.demo.services.UpdateLogService;

import jakarta.servlet.http.HttpSession;

@Controller
public class GRAVisualizationController {

    @Autowired
    private GRAService graService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private TicketMapService ticketMapService;

    @Autowired
    private AgentService agentService;

   @Autowired
   private QualificationService qualificationService;

    @Autowired
    private UpdateLogService updateLogService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private StatusTicketService statusTicketService;

    @Autowired
    private ClientService clientService;

    @GetMapping("/assignment-visualization")
    public String assignmentVisualization(HttpSession session, Model model) {
        TicketEntity submittedTicket = (TicketEntity) session.getAttribute("submittedTicket");
        Integer[] L = (Integer[]) session.getAttribute("L");
        if (submittedTicket == null || L == null) {
            return "redirect:/"; 
        }
    
        List<AgentEntity> agents = agentService.getAll(); 
        List<Role> roles = roleService.getAll();
    
        List<List<Double>> agentQualifications = new ArrayList<>();
        for (AgentEntity agent : agents) {
            double[] qualificationsArray = qualificationService.getAbilitiesByAgentId(agent.getId().intValue());
            List<Double> qualifications = Arrays.stream(qualificationsArray)
                                                .boxed()
                                                .collect(Collectors.toList());
            agentQualifications.add(qualifications);
        }
    
        // Initialize the assignment matrix
        int[][] assignmentMatrix = new int[agents.size()][roles.size()];
        
        // Convert L to int array
        int[] LArray = Arrays.stream(L).mapToInt(Integer::intValue).toArray();
        
        // Compute the optimal statistics
        Map<Integer, Integer> optimalStats = graService.GRAABD_WS(LArray, agents);
    
        // Fill the matrix based on optimalStats
        for (Map.Entry<Integer, Integer> entry : optimalStats.entrySet()) {
            int agentIndex = entry.getKey();
            int roleIndex = entry.getValue();
            assignmentMatrix[agentIndex][roleIndex] = 1; // Set specific entries to 1
        }
        // Print the matrix for debugging purposes
        System.out.println("Assignment Matrix:");
        for (int[] row : assignmentMatrix) {
            System.out.println(Arrays.toString(row));
        }
    
        model.addAttribute("ticket", submittedTicket);
        model.addAttribute("agentQualifications", agentQualifications);
        model.addAttribute("agents", agents);
        model.addAttribute("roles", roles);
        model.addAttribute("assignmentMatrix", assignmentMatrix);
        
        return "tickets/assignment-visualization";
    }

    // receives managers group and maps it to data base
    @PostMapping("/submit-highlighted-values")
    @Transactional
    public String receiveHighlightedValues(@RequestBody Map<Integer, Integer> highlightedValues, HttpSession session) {
        try {
            TicketEntity ticket = (TicketEntity) session.getAttribute("submittedTicket");
            if (ticket == null) {
                return "redirect:/";
            }
    
            // Save ticket to database
            ticketService.saveTicket(ticket);
            session.setAttribute("submittedTicket", ticket);
    
            // Find all agents
            List<AgentEntity> agents = agentService.getAll(); 
    
            // Process each highlighted value
            for (Map.Entry<Integer, Integer> entry : highlightedValues.entrySet()) {
                int agentId = entry.getKey();
                int roleId = entry.getValue();
                
                // Find agent by ID
                AgentEntity agent = agents.stream()
                                          .filter(a -> a.getId() == agentId)
                                          .findFirst()
                                          .orElseThrow(() -> {
                                              return new RuntimeException("Agent not found with id: " + agentId);
                                          });
    
                // Find role by ID (assuming roleId + 1 is the correct logic)
                Role role = roleService.getById((long)roleId);
    
                // Create and save TicketMap instance
                TicketMap ticketMap = new TicketMap();
                ticketMap.setAgent(agent);
                ticketMap.setRole(role);
                ticketMap.setTicketStatus(TicketStatus.NEW);
                ticketMap.setTicket(ticket);
                ticketMapService.saveMap(ticketMap);

                //  Create and save UpdateLog Instance
                UpdateLog updateLog = new UpdateLog();
                updateLog.setTicket(ticket);
                updateLog.setUpdateDescription("Ticket has been assigned to " + agent.getUser().getName() + " with role " + role.getRoleName());
                ClientEntity system = new ClientEntity();
                if (!clientService.existsByName("System")){
                   
                    system.setName("System");
                }
                else{
                    system = clientService.getWithName("System");
                }
                updateLog.setUpdatedBy(system);
                updateLog.setUpdateTime(new Timestamp(System.currentTimeMillis())); 
                updateLogService.saveLog(updateLog);
                if (statusTicketService.existsByTicketAndCondition(ticket, TicketCondition.OVERDUE)){
                    statusTicketService.deleteByTicketAndCondition(ticket, TicketCondition.OVERDUE);
                }
                if (statusTicketService.existsByTicketAndCondition(ticket, TicketCondition.UNASSIGNED)){
                    statusTicketService.deleteByTicketAndCondition(ticket, TicketCondition.UNASSIGNED);
                }
            }
    
            return "redirect:/dashboard";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/";
        }
    }


    // Displays all agents and tickets in a table, highlighting optimal agent assignment
    @GetMapping("/agent-mapping")
    public String ticketVisualization(HttpSession session, Model model) {
        try {
            // Retrieve tickets and agents
            List<StatusTicket> ticketQueue = statusTicketService.findAllByCondition(TicketCondition.NEW);
            List<TicketEntity> tickets = new ArrayList<>();
            List<AgentEntity> agents = agentService.getAll();

            for (StatusTicket queued : ticketQueue) {
                tickets.add(queued.getTicket());
            }

            // Create matrix of roles and determine amount per ticket
            List<int[]> roleLists = new ArrayList<>();
            List<Integer> roleAmt = new ArrayList<>();

            for (TicketEntity ticket : tickets) {
                Long taskId = ticket.getTaskId();
                int[] roleList = findRoles(taskId);
                int amt = 0;
                roleLists.add(roleList);
                for (int role : roleList) {
                    amt += role;
                }

                if (amt > agents.size()) {
                    session.setAttribute("taskId", taskId);
                    session.setAttribute("rolesExceedAgents", true);
                    return "redirect:/change-roles";
                }
                roleAmt.add(amt);
            }

            if (ticketQueue == null || roleLists == null) {
                return "redirect:/dashboard";
            }

            //get agents' abilities
            List<List<Double>> agentAbilities = new ArrayList<>();
            for (AgentEntity agent : agents) {
                agentAbilities.add(new ArrayList<>());
            }

            int x = 0;
            for (AgentEntity agent : agents) {
                double[] abils = qualificationService.getAbilitiesByAgentId(agent.getId());
                for (double abil : abils) {
                    agentAbilities.get(x).add(abil);
                }
                x++;
            }
            
            int[] rowIndex = new int[tickets.size()];
            // Populate table
            List<List<Double>> agentQualifications = new ArrayList<>();
            int[][] agentIndex = new int[agents.size()][8]; //8 is amount of abilities
            //loops through every role in every active ticket to see fitness to do specific ticket
            for (int[] L : roleLists){ 
                List<Double> quals = new ArrayList<>();
                int y = 0;
                for (AgentEntity agent : agents) {
                    int amount = 0;
                    double skill = 0;
                    double[] qualificationsArray = qualificationService.getAbilitiesByAgentId(agent.getId().intValue());
                    for (int i=0; i < L.length; i++){
                        if (qualificationsArray[i] > 0){
                            agentIndex[y][i] = 1;
                        }
                        if (L[i] > 0 ){ //  checks if role is relevant to task and if agent is qualified
                            skill += (qualificationsArray[i] * L[i]); //  adds qualification of specific role depending on amount
                            amount += L[i];
                        }
                    }
                    y++;
                   
                    skill/=amount;
                    skill = Math.round(skill * 100.0) / 100.0;
                    quals.add(skill);
                    
                    
                }
                agentQualifications.add(quals);
               
            }
            int[][] assignmentMatrix = new int[tickets.size()][agents.size()];
            List<int[]> optimalStats = graService.SGRAABD_WS(roleLists, agentQualifications, agents, tickets, agentIndex);
            List<TicketEntity> usableTickets = new ArrayList<>();
            for (TicketEntity ticket : tickets) {
                usableTickets.add(ticket); // Deep copy
            }
            int i = 0;
            for (StatusTicket queued : ticketQueue) {
                int[] assignment = optimalStats.get(i);
                if (assignment[0] == -1 ){
                  
                    rowIndex[i] = 1;
                    usableTickets.remove(queued.getTicket());
                    statusTicketService.deleteByTicketAndCondition(queued.getTicket(), TicketCondition.NEW);

                        //  Create and save UpdateLog Instance
                    UpdateLog updateLog = new UpdateLog();
                    updateLog.setTicket(queued.getTicket());
                    updateLog.setUpdateDescription("Ticket could not be assigned as there are not enough agents qualified for the necessary roles. Awaiting reassignment.");
                    ClientEntity system = new ClientEntity();
                    if (!clientService.existsByName("System")){
                       
                        system.setName("System");
                    }
                    else{
                        system = clientService.getWithName("System");
                    }
                    updateLog.setUpdatedBy(system);
                    updateLog.setUpdateTime(new Timestamp(System.currentTimeMillis())); 
                    updateLogService.saveLog(updateLog);
                    
                    //if ticket cant be assigned, add to unassigned list
                    if (!statusTicketService.existsByTicketAndCondition(queued.getTicket(), TicketCondition.UNASSIGNED)){
                        StatusTicket unassigned = new StatusTicket();
                        unassigned.setTicket(queued.getTicket());
                        unassigned.setCondition(TicketCondition.UNASSIGNED);
                        statusTicketService.save(unassigned);
                        
                       
                    }
                }
                for (int j = 0; j < assignment.length; j++) {
                    if (assignment[j] == 1) {
                        assignmentMatrix[i][j] = 1;
                    }
                }
                i++;
            }

            // Add attributes to model
            session.setAttribute("useableTickets", usableTickets);
            model.addAttribute("tickets", tickets);
            model.addAttribute("agentQualifications", agentQualifications);
            model.addAttribute("agents", agents);
            model.addAttribute("assignmentMatrix", assignmentMatrix);
            model.addAttribute("roleAmt", roleAmt);
            model.addAttribute("rowIndex", rowIndex);


            return "agents/agent-mapping";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/dashboard"; // or another appropriate error page
        }
    }

    @PostMapping("/submit-agent-mapping")
    @Transactional
    public String receiveAgentMapping(@RequestBody List<int[]> agentChoices, HttpSession session) {
        try {
            // Retrieve tickets from session
            List<TicketEntity> tickets = (List<TicketEntity>) session.getAttribute("useableTickets");
            
            if (tickets == null) {
                System.out.println("Tickets list is null. Redirecting to home.");
                return "redirect:/";
            }
            
            // Prepare list of role requirements
            List<int[]> roleLists = new ArrayList<>();
            for (TicketEntity ticket : tickets) {
                Long taskId = ticket.getTaskId();
                int[] roles = findRoles(taskId);
                if (roles == null) {
                    throw new RuntimeException("Roles not found for task ID: " + taskId);
                }
                roleLists.add(roles);
            }
    
            // Debug output
            System.out.println("Number of agent choices: " + agentChoices.size());
            System.out.println("Number of role lists: " + roleLists.size());
    
            // Check if sizes of agentChoices and roleLists match
            if (agentChoices.size() != roleLists.size()) {
                throw new IllegalArgumentException("Mismatch detected: agentChoices size = " 
                    + agentChoices.size() + ", roleLists size = " + roleLists.size());
            }
    
            // Process agent choices
            for (int i = 0; i < agentChoices.size(); i++) {
                int[] agentChoice = agentChoices.get(i);
                int[] roles = roleLists.get(i);
                
                
                

                List<AgentEntity> agentsForTask = new ArrayList<>();
                for (int agentId : agentChoice) {
                    AgentEntity agent = agentService.getWithId((long) agentId);
                    if (agent == null) {
                        throw new RuntimeException("Agent with ID " + agentId + " not found");
                    }
                    agentsForTask.add(agent);
                }
    
                // Perform role assignment
                Map<Integer, Integer> group = graService.GRAABD_WS(roles, agentsForTask);
                if (group == null) {
                    throw new RuntimeException("Group map returned null");
                }
                
                List<Role> allRoles = roleService.getAll();
                if (allRoles == null) {
                    throw new RuntimeException("Role list is null");
                }
    
                for (Map.Entry<Integer, Integer> entry : group.entrySet()) {
                    int agentIndex = entry.getKey();
                    int roleId = entry.getValue();
    
                    if (agentIndex >= agentsForTask.size()) {
                        throw new IndexOutOfBoundsException("Agent index " + agentIndex + " out of bounds");
                    }
                    if (roleId >= allRoles.size()) {
                        throw new IndexOutOfBoundsException("Role ID " + roleId + " out of bounds");
                    }
    
                    AgentEntity agent = agentsForTask.get(agentIndex);
                    Role role = allRoles.get(roleId);
    
                    // Create and save TicketMap instance
                    TicketMap ticketMap = new TicketMap();
                    ticketMap.setAgent(agent);
                    ticketMap.setRole(role);
                    ticketMap.setTicket(tickets.get(i));
                    ticketMap.setTicketStatus(TicketStatus.NEW);
                    ticketMapService.saveMap(ticketMap);
                
                    // Delete ticket from ticketQueue
                    statusTicketService.deleteByTicketAndCondition(tickets.get(i), TicketCondition.NEW);
    
                    // Create and save UpdateLog instance
                    UpdateLog updateLog = new UpdateLog();
                    updateLog.setTicket(tickets.get(i));
                    updateLog.setUpdateDescription("Ticket has been assigned to " + agent.getUser().getName() + " with role " + role.getRoleName());
                    ClientEntity system = new ClientEntity();
                    if (!clientService.existsByName("System")){
                       
                        system.setName("System");
                    }
                    else{
                        system = clientService.getWithName("System");
                    }
                    updateLog.setUpdatedBy(system);
                    updateLog.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                    updateLogService.saveLog(updateLog);
                }
            }
        
        
            return "redirect:/dashboard";
        } catch (IllegalArgumentException e) {
            System.err.println("IllegalArgumentException occurred: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/";
        } catch (RuntimeException e) {
            System.err.println("RuntimeException occurred: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/";
        } catch (Exception e) {
            System.err.println("Exception occurred: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/";
        }
    }
    //  uses info from ticket form to create the L array of required roles
    public int[] findRoles(Long taskId){
        //  gets necessary info
        Map<Long, Integer> roleEngine = roleService.getRolesAndAmountsForTask(taskId);
        int arraySize = roleService.totalRoles();
        // populate L array
        int[] L = new int[arraySize];
        for (int i = 0; i < arraySize; i++) {
            long roleId = (long) (i+1);
            Integer amount = roleEngine.get((roleId)); // get the amount for role ID i+1
            if (amount != null) {
                L[i] = amount; // set value in L at index i to the corresponding amount
            } else {
                L[i] = 0; // set value to 0 if no corresponding amount found
            }
        }
        return L;

    }

    public void setAgentsToTask(int[] L, TicketEntity ticket){

        //calls GRA algorithm to optimize
        List<AgentEntity> agents = agentService.getAll(); 
        Map<Integer, Integer> assignments = graService.GRAABD_WS(L, agents);
         //HANDLE NULL EXCEPTION HERE

         //iterate through Map to add agents and according roles to RoleMap
         //all values increment +1 as ids in database begin at 1 and arrays begin at 0
         for (int agentId : assignments.keySet()) {
            int roleId = assignments.get(agentId);

            AgentEntity agent = agents.get(agentId);

            Role role = roleService.getById((long)roleId);

            TicketMap ticketMap = new TicketMap();
            ticketMap.setAgent(agent);
            ticketMap.setRole(role);
            ticketMap.setTicket(ticket);

            ticketMapService.saveMap(ticketMap);
        }
    }
}
