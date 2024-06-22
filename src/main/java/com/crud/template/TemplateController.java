package com.crud.template;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/entities")
public class TemplateController {
    TemplateCrudRepository templateCrudRepository;

    private TemplateController(TemplateCrudRepository templateCrudRepository) {
        this.templateCrudRepository = templateCrudRepository;
    }

    @GetMapping("/{requestedId}")
    private ResponseEntity<EntityTemplate> getById(@PathVariable Long requestedId, Principal principal) {
        EntityTemplate entity = findEntityTemplase(requestedId, principal);
        if (entity != null) {
            return ResponseEntity.ok(entity);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private EntityTemplate findEntityTemplase(Long requestedId, Principal principal) {
        EntityTemplate entity = templateCrudRepository.findByIdAndOwner(requestedId, principal.getName());
        return entity;
    }

    @PostMapping
    private ResponseEntity<Void> createEntity(@RequestBody EntityTemplate newEntityRequest, UriComponentsBuilder ucb, Principal principal) {
        EntityTemplate entitydWithOwner = new EntityTemplate(null, newEntityRequest.name(), principal.getName());
        EntityTemplate savedEntity = templateCrudRepository.save(entitydWithOwner);
        URI locationOfNewEntity = ucb
                .path("entities/{id}")
                .buildAndExpand(savedEntity.id())
                .toUri();
        return ResponseEntity.created(locationOfNewEntity).build();
    }

    @GetMapping
    private ResponseEntity<List<EntityTemplate>> findAll(Pageable pageable, Principal principal) {
        Page<EntityTemplate> page = templateCrudRepository.findByOwner(principal.getName(),
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "name"))
                ));
        return ResponseEntity.ok(page.getContent());
    }

    @PutMapping("/{requestedId}")
    private ResponseEntity<Void> putEntity(@PathVariable Long requestedId, @RequestBody EntityTemplate entityUpdate, Principal principal) {
        EntityTemplate entity = findEntityTemplase(requestedId, principal);
        if (entity != null) {
            EntityTemplate updatedEntity = new EntityTemplate(requestedId, entityUpdate.name(), principal.getName());
            templateCrudRepository.save(updatedEntity);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    private ResponseEntity<Void> deleteEntity(@PathVariable Long id, Principal principal) {

        if (templateCrudRepository.existsByIdAndOwner(id, principal.getName())) {
            templateCrudRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
