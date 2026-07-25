package com.polycinema.backend.repository;

import com.polycinema.backend.entity.TheLoai;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TheLoaiRepository extends JpaRepository<TheLoai, Long> {

    Optional<TheLoai> findByTenTheLoai(String tenTheLoai);

    List<TheLoai> findAllByOrderByTenTheLoaiAsc();
}
