package com.stool.studentcooperationtools.domain.part;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PartTest {

    @DisplayName("역할의 이름을 빈칸 혹은 Null값으로 변경할 경우 에러가 발생한다.")
    @Test
    void test(){
        //given
        String invalidPartName = " ";
        Part part =Part.builder()
                .partName("역할의 이름")
                .build();
        //when
        //then
        assertThatThrownBy(() -> part.update(null,invalidPartName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageMatching("역할 이름은 필수입니다.");
    }

}