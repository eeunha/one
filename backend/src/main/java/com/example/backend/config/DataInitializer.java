package com.example.backend.config;

import com.example.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserService userService;

    // ⭐️ 애플리케이션 시작 후 모든 빈 생성 및 트랜잭션 환경 준비 완료 시 실행됨
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // UserService의 트랜잭션 메서드를 호출
        userService.initializeDummyUser();
    }
}
