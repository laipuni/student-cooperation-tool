package com.stool.studentcooperationtools.domain.member.controller.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberAddRequest {

    @NotNull(message = "추가할 친구가 존재하지 않습니다.")
    private Long friendId;

    @Builder
    private MemberAddRequest(final Long friendId) {
        this.friendId = friendId;
    }
}
