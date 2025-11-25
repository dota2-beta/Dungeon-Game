package com.abs.gamedataservice.config;

import com.abs.gamedataservice.dto.GameDataLoadingDto;
import com.abs.gamedataservice.mapper.PlayerTemplateMapper;
import com.abs.gamedataservice.repositories.PlayerTemplateRepository;
import com.abs.gamedataservice.templates.EntityStatsTemplate;
import com.abs.gamedataservice.templates.PlayerClassTemplate;
import com.dungeoncrawler.contracts.grpc.gamedata.Gamedata;
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

    private final PlayerTemplateRepository repository;
    private final PlayerTemplateMapper templateMapper;

    @Override
    public void run(String... args) {
        log.info("Loading data from yaml file");

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try(InputStream inputStream = getClass().getResourceAsStream("/templates/player_templates.yml")){
            if(inputStream == null){
                log.warn("Game data file not found");
                return;
            }
            GameDataLoadingDto data =  mapper.readValue(inputStream, GameDataLoadingDto.class);
            List<PlayerClassTemplate> templates = data.getPlayerTemplateDtoList().stream()
                    .map(templateMapper::mapToPlayerTemplate)
                    .toList();
            repository.saveAll(templates);
            log.info("Successfully loaded {} templates", templates.size());
        } catch (Exception e) {
            log.error("Failed to load templates from yaml file", e);
        }
    }
}