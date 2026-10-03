set nocount on;
select a.id, a.booking_code, a.status,
    (select count(*) from medical_records r where r.appointment_id=a.id) as medical_records,
    (select count(*) from invoices i where i.appointment_id=a.id) as invoices,
    (select count(*) from inventory_transactions t where t.reference_type='APPOINTMENT' and t.reference_id=a.id) as inventory_entries
from appointments a where a.booking_code='MED-261002-526A4F99';
select id, name, stock_quantity from medicines where id=1;
select pi.quantity, pi.dosage, pi.frequency, pi.instructions, pi.unit_price_at_time
from prescription_items pi join prescriptions p on p.id=pi.prescription_id
join medical_records r on r.id=p.medical_record_id join appointments a on a.id=r.appointment_id
where a.booking_code='MED-261002-526A4F99';
select i.invoice_code, i.consultation_fee, i.medicine_fee, i.total_amount, i.payment_status
from invoices i join appointments a on a.id=i.appointment_id where a.booking_code='MED-261002-526A4F99';
