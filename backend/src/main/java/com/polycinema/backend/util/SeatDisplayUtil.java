package com.polycinema.backend.util;

import com.polycinema.backend.entity.GheNgoi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tính nhãn số ghế hiển thị (soGheHienThi): đếm tuần tự các ghế KHÔNG phải
 * 'trống' trong cùng hàng, theo soGhe vật lý tăng dần. Ô 'trống' (lối đi)
 * không được đánh số và giữ soGheHienThi = null.
 *
 * Render-only: soGhe vật lý vẫn là định danh duy nhất cho API/grid-column.
 */
public final class SeatDisplayUtil {

    private static final Logger log = LoggerFactory.getLogger(SeatDisplayUtil.class);

    private SeatDisplayUtil() {}

    private static boolean laLoiDi(GheNgoi g) {
        String loai = g.getLoaiGhe();
        return loai != null && "trống".equals(loai.trim());
    }

    /**
     * Map<gheNgoiId, nhãnHiểnThi> cho toàn bộ ghế MỘT phòng (gộp theo hàng,
     * sort soGhe tăng dần, bỏ qua lối đi khi đếm).
     */
    public static Map<Long, Integer> buildRoomLabels(List<GheNgoi> tatCaGheCuaPhong) {
        Map<String, List<GheNgoi>> theoHang = new HashMap<>();
        for (GheNgoi g : tatCaGheCuaPhong) {
            String hang = g.getHangGhe() != null ? g.getHangGhe().trim() : "";
            theoHang.computeIfAbsent(hang, k -> new ArrayList<>()).add(g);
        }
        Map<Long, Integer> labels = new HashMap<>();
        for (List<GheNgoi> hang : theoHang.values()) {
            hang.sort(Comparator.comparing(GheNgoi::getSoGhe,
                    Comparator.nullsLast(Comparator.naturalOrder())));
            int n = 0;
            for (GheNgoi g : hang) {
                if (laLoiDi(g)) continue;
                labels.put(g.getId(), ++n);
            }
        }
        return labels;
    }

    /** Set trực tiếp field transient cho từng ghế (lối đi giữ null). */
    public static void applyLabels(Iterable<GheNgoi> seats, Map<Long, Integer> labels) {
        for (GheNgoi g : seats) {
            g.setSoGheHienThi(laLoiDi(g) ? null : labels.get(g.getId()));
        }
    }

    /**
     * Log WARN cho mọi ghế thường/vip/cặp đôi còn thiếu nhãn sau khi gán —
     * phát hiện sớm chỗ sót thay vì im lặng fallback về soGhe vật lý.
     * @return số ghế bị sót.
     */
    public static int warnMissing(Iterable<GheNgoi> seats, String endpoint) {
        int missing = 0;
        for (GheNgoi g : seats) {
            if (!laLoiDi(g) && g.getSoGheHienThi() == null) {
                missing++;
                log.warn("[soGheHienThi] SOT tai {}: gheNgoiId={} hang={}{} (loaiGhe={}) chua duoc gan nhan hien thi",
                        endpoint, g.getId(),
                        g.getHangGhe() != null ? g.getHangGhe().trim() : "?",
                        g.getSoGhe(), g.getLoaiGhe());
            }
        }
        return missing;
    }
}
