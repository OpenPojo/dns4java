/*
 * Copyright (c) 2018-2018 Osman Shoukry
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.openpojo.dns.routing.impl;

import java.util.List;
import java.util.Map;

import com.openpojo.dns.exception.RouteSetupException;
import com.openpojo.dns.routing.RoutingTable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.openpojo.dns.routing.utils.DomainUtils.toDnsDomain;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author oshoukry
 */
public class RoutingTableBuilderTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void shouldCreateEmptyRoutingTable() {
    RoutingTableBuilder routingTableBuilder = RoutingTableBuilder.create();
    assertThat(routingTableBuilder, notNullValue());
    assertThat(routingTableBuilder.getDestinationMap().size(), is(0));
  }

  @Test
  public void shouldCreateOptimizeRoutingTableWhenBuild() {
    RoutingTable routingTable = RoutingTableBuilder.create().build();
    assertThat(routingTable, notNullValue());
  }

  @Test
  public void whenCalledWithNullDnsServersThrowsException() {
    final String destination = "host.com";

    thrown.expect(RouteSetupException.class);
    thrown.expectMessage("Null server list passed for destination [" + destination + "]");

    RoutingTableBuilder.create().with(destination, (String[])null);
  }

  @Test
  public void whenHostPassedInMapHasOneEntryOptimized() {
    String destination = "host.com";
    String dnsServer = "127.0.0.1";
    final RoutingTableBuilder routingTableBuilder = RoutingTableBuilder.create().with(destination, dnsServer);

    final Map<String, List<String>> destinationMap = routingTableBuilder.getDestinationMap();
    assertThat(destinationMap, notNullValue());
    assertThat(destinationMap.size(), is(1));

    String hierarchicalDestination = destinationMap.keySet().iterator().next();
    assertThat(hierarchicalDestination, is(toDnsDomain(destination)));
    final List<String> actualDnsServers = destinationMap.get(hierarchicalDestination);
    assertThat(actualDnsServers, notNullValue());
    assertThat(actualDnsServers.size(), is(1));
    assertThat(actualDnsServers.get(0), is(dnsServer));
  }

  @Test
  public void shouldBuildProperRoutingTable() {
    String destination = "www.openpojo.com";
    String dnsServer = "127.0.0.1";
    final RoutingTable routingTable = RoutingTableBuilder.create().with(destination, dnsServer).build();
    assertThat(routingTable, notNullValue());
    assertThat(routingTable.getResolverFor(destination), notNullValue());
  }
}