CREATE TABLE admin_users (
 id uuid PRIMARY KEY, singleton boolean NOT NULL DEFAULT true UNIQUE CHECK(singleton),
 username varchar(100) NOT NULL UNIQUE, password_hash varchar(255) NOT NULL,
 enabled boolean NOT NULL DEFAULT true, created_at timestamptz NOT NULL, updated_at timestamptz NOT NULL
);
