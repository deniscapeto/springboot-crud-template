package com.crud.template;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;

interface TemplateCrudRepository extends CrudRepository<EntityTemplate, Long> {
    EntityTemplate findByIdAndOwner(Long id, String owner);
    Page<EntityTemplate> findByOwner(String name, PageRequest pageRequest);
    boolean existsByIdAndOwner(Long id, String name);
}
