package com.example.javastarterboilerplate.application.documentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.javastarterboilerplate.config.OpenApiDocumentationProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OpenApiDocumentationServiceTest {

  private static final TypeReference<Map<String, Object>> DOCUMENT_TYPE = new TypeReference<>() {};

  private final ObjectMapper jsonMapper = new ObjectMapper();
  private final YAMLMapper yamlMapper = new YAMLMapper();

  @Test
  void normalizesGeneratedSpecificationToConfiguredVersion() throws Exception {
    OpenApiDocumentationProperties properties = new OpenApiDocumentationProperties();
    properties.setTitle("Starter API reference");
    properties.setSpecResourcePath("testdata/openapi/generated-openapi.yaml");
    properties.setOpenApiVersion("3.1.1");
    properties.setScalarScriptUrl("https://cdn.example.test/scalar.js");

    OpenApiDocumentationService service = new OpenApiDocumentationService(properties);

    Map<String, Object> json = jsonMapper.readValue(service.openApiJson(), DOCUMENT_TYPE);
    Map<String, Object> yaml = yamlMapper.readValue(service.openApiYaml(), DOCUMENT_TYPE);
    String html = service.referenceHtml();

    assertThat(json).containsEntry("openapi", "3.1.1");
    assertThat(yaml).containsEntry("openapi", "3.1.1");
    assertThat(json).containsKey("paths");
    assertThat(yaml).containsKey("paths");
    assertThat(html).contains("Starter API reference");
    assertThat(html).contains("https://cdn.example.test/scalar.js");
    assertThat(html).contains("const jsonUrl = '/doc'");
    assertThat(html).contains("const yamlUrl = '/doc/openapi.yaml'");
    assertThat(html).contains("download yaml");
  }

  @Test
  void failsFastWhenGeneratedSpecificationIsMissing() {
    OpenApiDocumentationProperties properties = new OpenApiDocumentationProperties();
    properties.setSpecResourcePath("missing/openapi.yaml");

    OpenApiDocumentationService service = new OpenApiDocumentationService(properties);

    assertThatThrownBy(service::openApiJson)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Generated OpenAPI resource not found");
  }

  @Test
  void failsFastWhenGeneratedSpecificationCannotBeParsed() {
    OpenApiDocumentationProperties properties = new OpenApiDocumentationProperties();
    properties.setSpecResourcePath("testdata/openapi/invalid-openapi.yaml");

    OpenApiDocumentationService service = new OpenApiDocumentationService(properties);

    assertThatThrownBy(service::openApiJson)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Failed to load generated OpenAPI resource");
  }
}
