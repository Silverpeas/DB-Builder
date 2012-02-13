CREATE TABLE uniqueId ( tableName varchar (150) NOT NULL, maxId	bigint NOT NULL );
ALTER TABLE uniqueId  ADD  CONSTRAINT PK_UniqueId PRIMARY KEY ( tableName );
