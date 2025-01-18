package com.stool.studentcooperationtools.docs.member;

import com.stool.studentcooperationtools.docs.RestDocsSupport;
import com.stool.studentcooperationtools.domain.member.controller.MemberApiController;
import com.stool.studentcooperationtools.domain.member.controller.request.MemberAddRequest;
import com.stool.studentcooperationtools.domain.member.controller.request.MemberSearchMemberDto;
import com.stool.studentcooperationtools.domain.member.controller.response.MemberFindMemberDto;
import com.stool.studentcooperationtools.domain.member.controller.response.MemberFindResponse;
import com.stool.studentcooperationtools.domain.member.controller.response.MemberSearchResponse;
import com.stool.studentcooperationtools.domain.member.service.MemberService;
import com.stool.studentcooperationtools.security.oauth2.dto.SessionMember;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import java.util.List;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class MemberApiControllerDocsTest extends RestDocsSupport {

        private final MemberService memberService = mock(MemberService.class);

        @Override
        protected Object initController() {
            return new MemberApiController(memberService);
        }


        @Test
        void findFriend() throws Exception {
        //given
        List<MemberFindMemberDto> memberDtoList = List.of(
                MemberFindMemberDto.builder()
                        .id(1L)
                        .profile("profilePath")
                        .nickname("nickname")
                        .build()
        );
        MemberFindResponse response = MemberFindResponse.builder()
                .members(memberDtoList)
                .num(memberDtoList.size())
                .build();
        Mockito.when(memberService.findFriends(Mockito.any(SessionMember.class)))
                .thenReturn(response);

        //when
        //then
        mockMvc.perform(
                    MockMvcRequestBuilders.get("/api/v2/friends")
                            .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("friend-find",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("code").type(NUMBER)
                                        .description("상태 코드"),
                                fieldWithPath("status").type(STRING)
                                        .description("응답 상태"),
                                fieldWithPath("data").type(OBJECT)
                                        .description("응답 데이터"),
                                fieldWithPath("data.num").type(NUMBER)
                                        .description("데이터 개수"),
                                fieldWithPath("data.members[]").type(ARRAY)
                                        .description("유저 정보 리스트"),
                                fieldWithPath("data.members[].id").type(NUMBER)
                                        .description("유저 정보 리스트"),
                                fieldWithPath("data.members[].nickname").type(STRING)
                                        .description("유저 닉네임"),
                                fieldWithPath("data.members[].profile").type(STRING)
                                        .description("유저 프로필")
                        )
                    )
                );
        }

        @Test
        void searchNotFriend() throws Exception {
                //given
                List<MemberSearchMemberDto> memberDtoList = List.of(
                        MemberSearchMemberDto.builder()
                                .id(1L)
                                .profile("profilePath")
                                .nickname("nickname")
                                .build()
                );
                MemberSearchResponse response = MemberSearchResponse.builder()
                        .members(memberDtoList)
                        .num(memberDtoList.size())
                        .build();

                Mockito.when(memberService.searchFriend(Mockito.any(SessionMember.class),Mockito.anyBoolean(),Mockito.anyString()))
                        .thenReturn(response);

                //when
                //then
                mockMvc.perform(
                            MockMvcRequestBuilders.get("/api/v2/friends/search")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .param("relation","false")
                                    .param("name","라이푸니")
                        )
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andDo(document("friend-search-NotFriend",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                queryParameters(
                                			parameterWithName("relation").description("조회할 유저가 친구 상태인지"),
                                            parameterWithName("name").description("검색할 유저 이름")
                                ),
                                responseFields(
                                        fieldWithPath("code").type(NUMBER)
                                                .description("상태 코드"),
                                        fieldWithPath("status").type(STRING)
                                                .description("응답 상태"),
                                        fieldWithPath("data").type(OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.num").type(NUMBER)
                                                .description("데이터 개수"),
                                        fieldWithPath("data.members[]").type(ARRAY)
                                                .description("유저 정보 리스트"),
                                        fieldWithPath("data.members[].id").type(NUMBER)
                                                .description("유저 id"),
                                        fieldWithPath("data.members[].nickname").type(STRING)
                                                .description("유저 닉네임"),
                                        fieldWithPath("data.members[].profile").type(STRING)
                                                .description("유저 프로필")
                                )
                            )
                        );

        }

        @Test
        void searchFriend() throws Exception {
                //given
                List<MemberSearchMemberDto> memberDtoList = List.of(
                        MemberSearchMemberDto.builder()
                                .id(1L)
                                .profile("profilePath")
                                .nickname("nickname")
                                .build()
                );
                MemberSearchResponse response = MemberSearchResponse.builder()
                        .members(memberDtoList)
                        .num(memberDtoList.size())
                        .build();

                Mockito.when(memberService.searchFriend(Mockito.any(SessionMember.class), Mockito.anyBoolean(),Mockito.anyString()))
                        .thenReturn(response);

                //when
                //then
                mockMvc.perform(
                            MockMvcRequestBuilders.get("/api/v2/friends/search")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .param("relation","true")
                                    .param("name","라이푸니")
                        )
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andDo(document("friend-search",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                queryParameters(
                                			parameterWithName("relation").description("조회할 유저가 친구 상태인지"),
                                            parameterWithName("name").description("검색할 유저 이름")
                                ),
                                responseFields(
                                        fieldWithPath("code").type(NUMBER)
                                                .description("상태 코드"),
                                        fieldWithPath("status").type(STRING)
                                                .description("응답 상태"),
                                        fieldWithPath("data").type(OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.num").type(NUMBER)
                                                .description("데이터 개수"),
                                        fieldWithPath("data.members[]").type(ARRAY)
                                                .description("유저 정보 리스트"),
                                        fieldWithPath("data.members[].id").type(NUMBER)
                                                .description("유저 id"),
                                        fieldWithPath("data.members[].nickname").type(STRING)
                                                .description("유저 닉네임"),
                                        fieldWithPath("data.members[].profile").type(STRING)
                                                .description("유저 프로필")
                                )
                            )
                        );
        }
        @Test
        void addFriend() throws Exception {
            //given
            MemberAddRequest request = MemberAddRequest.builder()
                    .friendId(1L)
                    .build();

            String content = objectMapper.writeValueAsString(request);

            Mockito.when(memberService.addFriend(Mockito.any(SessionMember.class), Mockito.any(MemberAddRequest.class)))
                            .thenReturn(true);

            //when
            //then
            mockMvc.perform(
                    MockMvcRequestBuilders.post("/api/v1/friends")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(content)
                    )
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andDo(document("friend-add",
                                    preprocessRequest(prettyPrint()),
                                    preprocessResponse(prettyPrint()),
                                    requestFields(
                                            fieldWithPath("friendId").type(NUMBER)
                                                    .description("추가할 유저 식별키")
                                    ),
                                    responseFields(
                                            fieldWithPath("code").type(NUMBER)
                                                    .description("상태 코드"),
                                            fieldWithPath("status").type(STRING)
                                                    .description("응답 상태"),
                                            fieldWithPath("data").type(BOOLEAN)
                                                    .description("친구 추가를 성공했는지 확인 여부")
                                    )
                                )
                    );
                }

}
