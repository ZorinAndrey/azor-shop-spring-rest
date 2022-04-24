package ru.azor.api.core;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Модель профиля пользователя")
public class ProfileDto {
    @Schema(description = "Имя пользователя", required = true, example = "Иванов Иван Иванович")
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public ProfileDto() {
    }

    public ProfileDto(String username) {
        this.username = username;
    }
}
