/*
 * Copyright (c) 2024 Elliptic Consulting and as indicated by the @author tags
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.elliptic.ednsoperator.reconcilers;

import com.elliptic.ednsoperator.resources.ExternalDnsServiceDependentResource;
import io.fabric8.kubernetes.api.model.Service;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Dependent;
import io.javaoperatorsdk.operator.processing.event.source.filter.GenericFilter;
import lombok.extern.slf4j.Slf4j;

import static com.elliptic.ednsoperator.reconcilers.ResourceAnnotations.haveExternalDnsAnnotations;

/**
 * Reconciler for annotations on Service resources
 *
 * @author Kobus Grobler
 */
@ControllerConfiguration(genericFilter = ServiceReconciler.class, dependents = {
        @Dependent(type = ExternalDnsServiceDependentResource.class)
})
@Slf4j
public class ServiceReconciler implements
        Reconciler<Service>,
        GenericFilter<Service> {

    public UpdateControl<Service> reconcile(Service primary, Context<Service> context) {
        log.info("Reconciling DNS entries found on Service: {}, {}", primary.getMetadata().getName(), primary.getMetadata().getNamespace());
        return UpdateControl.noUpdate();
    }

    @Override
    public boolean accept(Service s) {
        return haveExternalDnsAnnotations(s.getMetadata().getAnnotations());
    }
}
