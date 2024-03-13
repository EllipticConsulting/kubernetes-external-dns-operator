/*
 * Copyright (c) 2024 Elliptic Consulting and as indicated by the @author tags
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.elliptic.ednsoperator.reconcilers;

import java.util.Map;

public class ResourceAnnotations {
    public static final String EXTERNAL_DNS_HOSTNAME = "elliptic.external.dns/hostname";
    public static final String EXTERNAL_DNS_VALUE = "elliptic.external.dns/value";
    public static final String EXTERNAL_DNS_ZONEID = "elliptic.external.dns/zoneid";
    public static final String EXTERNAL_DNS_TTL = "elliptic.external.dns/ttl";
    public static final String EXTERNAL_DNS_PROVIDER = "elliptic.external.dns/provider";
    public static final String EXTERNAL_DNS_RECORD_TYPE = "elliptic.external.dns/record.type";

    /**
     * Check if the resource has the required annotations for external dns
     * @param annotations the annotations to check
     * @return true if the annotations are present
     */
    static boolean haveExternalDnsAnnotations(Map<String, String> annotations) {
        return annotations != null
                && annotations.containsKey(EXTERNAL_DNS_HOSTNAME)
                && annotations.containsKey(EXTERNAL_DNS_ZONEID)
                && annotations.containsKey(EXTERNAL_DNS_RECORD_TYPE);
    }
}
