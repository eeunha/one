package com.example.backend.service;

import com.example.backend.entity.Post;
import com.example.backend.entity.User;
import com.example.backend.repository.PostRepository;
import com.example.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor // final 필드(Repository 등)를 위한 생성자 자동 생성
@Transactional(readOnly = true) // 기본적으로 읽기 전용 트랜잭션 적용 (성능 최적화)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional //CUD에 붙는다. 그래야 JPA의 변경 감지(Dirty Checking) 기능 활성화
    public Post createPost(Long authorId, String title, String content) {

        // 1. 작성자 User 엔티티 조회
        // 실제로는 인증 컨텍스트에서 User를 바로 가져오는 경우가 많지만, 예시를 위해 ID로 조회합니다.
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("작성자(User id: " + authorId + ")를 찾을 수 없습니다."));

        // 2. Post 엔티티 생성 및 저장
        Post newPost = Post.builder()
                .title(title)
                .content(content)
                .author(author)
                .viewCount(0)
                .build();

        return postRepository.save(newPost);
    }

    // === 3. 게시글 목록 조회 (Read - List) ===
    // readOnly = true 이므로 별도 @Transactional 필요 없음
    public List<Post> getAllPosts() {

        // Simple findAll()은 소프트 삭제를 적용하지 않았을 경우 삭제된 게시글도 포함할 수 있습니다.
        // 실제 운영에서는 Pageable을 사용한 페이징 처리와 deletedAt IS NULL 조건을 필터링해야 합니다.
        return postRepository.findAll();
    }

    // === 2. 게시글 상세 조회 (Read - Single) ===
    @Transactional // 조회수 증가(쓰기)가 있으므로 트랜잭션 설정
    public Post getPostDetail(Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글(Post Id: " + postId + ")를 찾을 수 없습니다."));

        // 1. 조회수 증가 비즈니스 로직 실행
        // Post 엔티티 내부 메서드를 사용하여 객체 지향적으로 상태 변경
        post.incrementViewCount();

        // 2. 트랜잭션 종료 시(커밋) JPA가 변경된 viewCount를 DB에 자동 반영 (Dirty Checking)

        return post;
    }

    // === 게시글 수정 ===
    @Transactional
    public Post updatePost(Long postId, Long userId, String newTitle, String newContent) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글(Post ID: " + postId + ")를 찾을 수 없습니다."));

        // 1. 권한 검사 (작성자 본인인지 확인)
        if (!post.getAuthor().getId().equals(userId)) {

            // 실제로는 Custom Exception을 사용해야 합니다.
            throw new IllegalArgumentException("게시글을 수정할 권한이 없습니다.");
        }

        // 2. 객체 상태 변경 (Setter 사용 대신 비즈니스 메서드를 정의하여 사용을 권장하지만, Lombok Setter로 대체)
        post.setTitle(newTitle);
        post.setContent(newContent);

        // 3. 트랜잭션 종료 시 자동 UPDATE (repository.save() 호출 필요 없음)
        return post;
    }

    //d
    @Transactional
    public void deleteSoftPost(Long postId, Long userId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글(Post ID: " + postId + ")을 찾을 수 없습니다."));

        // 1. 권한 검사
        if (!post.getAuthor().getId().equals(userId)) {
            throw new IllegalArgumentException("게시글을 삭제할 권한이 없습니다.");
        }

        // 2. 소프트 삭제 처리 (DB에서 실제 데이터 삭제는 안 함)
        post.markAsDeleted(); // BaseTime 엔티티에 정의된 메서드 호출

        // 3. 연관된 댓글도 함께 소프트 삭제 처리 (선택 사항, 비즈니스 정책에 따라 다름)
        // post.getComments().forEach(Comment::markAsDeleted);
    }
}
