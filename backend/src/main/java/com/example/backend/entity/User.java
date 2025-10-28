package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

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
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE) // ⭐️ id를 포함한 모든 필드를 인수로 받는 생성자
@NoArgsConstructor(access = AccessLevel.PROTECTED) // ⭐️ 인수가 없는 기본 생성자
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private String email;

    private String password;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.ROLE_USER;

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
        this.role = Role.ROLE_USER;
    }

    // 소셜 사용자 업데이트 메서드 (토큰 업데이트 등)
    public void updateRefreshToken(String token, LocalDateTime expiry) {
        this.refreshToken = token;
        this.refreshTokenExpiry = expiry;
    }

    // 사용자 생성 메서드
    public static User createSocialUser(String email, String name, String snsProvider, String snsId, Role role) {
        User user = new User(); // @NoArgsConstructor 사용
        user.email = email;
        user.name = name;
        user.snsProvider = snsProvider;
        user.snsId = snsId;
        user.role = role;
        return user;
    }

    public void markAsWithdrawn() {

        String uniqueAnonEmail = "deleted_" + this.id + "@withdrawn.com";

        this.email = uniqueAnonEmail;
        this.name = "탈퇴회원";
        this.password = null;

        // sns 정보 삭제
        this.snsId = null;
        this.snsProvider = null;

        // 권한 변경 (재로그인 방지)
        this.role = Role.ROLE_WITHDRAWN;

        super.markAsDeleted();
    }
}
