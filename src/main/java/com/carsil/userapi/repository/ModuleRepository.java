package com.carsil.userapi.repository;

import com.carsil.userapi.model.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    List<Module> findByNameContainingIgnoreCase(String name);
}