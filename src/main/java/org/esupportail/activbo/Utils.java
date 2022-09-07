package org.esupportail.activbo;

import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.acegisecurity.providers.ldap.authenticator.LdapShaPasswordEncoder;

public class Utils {
    
    public static String removePrefixOrNull(String s, String prefix) {
        return s.startsWith(prefix) ? s.substring(prefix.length()) : null;
    }
    public static String removeSuffixOrNull(final String s, final String suffix) {
        return s.endsWith(suffix) ? s.substring(0, s.length() - suffix.length()) : null;
    }
    public static String removeSuffix(final String s, final String suffix) {
        return s.endsWith(suffix) ? s.substring(0, s.length() - suffix.length()) : s;
    }

    public static String[] toArray(List<String> l) {
        return l.toArray(new String[0]);
    }
    public static String[] toArray(Set<String> l) {
        return l.toArray(new String[0]);
    }
    public static Set<String> toSet(String[] l) {
        var r = new HashSet<String>();
        for (var e: l) r.add(e);
        return r;
    }

    public static <V> MapBuilder<V> asMap(String key, V value) {
        return new MapBuilder<V>().add(key, value);
    }

    public static class MapBuilder<V> extends HashMap<String, V> {
        public MapBuilder<V> add(String key, V value) {
            this.put(key, value);
            return this;
        }
    }

    public static <T,V> Set<V> mapSet(final Set<T> in, final Function<T, V> function) {
        return in == null ? null : in.stream().map(function).collect(Collectors.toSet());
    }

    public static List<String> encodeBase64s(List<? extends Object> vals) {
        var r = new LinkedList<String>();
        for (var val: vals) {
            r.add(new String(Base64.getEncoder().encode((byte[]) val)));
        }
        return r;
    }

    public static List<byte[]> decodeBase64s(List<String> vals) {
        var r = new LinkedList<byte[]>();
        for (var val: vals) {
            r.add(Base64.getDecoder().decode(val));
        }
        return r;
    }

    public static String ldapShaPasswordEncoder(String password) {
        return ldapShaPasswordEncoder(password, null);
    }

    public static String ldapShaPasswordEncoder(String password, byte[] salt) {
        /*
         * If we look at phpldapadmin SSHA encryption algorithm in :
         * /usr/share/phpldapadmin/lib/functions.php function password_hash(
         * $password_clear, $enc_type ) salt length for SSHA is 4
         */
        final int SALT_LENGTH = 4;
        
        /* Salt generation */
        if (salt == null) {
            salt = new byte[SALT_LENGTH];
            new Random().nextBytes(salt);
        }
        /* SSHA encoding */
        return new LdapShaPasswordEncoder().encodePassword(password, salt);
    }

    public static String encryptSmbNTPassword(String clearPassword) {
        var salt = new byte[4];
        new SecureRandom().nextBytes(salt);
        var md4 = new jcifs.util.MD4();
        md4.reset();
        md4.update(clearPassword.getBytes(Charset.forName("UTF-16LE")));
        return bytes_to_string(md4.digest()).toUpperCase();
    }

    public static String bytes_to_string(byte[] bytes) {
        var hashedPwd = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            int v = b & 0xff;
            if (v < 16) {
                hashedPwd.append('0');
            }
            hashedPwd.append(Integer.toHexString(v));
        }
        return hashedPwd.toString();
    }

}
