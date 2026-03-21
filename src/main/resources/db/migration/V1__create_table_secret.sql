CREATE TABLE IF NOT EXISTS public.secret (
    id uuid NOT NULL,
    created_date timestamp without time zone NOT NULL,
    last_modified_date timestamp without time zone NOT NULL,
    active boolean NOT NULL,
    name character varying(50) NOT NULL,
    description character varying(250),
    link character varying(2048),
    password character varying(255) NOT NULL,
    username character varying(50) NOT NULL,
    CONSTRAINT secret_pkey PRIMARY KEY (id),
    CONSTRAINT uk_secret_name UNIQUE (name)
);
