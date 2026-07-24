package com.polycinema.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TheLoai")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TheLoai {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "TenTheLoai", unique = true, nullable = false, length = 100)
    private String tenTheLoai;
}
