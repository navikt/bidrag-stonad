ALTER TABLE periode
    ADD COLUMN gyldig_fra timestamp DEFAULT now() NOT NULL ;

ALTER TABLE periode
    ADD COLUMN gyldig_til timestamp ;