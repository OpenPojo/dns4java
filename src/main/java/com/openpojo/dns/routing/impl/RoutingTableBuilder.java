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

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.openpojo.dns.exception.RouteSetupException;
import com.openpojo.dns.routing.RoutingTable;
import com.openpojo.dns.routing.utils.DomainUtils;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Resolver;

import static com.openpojo.dns.routing.RoutingTable.DOT;

/**
 * @author oshoukry
 */
public class RoutingTableBuilder {
  private Map<String, List<String>> destinationMap = new HashMap<>();

  public static RoutingTableBuilder create() {
    return new RoutingTableBuilder();
  }

  public RoutingTableBuilder with(String destination, String... dnsServers) {
    if (dnsServers == null)
      throw RouteSetupException.getInstance("Null server list passed for destination [" + destination + "]");

    destination = cleanupDestination(destination);

    String hierarchicalDomain = DomainUtils.toDnsDomain(destination);
    List<String> dnsServersList = destinationMap.get(hierarchicalDomain);
    if (dnsServersList == null)
      dnsServersList = new ArrayList<>();

    for (String dnsServer : dnsServers) {
      if (dnsServer != null && dnsServer.length() > 0)
        dnsServersList.add(dnsServer);
    }
    destinationMap.put(hierarchicalDomain, dnsServersList);

    return this;
  }

  public Map<String, List<String>> getDestinationMap() {
    return destinationMap;
  }

  private String cleanupDestination(String destination) {
    if (destination == null || destination.length() == 0) {
      destination = DOT;
    }

    if (!destination.endsWith(DOT))
      destination += DOT;
    return destination.toLowerCase();
  }

  public RoutingTable build() {
    try {
      Map<String, Resolver> optimizedRoutingEntries = new HashMap<>();
      for (Map.Entry<String, List<String>> entry : destinationMap.entrySet())
        optimizedRoutingEntries.put(entry.getKey(), new ExtendedResolver(entry.getValue().toArray(new String[0])));

      return new OptimizedRoutingTable(optimizedRoutingEntries);

    } catch (UnknownHostException e) {
      throw RouteSetupException.getInstance("Failed to create dns routing map ", e);
    }
  }

  private RoutingTableBuilder() {
  }
}
