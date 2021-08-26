-- Table: stonad

-- DROP TABLE stonad;

CREATE TABLE IF NOT EXISTS stonad
(
    stonad_id integer NOT NULL GENERATED BY DEFAULT AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    stonad_type varchar(20) NOT NULL,
    sak_id varchar(20),
    skyldner_id varchar(20) NOT NULL,
    kravhaver_id varchar(20) NOT NULL,
    mottaker_id varchar(20) NOT NULL,
    opprettet_av character(7) NOT NULL,
    opprettet_timestamp timestamp DEFAULT now() NOT NULL,
    endret_av character(7),
    endret_timestamp timestamp,
    CONSTRAINT stonad_pkey PRIMARY KEY (stonad_id),
    UNIQUE (stonad_type, skyldner_id, kravhaver_id)
)

    TABLESPACE pg_default;