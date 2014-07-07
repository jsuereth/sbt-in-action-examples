# Kittens schema
 
# --- !Ups

CREATE SEQUENCE kitten_id_seq;
CREATE TABLE kitten (
    id integer NOT NULL DEFAULT nextval('kitten_id_seq'),
    name varchar(255)
);

CREATE SEQUENCE attribute_id_seq;
CREATE TABLE attribute (
    id integer NOT NULL DEFAULT nextval('attribute_id_seq'),
    label varchar(255)
);

CREATE SEQUENCE kitten_attribute_id_seq;
CREATE TABLE kitten_attribute (
    id integer NOT NULL DEFAULT nextval('kitten_attribute_id_seq'),
    kitten_id integer NOT NULL,
    attribute_id integer NOT NULL
);

INSERT INTO attribute(label) VALUES ('Tabby');
INSERT INTO attribute(label) VALUES ('Tuxedo');
INSERT INTO attribute(label) VALUES ('Harlequin');
INSERT INTO attribute(label) VALUES ('Tortoiseshell');
INSERT INTO attribute(label) VALUES ('Siamese');
INSERT INTO attribute(label) VALUES ('Alien');
INSERT INTO attribute(label) VALUES ('Rough');
INSERT INTO attribute(label) VALUES ('Tom');
INSERT INTO attribute(label) VALUES ('Sad');
INSERT INTO attribute(label) VALUES ('Overweight');
INSERT INTO attribute(label) VALUES ('Girl');
INSERT INTO attribute(label) VALUES ('Smelly');
INSERT INTO attribute(label) VALUES ('Paisley');
INSERT INTO attribute(label) VALUES ('Orange');
INSERT INTO attribute(label) VALUES ('Fuzzy');
INSERT INTO attribute(label) VALUES ('Custard');
INSERT INTO attribute(label) VALUES ('Happy');
INSERT INTO attribute(label) VALUES ('Grumpy');
INSERT INTO attribute(label) VALUES ('Sleepy');
INSERT INTO attribute(label) VALUES ('Doc');

INSERT INTO kitten(name) values ('Fred');
INSERT INTO kitten_attribute(kitten_id, attribute_id) select (select id from kitten where name = 'Fred') as kitten_id, id from attribute where label in ('Sleepy', 'Grumpy', 'Happy', 'Overweight');

INSERT INTO kitten(name) values ('Colin');
INSERT INTO kitten_attribute(kitten_id, attribute_id) select (select id from kitten where name = 'Colin') as kitten_id, id from attribute where label in ('Smelly', 'Rough', 'Alien', 'Siamese');

INSERT INTO kitten(name) values ('Gertrude');
INSERT INTO kitten_attribute(kitten_id, attribute_id) select (select id from kitten where name = 'Gertrude') as kitten_id, id from attribute where label in ('Sad', 'Harlequin', 'Orange', 'Tom');

INSERT INTO kitten(name) values ('Sleepy');
INSERT INTO kitten_attribute(kitten_id, attribute_id) select (select id from kitten where name = 'Sleepy') as kitten_id, id from attribute where label in ('Sleepy', 'Overweight', 'Orange');

INSERT INTO kitten(name) values ('Dionysus');
INSERT INTO kitten_attribute(kitten_id, attribute_id) select (select id from kitten where name = 'Dionysus') as kitten_id, id from attribute where label in ('Sleepy', 'Overweight');

INSERT INTO kitten(name) values ('Spot');
INSERT INTO kitten_attribute(kitten_id, attribute_id) select (select id from kitten where name = 'Spot') as kitten_id, id from attribute where label in ('Paisley', 'Orange', 'Tortoiseshell');

# --- !Downs

DROP TABLE kitten;
DROP SEQUENCE kitten_id_seq;

DROP TABLE attribute;
DROP SEQUENCE attribute_id_seq;

DROP TABLE kitten_attribute;
DROP SEQUENCE kitten_attribute_id_seq;