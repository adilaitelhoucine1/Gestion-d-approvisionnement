package com.tricol.gestionstock.dto.auth;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponseDTO {
    private String message;
    private Integer statusCode;
}
