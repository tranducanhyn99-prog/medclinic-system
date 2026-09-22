-- Email OTP authentication and profile security support.
-- Existing accounts remain email-verified; newly registered users are created unverified by the application.

alter table users
    add email_verified bit not null
        constraint DF_users_email_verified default 1;

alter table users
    add otp_bypass bit not null
        constraint DF_users_otp_bypass default 0;

create table auth_verification_challenges (
    id bigint identity not null,
    user_id bigint not null,
    type nvarchar(20) not null,
    code_hash nvarchar(100) not null,
    magic_token_hash nvarchar(64) not null,
    expires_at datetime2(6) not null,
    consumed_at datetime2(6),
    attempts int not null constraint DF_auth_challenge_attempts default 0,
    created_at datetime2(6) not null,
    primary key (id)
);

alter table auth_verification_challenges
    add constraint CK_auth_challenge_type
        check (type in ('REGISTRATION','LOGIN'));

alter table auth_verification_challenges
    add constraint CK_auth_challenge_attempts
        check (attempts >= 0 and attempts <= 5);

alter table auth_verification_challenges
    add constraint FK_auth_challenge_user
        foreign key (user_id) references users(id);

alter table auth_verification_challenges
    add constraint UK_auth_challenge_magic_token
        unique (magic_token_hash);

create index idx_auth_challenge_magic_token
    on auth_verification_challenges (magic_token_hash);

create index idx_auth_challenge_user_created
    on auth_verification_challenges (user_id, created_at desc);
