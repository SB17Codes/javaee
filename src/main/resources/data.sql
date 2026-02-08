INSERT INTO campus VALUES ('Triolet','Montpellier');
INSERT INTO campus VALUES ('St Priest','Montpellier');
INSERT INTO campus VALUES ('Pharmacie','Montpellier');
INSERT INTO campus VALUES ('Richter','Montpellier');
INSERT INTO campus VALUES ('FDE Mende','Mende');
INSERT INTO campus VALUES ('Medecine Nimes','Nimes');


INSERT INTO composante VALUES ('FDS','Faculte des Sciences','JM. Marin');
INSERT INTO composante VALUES ('IAE','Ecole Universitaire de Management','E Houze');
INSERT INTO composante VALUES ('Polytech','Polytech Montpellier','L. Torres');

INSERT INTO app_user (username, password_hash, role, enabled, created_at)
VALUES ('admin', '$2a$10$K0wx2TYTEEPirM2tAKI8PuviT9uhwDhqSumVMPc4yVEieyBirR86C', 'ADMIN', true, NOW());
INSERT INTO app_user (username, password_hash, role, enabled, created_at)
VALUES ('manager', '$2a$10$CqSQks9F5Fl.yTDaDYRyAOjm3vTaddWJX17Izk5/Q4jgv8l1J6wjq', 'MANAGER', true, NOW());
INSERT INTO app_user (username, password_hash, role, enabled, created_at)
VALUES ('teacher', '$2a$10$enWTRX.uyYeJxXo/TOF.ee3/rkG1Kp0ECkjorJcUnZ9mk3KuwbKwW', 'TEACHER', true, NOW());
INSERT INTO app_user (username, password_hash, role, enabled, created_at)
VALUES ('student', '$2a$10$xS6Zqx.SIbKrxclr5ExoeO6vmZJDqSQQxQiOqXpyG12Q5XZBmt3qa', 'STUDENT', true, NOW());
