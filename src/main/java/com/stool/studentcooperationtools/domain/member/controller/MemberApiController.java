package com.stool.studentcooperationtools.domain.member.controller;

import com.stool.studentcooperationtools.domain.api.ApiResponse;
import com.stool.studentcooperationtools.domain.member.controller.request.MemberAddRequest;
import com.stool.studentcooperationtools.domain.member.controller.response.MemberFindResponse;
import com.stool.studentcooperationtools.domain.member.controller.response.MemberSearchResponse;
import com.stool.studentcooperationtools.domain.member.service.MemberService;
import com.stool.studentcooperationtools.security.oauth2.dto.SessionMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    @GetMapping("/api/v1/friends")
    public ApiResponse<MemberFindResponse> findFriends(SessionMember member){
        MemberFindResponse response = memberService.findFriends(member);
        return ApiResponse.of(HttpStatus.OK,response);
    }

    @GetMapping("/api/v1/friends/search")
    public ApiResponse<MemberSearchResponse> searchFriends(
            @RequestParam("relation") boolean relation,
            @RequestParam("name") String searchNickName,
            SessionMember member){

        if(!StringUtils.hasText(searchNickName)){
            //검색할 유저의 이름이 ""," ", null 경우
            throw new IllegalArgumentException("검색할 유저의 이름을 입력해 주세요.");
        }

        MemberSearchResponse response = memberService.searchFriend(member, relation, searchNickName);
        return ApiResponse.of(HttpStatus.OK,response);
    }

    @PostMapping("/api/v1/friends")
    public ApiResponse<Boolean> addFriend(SessionMember member, @Valid @RequestBody MemberAddRequest request){
        Boolean result = memberService.addFriend(member, request);
        return ApiResponse.of(HttpStatus.OK,result);
    }
}
