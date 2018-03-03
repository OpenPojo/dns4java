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

package com.openpojo.dns.routing;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;

import static com.openpojo.dns.routing.RoutingTable.DOT;
import static com.openpojo.dns.routing.utils.NotSupportedMethodsValidator.validateMethodsNotImplemented;
import static com.openpojo.random.RandomFactory.getRandomValue;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author oshoukry
 */
public class NoOpResolverTest {
  private List<String> implementedMethods = Arrays.asList();

  @Test
  public void whenSendCalledReturnEmptyMessage() throws IOException {
    NoOpResolver noOpResolver = new NoOpResolver();
    Message query = Message.newQuery(Record.newRecord(new Name(anyHost()), anyType(), anyDClass()));

    Message response = noOpResolver.send(query);
    assertThat(response, notNullValue());
    assertThat(response.getSectionArray(Section.QUESTION), notNullValue());
    assertThat(response.getSectionArray(Section.QUESTION).length, is(1));
    final Record questionRecord = response.getSectionArray(Section.QUESTION)[0];
    assertThat(questionRecord, is(query.getQuestion()));

    verifyEmptySection(response, Section.ANSWER);
    verifyEmptySection(response, Section.ADDITIONAL);
    verifyEmptySection(response, Section.AUTHORITY);
    verifyEmptySection(response, Section.UPDATE);
    verifyEmptySection(response, Section.PREREQ);
  }

  private void verifyEmptySection(Message answer, int section) {
    assertThat(answer.getSectionArray(section), notNullValue());
    assertThat(answer.getSectionArray(section).length, is(0));
  }

  private int anyDClass() {
    return anyType();
  }

  private int anyType() {

    int type = getRandomValue(int.class);
    if (type < 0)
      type *= -1;

    return type % 0xFFFF;
  }

  private String anyHost() {
    return getRandomValue(String.class) + DOT + getRandomValue(String.class) + DOT;
  }

  @Test
  public void shouldThrowUnImplementedOnAllMethods() {
    validateMethodsNotImplemented(NoOpResolver.class, implementedMethods);
  }
}
