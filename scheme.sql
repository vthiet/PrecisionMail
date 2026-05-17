create table if not exists accounts
(
    id integer primary key autoincrement,
    email text not null unique,
    encrypt_app_password text not null,
    smtp_host text not null default 'smtp.gmail.com',
    smtp_port integer not null default 587,
    imap_host text not null default 'imap.gmail.com',
    imap_port integer not null default 993,
    security_mode text not null default 'TLS',
    created_at text not null,
    updated_at text not null
);