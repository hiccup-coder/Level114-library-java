/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.level114.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;

public final class ReportNonce {
    @JsonProperty(value="nonce")
    @JsonSerialize(using=Base64ByteArraySerializer.class)
    @JsonDeserialize(using=Base64ByteArrayDeserializer.class)
    private byte[] nonce;
    private UUID serverId;
    private OffsetDateTime expiresAt;
    private OffsetDateTime usedAt;

    @JsonIgnore
    public String getNonce() {
        return this.nonce == null ? null : Base64.getUrlEncoder().withoutPadding().encodeToString(this.nonce);
    }

    public void setNonce(byte[] nonce) {
        this.nonce = nonce;
    }

    @JsonIgnore
    public byte[] getNonceBytes() {
        return this.nonce;
    }

    public UUID getServerId() {
        return this.serverId;
    }

    public void setServerId(UUID serverId) {
        this.serverId = serverId;
    }

    public OffsetDateTime getExpiresAt() {
        return this.expiresAt;
    }

    public void setExpiresAt(OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public OffsetDateTime getUsedAt() {
        return this.usedAt;
    }

    public void setUsedAt(OffsetDateTime usedAt) {
        this.usedAt = usedAt;
    }

    public static final class Base64ByteArrayDeserializer
    extends JsonDeserializer<byte[]> {
        @Override
        public byte[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String s = p.getValueAsString();
            if (s == null) {
                return null;
            }
            try {
                return Base64.getUrlDecoder().decode(s);
            } catch (IllegalArgumentException e) {
                throw new JsonParseException(p, "Invalid base64 value for nonce");
            }
        }
    }

    public static final class Base64ByteArraySerializer
    extends JsonSerializer<byte[]> {
        @Override
        public void serialize(byte[] value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }
            gen.writeString(Base64.getUrlEncoder().withoutPadding().encodeToString(value));
        }
    }
}

