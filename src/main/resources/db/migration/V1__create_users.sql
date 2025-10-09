CREATE TABLE t_users (
    id SERIAL PRIMARY KEY,
    c_login VARCHAR(128) NOT NULL,
    c_password VARCHAR(128) NOT NULL,
    c_role VARCHAR(10) NOT NULL
);