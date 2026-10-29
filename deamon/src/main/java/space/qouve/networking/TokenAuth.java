package space.qouve.networking;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TokenAuth {

    private final Map<String, String> testInstanceTokens = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    private final String stagingSecret;
    private final String productiveSecret;

    public TokenAuth(String stagingSecret, String productiveSecret) {
        this.stagingSecret = stagingSecret;
        this.productiveSecret = productiveSecret;
    }

    public String createTestInstanceToken(String serverId) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        testInstanceTokens.put(serverId, token);
        return token;
    }

    public void revokeTestInstanceToken(String serverId) {
        testInstanceTokens.remove(serverId);
    }

    public boolean isValid(ServerType type, String serverId, String token) {
        if (type == null || serverId == null || token == null) return false;

        String expected = switch (type) {
            case TESTINSTANCE -> testInstanceTokens.get(serverId);
            case STAGING      -> stagingSecret;
            case PRODUCTIVE   -> productiveSecret;
        };

        return expected != null && !expected.isBlank()
                && MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                token.getBytes(StandardCharsets.UTF_8));
    }
}