/*
 * Copyright (c) 2024 Elliptic Consulting and as indicated by the @author tags
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.elliptic.ednsoperator.provider;

import com.elliptic.ednsoperator.resources.ExternalDnsSpec;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AWS DNS provider implementation
 * @author Kobus Grobler
 */
@Slf4j
public class AwsDnsProvider implements DnsProvider {

    public AwsDnsProvider() {
    }

    public Route53Client getAWSClient() {
        return Route53Client.builder()
                .region(Region.AWS_GLOBAL)
                .build();
    }

    @Override
    public List<ExternalDnsSpec> getRecordsByHost(String zone, String host) {
        try (Route53Client client = getAWSClient()) {
            List<ResourceRecordSet> records = listResourceRecord(client, zone, host);
            if (records.isEmpty()) {
                log.info("No records found for host: {}", host);
                return Collections.emptyList();
            }
            List<ExternalDnsSpec> list = new ArrayList<>();
            for (ResourceRecordSet set : records) {
                log.info("Retrieved record {}, {}, {}, [{}]", set.name(), set.type(), set.ttl(),
                        set.resourceRecords().stream().map(ResourceRecord::value)
                                .collect(Collectors.joining(",")));
                if (set.name().startsWith(host)) {
                    list.add(new ExternalDnsSpec(set.name(), set.type().toString(), set.resourceRecords().get(0).value(),
                            set.ttl().intValue(), zone, "aws"));
                }
            }
            return list;
        } catch (SdkException e) {
            log.error(e.getMessage());
            throw new DnsProviderException(e);
        }
    }

    @Override
    public ExternalDnsSpec createRecord(ExternalDnsSpec specIn) throws DnsProviderException {
        try (Route53Client client = getAWSClient()) {
            log.info("Creating record: {}, {}", specIn.getHost(), specIn.getValue());
            client.changeResourceRecordSets(ChangeResourceRecordSetsRequest.builder()
                    .hostedZoneId(specIn.getZone())
                    .changeBatch(ChangeBatch.builder()
                            .changes(Change.builder()
                                    .action(ChangeAction.CREATE)
                                    .resourceRecordSet(ResourceRecordSet.builder()
                                            .name(specIn.getHost())
                                            .type(RRType.valueOf(specIn.getRecordType()))
                                            .ttl((long) specIn.getTtl())
                                            .resourceRecords(ResourceRecord.builder()
                                                    .value(specIn.getValue())
                                                    .build())
                                            .build())
                                    .build())
                            .build())
                    .build());
            return specIn;
        } catch (SdkException e) {
            log.error(e.getMessage());
            throw new DnsProviderException(e);
        }
    }

    @Override
    public ExternalDnsSpec updateRecord(ExternalDnsSpec specIn) throws DnsProviderException {
        try (Route53Client client = getAWSClient()) {
            log.info("Updating record: {}, {}", specIn.getHost(), specIn.getValue());
            client.changeResourceRecordSets(ChangeResourceRecordSetsRequest.builder()
                    .hostedZoneId(specIn.getZone())
                    .changeBatch(ChangeBatch.builder()
                            .changes(Change.builder()
                                    .action(ChangeAction.UPSERT)
                                    .resourceRecordSet(ResourceRecordSet.builder()
                                            .name(specIn.getHost())
                                            .type(RRType.valueOf(specIn.getRecordType()))
                                            .ttl((long) specIn.getTtl())
                                            .resourceRecords(ResourceRecord.builder()
                                                    .value(specIn.getValue())
                                                    .build())
                                            .build())
                                    .build())
                            .build())
                    .build());
            return specIn;
        } catch (SdkException e) {
            log.error(e.getMessage());
            throw new DnsProviderException(e);
        }
    }

    @Override
    public void deleteRecord(ExternalDnsSpec specIn) throws DnsProviderException {
        try (Route53Client client = getAWSClient()) {
            log.info("Deleting record: {}", specIn.getHost());
            client.changeResourceRecordSets(ChangeResourceRecordSetsRequest.builder()
                    .hostedZoneId(specIn.getZone())
                    .changeBatch(ChangeBatch.builder()
                            .changes(Change.builder()
                                    .action(ChangeAction.DELETE)
                                    .resourceRecordSet(ResourceRecordSet.builder()
                                            .name(specIn.getHost())
                                            .type(RRType.valueOf(specIn.getRecordType()))
                                            .ttl((long) specIn.getTtl())
                                            .resourceRecords(ResourceRecord.builder()
                                                    .value(specIn.getValue())
                                                    .build())
                                            .build())
                                    .build())
                            .build())
                    .build());
        } catch (SdkException e) {
            log.error(e.getMessage());
            throw new DnsProviderException(e);
        }
    }

    private List<ResourceRecordSet> listResourceRecord(Route53Client route53Client, String hostedZoneId, String startRecord) throws Route53Exception {
        ListResourceRecordSetsRequest request = ListResourceRecordSetsRequest.builder()
                .startRecordName(startRecord)
                .hostedZoneId(hostedZoneId)
                .build();
        ListResourceRecordSetsResponse listResourceRecordSets = route53Client.listResourceRecordSets(request);
        return listResourceRecordSets.resourceRecordSets();
    }
}
