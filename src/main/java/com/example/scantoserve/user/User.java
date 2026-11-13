package com.example.scantoserve.user;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private String id;

    private String fullName;

    private String email;

    private String mobileNumber;

    private String password;

    private String role;

    private boolean enabled;

    private LocalDateTime createdAt;
}