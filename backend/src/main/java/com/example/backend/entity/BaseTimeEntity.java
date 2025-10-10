package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
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

    // 엔티티가 업데이트되기 전(수정 전)에 실행
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 소프트 삭제를 위한 메서드
    public void markAsDeleted() {
        this.deletedAt = LocalDateTime.now();
    }
}
