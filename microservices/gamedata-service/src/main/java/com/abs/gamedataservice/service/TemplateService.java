package com.abs.gamedataservice.service;

import com.abs.gamedataservice.repositories.PlayerTemplateRepository;
import com.abs.gamedataservice.templates.PlayerClassTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TemplateService {
    private final PlayerTemplateRepository playerTemplateRepository;

    @Cacheable(value = "player_templates", key = "#id")
    public Optional<PlayerClassTemplate> getPlayerTemplate(String id) {
        return playerTemplateRepository.findById(id);
    }
}
