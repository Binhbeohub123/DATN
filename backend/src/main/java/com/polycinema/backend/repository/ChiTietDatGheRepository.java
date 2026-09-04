package com.polycinema.backend.repository;

import com.polycinema.backend.entity.ChiTietDatGhe;
import com.polycinema.backend.entity.ChiTietDatGheId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChiTietDatGheRepository
        extends JpaRepository<ChiTietDatGhe, ChiTietDatGheId> {

    List<ChiTietDatGhe> findByDatVeId(Long datVeId);

    List<ChiTietDatGhe> findByLichChieuIdAndGheNgoiId(Long lichChieuId, Long gheNgoiId);

    List<ChiTietDatGhe> findByLichChieuId(Long lichChieuId);

    /**
     * Ghế đã được "giữ chỗ" (booked) của một suất chiếu — chỉ tính các
     * ChiTietDatGhe thuộc ĐƠN CÒN HIỆU LỰC (trangThai khác 'cancelled').
     * Bao gồm cả 'pending' (đang chờ thanh toán trong 2 phút) lẫn 'confirmed',
     * vì cả hai đều đang giữ ghế hợp lệ trên sơ đồ.
     * Đơn đã huỷ (cancelled) không còn giữ ghế → không tính vào "đã đặt".
     */
    @Query("SELECT ct FROM ChiTietDatGhe ct " +
           "WHERE ct.lichChieu.id = :lichChieuId " +
           "  AND ct.datVe.trangThai <> 'cancelled'")
    List<ChiTietDatGhe> findActiveByLichChieuId(@Param("lichChieuId") Long lichChieuId);
}
