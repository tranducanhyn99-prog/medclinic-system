package com.medclinic;

import com.medclinic.util.QrCodeUtil;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class QrCodeUtilTest {

    @Test
    void generatesPngDataUriForValidContent() {
        String qr = QrCodeUtil.generateQrCodeBase64("MED-TEST-001", 160, 160);

        assertTrue(qr.startsWith("data:image/png;base64,"));
        byte[] png = Base64.getDecoder().decode(qr.substring("data:image/png;base64,".length()));
        assertTrue(png.length > 8);
        assertArrayEquals(new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47},
                new byte[]{png[0], png[1], png[2], png[3]});
    }

    @Test
    void returnsEmptyForMissingContent() {
        assertEquals("", QrCodeUtil.generateQrCodeBase64(null, 160, 160));
        assertEquals("", QrCodeUtil.generateQrCodeBase64("", 160, 160));
    }
}
