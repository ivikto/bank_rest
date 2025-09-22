--liquibase formatted sql

--changeset ivikto:1

create table Users
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    modified_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    version      bigint not null default 0,
    password_hash VARCHAR(255) NOT NULL,


    CONSTRAINT uq_email UNIQUE (email)
);

CREATE TABLE cards
(
    id            BIGSERIAL PRIMARY KEY,
    num_encrypted TEXT           NOT NULL,
    num_last4     CHAR(4)        NOT NULL,
    num_hmac      VARCHAR(128)   NOT NULL,
    user_id       BIGINT         NOT NULL,
    expiration    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    cardStatus        VARCHAR(32)    NOT NULL,
    balance       NUMERIC(19, 2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    modified_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    version      bigint not null default 0,

    CONSTRAINT uq_cards_num_hmac UNIQUE (num_hmac),
    CONSTRAINT fk_cards_user FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_cards_last4_digits CHECK (num_last4 ~ '^[0-9]{4}$'
)
    );

CREATE SEQUENCE card_account_seq START WITH 100000000 INCREMENT BY 1;

CREATE UNIQUE INDEX ux_cards_num_hmac ON cards (num_hmac);

