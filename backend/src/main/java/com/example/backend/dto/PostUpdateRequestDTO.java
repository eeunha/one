package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateRequestDTO {

    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    @Size(max = 100, message = "새 제목은 100자를 초과할 수 없습니다.")
    private String newTitle;

    @NotBlank(message = "내용은 필수 입력 항목입니다.")
    private String newContent;
}
