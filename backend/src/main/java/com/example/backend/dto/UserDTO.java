package com.example.backend.dto;

import com.example.backend.entity.User;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private String name;
    private String email;
    private String provider;

    public static UserDTO from(User user) {
        return UserDTO.builder()
                .email(user.getEmail())
                .name(user.getName())
//                .provider(user.getProvider())
                .build();
    }
}
