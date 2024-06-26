/*
 * Copyright (C) 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fabric8.kubernetes.api.model.admissionregistration.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.Namespaced;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.assertThat;

class ValidatingAdmissionPolicyTest {
  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void isClusterScoped() {
    assertThat(ValidatingAdmissionPolicy.class).isNotInstanceOf(Namespaced.class);
  }

  @Test
  void apiGroup() {
    assertThat(new ValidatingAdmissionPolicy().getApiVersion()).isEqualTo("admissionregistration.k8s.io/v1");
  }

  @Test
  void deserializationAndSerializationShouldWorkAsExpected() throws IOException {
    // Given
    String originalJson = new Scanner(getClass().getResourceAsStream("/test-v1-validatingadmissionpolicy.json"))
        .useDelimiter("\\A")
        .next();

    // When
    final ValidatingAdmissionPolicy validatingAdmissionPolicy = mapper.readValue(originalJson, ValidatingAdmissionPolicy.class);
    final String serializedJson = mapper.writeValueAsString(validatingAdmissionPolicy);

    // Then
    assertThat(serializedJson).isNotNull();
    assertThat(validatingAdmissionPolicy)
        .isNotNull()
        .hasFieldOrPropertyWithValue("metadata.name", "demo-policy.example.com")
        .hasFieldOrPropertyWithValue("spec.failurePolicy", "Fail")
        .satisfies(v -> assertThat(v.getSpec().getValidations())
            .asInstanceOf(InstanceOfAssertFactories.LIST)
            .singleElement(InstanceOfAssertFactories.type(Validation.class))
            .hasFieldOrPropertyWithValue("expression", "object.spec.replicas <= 5"))
        .satisfies(v -> assertThat(v.getSpec().getMatchConstraints().getResourceRules())
            .asInstanceOf(InstanceOfAssertFactories.LIST)
            .singleElement(InstanceOfAssertFactories.type(NamedRuleWithOperations.class))
            .hasFieldOrPropertyWithValue("apiGroups", Collections.singletonList("apps"))
            .hasFieldOrPropertyWithValue("apiVersions", Collections.singletonList("v1"))
            .hasFieldOrPropertyWithValue("operations", Arrays.asList("CREATE", "UPDATE"))
            .hasFieldOrPropertyWithValue("resources", Collections.singletonList("deployments")));
  }

  @Test
  void builderShouldCreateObject() {
    // Given
    ValidatingAdmissionPolicyBuilder validatingAdmissionPolicyBuilder = new ValidatingAdmissionPolicyBuilder()
        .withNewMetadata().withName("demo-policy.example.com").endMetadata()
        .withNewSpec()
        .addNewValidation().withExpression("object.spec.replicas <= 5").endValidation()
        .withNewMatchConstraints()
        .addNewResourceRule()
        .addToApiGroups("apps")
        .addToApiVersions("v1")
        .addToOperations("CREATE", "UPDATE")
        .addToResources("deployments")
        .endResourceRule()
        .endMatchConstraints()
        .endSpec();

    // When
    ValidatingAdmissionPolicy validatingAdmissionPolicy = validatingAdmissionPolicyBuilder.build();

    // Then
    assertThat(validatingAdmissionPolicy)
        .isNotNull()
        .hasFieldOrPropertyWithValue("metadata.name", "demo-policy.example.com")
        .hasFieldOrPropertyWithValue("spec.matchConstraints.resourceRules",
            Collections.singletonList(new NamedRuleWithOperationsBuilder()
                .addToApiGroups("apps")
                .addToApiVersions("v1")
                .addToOperations("CREATE", "UPDATE")
                .addToResources("deployments")
                .build()))
        .hasFieldOrPropertyWithValue("spec.validations", Collections.singletonList(new ValidationBuilder()
            .withExpression("object.spec.replicas <= 5")
            .build()));
  }
}
