CREATE INDEX idx_visits_patient_doctor_endDateTime
ON visits (patient_id, doctor_id, end_date_time DESC);
