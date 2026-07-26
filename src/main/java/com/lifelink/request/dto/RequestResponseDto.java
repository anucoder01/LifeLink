package com.lifelink.request.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class RequestResponseDto {
    private UUID id;
    private UUID donorId;
    private String donorName;
    private String donorPhone;
    private String status;
    private LocalDateTime respondedAt;
}
