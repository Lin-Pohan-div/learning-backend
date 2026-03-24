package com.learning.api.dto.ChatRoom;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkPreviewDto {
    private String title;
    private String description;
    private String imageUrl;
    private String url;
}
