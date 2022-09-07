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

    @Test
    public void testLdapShaPasswordEncoder() {
        var hash = ldapShaPasswordEncoder("foo", "0000".getBytes());
        assertEquals(hash, "{SSHA}i90z2uOIA+5F7C0amoArooW546swMDAw");
    }

}