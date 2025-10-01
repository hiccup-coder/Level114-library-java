package io.level114.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;

public final class ReportNonce {
    @JsonProperty("nonce")
    @JsonSerialize(using = Base64ByteArraySerializer.class)
    @JsonDeserialize(using = Base64ByteArrayDeserializer.class)
    private byte[] nonce;

    private UUID serverId;
    private OffsetDateTime expiresAt;
    private OffsetDateTime usedAt; // optional

    @JsonIgnore
    public String getNonce() { return nonce == null ? null : Base64.getUrlEncoder().withoutPadding().encodeToString(nonce); }
    public void setNonce(byte[] nonce) { this.nonce = nonce; }

    @JsonIgnore
    public byte[] getNonceBytes() { return nonce; }

    public UUID getServerId() { return serverId; }
    public void setServerId(UUID serverId) { this.serverId = serverId; }

    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(OffsetDateTime expiresAt) { this.expiresAt = expiresAt; }

    public OffsetDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(OffsetDateTime usedAt) { this.usedAt = usedAt; }

    public static final class Base64ByteArraySerializer extends com.fasterxml.jackson.databind.JsonSerializer<byte[]> {
        @Override
        public void serialize(byte[] value, com.fasterxml.jackson.core.JsonGenerator gen, com.fasterxml.jackson.databind.SerializerProvider serializers) throws java.io.IOException {
            if (value == null) { gen.writeNull(); return; }
            gen.writeString(Base64.getUrlEncoder().withoutPadding().encodeToString(value));
        }
    }

    public static final class Base64ByteArrayDeserializer extends com.fasterxml.jackson.databind.JsonDeserializer<byte[]> {
        @Override
        public byte[] deserialize(com.fasterxml.jackson.core.JsonParser p, com.fasterxml.jackson.databind.DeserializationContext ctxt) throws java.io.IOException {
            String s = p.getValueAsString();
            if (s == null) return null;
            try {
                return Base64.getUrlDecoder().decode(s);
            } catch (IllegalArgumentException e) {
                throw new com.fasterxml.jackson.core.JsonParseException(p, "Invalid base64 value for nonce");
            }
        }
    }
}


