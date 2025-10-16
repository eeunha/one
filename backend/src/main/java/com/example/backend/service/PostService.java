package com.example.backend.service;

import com.example.backend.dto.PostResponseDTO;
import com.example.backend.entity.Post;
import com.example.backend.entity.User;
import com.example.backend.repository.PostRepository;
import com.example.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor // final 필드(Repository 등)를 위한 생성자 자동 생성
@Transactional(readOnly = true) // 기본적으로 읽기 전용 트랜잭션 적용 (성능 최적화)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional //CUD에 붙는다. 그래야 JPA의 변경 감지(Dirty Checking) 기능 활성화
    public PostResponseDTO createPost(Long authorId, String title, String content) {

        // 1. 작성자 User 엔티티 조회
        // 실제로는 인증 컨텍스트에서 User를 바로 가져오는 경우가 많지만, 예시를 위해 ID로 조회합니다.
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("작성자(User id: " + authorId + ")를 찾을 수 없습니다."));

        // 2. Post 엔티티 생성 및 저장
        Post newPost = Post.builder()
                .title(title)
                .content(content)
                .author(author)
                .build();

        Post savedPost = postRepository.save(newPost);

        // 3. ⭐️ DTO로 변환하여 반환
        return new PostResponseDTO(savedPost);
    }

    /**
     * 2. 게시글 목록 조회 (Read - List with Pagination)
     * @param pageable 페이징 및 정렬 정보
     * @return PostResponseDTO로 변환된 Page 객체
     */
    public Page<PostResponseDTO> getPosts(Pageable pageable) {

        Page<Post> postPage = postRepository.findAll(pageable);

        // DTO 변환 로직을 서비스에서 처리하여 Controller의 역할을 줄입니다.
        return postPage.map(PostResponseDTO::new);
    }

    /**
     * 3. 게시글 상세 조회 (Read - Single)
     * @param postId 조회할 게시글 ID
     * @return PostResponseDTO 게시글 상세 정보
     */
    @Transactional // 조회수 증가(쓰기)가 있으므로 트랜잭션 설정
    public PostResponseDTO getPostDetail(Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글(Post Id: " + postId + ")를 찾을 수 없습니다."));

        // 1. 조회수 증가 비즈니스 로직 실행
        // Post 엔티티 내부 메서드를 사용하여 객체 지향적으로 상태 변경
        post.incrementViewCount();

        // 2. 트랜잭션 종료 시(커밋) JPA가 변경된 viewCount를 DB에 자동 반영 (Dirty Checking)

        // DTO로 변환하여 반환
        return new PostResponseDTO(post);
    }

    // === 게시글 수정 ===
    @Transactional
    public PostResponseDTO updatePost(Long postId, Long userId, String newTitle, String newContent) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글(Post ID: " + postId + ")를 찾을 수 없습니다."));

        // 1. 권한 검사 (작성자 본인인지 확인)
        if (!post.getAuthor().getId().equals(userId)) {

            // 실제로는 Custom Exception을 사용해야 합니다.
            throw new IllegalArgumentException("게시글을 수정할 권한이 없습니다.");
        }

        // 2. 객체 상태 변경
        post.updatePost(newTitle, newContent);

        // 3. 트랜잭션 종료 시 자동 UPDATE (repository.save() 호출 필요 없음)
        return new PostResponseDTO(post);
    }

    // 게시글 삭제
    @Transactional
    public void deleteSoftPost(Long postId, Long userId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글(Post ID: " + postId + ")을 찾을 수 없습니다."));

        // 1. 권한 검사
        if (!post.getAuthor().getId().equals(userId)) {
            throw new IllegalArgumentException("게시글을 삭제할 권한이 없습니다.");
        }

        // 2. 소프트 삭제 처리 (DB에서 실제 데이터 삭제는 안 함)
        // JpaRepository.delete()는 @SQLDelete를 호출하여 소프트 삭제 수행
        postRepository.delete(post);
    }

    // === ⭐️ Spring Security SpEL에서 호출할 게시글 소유자 확인 메서드 ===
    /**
     * 특정 게시글의 작성자가 현재 인증된 사용자와 일치하는지 확인합니다.
     * @param postId 확인할 게시글 ID
     * @param principalName 현재 인증된 사용자의 ID (String 형태)
     * @return 일치하면 true, 아니면 false
     */
    public boolean isPostOwner(Long postId, String principalName) {

        // 1. Long.valueOf(principalName)으로 사용자 ID(Long) 변환
        Long userId = Long.valueOf(principalName);

        // 2. postId로 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글(Post ID: " + postId + ")을 찾을 수 없습니다."));

        // 3. 게시글 작성자 ID와 사용자 ID를 비교하여 true 또는 false 반환
        return userId.equals(post.getAuthor().getId());
    }
}
