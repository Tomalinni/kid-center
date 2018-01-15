CREATE TABLE account
(
    id BIGINT PRIMARY KEY NOT NULL,
    number VARCHAR(255),
    school_id BIGINT,
    bank VARCHAR(255),
    department VARCHAR(255),
    owner VARCHAR(255),
    city_id BIGINT
);
CREATE TABLE apppermission
(
    id INTEGER PRIMARY KEY NOT NULL
);
CREATE TABLE approle
(
    id VARCHAR(255) PRIMARY KEY NOT NULL
);
CREATE TABLE approle_apppermission
(
    approle_id VARCHAR(255) NOT NULL,
    permissions_id INTEGER NOT NULL,
    CONSTRAINT approle_apppermission_pkey PRIMARY KEY (approle_id, permissions_id)
);
CREATE TABLE appuser
(
    id VARCHAR(255) PRIMARY KEY NOT NULL,
    name VARCHAR(255),
    pass VARCHAR(255)
);
CREATE TABLE appuser_approle
(
    appuser_id VARCHAR(255) NOT NULL,
    roles_id VARCHAR(255) NOT NULL,
    CONSTRAINT appuser_approle_pkey PRIMARY KEY (appuser_id, roles_id)
);
CREATE TABLE card
(
    id BIGINT PRIMARY KEY NOT NULL,
    allowedsubjectsmask INTEGER NOT NULL,
    cancelslimit INTEGER NOT NULL,
    changeslimit INTEGER NOT NULL,
    creationdate DATE,
    durationdays INTEGER NOT NULL,
    expirationdate DATE,
    lessonslimit INTEGER NOT NULL,
    maxdiscount INTEGER NOT NULL,
    maxsalescount INTEGER NOT NULL,
    price INTEGER NOT NULL,
    suspendslimit INTEGER NOT NULL,
    visittype INTEGER,
    active BOOLEAN NOT NULL,
    durationdaysmax INTEGER NOT NULL
);
CREATE TABLE category
(
    id BIGINT PRIMARY KEY NOT NULL,
    level INTEGER NOT NULL,
    name VARCHAR(255),
    parent_id BIGINT
);
CREATE TABLE city
(
    id BIGINT PRIMARY KEY NOT NULL,
    name VARCHAR(255)
);
CREATE TABLE kindergarden
(
    id BIGINT PRIMARY KEY NOT NULL,
    address VARCHAR(255),
    name VARCHAR(255),
    phone VARCHAR(255)
);
CREATE TABLE lessonslot
(
    id VARCHAR(255) PRIMARY KEY NOT NULL,
    datetime TIMESTAMP,
    subject INTEGER,
    status INTEGER
);
CREATE TABLE lessontemplate
(
    id BIGINT PRIMARY KEY NOT NULL,
    enddate DATE,
    name VARCHAR(255),
    startdate DATE
);
CREATE TABLE payment
(
    id BIGINT PRIMARY KEY NOT NULL,
    comment VARCHAR(255),
    date DATE,
    price NUMERIC(19,2),
    producturl VARCHAR(255),
    account_id BIGINT,
    category_id BIGINT,
    category2_id BIGINT,
    category3_id BIGINT,
    category4_id BIGINT,
    category5_id BIGINT,
    monthdate DATE,
    productphotoscount INTEGER NOT NULL DEFAULT 0,
    receiptphotoscount INTEGER NOT NULL DEFAULT 0
);
CREATE TABLE school
(
    id BIGINT PRIMARY KEY NOT NULL,
    name VARCHAR(255),
    city_id BIGINT
);
CREATE TABLE student
(
    id BIGINT PRIMARY KEY NOT NULL,
    advisedby VARCHAR(255),
    birthdate DATE,
    businessid VARCHAR(255),
    gender INTEGER,
    istrial BOOLEAN NOT NULL,
    kindergarden VARCHAR(255),
    mobile VARCHAR(255),
    namecn VARCHAR(255),
    nameen VARCHAR(255),
    primaryphotoname VARCHAR(255),
    comment VARCHAR(255),
    kindergarden_id BIGINT
);
CREATE TABLE student_studentrelative
(
    student_id BIGINT NOT NULL,
    relatives_id BIGINT NOT NULL
);
CREATE TABLE studentcard
(
    id BIGINT PRIMARY KEY NOT NULL,
    activationdate DATE,
    cancelsavailable INTEGER NOT NULL,
    changesavailable INTEGER NOT NULL,
    lessonsavailable INTEGER NOT NULL,
    suspendsavailable INTEGER NOT NULL,
    visittype INTEGER,
    student_id BIGINT,
    cancelslimit INTEGER NOT NULL,
    changeslimit INTEGER NOT NULL,
    lessonslimit INTEGER NOT NULL,
    suspendslimit INTEGER NOT NULL,
    durationdays INTEGER NOT NULL,
    price INTEGER NOT NULL
);
CREATE TABLE studentrelative
(
    id BIGINT PRIMARY KEY NOT NULL,
    driverlicense VARCHAR(255),
    mail VARCHAR(255),
    mobile VARCHAR(255),
    name VARCHAR(255),
    passport VARCHAR(255),
    role VARCHAR(255),
    primaryphotoname VARCHAR(255),
    comment VARCHAR(255)
);
CREATE TABLE studentrelativerole
(
    id BIGINT PRIMARY KEY NOT NULL,
    name VARCHAR(255)
);
CREATE TABLE studentslot
(
    id BIGINT PRIMARY KEY NOT NULL,
    status INTEGER,
    visittype INTEGER,
    card_id BIGINT,
    lessonslot_id VARCHAR(255),
    student_id BIGINT
);
CREATE TABLE teacher
(
    id BIGINT PRIMARY KEY NOT NULL,
    name VARCHAR(255)
);
CREATE TABLE templatelessonslot
(
    id BIGINT PRIMARY KEY NOT NULL,
    agegroup INTEGER,
    day INTEGER NOT NULL,
    frommins INTEGER NOT NULL,
    subject INTEGER,
    lesson_template_id BIGINT
);
ALTER TABLE account ADD FOREIGN KEY (school_id) REFERENCES school (id);
ALTER TABLE account ADD FOREIGN KEY (city_id) REFERENCES city (id);
ALTER TABLE approle_apppermission ADD FOREIGN KEY (approle_id) REFERENCES approle (id);
ALTER TABLE approle_apppermission ADD FOREIGN KEY (permissions_id) REFERENCES apppermission (id);
ALTER TABLE appuser_approle ADD FOREIGN KEY (appuser_id) REFERENCES appuser (id);
ALTER TABLE appuser_approle ADD FOREIGN KEY (roles_id) REFERENCES approle (id);
ALTER TABLE category ADD FOREIGN KEY (parent_id) REFERENCES category (id);
ALTER TABLE payment ADD FOREIGN KEY (account_id) REFERENCES account (id);
ALTER TABLE payment ADD FOREIGN KEY (category_id) REFERENCES category (id);
ALTER TABLE payment ADD FOREIGN KEY (category_id) REFERENCES category (id);
ALTER TABLE payment ADD FOREIGN KEY (category2_id) REFERENCES category (id);
ALTER TABLE payment ADD FOREIGN KEY (category2_id) REFERENCES category (id);
ALTER TABLE payment ADD FOREIGN KEY (category3_id) REFERENCES category (id);
ALTER TABLE payment ADD FOREIGN KEY (category3_id) REFERENCES category (id);
ALTER TABLE payment ADD FOREIGN KEY (category4_id) REFERENCES category (id);
ALTER TABLE payment ADD FOREIGN KEY (category4_id) REFERENCES category (id);
ALTER TABLE payment ADD FOREIGN KEY (category5_id) REFERENCES category (id);
ALTER TABLE payment ADD FOREIGN KEY (category5_id) REFERENCES category (id);
ALTER TABLE school ADD FOREIGN KEY (city_id) REFERENCES city(id);
ALTER TABLE student ADD FOREIGN KEY (kindergarden_id) REFERENCES kindergarden (id);
ALTER TABLE student_studentrelative ADD FOREIGN KEY (student_id) REFERENCES student (id);
ALTER TABLE student_studentrelative ADD FOREIGN KEY (relatives_id) REFERENCES studentrelative (id);
CREATE UNIQUE INDEX uk_student_studentrelative ON student_studentrelative (relatives_id);
ALTER TABLE studentcard ADD FOREIGN KEY (student_id) REFERENCES student (id);
ALTER TABLE studentslot ADD FOREIGN KEY (card_id) REFERENCES studentcard (id);
ALTER TABLE studentslot ADD FOREIGN KEY (lessonslot_id) REFERENCES lessonslot (id);
ALTER TABLE studentslot ADD FOREIGN KEY (student_id) REFERENCES student (id);
ALTER TABLE templatelessonslot ADD FOREIGN KEY (lesson_template_id) REFERENCES lessontemplate (id);

CREATE SEQUENCE account_seq;
CREATE SEQUENCE card_seq;
CREATE SEQUENCE category_seq;
CREATE SEQUENCE city_seq;
CREATE SEQUENCE hibernate_sequence;
CREATE SEQUENCE kinder_garden_seq;
CREATE SEQUENCE lesson_template_seq;
CREATE SEQUENCE payment_seq;
CREATE SEQUENCE regular_id_seq;
CREATE SEQUENCE school_seq;
CREATE SEQUENCE student_card_seq;
CREATE SEQUENCE student_relative_role_seq;
CREATE SEQUENCE template_lesson_slot_seq;
CREATE SEQUENCE trial_id_seq;