package com.polycinema.backend.service;

import com.polycinema.backend.dto.TheLoaiDTO;
import com.polycinema.backend.entity.TheLoai;
import com.polycinema.backend.repository.TheLoaiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TheLoaiService {

    private final TheLoaiRepository theLoaiRepository;

    public List<TheLoaiDTO> getAll() {
        return theLoaiRepository.findAllByOrderByTenTheLoaiAsc()
                .stream()
                .map(TheLoaiDTO::from)
                .collect(Collectors.toList());
    }

    public TheLoaiDTO create(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("tenTheLoai không được để trống");
        }
        if (theLoaiRepository.findByTenTheLoai(name.trim()).isPresent()) {
            throw new IllegalArgumentException("Thể loại đã tồn tại");
        }
        TheLoai saved = theLoaiRepository.save(new TheLoai(null, name.trim()));
        return TheLoaiDTO.from(saved);
    }

    public TheLoaiDTO update(Long id, String name) {
        TheLoai tl = theLoaiRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thể loại id=" + id));
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("tenTheLoai không được để trống");
        }
        tl.setTenTheLoai(name.trim());
        return TheLoaiDTO.from(theLoaiRepository.save(tl));
    }

    public void delete(Long id) {
        if (!theLoaiRepository.existsById(id)) {
            throw new IllegalArgumentException("Không tìm thấy thể loại id=" + id);
        }
        theLoaiRepository.deleteById(id);
    }

    public java.util.Optional<TheLoai> findById(Long id) {
        return theLoaiRepository.findById(id);
    }
}
