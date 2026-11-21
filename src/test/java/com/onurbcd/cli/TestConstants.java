package com.onurbcd.cli;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestConstants {

    public static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID SOURCE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID CATEGORY_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    public static final UUID DOCUMENT_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
}
