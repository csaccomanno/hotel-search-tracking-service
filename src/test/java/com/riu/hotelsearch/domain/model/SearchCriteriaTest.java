package com.riu.hotelsearch.domain.model;

import com.riu.hotelsearch.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchCriteriaTest {

    private static final LocalDate CHECK_IN = LocalDate.of(2026, 9, 10);
    private static final LocalDate CHECK_OUT = LocalDate.of(2026, 9, 12);

    @Test
    void createsImmutableNormalizedCriteria() {
        List<Integer> sourceAges = new ArrayList<>(List.of(30, 29, 1, 3));

        SearchCriteria criteria = new SearchCriteria(" 1234aBc ", CHECK_IN, CHECK_OUT, sourceAges);
        sourceAges.add(99);

        assertAll(
                () -> assertEquals("1234aBc", criteria.hotelId()),
                () -> assertEquals(List.of(30, 29, 1, 3), criteria.ages()),
                () -> assertThrows(UnsupportedOperationException.class, () -> criteria.ages().add(5)));
    }

    @Test
    void ageOrderAffectsEquality() {
        SearchCriteria first = valid(List.of(30, 29, 1, 3));
        SearchCriteria reordered = valid(List.of(3, 29, 30, 1));

        assertNotEquals(first, reordered);
    }

    @Test
    void rejectsInvalidFields() {
        assertAll(
                () -> assertInvalid(() -> new SearchCriteria(null, CHECK_IN, CHECK_OUT, List.of(1))),
                () -> assertInvalid(() -> new SearchCriteria(" ", CHECK_IN, CHECK_OUT, List.of(1))),
                () -> assertInvalid(() -> new SearchCriteria("hotel", null, CHECK_OUT, List.of(1))),
                () -> assertInvalid(() -> new SearchCriteria("hotel", CHECK_IN, null, List.of(1))),
                () -> assertInvalid(() -> new SearchCriteria("hotel", CHECK_OUT, CHECK_IN, List.of(1))),
                () -> assertInvalid(() -> new SearchCriteria("hotel", CHECK_IN, CHECK_IN, List.of(1))),
                () -> assertInvalid(() -> new SearchCriteria("hotel", CHECK_IN, CHECK_OUT, null)),
                () -> assertInvalid(() -> new SearchCriteria("hotel", CHECK_IN, CHECK_OUT, List.of())),
                () -> assertInvalid(() -> new SearchCriteria("hotel", CHECK_IN, CHECK_OUT, List.of(-1))),
                () -> assertInvalid(() -> new SearchCriteria("hotel", CHECK_IN, CHECK_OUT, listWithNull())));
    }

    private SearchCriteria valid(List<Integer> ages) {
        return new SearchCriteria("hotel", CHECK_IN, CHECK_OUT, ages);
    }

    private List<Integer> listWithNull() {
        List<Integer> ages = new ArrayList<>();
        ages.add(null);
        return ages;
    }

    private void assertInvalid(Runnable constructor) {
        assertThrows(DomainValidationException.class, constructor::run);
    }
}

