/*
 * Copyright (c) 2024 Elliptic Consulting and as indicated by the @author tags
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.elliptic.ednsoperator.provider;

public class DnsProviderException extends RuntimeException {
    public DnsProviderException(String message) {
        super(message);
    }

    public DnsProviderException(String message, Throwable cause) {
        super(message, cause);
    }

    public DnsProviderException(Throwable cause) {
        super(cause);
    }

    public DnsProviderException() {
        super();
    }
}
