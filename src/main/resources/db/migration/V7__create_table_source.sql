CREATE TABLE IF NOT EXISTS public.source (
    id UUID NOT NULL,
    name VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL,
    created_date TIMESTAMP WITHOUT TIME ZONE,
    last_modified_date TIMESTAMP WITHOUT TIME ZONE,
    income_source_id UUID NOT NULL,
    source_type VARCHAR(8) NOT NULL,
    currency_type VARCHAR(7) NOT NULL,
    balance DECIMAL(19, 4) NOT NULL,
    CONSTRAINT pk_source PRIMARY KEY (id),
    CONSTRAINT uc_source_name UNIQUE (name),
    CONSTRAINT fk_source_income_source_id FOREIGN KEY (income_source_id) REFERENCES public.income_source (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION
);
