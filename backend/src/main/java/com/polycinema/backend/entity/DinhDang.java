package com.polycinema.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Movie / room format lookup (2D, 3D, IMAX, …).
 * Populated by migration_phase1.sql — 7 seed rows.
 */
@Entity
@Table(name = "DinhDang")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DinhDang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "TenDinhDang", nullable = false, unique = true, length = 50)
    private String tenDinhDang;
}
