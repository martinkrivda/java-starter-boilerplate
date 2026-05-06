package com.example.javastarterboilerplate.infrastructure.document;

import io.micronaut.context.annotation.ConfigurationProperties;

/**
 * Configuration properties for PDFBox document processing, bound to {@code document.pdfbox}.
 *
 * <p>The current adapter supports document inspection. The roadmap note documents future
 * large-document handling so operational endpoints can report the adapter status clearly.
 */
@ConfigurationProperties("document.pdfbox")
public class PdfBoxProperties {

  private boolean enabled = true;

  private String roadmapNote =
      "Scratch files will be introduced when large-document workflows are implemented";

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getRoadmapNote() {
    return roadmapNote;
  }

  public void setRoadmapNote(String roadmapNote) {
    this.roadmapNote = roadmapNote;
  }
}
