package com.abs.gamedataservice.repositories;

import com.abs.gamedataservice.data.templates.EntityClassTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntityTemplateRepository extends JpaRepository<EntityClassTemplate, String> {
    List<EntityClassTemplate> findAllByType(String type);
}
