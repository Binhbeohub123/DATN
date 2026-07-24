package com.polycinema.backend.dto;

import com.polycinema.backend.entity.TheLoai;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TheLoaiDTO {

    private Long id;
    private String tenTheLoai;

    public static TheLoaiDTO from(TheLoai entity) {
        if (entity == null) return null;
        return new TheLoaiDTO(entity.getId(), entity.getTenTheLoai());
    }
}
