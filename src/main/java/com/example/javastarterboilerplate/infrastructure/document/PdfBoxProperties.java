package com.example.javastarterboilerplate.infrastructure.document;

import io.micronaut.context.annotation.ConfigurationProperties;

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
