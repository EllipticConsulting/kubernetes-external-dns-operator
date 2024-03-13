/*
 * Copyright (c) 2024 Elliptic Consulting and as indicated by the @author tags
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.elliptic.ednsoperator.reconcilers;

import com.elliptic.ednsoperator.provider.DnsProvider;
import com.elliptic.ednsoperator.provider.DnsProviderException;
import com.elliptic.ednsoperator.provider.ProviderFactory;
import com.elliptic.ednsoperator.resources.ExternalDnsCustomResource;
import com.elliptic.ednsoperator.resources.ExternalDnsSpec;
import com.elliptic.ednsoperator.resources.ExternalDnsStatus;
import io.javaoperatorsdk.operator.api.reconciler.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

/**
 * Reconciler for the ExternalDnsCRD
 *
 * @author Kobus Grobler
 */
@ControllerConfiguration
@Slf4j
public class ExternalDnsManagerReconciler implements
        Reconciler<ExternalDnsCustomResource>,
        Cleaner<ExternalDnsCustomResource>,
        ErrorStatusHandler<ExternalDnsCustomResource> {

    public UpdateControl<ExternalDnsCustomResource> reconcile(ExternalDnsCustomResource primary,
                                                     Context<ExternalDnsCustomResource> context) {

        log.info("Reconciling " + primary.getMetadata().getName());
        final ExternalDnsSpec spec = primary.getSpec();
        var status = primary.getStatus();
        if (status == null) {
            status = new ExternalDnsStatus();
            log.info("Setting new status for " + primary.getMetadata().getName());
            primary.setStatus(status);
        } else {
            if (spec.getHost()!=null &&
                    (!spec.getHost().equals(status.getHost())
                    || !spec.getRecordType().equals(status.getRecordType())
                    || !spec.getProvider().equals(status.getProvider())
                    || !spec.getZone().equals(status.getZone()))
            ) {
                String msg = "Cannot update provider, host, record type or zone after record creation.";
                log.warn(msg);
                status.setErrorMessage(msg);
                return UpdateControl.patchStatus(primary);
            }
        }

        DnsProvider provider = ProviderFactory.getProvider(spec.getProvider());
        List<ExternalDnsSpec> recs = provider.getRecordsByHost(spec.getZone(), spec.getHost());
        if (recs.isEmpty()) {
            log.info("No records found for host, creating: {}", spec.getHost());
            provider.createManagedRecord(spec);
            status.setLastStatus("DNS record created for "+spec.getHost());
        } else {
            log.info("{} records found for host, updating: {}", recs.size(), spec.getHost());
            provider.updateRecord(spec);
            status.setLastStatus("DNS record updated for " +spec.getHost());
        }
        status.setErrorMessage(null);
        status.updateFromSpec(spec);
        return UpdateControl.patchStatus(primary);
    }

    @Override
    public DeleteControl cleanup(ExternalDnsCustomResource resource, Context<ExternalDnsCustomResource> context) {
        log.info("Cleaning up " + resource.getMetadata().getName());
        ExternalDnsStatus status = resource.getStatus();
        if (status == null) {
            log.info("No status found for " + resource.getMetadata().getName());
            return DeleteControl.defaultDelete();
        }
        if (status.getHost() == null) {
            log.info("No host found for " + resource.getMetadata().getName());
            return DeleteControl.defaultDelete();
        }
        DnsProvider provider = ProviderFactory.getProvider(status.getProvider());
        try {
            provider.deleteManagedRecord(new ExternalDnsSpec(status.getHost(), status.getRecordType(), status.getValue(),
                    status.getTtl(), status.getZone(), status.getProvider()));
        } catch (DnsProviderException e) {
            log.warn("Error during cleanup of {}, {}", resource.getMetadata().getName(), resource.getSpec(), e);
        }
        return DeleteControl.defaultDelete();
    }

    @Override
    public ErrorStatusUpdateControl<ExternalDnsCustomResource> updateErrorStatus(
            ExternalDnsCustomResource resource, Context<ExternalDnsCustomResource> context, Exception e) {
        log.error("Error during reconciliation of {}, {}", resource.getMetadata().getName(), resource.getSpec(), e);
        var status = Objects.requireNonNullElse(resource.getStatus(), new ExternalDnsStatus());
        status.setErrorMessage("Error: " + e.getMessage());
        return ErrorStatusUpdateControl.updateStatus(resource);
    }
}
