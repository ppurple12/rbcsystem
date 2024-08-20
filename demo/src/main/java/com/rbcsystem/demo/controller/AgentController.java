package com.rbcsystem.demo.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.rbcsystem.demo.persistence.entities.enums.AccessLevel;
import com.rbcsystem.demo.persistence.entities.enums.TicketCondition;
import com.rbcsystem.demo.config.SchemaContext;
import com.rbcsystem.demo.persistence.entities.AgentEntity;
import com.rbcsystem.demo.persistence.entities.ClientEntity;
import com.rbcsystem.demo.persistence.entities.TicketEntity;
import com.rbcsystem.demo.persistence.entities.TicketMap;
import com.rbcsystem.demo.persistence.entities.enums.TicketStatus;
import com.rbcsystem.demo.persistence.entities.UpdateLog;
import com.rbcsystem.demo.persistence.entities.gra.Role;
import com.rbcsystem.demo.services.AgentService;
import com.rbcsystem.demo.services.ClientService;
import com.rbcsystem.demo.services.QualificationService;
import com.rbcsystem.demo.services.RoleService;
import com.rbcsystem.demo.services.StatusTicketService;
import com.rbcsystem.demo.services.TicketMapService;
import com.rbcsystem.demo.services.TicketService;
import com.rbcsystem.demo.services.UpdateLogService;

import jakarta.servlet.http.HttpSession;


@Controller
public class AgentController {
    
    @Autowired
    private AgentService agentService;

    @Autowired
    private QualificationService qualificationService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketMapService ticketMapService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UpdateLogService updateLogService;

    @Autowired
    private StatusTicketService statusTicketService;


    //maps specific agent id to a page
    @GetMapping("/agents/{id}")
    public String getAgentDetail(@PathVariable Long id, Model model) {

        AgentEntity agent = agentService.getWithId(id);
        if (agent != null) {

            // gets and models all necessary info
            List<TicketEntity> tickets = ticketService.getWithAgent(agent);
            double[] abilities = qualificationService.getAbilitiesByAgentId(agent.getId());
            tickets = ticketService.sortTickets(tickets);
            String num = ticketService.formatTime(agent.getTotalEstimatedTime());
            List<String> roles = roleService.getAllRoleNames();
            model.addAttribute("roles", roles);
            model.addAttribute("num", num);
            model.addAttribute("agent", agent);
            model.addAttribute("abilities", abilities);
            model.addAttribute("activeTickets", tickets);
            return "agents/agent-view";
        }
        return "redirect:/";
    }

    // returns a page of all agents
    @GetMapping("/all-agents")
    public String getAllAgents(Model model) {
        List<AgentEntity> agents = agentService.getAll();
        model.addAttribute("agents", agents);
        return "agents/all-agents";
    }
    
    // returns all tickets for a specific agent
    @GetMapping("/agents/{id}/all-tickets-per-agent")
    public String getAllTicketsForAgent(@PathVariable Long id, Model model) {
        AgentEntity agent = agentService.getWithId(id);
        if (agent != null) {
            // gets tickets
            List<TicketEntity> tickets = agentService.getTicketsByAgentId(agent.getId());
            tickets = ticketService.sortTickets(tickets);
            
            // gets ticket id and specific ticket map for agent-ticket combo
            Map<Long, TicketMap> ticketMaps = new HashMap<>();
            for (TicketEntity ticket : tickets) {
                TicketMap ticketMap = ticketMapService.getTicketMap(ticket, agent);
                ticketMaps.put(ticket.getId(), ticketMap);
            }
            model.addAttribute("agent", agent);
            model.addAttribute("activeTickets", tickets);
            model.addAttribute("ticketMaps", ticketMaps);
            return "tickets/all-tickets-per-agent";
        }
        return "redirect:/";
    }
    
