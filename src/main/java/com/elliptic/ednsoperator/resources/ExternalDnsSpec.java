/*
 * Copyright (c) 2024 Elliptic Consulting and as indicated by the @author tags
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.elliptic.ednsoperator.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.fabric8.generator.annotation.Required;

/**
 * ExternalDnsSpec defines the desired state of an ExternalDns entry
 * @author Kobus Grobler
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class ExternalDnsSpec {
    @Required
    private String host;
    /**
     * record_type: CNAME, A, AAAA or TXT
     */
    @Required
    @JsonProperty("record_type")
    private String recordType;
    @Required
    private String value;
    @Builder.Default
    private int ttl = 300;
    /**
     * zone: zone id or reference
     */
    @Required
    private String zone;
    /**
     * Provider to use, default is aws
     * aws, gcp, azure etc.
     */
    @Builder.Default
    private String provider = "aws";
}
