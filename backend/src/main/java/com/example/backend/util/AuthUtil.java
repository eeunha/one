package com.example.backend.util;

import java.security.Principal;

/**
 * 인증(Authentication)과 관련된 유틸리티 메서드를 모아둔 클래스입니다.
 */
public class AuthUtil {

    // JWT/OAuth2 인증 구현 후, Principal 객체에서 Long 형태의 사용자 ID를 추출하는 헬퍼 메서드
    public static Long getUserIdFromPrincipal(Principal principal) {
        if (principal == null) {
            // 인증되지 않은 경우 임시 사용자 ID 반환 (실제 환경에서는 인증 예외를 던져야 함)
            return 1L;
        }
        return Long.valueOf(principal.getName());
    }

//    /**
//     * JWT/OAuth2 인증 후, Principal 객체에서 Long 형태의 사용자 ID를 추출하는 헬퍼 메서드.
//     * @param principal 현재 인증된 사용자 정보 객체
//     * @return 추출된 사용자 ID (Long 타입)
//     * @throws IllegalStateException 인증 정보가 유효하지 않을 경우
//     */
//    public static Long getUserIdFromPrincipal(Principal principal) {
//        if (principal == null || principal.getName() == null) {
//            // 실제 환경에서는 인증되지 않은 요청에 대해 401 Unauthorized 예외를 던지는 것이 일반적입니다.
//            // 여기서는 예시를 위해 임시 예외를 사용합니다.
//            throw new IllegalStateException("인증된 사용자 정보(Principal)를 찾을 수 없습니다.");
//        }
//
//        // Principal.getName()은 일반적으로 사용자 ID 문자열을 반환합니다.
//        try {
//            return Long.valueOf(principal.getName());
//        } catch (NumberFormatException e) {
//            throw new IllegalStateException("Principal name이 Long 타입의 사용자 ID가 아닙니다.", e);
//        }
//    }
}
