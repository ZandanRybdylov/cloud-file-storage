package com.zandan.app.filestorage.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "t_users", schema = "public")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "c_login")
    private String login;

    @Column(name = "c_password")
    private String password;

    @Column(name = "c_role")
    @Enumerated(EnumType.STRING)
    private UserRole role;
}
