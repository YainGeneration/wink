package com.wink.backend.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class SingleBase64Deserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

        // 배열로 들어온 경우 → 첫 번째 값만 반환
        if (p.currentToken() == JsonToken.START_ARRAY) {
            p.nextToken(); // 첫 번째 요소로 이동
            String first = p.getValueAsString(); // 첫 번째 값만 사용

            // 나머지는 그냥 skip
            while (p.nextToken() != JsonToken.END_ARRAY) {
                p.skipChildren();
            }
            return first;
        }

        // 문자열로 들어온 경우 → 그대로 사용
        return p.getValueAsString();
    }
}
