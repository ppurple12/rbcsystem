package com.rbcsystem.demo.services;

import com.rbcsystem.demo.config.SchemaContext;
import com.rbcsystem.demo.persistence.entities.TicketEntity;
import com.rbcsystem.demo.persistence.entities.gra.Role;
import com.rbcsystem.demo.persistence.entities.gra.RoleMap;
import com.rbcsystem.demo.persistence.entities.gra.Task;
import com.rbcsystem.demo.persistence.repositories.gra.RoleMapRepository;
import com.rbcsystem.demo.persistence.repositories.gra.RoleRepository;
import com.rbcsystem.demo.persistence.repositories.gra.TaskRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.logging.Logger;

@Service
public class RoleService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private RoleMapRepository roleMapRepository;

    @Autowired
    private RoleRepository roleRepository;
    
    private static final Logger logger = Logger.getLogger(RoleService.class.getName());

    //  gets all roles
    @Transactional
    public List<Role> getAll() {
        logCurrentSchemaContext("getAll");
        return roleRepository.findAll();
    }

    //  gets by Id
    @Transactional
    public Role getById(Long id) {
        logCurrentSchemaContext("getById");
        return roleRepository.findById(id).orElse(null);
    }

    //  gets all roles by ticket
    @Transactional
    public List<Role> getByTicket(TicketEntity ticket) {
        logCurrentSchemaContext("getByTicket");
        return roleRepository.findByTicketMapsTicket(ticket);
    }

    //  gets task id using category and subcategory
    public Long findTaskIdByCategoryAndSubcategory(String category, String subcategory) {
        logCurrentSchemaContext("findTaskIdByCategoryAndSubcategory");
        Task task = taskRepository.findByCategoryAndSubcategory(category, subcategory);
        return task.getId();
    }

    //  find amount of roles
    @Transactional
    public int totalRoles() {
        logCurrentSchemaContext("totalRoles");
        return roleRepository.countTotalRoles();
    }

    // returns all roles by name
    public List<String> getAllRoleNames() {
        logCurrentSchemaContext("getAllRoleNames");
        return roleRepository.findAllRoleNames();
    }

    // returns a map of the roles and their amounts of agents required
    @Transactional
    public Map<Long, Integer> getRolesAndAmountsForTask(long taskId) {
        logCurrentSchemaContext("getRolesAndAmountsForTask");
        List<Object[]> results = roleMapRepository.findRolesAndAmountsByTaskId(taskId);
        Map<Long, Integer> rolesAndAmounts = new HashMap<>();
        for (Object[] result : results) {
            Long role = (Long) result[0];
            int amount = (int) result[1];
            rolesAndAmounts.put(role, amount);
        }
        return rolesAndAmounts;
    }

    // updates roles for a task
    @Transactional
    public void updateRoles(Long taskId, List<Integer> roleAmounts) {
        logCurrentSchemaContext("updateRoles");

        // Delete existing role map entries for the task_id
        roleMapRepository.deleteByTaskId(taskId);
        
        Task task = taskRepository.findById(taskId);
        
        // Insert new role map entries for each role amount > 0
        for (int i = 0; i < roleAmounts.size(); i++) {
            int amount = roleAmounts.get(i);
            if (amount > 0) {
                Optional<Role> roleOp = roleRepository.findById((long) (i + 1)); // role id starts at 1
                Role role = roleOp.get();
                RoleMap roleMap = new RoleMap();
                roleMap.setTask(task);
                roleMap.setRole(role);
                roleMap.setAmount(amount);
                roleMapRepository.save(roleMap);
            }
        }
    }

    // Logs the current schema context for debugging
    private void logCurrentSchemaContext(String methodName) {
        String currentSchema = SchemaContext.getSchema();
        logger.info("Current Schema in " + methodName + ": " + currentSchema);
    }
}