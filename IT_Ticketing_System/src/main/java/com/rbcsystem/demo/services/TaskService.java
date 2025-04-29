package com.rbcsystem.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rbcsystem.demo.config.SchemaContext;
import com.rbcsystem.demo.persistence.entities.gra.Task;
import com.rbcsystem.demo.persistence.repositories.TicketRepository;
import com.rbcsystem.demo.persistence.repositories.gra.RoleMapRepository;
import com.rbcsystem.demo.persistence.repositories.gra.TaskRepository;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.logging.Logger;

@Service
@Lazy(false)
public class TaskService {
    private static final Logger logger = Logger.getLogger(TaskService.class.getName());

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TicketRepository ticketRepository;
    
    @Autowired
    private RoleMapRepository roleMapRepository;


    private Map<String, String[]> subcategoriesMap = new HashMap<>();

    @Transactional
    public List<Task> getAll() {
        // Direct query to check if data is being fetched correctly
        return taskRepository.findAll();
    }

    @PostConstruct
    @Transactional
    public void initializeSubcategories() {
        String schema = SchemaContext.getSchema();
        logger.info("Current Schema: " + schema);
        SchemaContext.setSchema(schema);
        List<Task> tasks = taskRepository.findAll();
        logger.info("Number of tasks retrieved: " + tasks.size());

        subcategoriesMap = tasks.stream()
                .collect(Collectors.groupingBy(
                        Task::getCategory,
                        Collectors.mapping(Task::getSubcategory, Collectors.toList())
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().toArray(new String[0])
                ));

        subcategoriesMap.forEach((key, value) -> 
            logger.info("Category: " + key + " Subcategories: " + String.join(", ", value))
        );
    }

    @Transactional(readOnly = true)
    public Map<String, String[]> getSubcategoriesMap() {
        return subcategoriesMap;
    }

    //updates estimated time for task when prompted
    @Transactional
    public void updateEstimatedTime(Long taskId, double estimatedTime) {
        // Retrieve the task from the database
        Task task = taskRepository.findById(taskId);
        // Set the estimated time for the task
        task.setEstimatedTime(estimatedTime);
        
        // Save the updated task back to the database
        taskRepository.save(task);
    }

    //  adds a category to database
    @Transactional
     public void addCategory(String category, String subCategory, int estimatedTime) {
        
        // Create a new category entity and save it to the database
        Task newTask = new Task();
        newTask.setCategory(category);
        newTask.setSubcategory(subCategory);
        newTask.setEstimatedTime(estimatedTime);
        taskRepository.save(newTask);
        initializeSubcategories();
    
    }


    //  deletes a task from database
    @Transactional
    public boolean deleteTask(Task task) {
        // Check if there are tickets associated with the task
        if (ticketRepository.existsByTaskId(task.getId())) {
            return false; // Tickets associated with this task exist
        }

        // No tickets associated, proceed with deletion
        roleMapRepository.deleteByTaskId(task.getId());
        taskRepository.delete(task);

        // Update the subcategories map after deletion
        initializeSubcategories();
        return true;
    }

    //  checks if task exists
    @Transactional
    public boolean taskExists(String category, String subcategory) {
        // Logic to check if the category exists in the database'
       
        return taskRepository.existsByCategoryAndSubcategory(category, subcategory);
    }
    //  gets a task by id
    
    @Transactional(readOnly = true)
    public Task getById(long id){
        return taskRepository.findById(id);
    }

    //gets a task by category and subcategory

    @Transactional(readOnly = true)
    public Task findByCategoryAndSubcategory(String cat, String sub){
        return taskRepository.findByCategoryAndSubcategory(cat, sub);
    }
}