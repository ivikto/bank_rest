--liquibase formatted sql

--changeset ivikto:2

INSERT INTO users (name, email, role, is_active, password_hash, created_at, modified_at, version, dtype)
SELECT 'Admin'                                                        AS name,
       'admin@bank.local'                                             AS email,
       'ADMIN'                                                        AS role,
       TRUE                                                           AS is_active,
       '$2a$12$iut2Ymv2kOxQjULKWKXhDObV9JzpZ.qo2KmUG1KpH4gpVBrS.0AiC' AS password_hash,
       CURRENT_TIMESTAMP                                              AS created_at,
       CURRENT_TIMESTAMP                                              AS modified_at,
       0                                                              AS version,
       'STANDARD'                                                     AS dtype
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@bank.local');

INSERT INTO users (name, email, role, is_active, password_hash, created_at, modified_at, version, dtype)
SELECT 'ivikto'                                                        AS name,
       'ivikto@yandex.ru'                                             AS email,
       'USER'                                                        AS role,
       TRUE                                                           AS is_active,
       '$2a$12$pJaf9BUDqCeGy/qhLFY1qual5r5JMr3sa37kLh30hUxk/zkpKcoWC' AS password_hash,
       CURRENT_TIMESTAMP                                              AS created_at,
       CURRENT_TIMESTAMP                                              AS modified_at,
       0                                                              AS version,
       'STANDARD'                                                     AS dtype
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'ivikto@yandex.ru');

