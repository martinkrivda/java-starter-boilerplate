package com.example.javastarterboilerplate.infrastructure.document;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("document.dss")
public class DssProperties {

    private boolean enabled = true;

    private String roadmapNote = "PAdES and validation flows will be added in a future iteration";

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
