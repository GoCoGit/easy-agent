CREATE TABLE "t_user" (
                        id BIGSERIAL PRIMARY KEY,
                        phone VARCHAR(64) NOT NULL,
                        email VARCHAR(64),
                        avatar VARCHAR(64) ,
                        password_salt VARCHAR(10) NOT NULL,
                        password VARCHAR(64) NOT NULL,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_phone ON "t_user"(phone);