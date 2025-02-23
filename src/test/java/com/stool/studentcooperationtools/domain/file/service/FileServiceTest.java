package com.stool.studentcooperationtools.domain.file.service;

import com.stool.studentcooperationtools.IntegrationTest;
import com.stool.studentcooperationtools.domain.file.File;
import com.stool.studentcooperationtools.domain.file.FileType;
import com.stool.studentcooperationtools.domain.file.controller.request.FileUploadRequest;
import com.stool.studentcooperationtools.domain.file.controller.response.FileUploadResponse;
import com.stool.studentcooperationtools.domain.file.repository.FileRepository;
import com.stool.studentcooperationtools.domain.member.Member;
import com.stool.studentcooperationtools.domain.member.Role;
import com.stool.studentcooperationtools.domain.member.repository.MemberRepository;
import com.stool.studentcooperationtools.domain.part.Part;
import com.stool.studentcooperationtools.domain.part.repository.PartRepository;
import com.stool.studentcooperationtools.domain.room.Room;
import com.stool.studentcooperationtools.domain.room.repository.RoomRepository;
import com.stool.studentcooperationtools.exception.global.UnAuthorizationException;
import com.stool.studentcooperationtools.s3.S3Service;
import com.stool.studentcooperationtools.security.oauth2.dto.SessionMember;
import com.stool.studentcooperationtools.websocket.controller.file.request.FileDeleteWebsocketRequest;
import com.stool.studentcooperationtools.websocket.controller.file.response.FileDeleteWebsocketResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@Transactional
class FileServiceTest extends IntegrationTest {

    @Autowired
    FileService fileService;

    @Autowired
    FileRepository fileRepository;

    @Autowired
    PartRepository partRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    RoomRepository roomRepository;

    @MockBean
    S3Service s3Service;

    @DisplayName("파일을 추가할 때, 파일을 추가할 역할이 존재하지 않으면 에러가 발생한다.")
    @Test
    void addFileWithNotExistPart(){
        //given
        Member member = Member.builder()
                .email("email")
                .nickName("닉네임")
                .profile("profile")
                .role(Role.USER)
                .build();
        memberRepository.save(member);
        Room room = Room.builder()
                .password("password")
                .title("제목")
                .leader(member)
                .participationNum(1)
                .build();
        roomRepository.save(room);
        Long invalidPartId = 2024L;
        String fileOriginalName = "파일 원래 이름";
        String fileName = UUID.randomUUID().toString();
        String extension = "docs";
        FileUploadRequest request = FileUploadRequest.builder()
                .partId(invalidPartId)
                .fileCode("base")
                .roomId(room.getId())
                .fileName(fileOriginalName)
                .build();

        SessionMember sessionMember = SessionMember.builder()
                .memberSeq(member.getId())
                .nickName(member.getNickName())
                .profile(member.getNickName())
                .build();

        HashMap<String, List<String>> fileSet = new HashMap<>();
        fileSet.put(fileName,List.of(fileName,extension));

        //when
        //then
        Assertions.assertThatThrownBy(() -> fileService.addFile(request,fileSet,sessionMember))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageMatching("파일을 추가할 역할이 존재하지 않습니다.");
    }


    @DisplayName("파일을 추가할 때, 권한(방장,본인)이 아닐 경우 접근 제한 에러가 발생한다.")
    @Test
    void addFileWithNotLeaderOrOwner(){
        //given
        Member member = Member.builder()
                .email("email")
                .nickName("닉네임")
                .profile("profile")
                .role(Role.USER)
                .build();
        Member another = Member.builder()
                .email("anotheremail")
                .nickName("another닉네임")
                .profile("anotherprofile")
                .role(Role.USER)
                .build();
        memberRepository.saveAll(List.of(member,another));
        Room room = Room.builder()
                .password("password")
                .title("제목")
                .leader(member)
                .participationNum(1)
                .build();
        roomRepository.save(room);
        String content = "조사할 부분";
        Part part = Part.builder()
                .partName(content)
                .room(room)
                .member(member)
                .build();
        partRepository.save(part);

        String fileOriginalName = "파일 원래 이름";
        String fileName = UUID.randomUUID().toString();
        String extension = "docs";
        FileUploadRequest request = FileUploadRequest.builder()
                .partId(part.getId())
                .fileCode("base64 코드")
                .roomId(room.getId())
                .fileName(fileOriginalName)
                .build();

        SessionMember sessionMember = SessionMember.builder()
                .memberSeq(another.getId())
                .nickName(another.getNickName())
                .profile(another.getNickName())
                .build();

        HashMap<String, List<String>> fileSet = new HashMap<>();
        fileSet.put(fileName,List.of(fileName,extension));

        //when
        //then
        Assertions.assertThatThrownBy(() -> fileService.addFile(request,fileSet,sessionMember))
                .isInstanceOf(UnAuthorizationException.class)
                .hasMessageMatching("파일을 올릴 권한이 없습니다.");
    }

