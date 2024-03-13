/*
 * Copyright (c) 2024 Elliptic Consulting and as indicated by the @author tags
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.elliptic.ednsoperator;

import org.junit.jupiter.api.BeforeAll;

public class TestBase {
    protected static String zoneId;
    protected static String testDomain;

    @BeforeAll
    static void setup() {
        zoneId = System.getenv().get("junit.zoneid");
        testDomain = System.getenv().get("junit.domain");
        if (zoneId == null || testDomain == null) {
            throw new IllegalStateException("junit.zoneid and junit.domain must be set");
        }
    }
}
