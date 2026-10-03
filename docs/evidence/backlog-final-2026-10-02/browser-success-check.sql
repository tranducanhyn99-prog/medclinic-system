set nocount on;
declare @appointment bigint = (select id from appointments where booking_code='MED-261002-526A4F99');
if @appointment is null or not exists (select 1 from appointments where id=@appointment and status='COMPLETED')
    throw 51000, 'DEMO appointment was not completed', 1;
if (select count(*) from medical_records where appointment_id=@appointment)<>1
    throw 51000, 'Unexpected medical record count', 1;
if (select count(*) from invoices where appointment_id=@appointment and payment_status='UNPAID' and consultation_fee=350000 and medicine_fee=3000 and total_amount=353000)<>1
    throw 51000, 'Incorrect unpaid invoice', 1;
if (select stock_quantity from medicines where id=1)<>1996
    throw 51000, 'Incorrect stock deduction', 1;
if (select count(*) from inventory_transactions where reference_type='APPOINTMENT' and reference_id=@appointment and quantity_change=-2 and stock_before=1998 and stock_after=1996)<>1
    throw 51000, 'Unexpected inventory transaction', 1;
if (select count(*) from prescription_items pi join prescriptions p on p.id=pi.prescription_id join medical_records r on r.id=p.medical_record_id
    where r.appointment_id=@appointment and pi.quantity=1 and pi.unit_price_at_time=1500 and pi.instructions=N''
    and ((pi.dosage=N'DEMO 1 viên, sau ăn' and pi.frequency=N'DEMO sáng, tối')
        or (pi.dosage=N'DEMO 1 viên, với nước' and pi.frequency=N'DEMO trưa, tối')))<>2
    throw 51000, 'Prescription text or row boundaries were changed', 1;
select 'PASS: completed appointment; exact prescription text; one unpaid invoice; stock and inventory match.' as acceptance_result;
