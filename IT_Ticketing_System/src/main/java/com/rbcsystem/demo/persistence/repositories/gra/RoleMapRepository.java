package com.rbcsystem.demo.persistence.repositories.gra;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.rbcsystem.demo.persistence.entities.gra.RoleMap;


@Repository
public interface RoleMapRepository extends JpaRepository<RoleMap, Integer> {

    //finds roles and amount of agents required per role for tasks
    @Query("SELECT r.role.id, r.amount FROM RoleMap r WHERE r.task.id = :taskId")
    List<Object[]> findRolesAndAmountsByTaskId(@Param("taskId") long taskId);

    void deleteByTaskId(Long taskId);

}
