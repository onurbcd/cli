package com.onurbcd.cli;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestClock {

    public static final LocalDate DEFAULT_DATE = LocalDate.of(2025, 7, 10);

    /**
     * Frozen at the start of {@link #DEFAULT_DATE} (UTC), so nothing depends on the real date.
     */
    public static final Clock DEFAULT = fixedAt(DEFAULT_DATE);

    public static Clock fixedAt(LocalDate date) {
        return Clock.fixed(date.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
    }
}
