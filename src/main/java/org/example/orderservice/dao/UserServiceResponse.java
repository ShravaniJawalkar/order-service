package org.example.orderservice.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserServiceResponse {
    private Long userId;
    private String userName;
    private String responseCode;
}
