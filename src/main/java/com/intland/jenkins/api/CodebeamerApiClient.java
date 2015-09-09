/*
 * Copyright (c) 2015 Intland Software (support@intland.com)
 */

package com.intland.jenkins.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intland.jenkins.api.dto.AttachmentDto;
import com.intland.jenkins.api.dto.MarkupDto;
import com.intland.jenkins.api.dto.UserDto;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CodebeamerApiClient {
    private final int HTTP_TIMEOUT = 10000;
    private HttpClient client;
    private RequestConfig requestConfig;
    private String baseUrl;
    private String wikiId;
    private ObjectMapper objectMapper;

    public CodebeamerApiClient(String username, String password, String url, String wikiIdentifier) {
        objectMapper = new ObjectMapper();
        wikiId = wikiIdentifier;
        baseUrl = url;

        CredentialsProvider provider = getCredentialsProvider(username, password);
        client = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
        requestConfig = RequestConfig.custom().setConnectionRequestTimeout(HTTP_TIMEOUT)
                                                .setConnectTimeout(HTTP_TIMEOUT)
                                                .setSocketTimeout(HTTP_TIMEOUT)
                                                .build();
    }

    public void createOrUpdateAttachment(String attachmentName, String newAttachmentContent) throws IOException {
        String attachmentId = getAttachmentId(attachmentName);
        if (attachmentId == null) {
            createAttachment(attachmentName, newAttachmentContent);
        } else {
            String oldAttachmentContent = getAttachmentContent(attachmentId);
            updateAttachment(attachmentId, attachmentName, oldAttachmentContent, newAttachmentContent);
        }
    }

    public String getWikiMarkup(String url, String wikiId) throws IOException {
        String tmpUrl = String.format("%s/rest/wikipage/%s", url, wikiId);
        String json = get(tmpUrl);
        MarkupDto markupDto = objectMapper.readValue(json, MarkupDto.class);
        return markupDto.getMarkup();
    }

    public void updateWikiMarkup(String url, String wikiId, String markup) throws IOException {
        HttpPut put = new HttpPut(String.format("%s/rest/wikipage", url));
        MarkupDto markupDto = new MarkupDto("/wikipage/"+wikiId, markup);
        String content = objectMapper.writeValueAsString(markupDto);

        StringEntity stringEntity = new StringEntity(content,"UTF-8");
        stringEntity.setContentType("application/json");
        put.setEntity(stringEntity);
        client.execute(put);
        put.releaseConnection();
    }

    public String getUserId(String author)  throws IOException {
        String authorNoSpace = author.replaceAll(" ", "");
        String tmpUrl = String.format("%s/rest/user/%s", baseUrl, authorNoSpace);

        //Fetch Page
        String httpResult = get(tmpUrl);
        String result = null;

        if (httpResult != null) { //20X success
            UserDto userDto = objectMapper.readValue(httpResult, UserDto.class);
            String uri = userDto.getUri();
            result = uri.substring(uri.lastIndexOf("/") + 1);
        }

        return result;
    }

    private String getAttachmentId(String attachmentName) throws IOException {
        String url = String.format("%s/rest/wikipage/%s/attachments", baseUrl, wikiId);
        String attachmentResponse = get(url);

        AttachmentDto[] attachments = objectMapper.readValue(attachmentResponse, AttachmentDto[].class);
        String result = null;
        for (AttachmentDto attachmentDto : attachments) {
            if (attachmentDto.getName().equals(attachmentName)) {
                result = attachmentDto.getId();
                break;
            }
        }

        return result;
    }

    private String getAttachmentContent(String attachmentId) throws IOException {
        String url = String.format("%s/rest/attachment/%s/content", baseUrl, attachmentId);
        return get(url);
    }

    private boolean createAttachment(String attachmentName, String content) throws IOException {
        HttpPost post = new HttpPost(baseUrl + "/rest/attachment");
        FileBody fileBody = new FileBody(createTempFile(content, null));

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.STRICT);
        String jsonContent = String.format("{\"parent\" : \"/wikipage/%s\", \"name\": \"%s\"}", wikiId, attachmentName);
        builder.addTextBody("body", jsonContent, ContentType.APPLICATION_JSON);
        builder.addPart(attachmentName, fileBody);
        HttpEntity entity = builder.build();

        post.setEntity(entity);
        client.execute(post);
        post.releaseConnection();
        return true;
    }

    private boolean updateAttachment(String attachmentId, String attachmentName, String oldContent, String newContent) throws IOException {
        HttpPut put = new HttpPut(baseUrl + "/rest/attachment");
        FileBody fileBody = new FileBody(createTempFile(newContent, oldContent));
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.STRICT);
        String jsonContent = String.format("{\"uri\" : \"/attachment/%s\"}", attachmentId);
        builder.addTextBody("body", jsonContent, ContentType.APPLICATION_JSON);
        builder.addPart(attachmentName, fileBody);
        HttpEntity entity = builder.build();

        put.setEntity(entity);
        client.execute(put);
        put.releaseConnection();
        return true;
    }

    private File createTempFile(String newContent, String oldContent) throws IOException{
        final File tempFile = File.createTempFile("tmpfile", "csv");
        tempFile.deleteOnExit();
        FileWriter fileWriter = null;

        try {
            fileWriter = new FileWriter(tempFile);
            fileWriter.append(newContent);
            if (oldContent != null) {
                fileWriter.append(oldContent);
            }
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
            }
        }

        return tempFile;
    }

    private String get(String url) throws IOException {
        HttpGet get = new HttpGet(url);
        get.setConfig(requestConfig);
        HttpResponse response = client.execute(get);
        int statusCode = response.getStatusLine().getStatusCode();

        String result = null;
        if (statusCode == 200) {
            result = new BasicResponseHandler().handleResponse(response);
        }

        get.releaseConnection();

        return result;
    }

    private CredentialsProvider getCredentialsProvider(String username, String password) {
        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
        provider.setCredentials(AuthScope.ANY, credentials);
        return provider;
    }
}