    // uses ticket to get all agents and roles involved
    @GetMapping("agent-change")
    public String changeAgent(Model model, HttpSession session) {
        List<AgentEntity> allAgents = new ArrayList<>( agentService.getAll());
        List<Role> allRoles = roleService.getAll();
        TicketEntity ticket = (TicketEntity) session.getAttribute("ticket");

        if (ticket != null) {
            List<AgentEntity> agents = agentService.getWithTicketMapsTicket(ticket);
            List<Role> roles = roleService.getByTicket(ticket);

            allAgents.removeIf(agents::contains); // to allowing adding of agents, removes active agents from allAgent list

            model.addAttribute("agents", agents);
            model.addAttribute("roles", roles);
            model.addAttribute("allAgents", allAgents);
            model.addAttribute("allRoles", allRoles);
        }

        return "agents/agent-change";
    }


    //removes agent from ticket
    @PostMapping("/remove")
    @Transactional
    public String removeAgent(@RequestBody Map<String, Long> payload, HttpSession session) {
        Long agentId = payload.get("agentId");
        TicketEntity ticket = (TicketEntity) session.getAttribute("ticket");

        if (ticket != null) {
            AgentEntity agent =  agentService.getWithId(agentId);
            if (agent != null) {
                TicketMap ticketMap = ticketMapService.getTicketMap(ticket, agent); //removes agent from ticket
                ticketMapService.removeAgentAndTicket(ticketMap);
                System.out.println(ticket.getTaskId());

                //  Create and save update to UpdateLog
                UpdateLog updateLog = new UpdateLog();
                updateLog.setTicket(ticket);
                updateLog.setUpdateDescription(agent.getUser().getName() + " has been reassigned elsewhere");
                updateLog.setUpdateTime(new Timestamp(System.currentTimeMillis())); 
                ClientEntity system = new ClientEntity();
                    if (!clientService.existsByName("System")){
                       
                        system.setName("System");
                    }
                    else{
                        system = clientService.getWithName("System");
                    }
                updateLog.setUpdatedBy(system);
                updateLogService.saveLog(updateLog);
                ticketService.updateTime(ticket);

                //  checks if ticket is overdue, remove from iverdue if it is
                if (statusTicketService.existsByTicketAndCondition(ticket, TicketCondition.OVERDUE)){
                    statusTicketService.deleteByTicketAndCondition(ticket, TicketCondition.OVERDUE);
                }
                
                //updates status of ticket map to "NEW" for notification mechanics
                List<TicketMap> ticketMaps = ticketMapService.getWithId(ticket);
                for (TicketMap ticketmap : ticketMaps){
                    ticketMapService.updateStatus(ticketmap.getId(), TicketStatus.NEW);
                }
            }
        } 

        return "redirect:/agent-change";
    }


    //adds an agent to ticket
    @PostMapping("/add")
    public String add(@RequestParam("agentId") Long agentId, @RequestParam("roleId") Long roleId, HttpSession session) {

        // adds new agent, links it to role and maps it to ticket
        AgentEntity agent =  agentService.getWithId(agentId);
        Role role = roleService.getById(roleId);

        if (agent != null && role != null) {
            TicketEntity ticket = (TicketEntity) session.getAttribute("ticket");

            if (ticket != null) {
                // create and save new ticketmap instance
                TicketMap ticketMap = new TicketMap();
                ticketMap.setAgent(agent);
                ticketMap.setTicket(ticket);
                ticketMap.setRole(role);
                ticketMapService.saveMap(ticketMap);

                // create and save new updatelog instance
                UpdateLog updateLog = new UpdateLog();
                updateLog.setTicket(ticket);
                updateLog.setUpdateDescription("Ticket has been assigned to " + agent.getUser().getName() + " with role " + role.getRoleName());
                updateLog.setUpdateTime(new Timestamp(System.currentTimeMillis())); 
                ClientEntity system = new ClientEntity();
                    if (!clientService.existsByName("System")){
                       
                        system.setName("System");
                    }
                    else{
                        system = clientService.getWithName("System");
                    }
                updateLog.setUpdatedBy(system);
                updateLogService.saveLog(updateLog);
                ticketService.updateTime(ticket);

                //  checks if ticket is overdue, remove from overdue if it is
                if (statusTicketService.existsByTicketAndCondition(ticket, TicketCondition.OVERDUE)){
                    statusTicketService.deleteByTicketAndCondition(ticket, TicketCondition.OVERDUE);
                }

                 //  checks if ticket is overdue, remove from iverdue if it is
                List<TicketMap> ticketMaps = ticketMapService.getWithId(ticket);
                for (TicketMap ticketmap : ticketMaps){
                    ticketMapService.updateStatus(ticketmap.getId(), TicketStatus.NEW);
                }
            } else {
                return "redirect:/error";
            }

            return "redirect:/agent-change";
        } else {
            return "redirect:/error";
        }
    }

