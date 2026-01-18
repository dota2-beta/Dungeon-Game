package com.abs.gamedataservice.service;

import com.abs.gamedataservice.data.templates.EntityClassTemplate;
import com.abs.gamedataservice.repositories.EntityTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TemplateService {
    private final EntityTemplateRepository entityTemplateRepository;

    @Cacheable(value = "entity_templates", key = "#id")
    public Optional<EntityClassTemplate> getEntityTemplate(String id) {
        return entityTemplateRepository.findById(id);
    }

    @Cacheable(value = "player_class_list", key = "'all'")
    public List<EntityClassTemplate> getAllPlayerClasses() {
        return entityTemplateRepository.findAllByType("player");
    }
}
