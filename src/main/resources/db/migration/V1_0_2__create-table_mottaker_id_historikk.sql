-- Table: stonad_mottaker_id_historikk

-- DROP TABLE stonad_mottaker_id_historikk;

CREATE TABLE IF NOT EXISTS mottaker_id_historikk
(
    stonad_id integer NOT NULL,
    mottaker_id_endret_fra varchar(20) NOT NULL,
    mottaker_id_endret_til varchar(20) NOT NULL,
    opprettet_av character(7) NOT NULL,
    opprettet_timestamp timestamp DEFAULT now() NOT NULL,
    CONSTRAINT mottaker_id_historikk_pkey PRIMARY KEY (stonad_id, opprettet_timestamp),
    CONSTRAINT fk_stonad_id FOREIGN KEY (stonad_id)
        REFERENCES stonad (stonad_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    UNIQUE (stonad_id, opprettet_timestamp)
)

    TABLESPACE pg_default;