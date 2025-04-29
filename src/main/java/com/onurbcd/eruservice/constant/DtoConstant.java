package com.onurbcd.eruservice.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DtoConstant {

    public static final int SIZE_3 = 3;
    public static final int SIZE_5 = 5;
    public static final int SIZE_7 = 7;
    public static final int SIZE_32 = 32;
    public static final int SIZE_50 = 50;
    public static final int SIZE_100 = 100;
    public static final int SIZE_150 = 150;
    public static final int SIZE_250 = 250;
    public static final int SIZE_255 = 255;
    public static final int SIZE_2048 = 2048;
    public static final String REGEXP_URL = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
    public static final String POSITIVE_AMOUNT_MIN = "0.0001";
    public static final String AMOUNT_MIN = "-999999999999999";
    public static final String AMOUNT_MAX = "999999999999999";
    public static final String REGEXP_PATH = "^[a-z|A-Z|0-9|-]+$";
}
