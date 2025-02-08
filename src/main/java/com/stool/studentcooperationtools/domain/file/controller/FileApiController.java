package com.stool.studentcooperationtools.domain.file.controller;

import com.stool.studentcooperationtools.domain.api.ApiResponse;
import com.stool.studentcooperationtools.domain.file.controller.request.FileUploadRequest;
import com.stool.studentcooperationtools.domain.file.controller.response.FileUploadResponse;
import com.stool.studentcooperationtools.domain.file.service.FileService;
import com.stool.studentcooperationtools.domain.room.service.RoomService;
import com.stool.studentcooperationtools.s3.S3Service;
import com.stool.studentcooperationtools.security.oauth2.dto.SessionMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class FileApiController {

    private final S3Service s3Service;
    private final RoomService roomService;
    private final FileService fileService;

    //방의 자료를 다운로드 요청을 다루는 핸들러이다.
    //해당방의 자원을 다운로드 받을 권한이 없다면 AccessDenied 예외가 발생한다.
    //Response Header에 CONTENT_DISPOSITION과 body에는 다운 받을 자원인 UrlResource를 넣는다.
    @GetMapping("/api/v1/files/{fileName}")
    public ResponseEntity<UrlResource> downloadFile(
            @PathVariable("fileName") String fileName,
            @RequestParam("roomId") Long roomId,
            @RequestParam("fileOriginalName") String originalFileName,
            SessionMember sessionMember
    ){
        //해당 방의 참여자가 아니면 다운로드를 거부한다.
        roomService.validateParticipationInRoom(roomId,sessionMember);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,s3Service.getContentDisposition(fileName))
                .body(s3Service.getUrlResource(fileName));
    }

    @PostMapping("/api/v1/files")
    public ApiResponse<FileUploadResponse> uploadFile(
            @Valid @RequestBody FileUploadRequest request,
            SessionMember sessionMember
    ){
        HashMap<String, List<String>> fileSet = s3Service.uploadFile(request.getFileName(), request.getFileCode());
        FileUploadResponse response = fileService.addFile(request,fileSet,sessionMember);
        return ApiResponse.of(HttpStatus.OK,response);
    }


}
