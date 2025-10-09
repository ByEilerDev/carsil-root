package com.carsil.userapi.controller;

import com.carsil.userapi.model.Team;
import com.carsil.userapi.model.Product;
import com.carsil.userapi.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    @Autowired
    private TeamService teamService;

    @GetMapping
    public List<Team> getAll() {
        return teamService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Team> findById(@PathVariable Long id) {
        return teamService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-name")
    public List<Team> findByName(@RequestParam String name) {
        return teamService.findByName(name);
    }

    @PostMapping
    public Team create(@RequestBody Team team) {
        return teamService.create(team);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Team> update(@PathVariable Long id, @RequestBody Team input) {
        return teamService.update(id, input)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/products")
    public List<Product> products(@PathVariable Long id) {
        return teamService.getProducts(id);
    }

    @PostMapping("/{teamsId}/assign/{productId}")
    public Team assignProduct(@PathVariable Long teamsId, @PathVariable Long productId) {
        return teamService.assignProduct(teamsId, productId);
    }
}