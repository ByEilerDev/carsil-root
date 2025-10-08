package com.carsil.userapi.repository;

import com.carsil.userapi.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
        SELECT p FROM Product p
        WHERE LOWER(p.op)        LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(p.reference) LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(p.campaign)  LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(p.brand)     LIKE LOWER(CONCAT('%', :q, '%'))
    """)
    List<Product> search(@Param("q") String q);
    boolean existsByOp(String op);
    boolean existsByOpAndIdNot(String op, Long id);

    Page<Product> findByPlantEntryDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    List<Product> findByPlantEntryDateBetween(LocalDate startDate, LocalDate endDate);

    List<Product> findByModuleId(Long moduleId);

    List<Product> findByOp(String op);
}