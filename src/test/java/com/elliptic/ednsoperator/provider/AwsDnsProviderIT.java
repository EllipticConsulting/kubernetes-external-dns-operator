/*
 * Copyright (c) 2024 Elliptic Consulting and as indicated by the @author tags
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.elliptic.ednsoperator.provider;

import com.elliptic.ednsoperator.TestBase;
import com.elliptic.ednsoperator.resources.ExternalDnsSpec;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AwsDnsProviderIT extends TestBase {

    @Test
    void testRecords() {
        var provider = ProviderFactory.getProvider(ProviderFactory.AWS);
        assertThrows(DnsProviderException.class, () -> provider.getRecordsByHost("invalidzoneid", "deadbeef."+testDomain));
        List<ExternalDnsSpec> recs = provider.getRecordsByHost(zoneId, "junit."+testDomain);
        assertTrue(recs.isEmpty());
        var newRec = new ExternalDnsSpec("junit."+testDomain, "A","8.8.8.8" , 10, zoneId, ProviderFactory.AWS);
        provider.createManagedRecord(newRec);
        recs = provider.getRecordsByHost(zoneId, "junit."+testDomain);
        assertEquals(1, recs.size());
        var updatedRec = provider.updateRecord(newRec.toBuilder().value("10.10.10.10").build());
        provider.deleteManagedRecord(updatedRec);
        newRec = new ExternalDnsSpec("junit."+testDomain, "CNAME","int."+testDomain , 10, zoneId, ProviderFactory.AWS);
        var rec = provider.createManagedRecord(newRec);
        provider.deleteManagedRecord(rec);
    }
}
