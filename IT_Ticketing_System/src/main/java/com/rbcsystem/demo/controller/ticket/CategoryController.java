package com.rbcsystem.demo.controller.ticket;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.rbcsystem.demo.config.SchemaContext;
import com.rbcsystem.demo.models.TicketForm;
import com.rbcsystem.demo.persistence.entities.gra.Task;
import com.rbcsystem.demo.services.TaskService;

import jakarta.servlet.http.HttpSession;


@Controller
public class CategoryController {
    
    @Autowired
    private TaskService taskService;

    //simple get categories
    @GetMapping("/categories")
    public String getCategories(){
        if (SchemaContext.getSchema() == null) {
          return "redirect:/login-info";
        }
        return "functional/categories";
    }

    //  gets all categories and subcategories to delete if need be
    @GetMapping("/remove-categories")
    public String removeCategories(Model model){
        Map<String, String[]> subcategoriesMap = taskService.getSubcategoriesMap();
        model.addAttribute("ticket", new TicketForm());
        model.addAttribute("categories", subcategoriesMap.keySet().toArray(new String[0]));
        model.addAttribute("subcategoriesMap", subcategoriesMap);
        return "functional/remove-categories";
    }


    //  handles removing the selected subcategory
   @PostMapping("/remove-categories")
    public ResponseEntity<String> processSubcategory(@RequestParam String category, @RequestParam String subcategory, Model model) {

        // gets task from database and removes it
        Task task = taskService.findByCategoryAndSubcategory(category, subcategory);

        //  only deletes if no tickets associate to task
        if (taskService.deleteTask(task)) {
            return ResponseEntity.status(HttpStatus.OK).body("Task removed successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Task cannot be removed because there are associated tickets.");
        }
    }

    //  handles getting all categories and subcategories
    @GetMapping("/add-categories")
    public String addCategories(Model model, HttpSession session){
        Map<String, String[]> subcategoriesMap = taskService.getSubcategoriesMap();
        session.setAttribute("rolesExceedAgents", false);
        model.addAttribute("ticket", new TicketForm());
        model.addAttribute("categories", subcategoriesMap.keySet().toArray(new String[0]));
        return "functional/add-categories";
    }

    //  checks if the user used a new category or an old one and maps accordingly
    @PostMapping("/add-categories")
    public String processAddCategory(
        @RequestParam(required = false) String category, 
        @RequestParam(required = false) String newCategory, 
        @RequestParam String subcategory, HttpSession session, Model model) {

            String finalCategory = category;

            //  if new category was used, check if not in database, add if wasnt
            if (newCategory != null && !newCategory.isEmpty()) {
                boolean categoryExists = taskService.taskExists(newCategory, subcategory);
                if (categoryExists) { //  handles if category exists
                    model.addAttribute("error", "Task already exists.");
                    Map<String, String[]> subcategoriesMap = taskService.getSubcategoriesMap();
                    model.addAttribute("ticket", new TicketForm());
                    model.addAttribute("categories", subcategoriesMap.keySet().toArray(new String[0]));
                    return "functional/add-categories"; 
                }
                else{
                    taskService.addCategory(newCategory, subcategory, 0); 
                    finalCategory = newCategory;
                }
            }
            else {
                // Add subcategory to existing category if it doesnt exist already
                boolean categoryExists = taskService.taskExists(finalCategory, subcategory);
                if (categoryExists) {
                    model.addAttribute("error", "Task already exists.");
                    Map<String, String[]> subcategoriesMap = taskService.getSubcategoriesMap();
                    model.addAttribute("ticket", new TicketForm());
                    model.addAttribute("categories", subcategoriesMap.keySet().toArray(new String[0]));
                    return "functional/add-categories"; 
                }
                else{
                    taskService.addCategory(finalCategory, subcategory, 0); 
                }
            }

            //add taskid to session
            Task task = taskService.findByCategoryAndSubcategory(finalCategory, subcategory);
            session.setAttribute("taskId", task.getId());
            return "redirect:/change-roles";
        }

}
