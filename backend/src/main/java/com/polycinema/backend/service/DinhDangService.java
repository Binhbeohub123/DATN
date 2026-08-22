package com.polycinema.backend.service;

import com.polycinema.backend.entity.DinhDang;
import com.polycinema.backend.repository.DinhDangRepository;
import com.polycinema.backend.repository.PhimRepository;
import com.polycinema.backend.repository.PhongChieuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DinhDangService {

    private final DinhDangRepository dinhDangRepository;
    private final PhimRepository phimRepository;
    private final PhongChieuRepository phongChieuRepository;

    public List<DinhDang> getAll() {
        return dinhDangRepository.findAllByOrderByTenDinhDangAsc();
    }

    public DinhDang create(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("tenDinhDang không được để trống");
        }
        if (dinhDangRepository.findByTenDinhDang(name.trim()).isPresent()) {
            throw new IllegalArgumentException("Định dạng '" + name.trim() + "' đã tồn tại");
        }
        DinhDang dd = new DinhDang();
        dd.setTenDinhDang(name.trim());
        return dinhDangRepository.save(dd);
    }

    public DinhDang update(Long id, String name) {
        DinhDang dd = dinhDangRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy định dạng id=" + id));
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("tenDinhDang không được để trống");
        }
        dd.setTenDinhDang(name.trim());
        return dinhDangRepository.save(dd);
    }

    public void delete(Long id) {
        if (!dinhDangRepository.existsById(id)) {
            throw new IllegalArgumentException("Không tìm thấy định dạng id=" + id);
        }

        long phimCount = phimRepository.countByDinhDangId(id);
        long phongCount = phongChieuRepository.countByDinhDangId(id);

        if (phimCount > 0 || phongCount > 0) {
            String detail = buildReferenceDetail(phimCount, phongCount);
            throw new IllegalArgumentException(
                    "Không thể xóa: định dạng này đang được sử dụng bởi " + detail);
        }

        dinhDangRepository.deleteById(id);
    }

    private String buildReferenceDetail(long phimCount, long phongCount) {
        if (phimCount > 0 && phongCount > 0) {
            return phimCount + " phim và " + phongCount + " phòng chiếu";
        }
        if (phimCount > 0) {
            return phimCount + " phim";
        }
        return phongCount + " phòng chiếu";
    }

    public java.util.Optional<DinhDang> findById(Long id) {
        return dinhDangRepository.findById(id);
    }
}
