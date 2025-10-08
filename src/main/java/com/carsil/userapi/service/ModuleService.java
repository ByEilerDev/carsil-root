package com.carsil.userapi.service;

import com.carsil.userapi.model.Module;
import com.carsil.userapi.model.Product;
import com.carsil.userapi.repository.ModuleRepository;
import com.carsil.userapi.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ModuleService {

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private  ProductRepository productRepository;

    public List<Module> getAll() {
        return moduleRepository.findAll();
    }


    @Transactional(readOnly = true)
    public Optional<Module> findById(Long id) {
        return Optional.ofNullable(moduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Module not found: " + id)));
    }

    public List<Module> findByName(String name) {
        return moduleRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional
    public Module create(Module m) {
        if (m.getNumPersons() == null) m.setNumPersons(0);
        return moduleRepository.save(m);
    }

    @Transactional
    public Module updatePeople(Long id, Integer numPersons) {
        Module m = moduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Module not found: " + id));
        if (numPersons == null || numPersons < 0) {
            throw new IllegalArgumentException("numPersons must be >= 0");
        }
        m.setNumPersons(numPersons);
        return moduleRepository.save(m);
    }

    @Transactional
    public Optional<Module> update(Long id, Module input) {
        return moduleRepository.findById(id).map(existing -> {
            existing.setDescription(input.getDescription());
            existing.setName(input.getName());
            existing.setNumPersons(input.getNumPersons());
            return moduleRepository.save(existing);
        });
    }

    @Transactional(readOnly = true)
    public List<Product> getProducts(Long id) {
        return productRepository.findByModuleId(id);
    }

    @Transactional
    public Module assignProduct(Long moduleId, Long productId) {
        Module m = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Module not found: " + moduleId));
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        p.setModule(m);
        productRepository.save(p);
        return m;
    }
}