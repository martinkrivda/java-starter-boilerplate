package com.example.javastarterboilerplate.application.documentation;

import com.example.javastarterboilerplate.config.OpenApiDocumentationProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Singleton
public class OpenApiDocumentationService {

  private static final TypeReference<Map<String, Object>> DOCUMENT_TYPE = new TypeReference<>() {};
  private static final String JSON_DOCUMENT_PATH = "/doc";
  private static final String YAML_DOCUMENT_PATH = "/doc/openapi.yaml";

  private final OpenApiDocumentationProperties properties;
  private final ObjectMapper jsonMapper;
  private final YAMLMapper yamlMapper;

  private volatile OpenApiDocument cachedDocument;

  public OpenApiDocumentationService(OpenApiDocumentationProperties properties) {
    this.properties = properties;
    this.jsonMapper = new ObjectMapper();
    this.yamlMapper = new YAMLMapper();
  }

  public String referenceHtml() {
    return """
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>%s</title>
                  <style>
                    :root {
                      color-scheme: light;
                      font-family: "IBM Plex Sans", "Segoe UI", sans-serif;
                    }
                    body {
                      margin: 0;
                      background: linear-gradient(180deg, #f7f8fa 0%%, #eef2f7 100%%);
                    }
                    .topbar {
                      display: flex;
                      gap: 12px;
                      align-items: center;
                      padding: 16px 20px;
                      border-bottom: 1px solid rgba(15, 23, 42, 0.08);
                      background: rgba(255, 255, 255, 0.92);
                      backdrop-filter: blur(10px);
                      position: sticky;
                      top: 0;
                      z-index: 1;
                    }
                    .topbar__title {
                      margin-right: auto;
                      font-weight: 600;
                      color: #0f172a;
                    }
                    .topbar__link {
                      color: #0f172a;
                      text-decoration: none;
                      font-size: 14px;
                    }
                    .topbar__link:hover {
                      text-decoration: underline;
                    }
                    #scalar-reference {
                      min-height: calc(100vh - 65px);
                    }
                  </style>
                </head>
                <body>
                  <nav class="topbar">
                    <div class="topbar__title">%s</div>
                    <a id="json-link" class="topbar__link" href="#">openapi.json</a>
                    <a id="yaml-link" class="topbar__link" href="#">openapi.yaml</a>
                    <a id="yaml-download-link" class="topbar__link" href="#">download yaml</a>
                  </nav>
                  <div id="scalar-reference"></div>
                  <script src="%s"></script>
                  <script>
                    const jsonUrl = '%s';
                    const yamlUrl = '%s';
                    document.getElementById('json-link').href = jsonUrl;
                    document.getElementById('yaml-link').href = yamlUrl;
                    document.getElementById('yaml-download-link').href = `${yamlUrl}/download`;
                    Scalar.createApiReference('#scalar-reference', {
                      url: jsonUrl,
                      theme: 'alternate',
                      hideDownloadButton: false,
                      searchHotKey: 'k'
                    });
                  </script>
                </body>
                </html>
                """
        .formatted(
            properties.getTitle(),
            properties.getTitle(),
            properties.getScalarScriptUrl(),
            JSON_DOCUMENT_PATH,
            YAML_DOCUMENT_PATH);
  }

  public String openApiJson() {
    return document().json();
  }

  public String openApiYaml() {
    return document().yaml();
  }

  private synchronized OpenApiDocument document() {
    if (cachedDocument == null) {
      cachedDocument = loadDocument();
    }
    return cachedDocument;
  }

  private OpenApiDocument loadDocument() {
    try (InputStream inputStream =
        getClass().getClassLoader().getResourceAsStream(properties.getSpecResourcePath())) {
      if (inputStream == null) {
        throw new IllegalStateException(
            "Generated OpenAPI resource not found: " + properties.getSpecResourcePath());
      }
      Map<String, Object> specification = yamlMapper.readValue(inputStream, DOCUMENT_TYPE);
      specification.put("openapi", properties.getOpenApiVersion());
      return new OpenApiDocument(
          jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(specification),
          yamlMapper.writeValueAsString(specification));
    } catch (IOException exception) {
      throw new IllegalStateException("Failed to load generated OpenAPI resource", exception);
    }
  }

  private record OpenApiDocument(String json, String yaml) {}
}
