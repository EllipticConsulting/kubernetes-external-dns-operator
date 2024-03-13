/*
 * Copyright (c) 2024 Elliptic Consulting and as indicated by the @author tags
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.elliptic.ednsoperator;

import com.elliptic.ednsoperator.provider.ProviderFactory;
import com.elliptic.ednsoperator.reconcilers.ExternalDnsManagerReconciler;
import com.elliptic.ednsoperator.reconcilers.IngressReconciler;
import com.elliptic.ednsoperator.reconcilers.ServiceReconciler;
import com.elliptic.ednsoperator.resources.ExternalDnsCustomResource;
import com.elliptic.ednsoperator.resources.ExternalDnsSpec;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.IngressBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.IngressRuleBuilder;
import io.javaoperatorsdk.operator.junit.LocallyRunOperatorExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class ExternalDnsManagerReconcilerIT extends TestBase {

    public static final String RESOURCE_NAME = "test1";

    @RegisterExtension
    LocallyRunOperatorExtension extension =
            LocallyRunOperatorExtension.builder()
                    .withReconciler(ExternalDnsManagerReconciler.class)
                    .withReconciler(ServiceReconciler.class)
                    .withReconciler(IngressReconciler.class)
                    .build();

    @Test
    void testServiceCRUDOperations() {
        var svc = extension.create(new ServiceBuilder()
                .withNewMetadata()
                    .withName(RESOURCE_NAME)
                    .addToAnnotations("elliptic.external.dns/hostname", "junit."+testDomain)
                    .addToAnnotations("elliptic.external.dns/record.type", "A")
                    .addToAnnotations("elliptic.external.dns/value", "8.8.8.8")
                    .addToAnnotations("elliptic.external.dns/ttl", "10")
                    .addToAnnotations("elliptic.external.dns/zoneid", zoneId)
                    .addToAnnotations("elliptic.external.dns/provider", ProviderFactory.AWS)
                .endMetadata()
                .withNewSpec()
                .withType("LoadBalancer")
                .addNewPort().withPort(80).withNewTargetPort(80).endPort()
                .endSpec()
                .build());

        await().untilAsserted(() -> {
            var rs = extension.get(ExternalDnsCustomResource.class, RESOURCE_NAME);
            assertThat(rs).isNotNull();
            assertThat(rs.getStatus()).isNotNull();
            assertThat(rs.getStatus().getValue()).isEqualTo("8.8.8.8");
        });

        svc.getMetadata().getAnnotations().put("elliptic.external.dns/value","10.10.10.10");
        svc = extension.replace(svc);
        await().untilAsserted(() -> {
            var rs = extension.get(ExternalDnsCustomResource.class, RESOURCE_NAME);
            assertThat(rs).isNotNull();
            assertThat(rs.getStatus().getValue()).isEqualTo("10.10.10.10");
        });

        // Use load balancer IP
        svc.getMetadata().getAnnotations().remove("elliptic.external.dns/value");
        svc = extension.replace(svc);
        await().untilAsserted(() -> {
            var rs = extension.get(ExternalDnsCustomResource.class, RESOURCE_NAME);
            assertThat(rs).isNotNull();
            assertThat(rs.getStatus()).isNotNull();
            assertThat(rs.getStatus().getValue()).isNotNull();
        });

        svc.getMetadata().getAnnotations().put("elliptic.external.dns/ttl","60");
        svc = extension.replace(svc);
        await().untilAsserted(() -> {
            var rs = extension.get(ExternalDnsCustomResource.class, RESOURCE_NAME);
            assertThat(rs).isNotNull();
            assertThat(rs.getStatus().getTtl()).isEqualTo(60);
        });

        svc.getMetadata().getAnnotations().put("elliptic.external.dns/hostname","junit2."+testDomain);
        svc = extension.replace(svc);

        await().untilAsserted(() -> {
            var rs = extension.get(ExternalDnsCustomResource.class, RESOURCE_NAME);
            assertThat(rs).isNotNull();
            assertThat(rs.getStatus().getErrorMessage()).isNotNull();
            assertThat(rs.getStatus().getHost()).isEqualTo("junit."+testDomain);
        });

        extension.delete(svc);

        await().untilAsserted(() -> {
            var dns = extension.get(ExternalDnsCustomResource.class, RESOURCE_NAME);
            assertThat(dns).isNull();
        });
    }

    @Test
    void testIngressCRUDOperations() {
        var ingress = extension.create(new IngressBuilder()
                .withNewMetadata()
                .withName(RESOURCE_NAME)
                .addToAnnotations("elliptic.external.dns/hostname", "junit."+testDomain)
                .addToAnnotations("elliptic.external.dns/record.type", "A")
                .addToAnnotations("elliptic.external.dns/value", "8.8.8.8")
                .addToAnnotations("elliptic.external.dns/ttl", "10")
                .addToAnnotations("elliptic.external.dns/zoneid", zoneId)
                .addToAnnotations("elliptic.external.dns/provider", ProviderFactory.AWS)
                .endMetadata()
                .withNewSpec()
                    .withIngressClassName("nginx")
                    .addToRules(new IngressRuleBuilder()
                            .withHost("junit."+testDomain)
                            .withNewHttp()
                            .addNewPath()
                            .withPath("/test")
                                .withPathType("Prefix")
                                .withNewBackend()
                                    .withNewService()
                                        .withName("test")
                                        .withNewPort()
                                            .withNumber(80)
                                        .endPort()
                                    .endService()
                                .endBackend()
                            .endPath()
                            .endHttp().build())
                .endSpec()
                .build());

        await().untilAsserted(() -> {
            var rs = extension.get(ExternalDnsCustomResource.class, RESOURCE_NAME);
            assertThat(rs).isNotNull();
            assertThat(rs.getStatus()).isNotNull();
            assertThat(rs.getStatus().getValue()).isEqualTo("8.8.8.8");
        });

        extension.delete(ingress);

        await().untilAsserted(() -> {
            var dns = extension.get(ExternalDnsCustomResource.class, RESOURCE_NAME);
            assertThat(dns).isNull();
        });
    }

    ExternalDnsCustomResource testResource() {
        var resource = new ExternalDnsCustomResource();
        resource.setMetadata(new ObjectMetaBuilder()
                .withName(RESOURCE_NAME)
                .build());
        resource.setSpec(new ExternalDnsSpec("junit."+testDomain, "A","8.8.8.8" , 10, zoneId, ProviderFactory.AWS));
        return resource;
    }
}
