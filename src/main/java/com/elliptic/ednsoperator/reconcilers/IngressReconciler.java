/*
 * Copyright (c) 2024 Elliptic Consulting and as indicated by the @author tags
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.elliptic.ednsoperator.reconcilers;

import com.elliptic.ednsoperator.resources.ExternalDnsIngressDependentResource;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Dependent;
import io.javaoperatorsdk.operator.processing.event.source.filter.GenericFilter;
import lombok.extern.slf4j.Slf4j;

import static com.elliptic.ednsoperator.reconcilers.ResourceAnnotations.haveExternalDnsAnnotations;

/**
 * Reconciler for annotations on Ingress resources
 *
 * @author Kobus Grobler
 */
@ControllerConfiguration(genericFilter = IngressReconciler.class, dependents = {
        @Dependent(type = ExternalDnsIngressDependentResource.class)
})
@Slf4j
public class IngressReconciler implements
        Reconciler<Ingress>,
        GenericFilter<Ingress> {

    public UpdateControl<Ingress> reconcile(Ingress primary, Context<Ingress> context) {
        log.info("Reconciling DNS entries found on Ingress: {}, {}", primary.getMetadata().getName(), primary.getMetadata().getNamespace());
        return UpdateControl.noUpdate();
    }

    @Override
    public boolean accept(Ingress s) {
        return haveExternalDnsAnnotations(s.getMetadata().getAnnotations());
    }
}
