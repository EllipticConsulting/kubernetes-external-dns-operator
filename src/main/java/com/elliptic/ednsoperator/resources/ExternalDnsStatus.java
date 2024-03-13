/*
 * Copyright (c) 2024 Elliptic Consulting and as indicated by the @author tags
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.elliptic.ednsoperator.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.javaoperatorsdk.operator.api.ObservedGenerationAwareStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ExternalDnsStatus defines the observed state of an ExternalDns entry
 * @author Kobus Grobler
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExternalDnsStatus extends ObservedGenerationAwareStatus {
    public void updateFromSpec(ExternalDnsSpec spec) {
        this.host = spec.getHost();
        this.provider = spec.getProvider();
        this.recordType = spec.getRecordType();
        this.value = spec.getValue();
        this.ttl = spec.getTtl();
        this.zone = spec.getZone();
    }

    private String provider;
    private String host;
    @JsonProperty("record_type")
    private String recordType;
    private String value;
    private int ttl;
    private String zone;

    private String lastStatus;
    private String errorMessage;
}
