CREATE TABLE IF NOT EXISTS public.bill_type (
    id uuid NOT NULL,
    created_date timestamp without time zone NOT NULL,
    last_modified_date timestamp without time zone NOT NULL,
    active boolean NOT NULL,
    name character varying(50) NOT NULL,
    category_id uuid NOT NULL,
    path character varying(250) NOT NULL,
    CONSTRAINT bill_type_pkey PRIMARY KEY (id),
    CONSTRAINT uk_bill_type_name UNIQUE (name),
    CONSTRAINT fk_bill_type_category_id FOREIGN KEY (category_id) REFERENCES public.category (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION
);
