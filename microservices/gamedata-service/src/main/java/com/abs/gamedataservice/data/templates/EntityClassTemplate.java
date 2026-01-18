package com.abs.gamedataservice.data.templates;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "entity_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityClassTemplate {
    @Id
    private String templateId;
    private String name;
    private String type;
    @Column(columnDefinition = "TEXT")
    private String description;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "entity_template_abilities",
            joinColumns = @JoinColumn(name = "template_id")
    )
    @Column(name = "ability_id")
    private List<String> abilities;
    @Embedded
    private EntityStatsTemplate stats;
}
