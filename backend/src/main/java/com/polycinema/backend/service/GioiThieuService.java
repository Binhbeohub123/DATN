package com.polycinema.backend.service;

import com.polycinema.backend.entity.GioiThieu;
import com.polycinema.backend.repository.GioiThieuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GioiThieuService {

    private static final Long SINGLETON_ID = 1L;
    private static final String DEFAULT_TIEU_DE = "HỆ THỐNG CỤM RẠP POLYCINEMA";
    private static final String DEFAULT_NOI_DUNG =
        "PolyCinema — hệ thống rạp chiếu phim hiện đại với chất lượng âm thanh và hình ảnh đỉnh cao.\n\n" +
        "Chúng tôi mang đến trải nghiệm điện ảnh đích thực tại nhiều tỉnh thành trên toàn quốc, " +
        "với các công nghệ chiếu phim tiên tiến như 2D, 3D, IMAX và 4DX.\n\n" +
        "Đặt vé nhanh chóng, tiện lợi — thưởng thức những bộ phim bom tấn ngay hôm nay cùng PolyCinema.";

    private final GioiThieuRepository gioiThieuRepository;

    public GioiThieu get() {
        return gioiThieuRepository.findById(SINGLETON_ID).orElseGet(() -> {
            GioiThieu g = new GioiThieu();
            g.setId(SINGLETON_ID);
            g.setTieuDe(DEFAULT_TIEU_DE);
            g.setNoiDung(DEFAULT_NOI_DUNG);
            g.setHinhAnhUrl(null);
            return g;
        });
    }

    public GioiThieu update(Map<String, String> body) {
        GioiThieu g = gioiThieuRepository.findById(SINGLETON_ID).orElseGet(() -> {
            GioiThieu n = new GioiThieu();
            n.setId(SINGLETON_ID);
            return n;
        });
        if (body.containsKey("tieuDe"))     g.setTieuDe(body.get("tieuDe"));
        if (body.containsKey("noiDung"))    g.setNoiDung(body.get("noiDung"));
        if (body.containsKey("hinhAnhUrl")) g.setHinhAnhUrl(body.get("hinhAnhUrl"));
        return gioiThieuRepository.save(g);
    }
}
