package com.example.backend.service;

import com.example.backend.dto.PostResponseDTO;
import com.example.backend.entity.Post;
import com.example.backend.entity.User;
import com.example.backend.repository.PostRepository;
import com.example.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor // final 필드(Repository 등)를 위한 생성자 자동 생성
@Transactional(readOnly = true) // 기본적으로 읽기 전용 트랜잭션 적용 (성능 최적화)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // ⭐️ 수정된 Self-Injection: 필드 주입(@Autowired)으로 변경하여
    // @RequiredArgsConstructor가 생성하는 생성자의 인자에서 제외시켜 순환 참조를 회피합니다.
    @Autowired // 필드 주입으로 변경
    @Lazy // 순환 참조 방지 및 프록시 주입을 위함
    private PostService postServiceProxy; // ⭐️ final 키워드 제거

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
    public PostResponseDTO getPostDetail(Long postId) {

        // 1. Fetch Join으로 Post와 User를 함께 로드합니다. (LazyException 원천 차단)
        Post post = postRepository.findPostWithAuthorById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

        // 2. 조회수 증가 (REQUIRES_NEW 트랜잭션 실행)
        Integer latestViewCount = postServiceProxy.incrementViewCount(postId);

        // 3. Post 엔티티에 최신 값 강제 설정
        // T2로부터 받은 확정된 값을 T1의 post 객체에 설정합니다.
        post.setViewCount(latestViewCount);

        System.out.println("viewCount: " + post.getViewCount());

        return new PostResponseDTO(post); // post는 이제 정확한 viewCount를 가집니다.
    }

    /**
     * ⭐️ 추가: 조회수 증가를 위한 별도의 쓰기 트랜잭션 메서드
     * 이 메서드는 Native Query를 사용하여 updated_at 변경 없이 view_count만 업데이트합니다.
     * @param postId 증가시킬 게시글 ID
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Integer incrementViewCount(Long postId) {

        // 1. DB에 조회수 업데이트 (DML 쿼리)
        postRepository.incrementViewCount(postId);

        // 2. ⭐️ 같은 REQUIRES_NEW 트랜잭션 내에서 Native Query를 실행하여
        //    방금 업데이트된 최신 값(N+1)을 DB로부터 가져옵니다. ⭐️
        Integer latestViewCount = postRepository.findViewCountByIdNative(postId);

        // 이 메서드가 끝나면 DB COMMIT이 발생하며, latestViewCount는 T1으로 전달됩니다.
        return latestViewCount;
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
