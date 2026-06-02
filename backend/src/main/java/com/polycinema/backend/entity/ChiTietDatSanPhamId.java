package com.polycinema.backend.entity;

import java.io.Serializable;
import java.util.Objects;

public class ChiTietDatSanPhamId implements Serializable {

    private Long datVe;
    private Long sanPham;

    public ChiTietDatSanPhamId() {}

    public ChiTietDatSanPhamId(Long datVe, Long sanPham) {
        this.datVe = datVe;
        this.sanPham = sanPham;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChiTietDatSanPhamId that)) return false;
        return Objects.equals(datVe, that.datVe) && Objects.equals(sanPham, that.sanPham);
    }

    @Override
    public int hashCode() {
        return Objects.hash(datVe, sanPham);
    }
}
