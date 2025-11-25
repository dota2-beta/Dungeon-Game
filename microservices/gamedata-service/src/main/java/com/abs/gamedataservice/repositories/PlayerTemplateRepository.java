package com.abs.gamedataservice.repositories;

import com.abs.gamedataservice.templates.PlayerClassTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerTemplateRepository extends JpaRepository<PlayerClassTemplate, String> {
}
