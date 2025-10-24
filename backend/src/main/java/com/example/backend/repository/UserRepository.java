package com.example.backend.repository;

import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일을 기준으로 User를 조회합니다.
     * DDL에서 email은 UNIQUE로 설정되어 있으므로 Optional<User>를 반환합니다.
     * @param email 조회할 사용자 이메일
     * @return User 객체 (Optional)
     */
    Optional<User> findByEmail(String email);

    /**
     * 인증을 위해 refreshToken을 기준으로 User를 조회합니다.
     * @param refreshToken 조회할 사용자 이메일
     * @return User 객체 (Optional)
     */
    Optional<User> findByRefreshToken(String refreshToken);

    /**
     * 소셜 로그인 (SNS) 정보를 기반으로 User를 조회합니다.
     * DDL에서 (sns_provider, sns_id) 조합은 고유(UNIQUE)로 설정되어 있습니다.
     * @param snsProvider 소셜 서비스 제공자 (예: "GOOGLE")
     * @param snsId 소셜 서비스의 고유 식별자
     * @return User 객체 (Optional)
     */
    Optional<User> findBySnsProviderAndSnsId(String snsProvider, String snsId);
}
