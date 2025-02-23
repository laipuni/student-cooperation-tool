package com.stool.studentcooperationtools.websocket.controller.part;

import com.stool.studentcooperationtools.domain.part.service.PartService;
import com.stool.studentcooperationtools.security.oauth2.dto.SessionMember;
import com.stool.studentcooperationtools.websocket.WebsocketTestSupport;
import com.stool.studentcooperationtools.websocket.controller.part.request.PartAddWebsocketRequest;
import com.stool.studentcooperationtools.websocket.controller.part.request.PartDeleteWebsocketRequest;
import com.stool.studentcooperationtools.websocket.controller.part.request.PartUpdateWebsocketRequest;
import com.stool.studentcooperationtools.websocket.controller.part.response.PartAddWebsocketResponse;
import com.stool.studentcooperationtools.websocket.controller.part.response.PartDeleteWebsocketResponse;
import com.stool.studentcooperationtools.websocket.controller.part.response.PartUpdateWebsocketResponse;
import com.stool.studentcooperationtools.websocket.controller.request.WebsocketResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.stool.studentcooperationtools.websocket.WebsocketMessageType.*;
import static com.stool.studentcooperationtools.websocket.controller.Utils.SimpleMessageSendingUtils.PART_RESEARCH_URL_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;

class PartWebsocketControllerTest extends WebsocketTestSupport {

    @MockBean
    PartService partService;

    @DisplayName("웹소켓으로 해당 조사 역할을 추가 요청을 받고 같은 url에 구독한 유저들에게 보낸다.")
    @Test
    void addPart() throws ExecutionException, InterruptedException, TimeoutException {
        //given
        Long roomId = 1L;
        String partName = "해당 조사 역할";
        PartAddWebsocketRequest request = PartAddWebsocketRequest.builder()
                .partName(partName)
                .roomId(1L)
                .memberId(1L)
                .build();

        PartAddWebsocketResponse response = PartAddWebsocketResponse.builder()
                .partId(1L)
                .profile("profile")
                .nickName("nickName")
                .partName(partName)
                .createTime(LocalDate.of(2024,11,5))
                .build();

        Mockito.when(partService.addPart(Mockito.any(PartAddWebsocketRequest.class))).thenReturn(response);

        stompSession.subscribe(PART_RESEARCH_URL_FORMAT.formatted(roomId),resultHandler);
        //when
        stompSession.send("/pub/parts/add",request);
        WebsocketResponse websocketResponse = resultHandler.get(3);
        //then
        assertThat(websocketResponse.getMessageType()).isEqualTo(PART_ADD);
        assertThat(websocketResponse.getData()).isNotNull();
    }

    @DisplayName("웹소켓으로 해당 조사 역할을 추가 요청을 받고 같은 url에 구독한 유저들에게 결과를 보낸다.")
    @Test
    void deletePart() throws ExecutionException, InterruptedException, TimeoutException {
        //given
        Long roomId = 1L;
        Long partId = 1L;
        PartDeleteWebsocketRequest request = PartDeleteWebsocketRequest.builder()
                .partId(partId)
                .roomId(roomId)
                .build();

        PartDeleteWebsocketResponse response = PartDeleteWebsocketResponse.builder()
                .partId(partId)
                .build();

        Mockito.when(partService.deletePart(
                Mockito.any(PartDeleteWebsocketRequest.class),Mockito.any(SessionMember.class)
        )).thenReturn(response);

        stompSession.subscribe(PART_RESEARCH_URL_FORMAT.formatted(roomId),resultHandler);
        //when
        stompSession.send("/pub/parts/delete",request);
        WebsocketResponse result = resultHandler.get(3);
        //then
        assertThat(result.getMessageType()).isEqualTo(PART_DELETE);
        assertThat(result.getData()).isNotNull();
    }

    @DisplayName("웹소켓으로 역할 수정 요청을 받아서 같은 url에 구독한 유저들에게 결과를 보낸다")
    @Test
    void updatePart() throws ExecutionException, InterruptedException, TimeoutException {
        //given
        Long roomId = 1L;
        Long partId = 1L;
        String partName = "partName";
        PartUpdateWebsocketRequest request = PartUpdateWebsocketRequest.builder()
                .partId(partId)
                .partName(partName)
                .memberId(1L)
                .roomId(roomId)
                .build();

        PartUpdateWebsocketResponse response = PartUpdateWebsocketResponse.builder()
                .partId(partId)
                .partName(partName)
                .profile("profile")
                .nickName("nickName")
                .build();

        Mockito.when(partService.updatePart(
                Mockito.any(PartUpdateWebsocketRequest.class),Mockito.any(SessionMember.class)
        )).thenReturn(response);

        stompSession.subscribe(PART_RESEARCH_URL_FORMAT.formatted(roomId),resultHandler);
        //when
        stompSession.send("/pub/parts/update",request);
        WebsocketResponse result = resultHandler.get(3);
        //then
        assertThat(result.getMessageType()).isEqualTo(PART_UPDATE);
        assertThat(result.getData()).isNotNull()
                .extracting("partId","partName","profile","nickName")
                .containsExactlyInAnyOrder(
                        response.getPartId().intValue(),
                        response.getPartName(),
                        response.getProfile(),
                        response.getNickName()
                );
    }
}