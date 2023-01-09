ALTER TABLE stonad ADD COLUMN innkreving varchar(20) NOT NULL;
ALTER TABLE stonad RENAME COLUMN stonad_type TO type;