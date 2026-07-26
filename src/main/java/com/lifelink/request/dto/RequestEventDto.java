package com.lifelink.request.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class RequestEventDto {
    private UUID id;
    private String eventType;
    private String message;
    private LocalDateTime createdAt;
}
