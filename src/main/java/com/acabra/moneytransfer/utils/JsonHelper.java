package com.acabra.moneytransfer.utils;

import com.acabra.moneytransfer.dto.AccountDTO;
import com.acabra.moneytransfer.dto.CreateAccountRequestDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;

public class JsonHelper {

    private static final JsonHelper instance = new JsonHelper();

    private final ObjectMapper mapper;

    public JsonHelper(){
        mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(new Jdk8Module())
                .registerModule(new SimpleModule(){{
                    addSerializer(AccountDTO.class, new CustomJsonParser.AccountDTOJsonHelper());
                    addDeserializer(CreateAccountRequestDTO.class, new CustomJsonParser.CreateAccountRequestDTOJsonHelper());
                }});
    }

    public static JsonHelper getInstance() {
        return instance;
    }

    public String toJson(Object object) throws JsonProcessingException {
        return mapper.writeValueAsString(object);
    }

    public <T> T fromJson(String body, Class<T> clazz) throws IOException {
        return mapper.readValue(body, clazz);
    }
}
