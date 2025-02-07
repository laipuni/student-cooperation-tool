package com.stool.studentcooperationtools.domain.presentation.controller;

import com.google.api.client.auth.oauth2.Credential;
import com.stool.studentcooperationtools.domain.api.ApiResponse;
import com.stool.studentcooperationtools.domain.presentation.controller.response.PresentationFindResponse;
import com.stool.studentcooperationtools.domain.presentation.service.PresentationService;
import com.stool.studentcooperationtools.security.credential.GoogleCredentialProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class PresentationApiController {

    private final PresentationService presentationService;
    private final GoogleCredentialProvider credentialProvider;

    @GetMapping("/api/v1/rooms/{roomId}/presentation")
    public ApiResponse<PresentationFindResponse> findPresentation(@PathVariable("roomId") Long roomId){
        PresentationFindResponse response = presentationService.findPresentation(roomId);
        return ApiResponse.of(HttpStatus.OK,response);
    }

    @GetMapping("/api/v1/presentation/{presentationId}/exportPdf")
    public void exportToPdf(@PathVariable("presentationId") Long presentationId,
                            HttpServletResponse response){
        // ByteArrayOutputStream의 데이터를 응답으로 전송
        //ByteArrayOutputStream 사용 후 닫기
        Credential credential = credentialProvider.getCredential();
        try (ByteArrayOutputStream pdfStream = presentationService.exportPdf(credential, presentationId)) {
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"exported.pdf\"");
            response.getOutputStream().write(pdfStream.toByteArray());
            response.getOutputStream().flush();
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e.getCause());
        }
    }


    @GetMapping("/api/v1/presentation/{presentationId}/exportPptx")
    public void exportToPptx(@PathVariable("presentationId") Long presentationId,
                             HttpServletResponse response) {
        Credential credential = credentialProvider.getCredential();
        try(ByteArrayOutputStream pdfStream = presentationService.exportPpt(credential, presentationId)){
            response.setContentType("application/vnd.openxmlformats-officedocument.presentationml.presentation");
            response.setHeader("Content-Disposition", "attachment; filename=\"exported.pptx\"");
            // ByteArrayOutputStream의 데이터를 응답으로 전송
            response.getOutputStream().write(pdfStream.toByteArray());
            response.getOutputStream().flush();
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(),e.getCause());
        }
    }

    @GetMapping("/api/v1/checkValidPPT/{presentationPath}")
    public ApiResponse<Boolean> checkValidPPT(@PathVariable("presentationPath") String presentationPath){
        Credential credential = credentialProvider.getCredential();
        boolean result = presentationService.checkValidPPT(credential, presentationPath);
        return ApiResponse.of(HttpStatus.OK, result);
    }

}
