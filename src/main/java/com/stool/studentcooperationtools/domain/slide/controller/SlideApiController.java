package com.stool.studentcooperationtools.domain.slide.controller;

import com.google.api.client.auth.oauth2.Credential;
import com.stool.studentcooperationtools.domain.api.ApiResponse;
import com.stool.studentcooperationtools.domain.slide.controller.response.SlideFindResponse;
import com.stool.studentcooperationtools.domain.slide.service.SlideService;
import com.stool.studentcooperationtools.security.credential.GoogleCredentialProvider;
import com.stool.studentcooperationtools.security.oauth2.dto.SessionMember;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SlideApiController {

    private final SlideService slideService;
    private final GoogleCredentialProvider credentialProvider;

    @GetMapping("/api/v1/presentation/{presentationId}/slides")
    public ApiResponse<SlideFindResponse> findSlides(@PathVariable("presentationId") Long presentationId){
        SlideFindResponse result = slideService.findSlides(presentationId);
        return ApiResponse.of(HttpStatus.OK,result);
    }

    @PostMapping("/api/v1/presentation/{presentationId}/slides-compare")
    public ApiResponse<Boolean> updateNewSlides(@PathVariable("presentationId") Long presentationId, SessionMember member) {
        Credential credential = credentialProvider.getCredential();
        boolean result = slideService.compareSlides(presentationId, credential);
        return ApiResponse.of(HttpStatus.OK,result);
    }

    @GetMapping("/api/v1/presentation/{presentationId}/first-page")
    public ApiResponse<String> getFirstPage(@PathVariable("presentationId") Long presentationId){
        String firstPage = slideService.findFirstPage(presentationId);
        return ApiResponse.of(HttpStatus.OK,firstPage);
    }

    @PostMapping("/api/v1/presentation/{presentationId}/slides-sync")
    public ApiResponse<Boolean> syncSlides(@PathVariable("presentationId") Long presentationId, SessionMember member) {
        Credential credential = credentialProvider.getCredential();
        boolean result = slideService.syncSlides(presentationId, credential);
        return ApiResponse.of(HttpStatus.OK,result);
    }
}
