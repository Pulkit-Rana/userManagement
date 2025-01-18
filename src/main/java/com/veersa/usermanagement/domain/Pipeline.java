package com.veersa.usermanagement.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "pipelines")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pipeline extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type; // e.g., ADO, Jenkins, TeamCity

    @Column(nullable = false)
    private String triggerUrl; // URL to trigger the pipeline

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @OneToMany(mappedBy = "pipeline", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default    private Set<Report> reports = new HashSet<>();
}