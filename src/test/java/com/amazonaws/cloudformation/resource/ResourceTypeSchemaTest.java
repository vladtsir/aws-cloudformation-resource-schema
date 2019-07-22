/*
* Copyright 2010-2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License").
* You may not use this file except in compliance with the License.
* A copy of the License is located at
*
*  http://aws.amazon.com/apache2.0
*
* or in the "license" file accompanying this file. This file is distributed
* on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
* express or implied. See the License for the specific language governing
* permissions and limitations under the License.
*/
package com.amazonaws.cloudformation.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.amazonaws.cloudformation.resource.exceptions.ValidationException;

import java.util.List;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.Test;

public class ResourceTypeSchemaTest {
    private static final String TEST_SCHEMA_PATH = "/test-schema.json";
    private static final String EMPTY_SCHEMA_PATH = "/empty-schema.json";
    private static final String MINIMAL_SCHEMA_PATH = "/minimal-schema.json";

    @Test
    public void getProperties() {
        JSONObject o = new JSONObject(new JSONTokener(this.getClass().getResourceAsStream(TEST_SCHEMA_PATH)));
        final ResourceTypeSchema schema = ResourceTypeSchema.load(o);

        assertThat(schema.getDescription()).isEqualTo("A test schema for unit tests.");
        assertThat(schema.getSourceUrl()).isEqualTo("my-repo.git");
        assertThat(schema.getTypeName()).isEqualTo("AWS::Test::TestModel");
        assertThat(schema.getUnprocessedProperties()).isEmpty();
    }

    @Test
    public void getCreateOnlyProperties() {
        JSONObject o = new JSONObject(new JSONTokener(this.getClass().getResourceAsStream(TEST_SCHEMA_PATH)));
        final ResourceTypeSchema schema = ResourceTypeSchema.load(o);

        List<String> result = schema.getCreateOnlyPropertiesAsStrings();

        assertThat(result).containsExactly("/properties/propertyA", "/properties/propertyD");
    }

    @Test
    public void getDeprecatedProperties() {
        JSONObject o = new JSONObject(new JSONTokener(this.getClass().getResourceAsStream(TEST_SCHEMA_PATH)));
        final ResourceTypeSchema schema = ResourceTypeSchema.load(o);

        List<String> result = schema.getDeprecatedPropertiesAsStrings();
        assertThat(result).containsExactly("/properties/propertyC");
    }

    @Test
    public void getPrimaryIdentifier() {
        JSONObject o = new JSONObject(new JSONTokener(this.getClass().getResourceAsStream(TEST_SCHEMA_PATH)));
        final ResourceTypeSchema schema = ResourceTypeSchema.load(o);

        List<String> result = schema.getPrimaryIdentifierAsStrings();
        assertThat(result).containsExactly("/properties/propertyA");
    }

    @Test
    public void getAdditionalIdentifiers() {
        JSONObject o = new JSONObject(new JSONTokener(this.getClass().getResourceAsStream(TEST_SCHEMA_PATH)));
        final ResourceTypeSchema schema = ResourceTypeSchema.load(o);

        List<List<String>> result = schema.getAdditionalIdentifiersAsStrings();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).containsExactly("/properties/propertyB");
    }

    @Test
    public void getReadOnlyProperties() {
        JSONObject o = new JSONObject(new JSONTokener(this.getClass().getResourceAsStream(TEST_SCHEMA_PATH)));
        final ResourceTypeSchema schema = ResourceTypeSchema.load(o);

        List<String> result = schema.getReadOnlyPropertiesAsStrings();
        assertThat(result).containsExactly("/properties/propertyB");
    }

    @Test
    public void getWriteOnlyProperties() {
        JSONObject o = new JSONObject(new JSONTokener(this.getClass().getResourceAsStream(TEST_SCHEMA_PATH)));
        final ResourceTypeSchema schema = ResourceTypeSchema.load(o);

        List<String> result = schema.getWriteOnlyPropertiesAsStrings();
        assertThat(result).containsExactly("/properties/propertyC");
    }

    @Test
    public void invalidSchema_shouldThrow() {
        JSONObject o = new JSONObject(new JSONTokener(this.getClass().getResourceAsStream(EMPTY_SCHEMA_PATH)));

        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> ResourceTypeSchema.load(o)).withNoCause()
            .withMessage("#/properties: minimum size: [1], found: [0]");
    }

    @Test
    public void minimalSchema_hasNoSemantics() {
        JSONObject o = new JSONObject(new JSONTokener(this.getClass().getResourceAsStream(MINIMAL_SCHEMA_PATH)));
        final ResourceTypeSchema schema = ResourceTypeSchema.load(o);

        assertThat(schema.getDescription()).isEqualTo("A test schema for unit tests.");
        assertThat(schema.getSourceUrl()).isNull();
        assertThat(schema.getTypeName()).isEqualTo("AWS::Test::TestModel");
        assertThat(schema.getUnprocessedProperties()).isEmpty();
        assertThat(schema.getCreateOnlyPropertiesAsStrings()).isEmpty();
        assertThat(schema.getDeprecatedPropertiesAsStrings()).isEmpty();
        assertThat(schema.getPrimaryIdentifierAsStrings()).containsExactly("/properties/PropertyA");
        assertThat(schema.getAdditionalIdentifiersAsStrings()).isEmpty();
        assertThat(schema.getReadOnlyPropertiesAsStrings()).isEmpty();
        assertThat(schema.getWriteOnlyPropertiesAsStrings()).isEmpty();
    }
}