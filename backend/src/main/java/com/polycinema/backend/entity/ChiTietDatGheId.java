package com.polycinema.backend.entity;

import java.io.Serializable;
import java.util.Objects;

public class ChiTietDatGheId implements Serializable {

    private Long datVe;
    private Long gheNgoi;

    public ChiTietDatGheId() {}

    public ChiTietDatGheId(Long datVe, Long gheNgoi) {
        this.datVe = datVe;
        this.gheNgoi = gheNgoi;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChiTietDatGheId that)) return false;
        return Objects.equals(datVe, that.datVe) && Objects.equals(gheNgoi, that.gheNgoi);
    }

    @Override
    public int hashCode() {
        return Objects.hash(datVe, gheNgoi);
    }
}
