create table if not exists accounts
(
    id integer primary key autoincrement,
    email text not null unique,
    display_name text,
    is_primary integer not null default 0,
    encrypt_app_password text not null,
    smtp_host text not null default 'smtp.gmail.com',
    smtp_port integer not null default 587,
    imap_host text not null default 'imap.gmail.com',
    imap_port integer not null default 993,
    security_mode text not null default 'TLS',
    smtp_security_mode text not null default 'TLS',
    imap_security_mode text not null default 'SSL',
    created_at text not null,
    updated_at text not null
);

create table if not exists recipient_groups
(
    id integer primary key autoincrement,
    name text not null unique,
    description text,
    emails text not null,
    email_count integer not null default 0,
    created_at text not null,
    updated_at text not null
);
