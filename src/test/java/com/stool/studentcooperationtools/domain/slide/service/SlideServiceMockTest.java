package com.stool.studentcooperationtools.domain.slide.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.slides.v1.Slides;
import com.google.api.services.slides.v1.model.Presentation;
import com.google.api.services.slides.v1.model.Page;
import com.google.api.services.slides.v1.model.Thumbnail;
import com.stool.studentcooperationtools.domain.presentation.repository.PresentationRepository;
import com.stool.studentcooperationtools.domain.room.Room;
import com.stool.studentcooperationtools.domain.room.repository.RoomRepository;
import com.stool.studentcooperationtools.domain.script.repository.ScriptRepository;
import com.stool.studentcooperationtools.domain.slide.SlidesFactory;
import com.stool.studentcooperationtools.domain.slide.repository.SlideRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class SlideServiceMockTest {

    @Autowired
    private PresentationRepository presentationRepository;

    @Autowired
    private SlideRepository slideRepository;

    @Autowired
    private ScriptRepository scriptRepository;

    @MockBean
    private SlidesFactory slidesFactory;

    @MockBean
    private Credential credential;

    @MockBean
    private Slides slidesService;

    @Autowired
    private SlideService slideService;
    @Autowired
    private RoomRepository roomRepository;

    @Test
    @DisplayName("발표 자료의 모든 슬라이드를 저장")
    void updateSlides() throws IOException, GeneralSecurityException {
        // Given
        String presentationPath = "presentationPath";
        String objectId = "objectId";
        String thumbnailUrl = "thumbnailUrl";
        Room room = Room.builder()
                .title("t")
                .password("1234")
                .build();
        roomRepository.save(room);
        // Mock Presentation object
        com.stool.studentcooperationtools.domain.presentation.Presentation presentation =
                com.stool.studentcooperationtools.domain.presentation.Presentation.builder()
                        .presentationPath(presentationPath)
                        .room(room)
                        .build();
        presentationRepository.save(presentation);
        // Mock Google Slides API objects
        Presentation mockGooglePresentation = new Presentation()
                .setSlides(List.of(new Page().setObjectId(objectId)));
        Thumbnail mockThumbnail = new Thumbnail().setContentUrl(thumbnailUrl);

        // Presentation and Repository Mocks
        when(slidesFactory.createSlidesService(credential)).thenReturn(slidesService);
        when(slidesService.presentations()).thenReturn(mock(Slides.Presentations.class));
        when(slidesService.presentations().get(presentationPath)).thenReturn(mock(Slides.Presentations.Get.class));
        when(slidesService.presentations().get(presentationPath).execute()).thenReturn(mockGooglePresentation);
        when(slidesService.presentations().pages()).thenReturn(mock(Slides.Presentations.Pages.class));
        when(slidesService.presentations().pages().getThumbnail(presentationPath, objectId))
                .thenReturn(mock(Slides.Presentations.Pages.GetThumbnail.class));
        when(slidesService.presentations().pages().getThumbnail(presentationPath, objectId).execute()).thenReturn(mockThumbnail);

        // When
        boolean result = slideService.updateSlides(1L, credential);

        // Then
        assertTrue(result);
        assertThat(slideRepository.findSlidesAndScriptsByPresentationId(1L))
                .isNotEmpty();
    }

    @Test
    @DisplayName("발표 자료의 정보가 올바르지 않으면 에러 발생")
    void updateSlidesWithInvalidPpt(){
        //when
        Long invalidPresentationId = 1L;
        //given
        //then
        assertThrows(IllegalArgumentException.class, () -> slideService.updateSlides(invalidPresentationId, credential));
    }
}