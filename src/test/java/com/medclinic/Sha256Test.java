package com.medclinic;

import com.medclinic.service.EmailDeliveryMonitor;
import com.medclinic.util.Sha256;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Sha256Test {
    @Test
    void hashesKnownVectorsAndPreservesEmailFingerprints() {
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", Sha256.hex(""));
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", Sha256.hex("abc"));
        assertEquals(Sha256.hex("patient@example.com").substring(0, 12),
                EmailDeliveryMonitor.recipientFingerprint("  PATIENT@example.com  "));
        assertEquals(Sha256.hex("unknown").substring(0, 12), EmailDeliveryMonitor.recipientFingerprint(null));
    }
}
