package org.esupportail.activbo;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import static org.esupportail.activbo.Utils.*;


public class UtilsTest {

    @Test
    public void testEncryptSmbNTPassword() {
        var hash = encryptSmbNTPassword("foo");
        assertEquals(hash, "AC8E657F83DF82BEEA5D43BDAF7800CC");
    }

}