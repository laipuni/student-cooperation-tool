package com.stool.studentcooperationtools.domain.member.controller.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FriendRemoveRequest {

    @NotNull
    private String email;

    @Builder
    private FriendRemoveRequest(final String email) {
        this.email = email;
    }

}
