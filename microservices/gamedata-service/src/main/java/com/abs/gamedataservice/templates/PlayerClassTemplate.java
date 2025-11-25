package com.abs.gamedataservice.templates;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "player_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerClassTemplate {
    @Id
    private String templateId;
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "player_template_abilities",
            joinColumns = @JoinColumn(name = "template_id")
    )
    @Column(name = "ability_id")
    private List<String> abilities;
    @Embedded
    private EntityStatsTemplate stats;
}
