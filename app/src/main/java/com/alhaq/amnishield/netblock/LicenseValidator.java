package com.alhaq.amnishield.netblock;

/*
    This file is part of AmniGuard Firewall.

    AmniGuard Firewall is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    AmniGuard Firewall is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with AmniGuard Firewall.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2015-2026 by Alhaq Initiative
*/

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Offline Cryptographic NIST P-256 (secp256r1) ECDSA License Validator.
 * Enables zero-knowledge, 100% on-device Pro / Supporter Pass activation.
 */
public final class LicenseValidator {
    private static final String TAG = "AmniGuard.License";
    private static final String PREF_LICENSE = "AmniGuard_License";
    private static final String KEY_LICENSE_STRING = "license_key";
    private static final String KEY_LICENSE_PAYLOAD = "license_payload";

    // NIST P-256 (secp256r1) Public Key keyring in X.509 format (Base64)
    private static final Map<Integer, String> KEYRING = new HashMap<>();

    static {
        KEYRING.put(1, "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE7EFR1qxpfZTMeR52M1+04+tPb6ItmVmhPbRCIJYje3jtglTdBbcct+/xvc1D1NZtXuvSb4Egtdqm/EJ6H67fEA==");
    }

    private LicenseValidator() {}

    /**
     * Verifies an ECDSA-signed license string format: {Base64Payload}.{Base64Signature}
     */
    public static LicensePayload verifyLicense(String licenseString) {
        if (licenseString == null) return null;
        try {
            String cleanString = licenseString.trim().replace("\"", "").replace("'", "").replace("\r", "").replace("\n", "");
            String[] parts = cleanString.split("\\.");
            if (parts.length != 2) return null;

            String payloadBase64 = parts[0];
            String signatureBase64 = parts[1];

            byte[] payloadBytes = decodeBase64Safe(payloadBase64);
            String payloadJson = new String(payloadBytes, StandardCharsets.UTF_8);

            LicensePayload payload = LicensePayload.fromJson(payloadJson);
            if (payload == null) return null;

            // Check expiration timestamp
            if (payload.isExpired()) {
                Log.w(TAG, "License key has expired: " + payload.getExpires());
                return null;
            }

            // Retrieve corresponding public key for this version
            String publicKeyBase64 = KEYRING.get(payload.getVersion());
            if (publicKeyBase64 == null) return null;

            byte[] publicKeyBytes = decodeBase64Safe(publicKeyBase64);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            byte[] rawSigBytes = decodeBase64Safe(signatureBase64);
            byte[] derSigBytes = (rawSigBytes.length == 64) ? p1363ToDer(rawSigBytes) : rawSigBytes;

            // Primary: Verify against decoded JSON bytes
            Signature sig = Signature.getInstance("SHA256withECDSA");
            sig.initVerify(publicKey);
            sig.update(payloadBytes);

            if (sig.verify(derSigBytes)) {
                Log.i(TAG, "License successfully verified via ECDSA signature for " + payload.getEmail());
                return payload;
            }

            // Fallback: Verify against raw payloadBase64 string bytes
            Signature sigFallback = Signature.getInstance("SHA256withECDSA");
            sigFallback.initVerify(publicKey);
            sigFallback.update(payloadBase64.getBytes(StandardCharsets.UTF_8));

            if (sigFallback.verify(derSigBytes)) {
                Log.i(TAG, "License successfully verified (fallback) for " + payload.getEmail());
                return payload;
            }
        } catch (Exception ex) {
            Log.w(TAG, "License verification exception: " + ex.getMessage());
        }
        return null;
    }

    /**
     * Stores and activates a validated license key locally.
     */
    public static boolean saveLicense(Context context, String licenseString) {
        LicensePayload payload = verifyLicense(licenseString);
        if (payload == null) return false;

        SharedPreferences prefs = context.getSharedPreferences(PREF_LICENSE, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_LICENSE_STRING, licenseString.trim())
                .putString(KEY_LICENSE_PAYLOAD, payload.toJson())
                .apply();

        // Also notify legacy IAB flags
        IAB.setBought(ActivityPro.SKU_PRO1, context);
        IAB.setBought(ActivityPro.SKU_DONATION, context);
        return true;
    }

    /**
     * Retrieves currently active verified license payload, or null if unverified / expired.
     */
    public static LicensePayload getActiveLicense(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_LICENSE, Context.MODE_PRIVATE);
        String licenseString = prefs.getString(KEY_LICENSE_STRING, null);
        if (licenseString == null) return null;

        LicensePayload payload = verifyLicense(licenseString);
        if (payload == null) {
            // Invalidate corrupt/expired key
            prefs.edit().clear().apply();
        }
        return payload;
    }

    /**
     * Checks if the app currently has active, valid Pro status.
     */
    public static boolean isLicensed(Context context) {
        LicensePayload active = getActiveLicense(context);
        return active != null && !active.isExpired();
    }

    /**
     * Clears and deactivates the current license.
     */
    public static void clearLicense(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_LICENSE, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    /**
     * Decodes standard Base64, URL-safe Base64, with padding adjustment.
     */
    private static byte[] decodeBase64Safe(String str) {
        String sanitized = str.trim().replace('-', '+').replace('_', '/');
        int remainder = sanitized.length() % 4;
        if (remainder > 0) {
            StringBuilder sb = new StringBuilder(sanitized);
            for (int i = 0; i < (4 - remainder); i++) {
                sb.append('=');
            }
            sanitized = sb.toString();
        }
        try {
            return java.util.Base64.getDecoder().decode(sanitized);
        } catch (Throwable t) {
            return Base64.decode(sanitized, Base64.DEFAULT);
        }
    }

    /**
     * Converts IEEE P1363 64-byte raw signature (r[32] || s[32]) from WebCrypto/Deno
     * into ASN.1 DER format expected by Java's SHA256withECDSA verifier.
     */
    private static byte[] p1363ToDer(byte[] signature) {
        if (signature == null || signature.length != 64) {
            return signature; // Already ASN.1 DER formatted
        }
        byte[] r = extractUnsignedArray(signature, 0, 32);
        byte[] s = extractUnsignedArray(signature, 32, 32);

        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        baos.write(0x02); // ASN.1 INTEGER tag
        baos.write(r.length);
        baos.write(r, 0, r.length);
        baos.write(0x02); // ASN.1 INTEGER tag
        baos.write(s.length);
        baos.write(s, 0, s.length);

        byte[] body = baos.toByteArray();
        java.io.ByteArrayOutputStream der = new java.io.ByteArrayOutputStream();
        der.write(0x30); // ASN.1 SEQUENCE tag
        der.write(body.length);
        der.write(body, 0, body.length);
        return der.toByteArray();
    }

    private static byte[] extractUnsignedArray(byte[] src, int offset, int len) {
        int start = offset;
        while (start < offset + len && src[start] == 0) {
            start++;
        }
        boolean needsPad = (start < offset + len) && ((src[start] & 0x80) != 0);
        int bodyLen = (offset + len) - start;
        byte[] res = new byte[bodyLen + (needsPad ? 1 : 0)];
        if (needsPad) {
            res[0] = 0x00;
            System.arraycopy(src, start, res, 1, bodyLen);
        } else {
            System.arraycopy(src, start, res, 0, bodyLen);
        }
        return res;
    }
}