    @DisplayName("추가할 파일의 값들로 파일을 저장한다.")
    @Test
    void addFile(){
        //given
        Member member = Member.builder()
                .email("email")
                .nickName("닉네임")
                .profile("profile")
                .role(Role.USER)
                .build();
        memberRepository.save(member);
        Room room = Room.builder()
                .password("password")
                .title("제목")
                .leader(member)
                .participationNum(1)
                .build();
        roomRepository.save(room);
        String content = "조사할 부분";
        Part part = Part.builder()
                .partName(content)
                .room(room)
                .member(member)
                .build();
        partRepository.save(part);

        String fileOriginalName = "파일 원래 이름";
        String fileName = UUID.randomUUID().toString();
        String extension = "docx";
        FileUploadRequest request = FileUploadRequest.builder()
                .partId(part.getId())
                .fileCode(extension)
                .roomId(room.getId())
                .fileName(fileOriginalName)
                .build();

        SessionMember sessionMember = SessionMember.builder()
                .memberSeq(member.getId())
                .nickName(member.getNickName())
                .profile(member.getNickName())
                .build();

        HashMap<String, List<String>> fileSet = new HashMap<>();
        fileSet.put(fileOriginalName,List.of(fileName,extension));
        //when
        FileUploadResponse response = fileService.addFile(request, fileSet, sessionMember);
        List<File> files = fileRepository.findAll();
        //then
        assertThat(response.getNum()).isEqualTo(1);
        assertThat(response.getFiles()).hasSize(1)
                .extracting("fileId","originalName","fileName")
                .containsExactlyInAnyOrder(tuple(files.get(0).getId(),fileOriginalName,fileName));
    }

    @DisplayName("삭제할 파일의 값들로 파일을 삭제한다.")
    @Test
    void deleteFile(){
        //given
        Member member = Member.builder()
                .email("email")
                .nickName("닉네임")
                .profile("profile")
                .role(Role.USER)
                .build();
        memberRepository.save(member);
        Room room = Room.builder()
                .password("password")
                .title("제목")
                .leader(member)
                .participationNum(1)
                .build();
        roomRepository.save(room);
        String content = "조사할 부분";
        Part part = Part.builder()
                .partName(content)
                .room(room)
                .member(member)
                .build();
        partRepository.save(part);
        String originalName = "originalFileName";
        String fileName = UUID.randomUUID().toString();
        File file = File.builder()
                .part(part)
                .originalName(originalName)
                .fileName(fileName)
                .fileType(FileType.JPG)
                .build();
        part.addFile(file);
        fileRepository.save(file);

        FileDeleteWebsocketRequest request = FileDeleteWebsocketRequest.builder()
                .fileId(file.getId())
                .fileName(fileName)
                .roomId(room.getId())
                .build();

        SessionMember sessionMember = SessionMember.builder()
                .memberSeq(member.getId())
                .nickName(member.getNickName())
                .profile(member.getProfile())
                .build();

        //when
        FileDeleteWebsocketResponse response = fileService.deleteFile(request, sessionMember);
        List<File> files = fileRepository.findAll();
        //then
        assertThat(files.size()).isZero();
        assertThat(response.getFileId()).isEqualTo(file.getId());
    }

    @DisplayName("파일을 삭제할 때, 권한(방장,본인)이 없을 경우 접근 제한 에러가 발생한다.")
    @Test
    void deleteFileWithNotLeaderOrOwner(){
        //given
        Member member = Member.builder()
                .email("email")
                .nickName("닉네임")
                .profile("profile")
                .role(Role.USER)
                .build();
        Member another = Member.builder()
                .email("anotheremail")
                .nickName("another닉네임")
                .profile("anotherprofile")
                .role(Role.USER)
                .build();
        memberRepository.saveAll(List.of(member,another));
        Room room = Room.builder()
                .password("password")
                .title("제목")
                .leader(member)
                .participationNum(1)
                .build();
        roomRepository.save(room);
        String content = "조사할 부분";
        Part part = Part.builder()
                .partName(content)
                .room(room)
                .member(member)
                .build();
        partRepository.save(part);
        String originalName = "originalFileName";
        String fileName = UUID.randomUUID().toString();
        File file = File.builder()
                .part(part)
                .originalName(originalName)
                .fileName(fileName)
                .fileType(FileType.JPG)
                .build();
        part.addFile(file);
        partRepository.save(part);
        fileRepository.save(file);

        FileDeleteWebsocketRequest request = FileDeleteWebsocketRequest.builder()
                .fileId(file.getId())
                .fileName(fileName)
                .roomId(room.getId())
                .build();

        SessionMember sessionMember = SessionMember.builder()
                .memberSeq(another.getId())
                .nickName(another.getNickName())
                .profile(another.getNickName())
                .build();

        //when
        //then
        assertThatThrownBy(() -> fileService.deleteFile(request,sessionMember))
                .isInstanceOf(UnAuthorizationException.class)
                .hasMessageMatching("파일을 삭제할 권한이 없습니다.");
    }

}