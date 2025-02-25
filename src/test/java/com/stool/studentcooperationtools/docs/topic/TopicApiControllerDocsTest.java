package com.stool.studentcooperationtools.docs.topic;

import com.stool.studentcooperationtools.docs.RestDocsSupport;
import com.stool.studentcooperationtools.domain.topic.controller.TopicApiController;
import com.stool.studentcooperationtools.domain.topic.controller.response.TopicFindDto;
import com.stool.studentcooperationtools.domain.topic.controller.response.TopicFindResponse;
import com.stool.studentcooperationtools.domain.topic.service.TopicService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TopicApiControllerDocsTest extends RestDocsSupport {

    private final TopicService topicService = mock(TopicService.class);
    @Override
    protected Object initController() {
        return new TopicApiController(topicService);
    }

    @Test
    void findTopics() throws Exception {
        //given
        String roomId = "1";
        List<TopicFindDto> topicFindDtoList = List.of(
                TopicFindDto.builder()
                        .topicId(1L)
                        .memberId(1L)
                        .voteNum(1)
                        .topic("주제")
                        .build()
        );

        TopicFindResponse topicFindResponse = TopicFindResponse.builder()
                .num(topicFindDtoList.size())
                .topics(topicFindDtoList)
                .build();
        Mockito.when(topicService.findTopics(anyLong()))
                .thenReturn(topicFindResponse);
        //then
        //when
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/rooms/"+ roomId +"/topics")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("Topic-find",
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
                                                .description("조회된 주제 개수"),
                                        fieldWithPath("data.topics[]").type(ARRAY)
                                                .description("주제 리스트"),
                                        fieldWithPath("data.topics[].memberId").type(NUMBER)
                                                .description("주제를 생성한 유저의 식별키"),
                                        fieldWithPath("data.topics[].topicId").type(NUMBER)
                                                .description("주제의 식별키"),
                                        fieldWithPath("data.topics[].voteNum").type(NUMBER)
                                                .description("투표 개수"),
                                        fieldWithPath("data.topics[].topic").type(STRING)
                                                .description("방 주제")
                                )
                        )
                );
    }
}
