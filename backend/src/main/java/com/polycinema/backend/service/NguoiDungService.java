package com.polycinema.backend.service;

import com.polycinema.backend.entity.NguoiDung;
import com.polycinema.backend.repository.NguoiDungRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NguoiDungService {

    private final NguoiDungRepository nguoiDungRepository;

    public Page<NguoiDung> searchAdmin(String q, Pageable pageable) {
        return nguoiDungRepository.searchAdmin(q, pageable);
    }

    public java.util.Optional<NguoiDung> findById(Long id) {
        return nguoiDungRepository.findById(id);
    }

    public java.util.Optional<NguoiDung> findByEmail(String email) {
        return nguoiDungRepository.findByEmail(email);
    }

    public NguoiDung save(NguoiDung user) {
        return nguoiDungRepository.save(user);
    }

    public long count() {
        return nguoiDungRepository.count();
    }

    public long countByVaiTro(String vaiTro) {
        return nguoiDungRepository.countByVaiTro(vaiTro);
    }

    public long countByVaiTroAndTrangThai(String vaiTro, Boolean trangThai) {
        return nguoiDungRepository.countByVaiTroAndTrangThai(vaiTro, trangThai);
    }
}
