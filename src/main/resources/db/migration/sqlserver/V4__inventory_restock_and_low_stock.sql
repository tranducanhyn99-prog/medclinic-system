-- Inventory replenishment and low-stock audit trail
-- Adds per-medicine reorder threshold and immutable stock movement history.

alter table medicines
    add reorder_level int not null
        constraint DF_medicines_reorder_level default 50;

alter table medicines
    add constraint CK_medicines_reorder_level_nonnegative
        check (reorder_level >= 0);

create table inventory_transactions (
    id bigint identity not null,
    medicine_id bigint not null,
    type nvarchar(20) not null,
    quantity_change int not null,
    stock_before int not null,
    stock_after int not null,
    unit_cost numeric(12,2),
    note nvarchar(255),
    reference_type nvarchar(30),
    reference_id bigint,
    performed_by_user_id bigint not null,
    created_at datetime2(6) not null,
    primary key (id)
);

alter table inventory_transactions
    add constraint CK_inventory_tx_type
        check (type in ('RESTOCK','DISPENSE','ADJUSTMENT'));

alter table inventory_transactions
    add constraint CK_inventory_tx_quantity_change
        check (quantity_change <> 0);

alter table inventory_transactions
    add constraint CK_inventory_tx_stock_nonnegative
        check (stock_before >= 0 and stock_after >= 0);

alter table inventory_transactions
    add constraint CK_inventory_tx_unit_cost_nonnegative
        check (unit_cost is null or unit_cost >= 0);

alter table inventory_transactions
    add constraint FK_inventory_tx_medicine
        foreign key (medicine_id) references medicines(id);

alter table inventory_transactions
    add constraint FK_inventory_tx_performed_by
        foreign key (performed_by_user_id) references users(id);

create index idx_inventory_tx_medicine_created
    on inventory_transactions (medicine_id, created_at desc);

create index idx_inventory_tx_created
    on inventory_transactions (created_at desc);
