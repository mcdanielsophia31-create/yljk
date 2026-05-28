USE hospital_management;
UPDATE user SET password = '$2a$10$x6X4SC9muULJZnZ.RcJrauozgAH51fhI9HLX89oe4McPfuvB0HTUW' WHERE username = 'patient3';
SELECT id, username, password FROM user WHERE username = 'patient3';