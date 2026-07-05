package com.devdoyen.nemologic.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class SolveProofTokenService {

    @Value("${solve.proof.secret:a-very-secret-key-that-is-at-least-256-bits-long-for-hmac-sha-256}")
    private String secretKey;

    public String generateProofToken(Long stageId, int elapsedTime) {
        try {
            JWSSigner signer = new MACSigner(secretKey.getBytes());
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject("solve-proof")
                    .claim("stageId", stageId)
                    .claim("elapsedTime", elapsedTime)
                    .issueTime(new Date())
                    .expirationTime(new Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000)) // 7 days
                    .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to generate solve proof token", e);
        }
    }

    public boolean verifyProofToken(String token, Long stageId, int elapsedTime) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(secretKey.getBytes());

            if (!signedJWT.verify(verifier)) {
                return false;
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            
            // Check expiration
            Date exp = claims.getExpirationTime();
            if (exp == null || exp.before(new Date())) {
                return false;
            }

            // Verify claims match
            Long claimStageId = claims.getLongClaim("stageId");
            Number claimElapsedTime = (Number) claims.getClaim("elapsedTime");

            return stageId.equals(claimStageId) && elapsedTime == claimElapsedTime.intValue();
        } catch (Exception e) {
            return false;
        }
    }
}
