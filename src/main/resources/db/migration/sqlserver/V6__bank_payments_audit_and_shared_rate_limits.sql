-- P2: auditable VietQR bank transfers and SQL Server-backed rate-limit buckets.

declare @payment_method_constraint sysname;
select top (1) @payment_method_constraint = cc.name
from sys.check_constraints cc
where cc.parent_object_id = object_id(N'invoices')
  and cc.definition like N'%payment_method%';

if @payment_method_constraint is not null
    exec(N'alter table invoices drop constraint [' + @payment_method_constraint + N']');

alter table invoices
    add constraint CK_invoices_payment_method
        check (payment_method in ('CASH','BANK_TRANSFER','VNPAY'));

GO

create table bank_transfer_payments (
    id bigint identity not null,
    invoice_id bigint not null,
    transfer_code nvarchar(25) not null,
    amount numeric(12,2) not null,
    bank_id nvarchar(20) not null,
    status nvarchar(20) not null,
    provider_event_id nvarchar(100),
    bank_transaction_id nvarchar(100),
    expires_at datetime2(6) not null,
    confirmed_at datetime2(6),
    created_at datetime2(6) not null,
    version bigint,
    primary key (id),
    constraint FK_bank_transfer_invoice foreign key (invoice_id) references invoices(id),
    constraint UK_bank_transfer_code unique (transfer_code),
    constraint CK_bank_transfer_status check (status in ('PENDING','CONFIRMED','REJECTED','EXPIRED')),
    constraint CK_bank_transfer_amount check (amount > 0)
);

create index idx_bank_transfer_invoice on bank_transfer_payments(invoice_id);
create index idx_bank_transfer_status_expiry on bank_transfer_payments(status, expires_at);
create unique index uk_bank_transfer_provider_event
    on bank_transfer_payments(provider_event_id) where provider_event_id is not null;
create unique index uk_bank_transfer_bank_transaction
    on bank_transfer_payments(bank_transaction_id) where bank_transaction_id is not null;

create table business_audit_events (
    id bigint identity not null,
    actor_id bigint,
    action nvarchar(60) not null,
    entity_type nvarchar(40) not null,
    entity_id nvarchar(100) not null,
    outcome nvarchar(20) not null,
    correlation_id nvarchar(100),
    detail_code nvarchar(100),
    created_at datetime2(6) not null,
    primary key (id),
    constraint FK_business_audit_actor foreign key (actor_id) references users(id)
);

create index idx_business_audit_entity on business_audit_events(entity_type, entity_id);
create index idx_business_audit_created on business_audit_events(created_at);

create table security_rate_limit_buckets (
    bucket_key nvarchar(180) not null,
    window_start datetime2(6) not null,
    request_count int not null,
    primary key (bucket_key),
    constraint CK_rate_limit_count check (request_count >= 0)
);

create index idx_rate_limit_window_start on security_rate_limit_buckets(window_start);
