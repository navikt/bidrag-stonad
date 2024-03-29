-- Table: stønad

-- DROP TABLE stønad;

CREATE TABLE IF NOT EXISTS stønad
(
    stønadsid integer NOT NULL GENERATED BY DEFAULT AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    type varchar(20) NOT NULL,
    sak varchar(20) NOT NULL,
    skyldner varchar(20) NOT NULL,
    kravhaver varchar(20) NOT NULL,
    mottaker varchar(20) NOT NULL,
    første_indeksreguleringsår integer,
    innkreving varchar(50) NOT NULL,
    opprettet_av varchar(50) NOT NULL,
    opprettet_tidspunkt timestamp DEFAULT now() NOT NULL,
    endret_av varchar(50),
    endret_tidspunkt timestamp,
    CONSTRAINT stønad_pkey PRIMARY KEY (stønadsid),
    UNIQUE (type, skyldner, kravhaver, sak)
)

    TABLESPACE pg_default;

CREATE INDEX idx_stønad_1 ON stønad(første_indeksreguleringsår);

CREATE INDEX idx_stønad_2 ON stønad(sak);