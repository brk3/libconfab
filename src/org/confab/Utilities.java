/*
 * Copyright (C) 2011 Paul Bourke <pauldbourke@gmail.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.confab;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;

import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.List;
import java.util.ArrayList;

public class Utilities {

    private static final boolean DEBUG_ON = true;

    public static void printCookieStore(CookieStore cookieStore) {
        debug("printCookies");
        List<Cookie> cookies = cookieStore.getCookies();
        if (cookies.isEmpty()) {
            System.out.println("None");
        } else {
            for (int i = 0; i < cookies.size(); i++) {
                System.out.println("- " + cookies.get(i).toString());
            }
        }
        debug("end printCookies");
    }

    public static String md5(String pass) {
        String md5 = "";
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            byte[] data = pass.getBytes(); 
            m.update(data, 0, data.length);
            BigInteger i = new BigInteger(1, m.digest());
            md5 = String.format("%1$032x", i);
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e);
        } 
        return md5;
    }

    public static long unixTime() {
        return System.currentTimeMillis() / 1000L;
    }

    public static void debug(String msg) {
        //TODO: add levels of debug
        if (DEBUG_ON) {
            System.out.println("Debug: " + msg);
        }
    }
}
