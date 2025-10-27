package com.example.backend.service;

import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserWithdrawalService {

    private final UserRepository userRepository;
    private final PostService postService;
    private final CommentService commentService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    @Transactional
    public void executeWithdrawal(Long currentUserId) {

        System.out.println("UserWithdrawalService - executeWithdrawal 진입 (새 트랜잭션)");
        System.out.println("UserWithdrawalService - currentUserId: " + currentUserId);

        try {

            // 3. 사용자 엔티티 조회
            User user = userRepository.findById(currentUserId)
                    .orElseThrow(() -> {
                        // 이 로그를 추가하면 콘솔에서 왜 못 찾았는지 바로 알 수 있습니다.
                        System.err.println("❌ UserWithdrawalService: 사용자 ID " + currentUserId + "를 DB에서 찾을 수 없습니다.");
                        return new RuntimeException("User not found...");
                    });

            System.out.println("UserWithdrawalService - 사용자 엔티티 조회 성공");

            // 4. Soft Delete 처리 (Dirty Checking 대상)
            user.markAsDeleted();

            System.out.println("UserWithdrawalService - markAsDeleted 완료");

            // 5. 연관 데이터 (게시글/댓글) 익명화
            Long dummyUserId = this.getWithdrawnUser().getId();

            System.out.println("UserWithdrawalService - dummyUserId: " + dummyUserId);

            postService.anonymizePosts(currentUserId, dummyUserId);
            commentService.anonymizeComments(currentUserId, dummyUserId);

            System.out.println("UserWithdrawalService - 연관 데이터 익명화 완료");
            // 이 메서드 종료 시 Soft Delete와 익명화 작업이 커밋됨
        } catch (Exception e) {
            // 모든 예외를 잡아서 로그를 찍습니다.
            System.err.println("❌❌❌ 치명적 오류: 회원 탈퇴 트랜잭션 롤백 원인 ❌❌❌: " + e.getMessage());

            // 예외를 다시 던져서 T2 롤백을 확실히 유도하고, 상위 계층(T0)에서 400을 반환하도록 합니다.
            throw new RuntimeException("회원 탈퇴 처리 중 오류 발생", e);
        }
    }

    // ⭐️ UserService에서 가져온 getWithdrawnUser 로직을 여기에 추가합니다. ⭐️
    // 이 메서드는 UserWithdrawalService 내에서 호출되므로 @Transactional(readOnly = true)는 필요 없습니다.
    public User getWithdrawnUser() {
        // UserService.WITHDRAWN_USER_EMAIL 상수에 접근할 수 있다면 사용
        return userRepository.findByEmail(UserService.WITHDRAWN_USER_EMAIL)
                .orElseThrow(() -> new RuntimeException("시스템 더미 탈퇴 회원(ID)를 찾을 수 없습니다."));
    }
}
