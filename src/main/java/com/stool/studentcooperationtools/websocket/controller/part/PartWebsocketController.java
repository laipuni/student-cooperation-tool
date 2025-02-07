package com.stool.studentcooperationtools.websocket.controller.part;

import com.stool.studentcooperationtools.domain.part.service.PartService;
import com.stool.studentcooperationtools.security.oauth2.dto.SessionMember;
import com.stool.studentcooperationtools.websocket.controller.Utils.SimpleMessageSendingUtils;
import com.stool.studentcooperationtools.websocket.controller.part.request.PartAddWebsocketRequest;
import com.stool.studentcooperationtools.websocket.controller.part.request.PartDeleteWebsocketRequest;
import com.stool.studentcooperationtools.websocket.controller.part.request.PartUpdateWebsocketRequest;
import com.stool.studentcooperationtools.websocket.controller.part.response.PartAddWebsocketResponse;
import com.stool.studentcooperationtools.websocket.controller.part.response.PartDeleteWebsocketResponse;
import com.stool.studentcooperationtools.websocket.controller.part.response.PartUpdateWebsocketResponse;
import com.stool.studentcooperationtools.websocket.controller.request.WebsocketResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import static com.stool.studentcooperationtools.websocket.WebsocketMessageType.*;

@Controller
@RequiredArgsConstructor
public class PartWebsocketController {

    private final PartService partService;
    private final SimpleMessageSendingUtils sendingUtils;

    @MessageMapping("/parts/add")
    public void addPart(@Valid @RequestBody PartAddWebsocketRequest request, SessionMember member){
        PartAddWebsocketResponse response = partService.addPart(request);
        sendingUtils.convertAndSend(
                sendingUtils.creatPartResearchSubUrl(request.getRoomId()),
                WebsocketResponse.of(PART_ADD,response)
        );
    }

    @MessageMapping("/parts/delete")
    public void deletePart(@Valid @RequestBody PartDeleteWebsocketRequest request, SessionMember member){
        PartDeleteWebsocketResponse response = partService.deletePart(request,member);
        sendingUtils.convertAndSend(
                sendingUtils.creatPartResearchSubUrl(request.getRoomId()),
                WebsocketResponse.of(PART_DELETE,response)
        );
    }


    @MessageMapping("/parts/update")
    public void updatePart(@Valid @RequestBody PartUpdateWebsocketRequest request, SessionMember member){
        PartUpdateWebsocketResponse response = partService.updatePart(request,member);
        sendingUtils.convertAndSend(
                sendingUtils.creatPartResearchSubUrl(request.getRoomId()),
                WebsocketResponse.of(PART_UPDATE,response)
        );
    }

}
