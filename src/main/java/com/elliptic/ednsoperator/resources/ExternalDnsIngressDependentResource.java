/*
 * Copyright (c) 2024 Elliptic Consulting and as indicated by the @author tags
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.elliptic.ednsoperator.resources;

import com.elliptic.ednsoperator.provider.ProviderFactory;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import lombok.extern.slf4j.Slf4j;

import static com.elliptic.ednsoperator.reconcilers.ResourceAnnotations.*;

/**
 * ExternalDnsCustomResource that is dependent on a Ingress resource
 * @author Kobus Grobler
 */
@KubernetesDependent
@Slf4j
public class ExternalDnsIngressDependentResource
        extends CRUDKubernetesDependentResource<ExternalDnsCustomResource, Ingress> {

    public ExternalDnsIngressDependentResource() {
        super(ExternalDnsCustomResource.class);
    }

    @Override
    public void delete(Ingress primary, Context<Ingress> context) {
        log.info("Deleting ExternalDnsCustomResource for " + primary.getMetadata().getName());
    }

    @Override
    protected ExternalDnsCustomResource desired(Ingress primary,
                                Context<Ingress> context) {
        var secondary = context.getSecondaryResource(ExternalDnsCustomResource.class);
        log.info("Creating ExternalDnsCustomResource for {}, {}" , primary.getStatus(), secondary.isPresent());
        String lbIp = null;
        if (!primary.getStatus().getLoadBalancer().getIngress().isEmpty()) {
            lbIp = primary.getStatus().getLoadBalancer().getIngress().get(0).getIp();
        }
        return fromResource(primary, lbIp);
    }

    public static ExternalDnsCustomResource fromResource(HasMetadata primary, String lbIp) {
        var annotations = primary.getMetadata().getAnnotations();
        lbIp = annotations.getOrDefault(EXTERNAL_DNS_VALUE, lbIp);
        if (lbIp == null) {
            log.warn("No LB IP ready for service and none specified." + primary.getMetadata().getName());
            return null;
        }
        log.info("Using dns record value: {}", lbIp);
        var rs = new ExternalDnsCustomResource();
        rs.setMetadata(new ObjectMetaBuilder()
                .withName(primary.getMetadata().getName())
                .withNamespace(primary.getMetadata().getNamespace())
                .build());
        rs.setSpec(new ExternalDnsSpec(
                annotations.get(EXTERNAL_DNS_HOSTNAME),
                annotations.getOrDefault(EXTERNAL_DNS_RECORD_TYPE, "A"),
                lbIp,
                Integer.parseInt(annotations.getOrDefault(EXTERNAL_DNS_TTL,"60")),
                annotations.get(EXTERNAL_DNS_ZONEID),
                annotations.getOrDefault(EXTERNAL_DNS_PROVIDER, ProviderFactory.AWS)));
        return rs;
    }
}