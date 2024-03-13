/*
 * Copyright (c) 2024 Elliptic Consulting and as indicated by the @author tags
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.elliptic.ednsoperator.probes;

import io.javaoperatorsdk.operator.Operator;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

public class LivenessHandler implements HttpHandler {

    private final Operator operator;

    public LivenessHandler(Operator operator) {
        this.operator = operator;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        if (operator.getRuntimeInfo().allEventSourcesAreHealthy()) {
            StartupHandler.sendMessage(httpExchange, 200, "healthy");
        } else {
            StartupHandler.sendMessage(httpExchange, 400, "an event source is not healthy");
        }
    }
}
