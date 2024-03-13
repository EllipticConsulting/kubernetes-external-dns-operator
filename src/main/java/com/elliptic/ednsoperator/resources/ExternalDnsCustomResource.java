/*
 * Copyright (c) 2024 Elliptic Consulting and as indicated by the @author tags
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.elliptic.ednsoperator.resources;

import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;
import io.fabric8.kubernetes.model.annotation.Kind;

@Group("elliptic.external.dns")
@Kind("ExternalDns")
@Version("v1")
public class ExternalDnsCustomResource extends CustomResource<ExternalDnsSpec, ExternalDnsStatus> implements Namespaced {
}
