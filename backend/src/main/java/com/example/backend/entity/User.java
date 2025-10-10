package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users",
        uniqueConstraints = {
                // DDL의 고유 제약 조건 반영
                @UniqueConstraint(name = "uk_sns", columnNames = {"sns_provider", "sns_id"})
        })
@Getter
@Setter
@NoArgsConstructor
public class User extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private String email;

    private String password;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    private String role = "ROLE_USER";

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "refresh_token_expiry")
    private LocalDateTime refreshTokenExpiry;

    @Column(name = "sns_provider", length = 50)
    private String snsProvider;

    @Column(name = "sns_id")
    private String snsId;

    // Post의 author 필드에 의해 매핑됨
    @OneToMany(mappedBy = "author")
    private List<Post> posts = new ArrayList<>();

    // Comment의 author 필드에 의해 매핑됨
    @OneToMany(mappedBy = "author")
    private List<Comment> comments = new ArrayList<>();

    // 비밀번호 기반 사용자 생성자 (필수 필드)
    public User(String email, String password, String name) {
        this.email = email;
        this.password = password; // 실제로는 암호화된 비밀번호가 전달되어야 함
        this.name = name;
    }

    // 소셜 사용자 업데이트 메서드 (토큰 업데이트 등)
    public void updateRefreshToken(String token, LocalDateTime expiry) {
        this.refreshToken = token;
        this.refreshTokenExpiry = expiry;
    }
}
