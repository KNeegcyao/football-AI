package com.soccer.forum.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        // Current hash in database
        String hashedPassword = "$2a$10$2i1FHoo.xOBQuqY3C7/C6eT/DOsiG2t2.zwt662NAo9kjwegC3lOG";
        String rawPassword = "admin123";

        boolean matches = encoder.matches(rawPassword, hashedPassword);
        System.out.println("Password matches: " + matches);

        if (!matches) {
            // Generate a new hash for admin123
            String newHash = encoder.encode(rawPassword);
            System.out.println("New hash: " + newHash);
        }
    }
}
