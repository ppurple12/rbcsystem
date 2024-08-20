package com.rbcsystem.demo.persistence.repositories.gra;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.rbcsystem.demo.persistence.entities.gra.Task;

import java.util.List;


@Repository
@Lazy(false)
public interface TaskRepository extends JpaRepository<Task, Integer> {

    //returns task by searching with catgory and subcategory
    Task findByCategoryAndSubcategory(String category, String subcategory);

    
    // check if task exists by searching with catgory and subcategory
    boolean existsByCategoryAndSubcategory(String category, String subcategory);

    Task findById(Long id);

    List<Task> findAll();

    void deleteById(Long id);
}