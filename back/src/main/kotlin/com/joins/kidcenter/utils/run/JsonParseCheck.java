package com.joins.kidcenter.utils.run;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonParseCheck {
    public static void main(String[] args) throws IOException {
        String json = "{\n" +
                "  \"code\": 200,\n" +
                "  \"msg\": \"88\",\n" +
                "  \"obj\": \"1908\"\n" +
                "}";

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);

        String statusCode = root.path("code").asText();
        String verificationCode = root.get("obj").asText();
        System.out.printf("Status: %s, data: %s", statusCode, verificationCode);

    }
}
