CREATE TABLE users
(
    id                BIGINT AUTO_INCREMENT
        PRIMARY KEY,
    name              VARCHAR(255)     NOT NULL,
    password          VARCHAR(255)     NOT NULL,
    phone             VARCHAR(255)     NOT NULL,
    create_at         DATETIME         NOT NULL,
    modified_at       DATETIME         NOT NULL,
    email             VARCHAR(255)     NOT NULL,
    is_otp_registered BIT DEFAULT 0 NOT NULL,
    is_verified_email BIT DEFAULT 0 NOT NULL,

    CONSTRAINT UNIQUE (email)
);

CREATE TABLE accounts
(
    id             BIGINT AUTO_INCREMENT
        PRIMARY KEY,
    create_at      DATETIME     NOT NULL,
    modified_at    DATETIME     NOT NULL,
    account_number VARCHAR(255) NOT NULL,
    balance        BIGINT       NOT NULL,
    status         VARCHAR(50)  NOT NULL,
    user_id        BIGINT       NOT NULL,
    CONSTRAINT FK_account_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE otp_secret_keys
(
    id          BIGINT AUTO_INCREMENT
        PRIMARY KEY,
    create_at   DATETIME(6)  NOT NULL,
    modified_at DATETIME(6)  NOT NULL,
    secret_key  VARCHAR(255) NOT NULL,
    user_id     BIGINT       NOT NULL,
    CONSTRAINT FK_otp_secret_key_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE transactions
(
    id                   BIGINT AUTO_INCREMENT
        PRIMARY KEY,
    amount               BIGINT       NOT NULL,
    memo                 VARCHAR(255) NULL,
    recipient_account_id BIGINT       NULL,
    transaction_date     DATE         NOT NULL,
    transaction_time     TIME         NOT NULL,
    transaction_type     VARCHAR(50)  NOT NULL,
    account_id           BIGINT       NOT NULL,

    CONSTRAINT FK_transactions_account FOREIGN KEY (account_id) REFERENCES accounts (id)
);

CREATE TABLE archived_transactions
(
    id                   BIGINT AUTO_INCREMENT
        PRIMARY KEY,
    amount               BIGINT       NOT NULL,
    memo                 VARCHAR(255) NULL,
    recipient_account_id BIGINT       NULL,
    transaction_date     DATE         NOT NULL,
    transaction_time     TIME         NOT NULL,
    transaction_type     VARCHAR(50)  NOT NULL,
    account_id           BIGINT       NOT NULL
);


