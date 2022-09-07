package org.esupportail.activbo;

import java.util.Arrays;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import static org.esupportail.activbo.Utils.*;


public class UtilsTest {

    @Test
    public void testEncodeBase64s() {
        var hash = encodeBase64s(Arrays.asList(new Object[] { "foo".getBytes() }));
        assertArrayEquals(hash.toArray(), new String[] { "Zm9v" });
    }
    
    @Test
    public void testDecodeBase64s() {
        var hash = decodeBase64s(Arrays.asList(new String[] { "Zm9v" }));
        assertArrayEquals(hash.toArray(), new Object[] { "foo".getBytes() });
    } 

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