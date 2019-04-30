package com.acabra.moneytransfer.utils;

import com.acabra.moneytransfer.dto.AccountDTO;
import com.acabra.moneytransfer.dto.CreateAccountRequestDTO;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;

public class CustomJsonParser {

    public static class AccountDTOJsonHelper extends StdSerializer<AccountDTO> {

        final static DecimalFormat FORMAT = new DecimalFormat("0.00");
        static {
            FORMAT.setMaximumFractionDigits(2);
        }

        public AccountDTOJsonHelper(Class<AccountDTO> t) {
            super(t);
        }

        public AccountDTOJsonHelper(){
            this(null);
        }

        @Override
        public void serialize(AccountDTO value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeStartObject();
            jgen.writeNumberField("id", value.getId());
            jgen.writeNumberField("balance", value.getBalance());
            jgen.writeEndObject();
        }
    }

    public static class CreateAccountRequestDTOJsonHelper extends StdDeserializer<CreateAccountRequestDTO> {

        public CreateAccountRequestDTOJsonHelper(Class<?> vc) {
            super(vc);
        }

        public CreateAccountRequestDTOJsonHelper(){
            this(null);
        }

        @Override
        public CreateAccountRequestDTO deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            JsonNode node = jp.getCodec().readTree(jp);
            BigDecimal initialBalance = new BigDecimal(node.get("initialBalance").asText());
            return new CreateAccountRequestDTO(initialBalance);
        }
    }
}
