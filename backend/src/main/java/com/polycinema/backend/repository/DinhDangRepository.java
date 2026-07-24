package com.polycinema.backend.repository;

import com.polycinema.backend.entity.DinhDang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DinhDangRepository extends JpaRepository<DinhDang, Long> {

    Optional<DinhDang> findByTenDinhDang(String tenDinhDang);

    List<DinhDang> findAllByOrderByTenDinhDangAsc();
}
