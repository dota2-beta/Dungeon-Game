package com.abs.gamedataservice.config;

import com.abs.gamedataservice.data.templates.EntityClassTemplate;
import com.abs.gamedataservice.dto.GameDataLoadingDto;
import com.abs.gamedataservice.mapper.EntityTemplateMapper;
import com.abs.gamedataservice.repositories.EntityTemplateRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final EntityTemplateRepository repository;
    private final EntityTemplateMapper templateMapper;

    @Override
    public void run(String... args) {
        log.info("Loading data from yaml files");
        loadDataToDatabase("/templates/player_templates.yml");
        loadDataToDatabase("/templates/monster_templates.yml");
    }
    private void loadDataToDatabase(String filepath) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try(InputStream inputStream = getClass().getResourceAsStream(filepath)){
            if(inputStream == null){
                log.warn("Game data file not found");
                return;
            }
            GameDataLoadingDto data =  mapper.readValue(inputStream, GameDataLoadingDto.class);
            List<EntityClassTemplate> templates = data.getEntityTemplateDtoList().stream()
                    .map(templateMapper::mapToEntityTemplate)
                    .toList();
            repository.saveAll(templates);
            log.info("Successfully loaded {} templates", templates.size());
        } catch (Exception e) {
            log.error("Failed to load templates from yaml file", e);
        }
    }
}