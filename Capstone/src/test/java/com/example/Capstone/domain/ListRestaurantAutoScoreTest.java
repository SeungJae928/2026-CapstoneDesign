package com.example.Capstone.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ListRestaurantAutoScoreTest {

    @Test
    @DisplayName("리스트 식당 생성 시 autoScore는 문서 기준 공식으로 계산된다")
    void builderCalculatesAutoScoreWithExpectedWeights() {
        ListRestaurant listRestaurant = ListRestaurant.builder()
                .userList(null)
                .restaurant(null)
                .tasteScore(new BigDecimal("8.0"))
                .valueScore(new BigDecimal("6.0"))
                .moodScore(new BigDecimal("7.0"))
                .build();

        assertBigDecimalEquals("74.0", listRestaurant.getAutoScore());
    }

    @Test
    @DisplayName("점수 수정 시 autoScore는 동일한 문서 기준 공식으로 다시 계산된다")
    void updateScoreRecalculatesAutoScoreWithSameWeights() {
        ListRestaurant listRestaurant = ListRestaurant.builder()
                .userList(null)
                .restaurant(null)
                .tasteScore(new BigDecimal("5.0"))
                .valueScore(new BigDecimal("5.0"))
                .moodScore(new BigDecimal("5.0"))
                .build();

        listRestaurant.updateScore(
                new BigDecimal("9.0"),
                new BigDecimal("4.0"),
                new BigDecimal("3.0")
        );

        assertBigDecimalEquals("68.0", listRestaurant.getAutoScore());
    }

    private void assertBigDecimalEquals(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
