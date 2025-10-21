package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 엔티티가 영속화되기 전(저장 전)에 실행
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // 소프트 삭제를 위한 메서드
    public void markAsDeleted() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * updated_at 필드를 현재 시각으로 갱신하는 메서드
     * (비즈니스 로직에 의해 수동으로 호출됨)
     */
    public void updateModifiedAt() {
        this.updatedAt = LocalDateTime.now();
    }
}
