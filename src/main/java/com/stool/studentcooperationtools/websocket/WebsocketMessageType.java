package com.stool.studentcooperationtools.websocket;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WebsocketMessageType {

    ROOM_ENTER("room_enter"),
    ROOM_EXIT("room_exit"),
    TOPIC_ADD("topic_add"),
    TOPIC_DELETE("topic_delete"),
    VOTE_UPDATE("vote_update"),
    CHAT_ENTER("chat_enter"),
    CHAT_ADD("chat_add"),
    CHAT_DELETE("chat_delete"),
    PART_ADD("part_add"),
    PART_DELETE("part_delete"),
    PART_UPDATE("part_update"),
    PART_FILE_UPLOAD("file_upload"),
    PART_FILE_REMOVE("file_delete"),
    PRESENTATION_CREATE("presentation_create"),
    PRESENTATION_UPDATE("presentation_update"),
    SCRIPT_UPDATE("script_update"),
    ERROR("error");

    private final String key;

}
