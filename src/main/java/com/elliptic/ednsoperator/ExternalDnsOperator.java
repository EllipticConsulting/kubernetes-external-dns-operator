/*
 * Copyright (c) 2024 Elliptic Consulting and as indicated by the @author tags
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.elliptic.ednsoperator;

import com.elliptic.ednsoperator.probes.LivenessHandler;
import com.elliptic.ednsoperator.probes.StartupHandler;
import com.elliptic.ednsoperator.reconcilers.ExternalDnsManagerReconciler;
import com.elliptic.ednsoperator.reconcilers.IngressReconciler;
import com.elliptic.ednsoperator.reconcilers.ServiceReconciler;
import com.sun.net.httpserver.HttpServer;
import io.javaoperatorsdk.operator.Operator;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Main class for the ExternalDns Operator.
 * @author Kobus Grobler
 *
 */
@Slf4j
public class ExternalDnsOperator {

    public static void main(String[] args) throws IOException {
        Operator operator = new Operator();
        operator.register(new ExternalDnsManagerReconciler());
        operator.register(new ServiceReconciler());
        operator.register(new IngressReconciler());
        operator.start();
        log.info("Operator started.");

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/startup", new StartupHandler(operator));
        server.createContext("/healthz", new LivenessHandler(operator));
        server.setExecutor(null);
        server.start();
    }
}
