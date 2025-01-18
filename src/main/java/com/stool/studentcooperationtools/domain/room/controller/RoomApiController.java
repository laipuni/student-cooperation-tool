package com.stool.studentcooperationtools.domain.room.controller;

import com.stool.studentcooperationtools.domain.api.ApiResponse;
import com.stool.studentcooperationtools.domain.room.controller.request.RoomAddRequest;
import com.stool.studentcooperationtools.domain.room.controller.request.RoomEnterRequest;
import com.stool.studentcooperationtools.domain.room.controller.request.RoomRemoveRequest;
import com.stool.studentcooperationtools.domain.room.controller.request.RoomTopicUpdateRequest;
import com.stool.studentcooperationtools.domain.room.controller.response.RoomAddResponse;
import com.stool.studentcooperationtools.domain.room.controller.response.RoomEnterResponse;
import com.stool.studentcooperationtools.domain.room.controller.response.RoomSearchResponse;
import com.stool.studentcooperationtools.domain.room.service.RoomDeleteService;
import com.stool.studentcooperationtools.domain.room.service.RoomService;
import com.stool.studentcooperationtools.security.oauth2.dto.SessionMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class RoomApiController {

    private final RoomService roomService;
    private final RoomDeleteService roomDeleteService;

    @PostMapping("/api/v1/rooms")
    public ApiResponse<RoomAddResponse> addRoom(SessionMember member, @Valid @RequestBody RoomAddRequest request){
        RoomAddResponse response = roomService.addRoom(member, request);
        return ApiResponse.of(HttpStatus.OK,response);
    }

    @GetMapping("/api/v2/rooms")
    public ApiResponse<RoomSearchResponse> searchRoom(
            @RequestParam(value = "isParticipation",defaultValue = "true") boolean isParticipation,
            @RequestParam(value = "title", defaultValue = "") String title,
            @RequestParam(value = "page",defaultValue = "0") int page,
            SessionMember userInfo
    ){
        RoomSearchResponse response = roomService.searchRoom(
                title, page,
                isParticipation,
                userInfo.getMemberSeq()
        );

        return ApiResponse.of(HttpStatus.OK,response);
    }

    @DeleteMapping("/api/v1/rooms")
    public ApiResponse<Boolean> removeRoom(SessionMember member, @Valid @RequestBody RoomRemoveRequest request) {
        Boolean result = roomDeleteService.removeRoom(member, request);
        return ApiResponse.of(HttpStatus.OK,result);
    }

    @PostMapping("/api/v1/rooms/enter-room")
    public ApiResponse<RoomEnterResponse> enterRoom(SessionMember member, @Valid @RequestBody RoomEnterRequest request){
        RoomEnterResponse response = roomService.enterRoom(member, request);
        return ApiResponse.of(HttpStatus.OK, response);
    }

    @PostMapping("/api/v1/rooms/topics")
    public ApiResponse<Boolean> updateRoomTopic(SessionMember member, @Valid @RequestBody RoomTopicUpdateRequest request){
        Boolean result = roomService.updateRoomTopic(member, request);
        return ApiResponse.of(HttpStatus.OK,result);
    }
}
