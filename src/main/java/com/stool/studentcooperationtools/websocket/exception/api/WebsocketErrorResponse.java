package com.stool.studentcooperationtools.websocket.exception.api;

import com.stool.studentcooperationtools.websocket.WebsocketMessageType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WebsocketErrorResponse<T> {
    private WebsocketMessageType messageType;
    private String message;
    private T data;

    @Builder
    private WebsocketErrorResponse(final WebsocketMessageType messageType,final String message, final T data) {
        this.messageType = messageType;
        this.message = message;
        this.data = data;
    }

    public static <T> WebsocketErrorResponse<T> of(final WebsocketMessageType messageType,
                                                   final String message , final T data){
        return new WebsocketErrorResponse<>(messageType, message, data);
    }
}
