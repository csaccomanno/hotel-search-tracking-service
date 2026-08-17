package com.riu.hotelsearch.infrastructure.adapter.out.persistence;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AgesHashTest {

    @Test
    void isDeterministicAndOrderSensitive() {
        String first = AgesHash.calculate(List.of(30, 29, 1, 3));
        String same = AgesHash.calculate(List.of(30, 29, 1, 3));
        String reordered = AgesHash.calculate(List.of(3, 29, 30, 1));

        assertAll(
                () -> assertEquals(first, same),
                () -> assertEquals(64, first.length()),
                () -> assertNotEquals(first, reordered));
    }
}
