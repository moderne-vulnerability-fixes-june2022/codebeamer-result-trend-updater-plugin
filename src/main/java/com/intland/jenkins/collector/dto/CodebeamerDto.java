/*
 * Copyright (c) 2015 Intland Software (support@intland.com)
 */
package com.intland.jenkins.collector.dto;

public class CodebeamerDto {
    private String markup;
    private String attachmentContent;
    private String attachmentName;

    public CodebeamerDto(String markup, String attachmentContent, String attachmentName) {
        this.markup = markup;
        this.attachmentContent = attachmentContent;
        this.attachmentName = attachmentName;
    }

    public String getMarkup() {
        return markup;
    }

    public String getAttachmentContent() {
        return attachmentContent;
    }

    public String getAttachmentName() {
        return attachmentName;
    }
}
