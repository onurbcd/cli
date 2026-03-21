CREATE TABLE IF NOT EXISTS public.budget (
    id uuid NOT NULL,
    created_date timestamp without time zone NOT NULL,
    last_modified_date timestamp without time zone NOT NULL,
    active boolean NOT NULL,
    name character varying(50) NOT NULL,
    amount numeric(19,4) NOT NULL,
    paid boolean NOT NULL,
    ref_month smallint NOT NULL,
    ref_year smallint NOT NULL,
    sequence smallint NOT NULL,
    bill_type_id uuid NOT NULL,
    CONSTRAINT budget_pkey PRIMARY KEY (id),
    CONSTRAINT uk_budget_name_ref_year_ref_month UNIQUE (name, ref_year, ref_month),
    CONSTRAINT uk_budget_sequence_ref_year_ref_month UNIQUE (sequence, ref_year, ref_month),
    CONSTRAINT fk_budget_bill_type_id FOREIGN KEY (bill_type_id) REFERENCES public.bill_type (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION,
    CONSTRAINT budget_ref_month_check CHECK (ref_month >= 1 AND ref_month <= 12),
    CONSTRAINT budget_sequence_check CHECK (sequence >= 1)
);
