-- Table: periode

-- DROP TABLE periode;

CREATE TABLE IF NOT EXISTS periode
(
    periodeid integer NOT NULL GENERATED BY DEFAULT AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    fom date NOT NULL,
    til date,
    stønadsid integer NOT NULL,
    vedtaksid integer NOT NULL,
    gyldig_fra timestamp DEFAULT now() NOT NULL,
    gyldig_til timestamp,
    periode_gjort_ugyldig_av_vedtaksid integer,
    beløp float,
    valutakode varchar(10),
    resultatkode varchar(255) NOT NULL,
    CONSTRAINT periode_pkey PRIMARY KEY (periodeid),
    CONSTRAINT fk_stønad_id FOREIGN KEY (stønadsid)
        REFERENCES stønad (stønadsid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    UNIQUE (stønadsid, fom, periode_gjort_ugyldig_av_vedtaksid)
)

    TABLESPACE pg_default;