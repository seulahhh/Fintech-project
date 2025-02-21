CREATE TABLE users (
                       id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       name VARCHAR(255) NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       phone VARCHAR(255) NOT NULL,
                       create_at TIMESTAMP NOT NULL,
                       modified_at TIMESTAMP NOT NULL,
                       email VARCHAR(255) NOT NULL,
                       is_otp_registered BOOLEAN DEFAULT FALSE NOT NULL,
                       is_verified_email BOOLEAN DEFAULT FALSE NOT NULL,

                       CONSTRAINT user_email_unique UNIQUE (email)
);

CREATE TABLE account (
                         id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                         create_at TIMESTAMP NOT NULL,
                         modified_at TIMESTAMP NOT NULL,
                         account_number VARCHAR(255) NOT NULL,
                         balance BIGINT NOT NULL,
                         status SMALLINT NOT NULL,  -- TINYINT → SMALLINT (또는 INT)
                         user_id BIGINT NOT NULL,

                         CONSTRAINT FK_account_user
                             FOREIGN KEY (user_id) REFERENCES users (id),

                         CHECK (status BETWEEN 0 AND 1)
);

CREATE TABLE otp_secret_key (
                                id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                create_at TIMESTAMP NOT NULL,
                                modified_at TIMESTAMP NOT NULL,
                                secret_key VARCHAR(255) NOT NULL,
                                user_id BIGINT NOT NULL,

                                CONSTRAINT FK_otp_secret_key_user
                                    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE transactions (
                              id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                              amount BIGINT NOT NULL,
                              memo VARCHAR(255) NULL,
                              recipient_account_id BIGINT NULL,
                              transaction_date DATE NOT NULL,
                              transaction_time TIME NOT NULL,
                              transaction_type SMALLINT NOT NULL,  -- TINYINT → SMALLINT
                              account_id BIGINT NOT NULL,

                              CONSTRAINT FK_transactions_account
                                  FOREIGN KEY (account_id) REFERENCES account (id),

                              CHECK (transaction_type BETWEEN 0 AND 2)
);

CREATE TABLE archived_transactions (
                                       id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                       amount BIGINT NOT NULL,
                                       memo VARCHAR(255) NULL,
                                       recipient_account_id BIGINT NULL,
                                       transaction_date DATE NOT NULL,
                                       transaction_time TIME NOT NULL,
                                       transaction_type SMALLINT NOT NULL, -- TINYINT → SMALLINT
                                       account_id BIGINT NOT NULL,

                                       CHECK (transaction_type BETWEEN 0 AND 2)
);