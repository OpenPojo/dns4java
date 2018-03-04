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

package com.openpojo.dns.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.PojoClassFilter;
import com.openpojo.reflection.filters.FilterChain;
import com.openpojo.reflection.filters.FilterClassName;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Computer;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import static com.openpojo.reflection.impl.PojoClassFactory.getPojoClassesRecursively;

/**
 * @author oshoukry
 */
public class OutOfOrderTestFragilityTest {

  private List<PojoClass> testClasses;
  private FilterClassName testClassFilter;
  private PojoClassFilter selfFilter;

  @Before
  public void setup() {
    testClassFilter = new FilterClassName(".*Test$");
    selfFilter = new OutOfOrderTestFragilityFilter();

    testClasses = getPojoClassesRecursively("com.openpojo.dns", new FilterChain(testClassFilter, selfFilter));
  }

  @Test
  public void regressTestingOrder() {
    for (int i = 0; i < 5; i++)
      runAllTestsInRandomOrder();
  }

  private void runAllTestsInRandomOrder() {
    Map<String, Result> testsAndResults = new LinkedHashMap<>();

    Collections.shuffle(testClasses);

    for (PojoClass clazz : testClasses) {
      Computer computer = new Computer();
      JUnitCore jUnitCore = new JUnitCore();
      testsAndResults.put(clazz.getName(), jUnitCore.run(computer, clazz.getClazz()));
    }

    boolean passed = true;
    List<String> orderOfTests = new ArrayList<>();
    for (Map.Entry<String, Result> entry : testsAndResults.entrySet()) {
      final boolean wasSuccessful = entry.getValue().wasSuccessful();
      passed &= wasSuccessful;
      final String resultValue = getResultValue(wasSuccessful);
      final String testOutcome = "Test: " + entry.getKey() + "..." + resultValue;
      orderOfTests.add(testOutcome);
    }
    if (!passed) {
      for (String entry : orderOfTests) {
        System.out.println(entry);
      }

      Assert.fail("One or more test failed!");
    }
  }

  private String getResultValue(boolean wasSuccessful) {
    return wasSuccessful ? "Passed!" : "Failed!";
  }

  private static class OutOfOrderTestFragilityFilter implements PojoClassFilter {

    @Override
    public boolean include(PojoClass pojoClass) {
      return !pojoClass.getName().equals(OutOfOrderTestFragilityTest.class.getName());
    }
  }
}
