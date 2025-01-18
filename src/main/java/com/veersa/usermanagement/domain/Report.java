package com.veersa.usermanagement.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report extends BaseEntity {

    @Column(nullable = false)
    private String status; // e.g., SUCCESS, FAILED

    @Column(nullable = false)
    private String details; // Additional information about the run

    @Column(nullable = false)
    private LocalDateTime timestamp; // Date and time of the report

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_id", nullable = false)
    private Pipeline pipeline;
}