package com.stool.studentcooperationtools.domain.file.service;

import com.amazonaws.AmazonServiceException;
import com.stool.studentcooperationtools.domain.file.File;
import com.stool.studentcooperationtools.domain.file.FileType;
import com.stool.studentcooperationtools.domain.file.controller.request.FileUploadRequest;
import com.stool.studentcooperationtools.domain.file.controller.response.FileUploadResponse;
import com.stool.studentcooperationtools.domain.file.repository.FileRepository;
import com.stool.studentcooperationtools.domain.part.Part;
import com.stool.studentcooperationtools.domain.part.repository.PartRepository;
import com.stool.studentcooperationtools.exception.global.UnAuthorizationException;
import com.stool.studentcooperationtools.s3.S3Service;
import com.stool.studentcooperationtools.security.oauth2.dto.SessionMember;
import com.stool.studentcooperationtools.websocket.controller.file.request.FileDeleteWebsocketRequest;
import com.stool.studentcooperationtools.websocket.controller.file.response.FileDeleteWebsocketResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileService {

    private final FileRepository fileRepository;
    private final PartRepository partRepository;
    private final S3Service s3Service;
    @Transactional
    public FileUploadResponse addFile(
            final FileUploadRequest request,
            final HashMap<String, List<String>> fileMap,
            final SessionMember sessionMember) {
        Part part = partRepository.findById(request.getPartId())
                .orElseThrow(() -> new IllegalArgumentException("파일을 추가할 역할이 존재하지 않습니다."));
        if(hasNotAuthorization(sessionMember.getMemberSeq(), part)){
            log.debug("사용자(Id : {})는 방(id:{})에 파일을 올릴 권한이 없다.",
                    sessionMember.getMemberSeq(),request.getRoomId());
            throw new UnAuthorizationException("파일을 올릴 권한이 없습니다.");
        }
        List<File> files = createFiles(fileMap, part);
        fileRepository.saveAll(files);
        log.info("사용자(Id : {})는 방(id:{})에 파일 {}개를 올렸다.",
                sessionMember.getMemberSeq(),request.getRoomId(),files.size());
        return FileUploadResponse.of(files);
    }

    private static List<File> createFiles(final HashMap<String, List<String>> fileMap, final Part part) {
        List<File> files = new ArrayList<>();
        fileMap.forEach((filName, metaData) ->{
            File file = File.of(metaData.get(0),
                    FileType.getFileType(metaData.get(1)), filName, part);
            files.add(file);
        });
        return files;
    }

    private static boolean hasNotAuthorization(final Long memberId, final Part part) {
        return !(part.isResponsibleForPart(memberId) || part.getRoom().isLeader(memberId));
    }

    //작업 접근 권한이 없다면 rollback한다.
    @Transactional(rollbackFor = {AccessDeniedException.class, AmazonServiceException.class})
    public FileDeleteWebsocketResponse deleteFile(final FileDeleteWebsocketRequest request,final SessionMember sessionMember) {
        int result = fileRepository.deleteFileByIdAndLeaderOrOwner(sessionMember.getMemberSeq(),request.getFileId());
        if(result == 0){
            log.debug("사용자(Id : {})는 파일(id : {})을 삭제할 권한이 없다.",
                    sessionMember.getMemberSeq(),request.getFileId());
            throw new UnAuthorizationException("파일을 삭제할 권한이 없습니다.");
        }
        log.info("사용자(Id : {})는 파일(id : {})을 삭제했다.",
                sessionMember.getMemberSeq(),request.getFileId());
        s3Service.deleteFile(request.getFileName());
        return FileDeleteWebsocketResponse.of(request.getFileId(),request.getPartId());
    }
}
