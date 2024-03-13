/*
 * Copyright (c) 2024 Elliptic Consulting and as indicated by the @author tags
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.elliptic.ednsoperator.resources;

import io.fabric8.kubernetes.api.model.Service;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import lombok.extern.slf4j.Slf4j;

/**
 * ExternalDnsCustomResource that is dependent on a Service resource
 * @author Kobus Grobler
 */
@KubernetesDependent
@Slf4j
public class ExternalDnsServiceDependentResource
        extends CRUDKubernetesDependentResource<ExternalDnsCustomResource, Service> {

    public ExternalDnsServiceDependentResource() {
        super(ExternalDnsCustomResource.class);
    }

    @Override
    public void delete(Service primary, Context<Service> context) {
        log.info("Deleting ExternalDnsCustomResource for " + primary.getMetadata().getName());
    }

    @Override
    protected ExternalDnsCustomResource desired(Service primary,
                                Context<Service> context) {
        var secondary = context.getSecondaryResource(ExternalDnsCustomResource.class);
        log.info("Creating ExternalDnsCustomResource for {}, {}" , primary.getStatus(), secondary.isPresent());
        String lbIp = null;
        if (!primary.getStatus().getLoadBalancer().getIngress().isEmpty()) {
            lbIp = primary.getStatus().getLoadBalancer().getIngress().get(0).getIp();
        }
        return ExternalDnsIngressDependentResource.fromResource(primary, lbIp);
    }
}