    //  removes agent
   @PostMapping("/remove-agent")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeAgent(@RequestParam("agentId") Long agentId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean isDeleted = agentService.deleteAgentById(agentId);
            if (isDeleted) {
                response.put("success", true);
                response.put("message", "Agent removed successfully!");
            } else {
                response.put("success", false);
                response.put("message", "Failed to remove agent.");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error removing agent: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
        
    // creates new agent and sets qualifications to roles
    @PostMapping("/confirm-make-agent")
    public String confirmMakeAgent(@RequestParam("clientId") Long clientId,
                                @RequestParam("qualifications") List<Integer> qualification) {
        //  process the form submission here
        // 'clientId' contains the ID of the client
        // 'qualifications' contains a list of qualification ratings  decided by manager
        ClientEntity user = (ClientEntity) clientService.getWithId(clientId);
        AgentEntity agent = new AgentEntity(0,  user);
        agentService.saveAgent(agent);
        agentService.updateAccessLevel(clientId, AccessLevel.IT); //sets their access level to IT
        qualificationService.saveQualification(agent, qualification);

        return "redirect:/enterprise-view";
    
    }

    // adds update to tickets as a communication feature
    @PostMapping("/add-update")
    public String addUpdate(@RequestParam("ticketId") TicketEntity ticket,
                            @RequestParam("updateMessage") String updateMessage,
                            @RequestParam("agentId") Long clientId, HttpSession session) {
        ClientEntity client = (ClientEntity) session.getAttribute("loggedInClient");
        if (client == null || SchemaContext.getSchema() == null ) {
            return "functional/login-info";
        }
        //  creates and saves a new instance of update log for the message
        UpdateLog updateLog = new UpdateLog();
        updateLog.setTicket(ticket);
        updateLog.setUpdateDescription(updateMessage);
        updateLog.setUpdatedBy(client);
        updateLog.setUpdateTime(new Timestamp(System.currentTimeMillis())); // sets update to current time
        ticketService.updateTime(ticket); // changes tickets last update time
        updateLogService.saveLog(updateLog);

          //  checks if ticket is overdue, remove from overdue if it is
          if (statusTicketService.existsByTicketAndCondition(ticket, TicketCondition.OVERDUE)){
            statusTicketService.deleteByTicketAndCondition(ticket, TicketCondition.OVERDUE);
        }

         //  checks if ticket is overdue, remove from iverdue if it is
        List<TicketMap> ticketMaps = ticketMapService.getWithId(ticket);
        for (TicketMap ticketMap : ticketMaps){
            ticketMapService.updateStatus(ticketMap.getId(), TicketStatus.NEW);
        }
        return "redirect:/tickets/"+ ticket.getId();
    }
    
    //  for remapping a ticket
    //  takes the current L to use GRAABD_WS with
    @GetMapping("/reassign-agent/{id}")
    public String getTicketDetail(@PathVariable Long id, Model model, HttpSession session){
        TicketEntity ticket = ticketService.getWithId(id);
        List<TicketMap> ticketMaps = ticketMapService.getWithId(ticket);
        Integer[] L = new Integer[roleService.totalRoles()];
        Arrays.fill(L, 0);
        // loops through ticket mapping to find L
        for (TicketMap ticketMap : ticketMaps){
            int roleId = (int) (ticketMap.getRole().getId() - 1);
            L[roleId] += 1;
        }
        // removes current agents when reassigning
        ticketMapService.removeByTicket(ticket);

        session.setAttribute("L", L);
        session.setAttribute("submittedTicket", ticket);
        return "redirect:/assignment-visualization";
    }

}