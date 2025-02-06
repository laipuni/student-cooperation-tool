package com.stool.studentcooperationtools.websocket.exception.handler;

import com.stool.studentcooperationtools.exception.global.PrincipalNotFoundException;
import com.stool.studentcooperationtools.exception.global.UnAuthorizationException;
import com.stool.studentcooperationtools.websocket.WebsocketMessageType;
import com.stool.studentcooperationtools.websocket.controller.Utils.SimpleMessageSendingUtils;
import com.stool.studentcooperationtools.websocket.exception.api.WebsocketErrorResponse;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.security.Principal;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class WebsocketExceptionHandler {

    private final SimpleMessageSendingUtils sendingUtils;

    @MessageExceptionHandler(value = IllegalArgumentException.class)
    public void IllegalArgumentException(Message<?> message, IllegalArgumentException e) {
        log.trace("[{}] {}",e.getClass().getSimpleName(), e.getMessage());
        sendErrorMessage(e, getPrincipal(message));
    }

    @MessageExceptionHandler(value = PrincipalNotFoundException.class)
    public void IllegalArgumentException(Message<?> message, PrincipalNotFoundException e) {
        log.warn("[{}] {}",e.getClass().getSimpleName(), e.getMessage());
        sendErrorMessage(e, getPrincipal(message));
    }

    @MessageExceptionHandler(value = BindException.class)
    public void BindException(Message<?> message, BindException e) {
        log.trace("[{}] {}, {}",e.getClass().getSimpleName(), e.getMessage(), e.getCause());
        sendErrorMessage(e, getPrincipal(message));
    }

    @MessageExceptionHandler(value = ValidationException.class)
    public void ValidationException(Message<?> message, ValidationException e) {
        log.trace("[{}] {}, {}",e.getClass().getSimpleName(), e.getMessage(), e.getCause());
        sendErrorMessage(e, getPrincipal(message));
    }

    @MessageExceptionHandler(value = UnAuthorizationException.class)
    public void AccessException(Message<?> message, UnAuthorizationException e) {
        log.trace("[{}] {}",e.getClass().getSimpleName(), e.getMessage());
        sendErrorMessage(e, getPrincipal(message));
    }
    @MessageExceptionHandler(value = IllegalStateException.class)
    public void BindException(Message<?> message, IllegalStateException e) {
        log.error("[{}] {} {}",e.getClass().getSimpleName(), e.getMessage(), e.getCause());
        sendErrorMessage(e, getPrincipal(message));
    }

    private static Principal getPrincipal(final Message<?> message) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        Principal principal = (Principal) accessor.getHeader("simpUser");
        if(principal == null){
            throw new PrincipalNotFoundException("예기치 못한 예외가 발생했습니다.");
        }
        return principal;
    }

    private void sendErrorMessage(final Exception e, final Principal principal) {
        WebsocketErrorResponse<Object> errorResponse = WebsocketErrorResponse.of(WebsocketMessageType.ERROR, e.getMessage(),null);
        String userId = principal.getName();
        log.trace("[Websocket Error] send Error message to {}, {}", userId, e.getMessage());
        sendingUtils.SendErrorMessageToUser(userId, errorResponse);
    }


}
