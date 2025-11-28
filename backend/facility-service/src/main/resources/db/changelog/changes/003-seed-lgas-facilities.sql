INSERT INTO organisation (id, name, code, parent_id, organisation_type, address, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'Ikeja LGA', 'NG-LA-IKE', (SELECT id FROM organisation WHERE code = 'NG-LA'), 'LGA', 'Ikeja, Lagos', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Lagos Island LGA', 'NG-LA-ISL', (SELECT id FROM organisation WHERE code = 'NG-LA'), 'LGA', 'Lagos Island, Lagos', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Lagos Mainland LGA', 'NG-LA-MAI', (SELECT id FROM organisation WHERE code = 'NG-LA'), 'LGA', 'Lagos Mainland, Lagos', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Surulere LGA', 'NG-LA-SUR', (SELECT id FROM organisation WHERE code = 'NG-LA'), 'LGA', 'Surulere, Lagos', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Alimosho LGA', 'NG-LA-ALI', (SELECT id FROM organisation WHERE code = 'NG-LA'), 'LGA', 'Alimosho, Lagos', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- LGAs for Kano State
INSERT INTO organisation (id, name, code, parent_id, organisation_type, address, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'Kano Municipal LGA', 'NG-KN-MUN', (SELECT id FROM organisation WHERE code = 'NG-KN'), 'LGA', 'Kano Municipal, Kano', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Nassarawa LGA', 'NG-KN-NAS', (SELECT id FROM organisation WHERE code = 'NG-KN'), 'LGA', 'Nassarawa, Kano', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Fagge LGA', 'NG-KN-FAG', (SELECT id FROM organisation WHERE code = 'NG-KN'), 'LGA', 'Fagge, Kano', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- LGAs for Rivers State
INSERT INTO organisation (id, name, code, parent_id, organisation_type, address, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'Port Harcourt LGA', 'NG-RI-PHC', (SELECT id FROM organisation WHERE code = 'NG-RI'), 'LGA', 'Port Harcourt, Rivers', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Obio-Akpor LGA', 'NG-RI-OAK', (SELECT id FROM organisation WHERE code = 'NG-RI'), 'LGA', 'Obio-Akpor, Rivers', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Eleme LGA', 'NG-RI-ELE', (SELECT id FROM organisation WHERE code = 'NG-RI'), 'LGA', 'Eleme, Rivers', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- LGAs for Abuja FCT
INSERT INTO organisation (id, name, code, parent_id, organisation_type, address, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'Abuja Municipal Area Council', 'NG-FC-AMAC', (SELECT id FROM organisation WHERE code = 'NG-FC'), 'LGA', 'Central Abuja, FCT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Gwagwalada Area Council', 'NG-FC-GWA', (SELECT id FROM organisation WHERE code = 'NG-FC'), 'LGA', 'Gwagwalada, FCT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Bwari Area Council', 'NG-FC-BWA', (SELECT id FROM organisation WHERE code = 'NG-FC'), 'LGA', 'Bwari, FCT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Facilities for Ikeja LGA (Lagos)
INSERT INTO organisation (id, name, code, parent_id, organisation_type, address, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'Lagos State University Teaching Hospital', 'FAC-LA-LASUTH', (SELECT id FROM organisation WHERE code = 'NG-LA-IKE'), 'FACILITY', 'Ikeja, Lagos State', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Ikeja Primary Health Centre', 'FAC-LA-IKPHC', (SELECT id FROM organisation WHERE code = 'NG-LA-IKE'), 'FACILITY', 'Ikeja GRA, Lagos', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Allen Avenue Medical Centre', 'FAC-LA-ALLEN', (SELECT id FROM organisation WHERE code = 'NG-LA-IKE'), 'FACILITY', 'Allen Avenue, Ikeja, Lagos', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Facilities for Lagos Island LGA
INSERT INTO organisation (id, name, code, parent_id, organisation_type, address, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'Lagos Island General Hospital', 'FAC-LA-LIGHC', (SELECT id FROM organisation WHERE code = 'NG-LA-ISL'), 'FACILITY', 'Marina, Lagos Island', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Lagos Island Maternity Hospital', 'FAC-LA-LIMAT', (SELECT id FROM organisation WHERE code = 'NG-LA-ISL'), 'FACILITY', 'Broad Street, Lagos Island', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Facilities for Surulere LGA
INSERT INTO organisation (id, name, code, parent_id, organisation_type, address, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'Randle General Hospital', 'FAC-LA-RANDLE', (SELECT id FROM organisation WHERE code = 'NG-LA-SUR'), 'FACILITY', 'Surulere, Lagos', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Shitta Primary Health Centre', 'FAC-LA-SHITTA', (SELECT id FROM organisation WHERE code = 'NG-LA-SUR'), 'FACILITY', 'Shitta, Surulere, Lagos', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Facilities for Kano Municipal LGA
INSERT INTO organisation (id, name, code, parent_id, organisation_type, address, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'Murtala Muhammad Specialist Hospital', 'FAC-KN-MMSH', (SELECT id FROM organisation WHERE code = 'NG-KN-MUN'), 'FACILITY', 'Kano Municipal, Kano', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Kano Municipal Primary Health Centre', 'FAC-KN-MUNPHC', (SELECT id FROM organisation WHERE code = 'NG-KN-MUN'), 'FACILITY', 'Central Kano', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Sabon Gari Health Clinic', 'FAC-KN-SGCL', (SELECT id FROM organisation WHERE code = 'NG-KN-MUN'), 'FACILITY', 'Sabon Gari, Kano', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Facilities for Nassarawa LGA (Kano)
INSERT INTO organisation (id, name, code, parent_id, organisation_type, address, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'Nassarawa General Hospital', 'FAC-KN-NASGH', (SELECT id FROM organisation WHERE code = 'NG-KN-NAS'), 'FACILITY', 'Nassarawa, Kano', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Nassarawa Primary Health Centre', 'FAC-KN-NASPHC', (SELECT id FROM organisation WHERE code = 'NG-KN-NAS'), 'FACILITY', 'Nassarawa LGA, Kano', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Facilities for Port Harcourt LGA
INSERT INTO organisation (id, name, code, parent_id, organisation_type, address, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'University of Port Harcourt Teaching Hospital', 'FAC-RI-UPTH', (SELECT id FROM organisation WHERE code = 'NG-RI-PHC'), 'FACILITY', 'Port Harcourt, Rivers State', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Braithwaite Memorial Hospital', 'FAC-RI-BMH', (SELECT id FROM organisation WHERE code = 'NG-RI-PHC'), 'FACILITY', 'Port Harcourt, Rivers', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Military Hospital Port Harcourt', 'FAC-RI-MHPH', (SELECT id FROM organisation WHERE code = 'NG-RI-PHC'), 'FACILITY', 'Borikiri, Port Harcourt', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Facilities for Obio-Akpor LGA
INSERT INTO organisation (id, name, code, parent_id, organisation_type, address, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'Rumuokoro Primary Health Centre', 'FAC-RI-RUMPHC', (SELECT id FROM organisation WHERE code = 'NG-RI-OAK'), 'FACILITY', 'Rumuokoro, Obio-Akpor', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Choba Community Health Centre', 'FAC-RI-CHOBA', (SELECT id FROM organisation WHERE code = 'NG-RI-OAK'), 'FACILITY', 'Choba, Obio-Akpor', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Facilities for Abuja Municipal Area Council
INSERT INTO organisation (id, name, code, parent_id, organisation_type, address, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'National Hospital Abuja', 'FAC-FC-NHA', (SELECT id FROM organisation WHERE code = 'NG-FC-AMAC'), 'FACILITY', 'Central Business District, Abuja', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Garki General Hospital', 'FAC-FC-GARKI', (SELECT id FROM organisation WHERE code = 'NG-FC-AMAC'), 'FACILITY', 'Garki, Abuja', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Wuse General Hospital', 'FAC-FC-WUSE', (SELECT id FROM organisation WHERE code = 'NG-FC-AMAC'), 'FACILITY', 'Wuse District, Abuja', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Asokoro District Hospital', 'FAC-FC-ASOK', (SELECT id FROM organisation WHERE code = 'NG-FC-AMAC'), 'FACILITY', 'Asokoro, Abuja', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Facilities for Gwagwalada Area Council
INSERT INTO organisation (id, name, code, parent_id, organisation_type, address, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'University of Abuja Teaching Hospital', 'FAC-FC-UATH', (SELECT id FROM organisation WHERE code = 'NG-FC-GWA'), 'FACILITY', 'Gwagwalada, Abuja', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Gwagwalada General Hospital', 'FAC-FC-GWAGH', (SELECT id FROM organisation WHERE code = 'NG-FC-GWA'), 'FACILITY', 'Gwagwalada, FCT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Facilities for Bwari Area Council
INSERT INTO organisation (id, name, code, parent_id, organisation_type, address, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'Bwari General Hospital', 'FAC-FC-BWAGH', (SELECT id FROM organisation WHERE code = 'NG-FC-BWA'), 'FACILITY', 'Bwari, Abuja', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Kubwa General Hospital', 'FAC-FC-KUBWA', (SELECT id FROM organisation WHERE code = 'NG-FC-BWA'), 'FACILITY', 'Kubwa, Bwari Area Council', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);