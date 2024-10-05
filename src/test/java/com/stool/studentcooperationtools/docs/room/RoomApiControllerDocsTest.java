package com.stool.studentcooperationtools.docs.room;

import com.stool.studentcooperationtools.docs.RestDocsSupport;
import com.stool.studentcooperationtools.domain.room.controller.RoomApiController;
import com.stool.studentcooperationtools.domain.room.controller.request.RoomAddRequest;
import com.stool.studentcooperationtools.domain.room.controller.response.RoomAddResponse;
import com.stool.studentcooperationtools.domain.room.controller.response.RoomFindDto;
import com.stool.studentcooperationtools.domain.room.controller.response.RoomsFindResponse;
import com.stool.studentcooperationtools.domain.room.service.RoomService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RoomApiControllerDocsTest extends RestDocsSupport {

    private final RoomService roomService = mock(RoomService.class);

    @Override
    protected Object initController() {
        return new RoomApiController(roomService);
    }

    @Test
    void findRooms() throws Exception {
        //given
        List<RoomFindDto> findDtoList = List.of(
                RoomFindDto.builder()
                        .roomId(1L)
                        .title("방 제목")
                        .topic("방 주제")
                        .participationNum(5)
                        .build()
        );
        RoomsFindResponse roomsFindResponse = RoomsFindResponse.builder()
                .num(findDtoList.size())
                .rooms(findDtoList)
                .build();
        Mockito.when(roomService.findRooms(anyInt()))
                .thenReturn(roomsFindResponse);
        //when
        //then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/rooms")
                .param("page","1")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("room-find",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(
                                parameterWithName("page").description("조회할 방들의 페이지")
                        ),
                        responseFields(
                                fieldWithPath("code").type(NUMBER)
                                        .description("상태 코드"),
                                fieldWithPath("status").type(STRING)
                                        .description("응답 상태"),
                                fieldWithPath("data").type(OBJECT)
                                        .description("응답 데이터"),
                                fieldWithPath("data.num").type(NUMBER)
                                        .description("조회된 방 개수"),
                                fieldWithPath("data.rooms[]").type(ARRAY)
                                        .description("방 정보 리스트"),
                                fieldWithPath("data.rooms[].roomId").type(NUMBER)
                                        .description("방 식별키"),
                                fieldWithPath("data.rooms[].title").type(STRING)
                                        .description("방 제목"),
                                fieldWithPath("data.rooms[].topic").type(STRING)
                                        .description("방 주제"),
                                fieldWithPath("data.rooms[].participationNum").type(NUMBER)
                                        .description("방 참가자")
                        )
                        )
                );

    }

    @Test
    void addRoom() throws Exception {
        //given
        RoomAddRequest request = RoomAddRequest.builder()
                .title("방 제목")
                .password("password")
                .participation(
                       List.of(1L,2L)
                )
                .build();

        String content = objectMapper.writeValueAsString(request);

        RoomAddResponse response = RoomAddResponse.builder()
                .roomId(1L)
                .title("방 제목")
                .build();

        Mockito.when(roomService.addRoom(any(RoomAddRequest.class)))
                        .thenReturn(response);

        //when
        //then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
            )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("room-add",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("title").type(STRING)
                                        .description("생성할 방 제목"),
                                fieldWithPath("password").type(STRING)
                                        .description("생성할 방 비밀번호"),
                                fieldWithPath("participation").type(ARRAY)
                                        .description("생성할 방에 참가자들")
                        ),
                        responseFields(
                                fieldWithPath("code").type(NUMBER)
                                        .description("상태 코드"),
                                fieldWithPath("status").type(STRING)
                                        .description("응답 상태"),
                                fieldWithPath("data").type(OBJECT)
                                        .description("응답 데이터"),
                                fieldWithPath("data.roomId").type(NUMBER)
                                        .description("생성한 방 식별키"),
                                fieldWithPath("data.title").type(STRING)
                                        .description("생성한 방 제목")
                        )
                        )
                );

        }
}