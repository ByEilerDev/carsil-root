package com.carsil.userapi.service;

import com.carsil.userapi.model.Team;
import com.carsil.userapi.model.Product;
import com.carsil.userapi.repository.TeamRepository;
import com.carsil.userapi.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private  ProductRepository productRepository;

    public List<Team> getAll() {
        return teamRepository.findAll();
    }


    @Transactional(readOnly = true)
    public Optional<Team> findById(Long id) {
        return Optional.ofNullable(teamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + id)));
    }

    public List<Team> findByName(String name) {
        return teamRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional
    public Team create(Team m) {
        if (m.getNumPersons() == null) m.setNumPersons(0);
        return teamRepository.save(m);
    }

    @Transactional
    public Team updatePeople(Long id, Integer numPersons) {
        Team m = teamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + id));
        if (numPersons == null || numPersons < 0) {
            throw new IllegalArgumentException("numPersons must be >= 0");
        }
        m.setNumPersons(numPersons);
        return teamRepository.save(m);
    }

    @Transactional
    public Optional<Team> update(Long id, Team input) {
        return teamRepository.findById(id).map(existing -> {
            existing.setDescription(input.getDescription());
            existing.setName(input.getName());
            existing.setNumPersons(input.getNumPersons());
            return teamRepository.save(existing);
        });
    }

    @Transactional(readOnly = true)
    public List<Product> getProducts(Long id) {
        return productRepository.findByTeamId(id);
    }

    @Transactional
    public Team assignProduct(Long teamId, Long productId) {
        Team m = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        p.setTeam(m);
        productRepository.save(p);
        return m;
    }
}