-- Fail rather than rounding historical catalog/accounting values automatically.
IF EXISTS (SELECT 1 FROM dbo.medicines WHERE unit_price < 0 OR unit_price <> FLOOR(unit_price))
   OR EXISTS (SELECT 1 FROM dbo.doctor_profiles WHERE consultation_fee < 0 OR consultation_fee <> FLOOR(consultation_fee))
   OR EXISTS (SELECT 1 FROM dbo.inventory_transactions WHERE unit_cost < 0 OR unit_cost <> FLOOR(unit_cost))
    THROW 51009, 'Fractional or negative VND catalog prices exist. Review and correct them explicitly before applying V9; existing invoices are unchanged.', 1;

ALTER TABLE dbo.medicines ADD CONSTRAINT ck_medicine_integral_vnd CHECK (unit_price >= 0 AND unit_price = FLOOR(unit_price));
ALTER TABLE dbo.doctor_profiles ADD CONSTRAINT ck_doctor_integral_vnd CHECK (consultation_fee >= 0 AND consultation_fee = FLOOR(consultation_fee));
ALTER TABLE dbo.inventory_transactions ADD CONSTRAINT ck_inventory_integral_vnd CHECK (unit_cost IS NULL OR (unit_cost >= 0 AND unit_cost = FLOOR(unit_cost)));
