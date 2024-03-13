/*
 * Copyright (c) 2024 Elliptic Consulting and as indicated by the @author tags
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.elliptic.ednsoperator.provider;

/**
 * Factory class to create DNS providers
 * @author Kobus Grobler
 */
public class ProviderFactory {
    public static final String AWS = "aws";
    private ProviderFactory() {
    }
    public static DnsProvider getProvider(String provider) {
        if (provider.equals(AWS)) {
            return new AwsDnsProvider();
        } else {
            throw new IllegalArgumentException("Provider not supported: " + provider);
        }
    }
}
