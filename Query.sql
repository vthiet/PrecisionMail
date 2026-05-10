create table if not exists accounts
(
    id                   integer primary key autoincrement,
    email                text not null,
    encrypt_app_password text not null,
    created_at           text not null,
    updated_at           text not null
);

CREATE TABLE IF NOT EXISTS emails
(
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    sender      TEXT NOT NULL,
    to_list     TEXT NOT NULL,
    cc_list     TEXT,
    bcc_list    TEXT,
    subject     TEXT,
    content     TEXT,
    attachments TEXT,
    sent_at  TEXT NOT NULL
);