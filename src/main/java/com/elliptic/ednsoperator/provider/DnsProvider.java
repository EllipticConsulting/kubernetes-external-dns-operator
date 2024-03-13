/*
 * Copyright (c) 2024 Elliptic Consulting and as indicated by the @author tags
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.elliptic.ednsoperator.provider;

import com.elliptic.ednsoperator.resources.ExternalDnsSpec;

import java.util.List;

/**
 * Interface for DNS providers
 * @author Kobus Grobler
 */
public interface DnsProvider {
    List<ExternalDnsSpec> getRecordsByHost(String zone, String host) throws DnsProviderException;
    ExternalDnsSpec createRecord(ExternalDnsSpec specIn) throws DnsProviderException;
    ExternalDnsSpec updateRecord(ExternalDnsSpec specIn) throws DnsProviderException;
    void deleteRecord(ExternalDnsSpec specIn) throws DnsProviderException;
    default ExternalDnsSpec createManagedRecord(ExternalDnsSpec specIn) throws DnsProviderException {
        return createRecord(specIn);
    }

    default void deleteManagedRecord(ExternalDnsSpec specIn) throws DnsProviderException {
        this.deleteRecord(specIn);
    }
}
