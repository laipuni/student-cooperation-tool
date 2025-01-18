package com.stool.studentcooperationtools.docs.room;

import com.stool.studentcooperationtools.docs.RestDocsSupport;
import com.stool.studentcooperationtools.domain.room.controller.RoomApiController;
import com.stool.studentcooperationtools.domain.room.controller.request.RoomAddRequest;
import com.stool.studentcooperationtools.domain.room.controller.request.RoomEnterRequest;
import com.stool.studentcooperationtools.domain.room.controller.request.RoomRemoveRequest;
import com.stool.studentcooperationtools.domain.room.controller.request.RoomTopicUpdateRequest;
import com.stool.studentcooperationtools.domain.room.controller.response.RoomAddResponse;
import com.stool.studentcooperationtools.domain.room.controller.response.RoomEnterResponse;
import com.stool.studentcooperationtools.domain.room.service.RoomDeleteService;
import com.stool.studentcooperationtools.domain.room.service.RoomService;
import com.stool.studentcooperationtools.security.oauth2.dto.SessionMember;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RoomApiControllerDocsTest extends RestDocsSupport {

    private final RoomService roomService = mock(RoomService.class);
    private final RoomDeleteService roomDeleteService = mock(RoomDeleteService.class);

    @Override
    protected Object initController() {
        return new RoomApiController(roomService,roomDeleteService);
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

        Mockito.when(roomService.addRoom(any(SessionMember.class), any(RoomAddRequest.class)))
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

    @Test
    void removeRoom() throws Exception {
        //given
        RoomRemoveRequest request = RoomRemoveRequest.builder()
                .roomId(1L)
                .build();

        String content = objectMapper.writeValueAsString(request);

        Mockito.when(roomDeleteService.removeRoom(any(SessionMember.class), any(RoomRemoveRequest.class)))
                .thenReturn(true);

        //when
        //then
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("room-remove",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("roomId").type(NUMBER)
                                                .description("제거할 방의 식별키")
                                ),
                                responseFields(
                                        fieldWithPath("code").type(NUMBER)
                                                .description("상태 코드"),
                                        fieldWithPath("status").type(STRING)
                                                .description("응답 상태"),
                                        fieldWithPath("data").type(BOOLEAN)
                                                .description("삭제 성공 여부")
                                )
                        )
                );

    }

    @Test
    void enterRoom() throws Exception {
        //given
        RoomEnterRequest request = RoomEnterRequest.builder()
                .roomId(1L)
                .password("password")
                .build();
        RoomEnterResponse response = RoomEnterResponse.builder()
                .leaderId(1L)
                .build();
        String content = objectMapper.writeValueAsString(request);

        Mockito.when(roomService.enterRoom(any(SessionMember.class), any(RoomEnterRequest.class)))
                .thenReturn(response);

        //when
        //then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/rooms/enter-room")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("room-enter",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("roomId").type(NUMBER)
                                                        .description("들어갈 방 식별키"),
                                        fieldWithPath("password").type(STRING)
                                                .description("들어갈 방의 비밀번호")
                                ),
                                responseFields(
                                        fieldWithPath("code").type(NUMBER)
                                                .description("상태 코드"),
                                        fieldWithPath("status").type(STRING)
                                                .description("응답 상태"),
                                        fieldWithPath("data").type(OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.leaderId").type(NUMBER)
                                                .description("방장의 식별키")
                                )
                        )
                );

    }

    @Test
    void updateRoomTopic() throws Exception {
        //given
        RoomTopicUpdateRequest request = RoomTopicUpdateRequest.builder()
                .roomId(1L)
                .topicId(1L)
                .build();

        String content = objectMapper.writeValueAsString(request);

        Mockito.when(roomService.updateRoomTopic(any(SessionMember.class), any(RoomTopicUpdateRequest.class)))
                .thenReturn(true);

        //when
        //then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/rooms/topics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("room-topic-update",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("roomId").type(NUMBER)
                                                .description("주제를 정할 방 식별키"),
                                        fieldWithPath("topicId").type(NUMBER)
                                                .description("메인 주제로 정해질 주제의 식별키")
                                ),
                                responseFields(
                                        fieldWithPath("code").type(NUMBER)
                                                .description("상태 코드"),
                                        fieldWithPath("status").type(STRING)
                                                .description("응답 상태"),
                                        fieldWithPath("data").type(BOOLEAN)
                                                .description("인증 성공 여부")
                                )
                        )
                );

    }
}
