/*
 * Copyright (c) 2015 Intland Software (support@intland.com)
 */
package com.intland.jenkins.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MarkupDto {
    String uri;
    String markup;

    public MarkupDto() {}

    public MarkupDto(String uri, String markup) {
        this.uri = uri;
        this.markup = markup;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getMarkup() {
        return markup;
    }

    public void setMarkup(String markup) {
        this.markup = markup;
    }
}
