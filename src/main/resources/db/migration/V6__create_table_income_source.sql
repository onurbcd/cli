CREATE TABLE IF NOT EXISTS public.income_source (
    id UUID NOT NULL,
    name VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL,
    created_date TIMESTAMP WITHOUT TIME ZONE,
    last_modified_date TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_income_source PRIMARY KEY (id),
    CONSTRAINT uc_income_source_name UNIQUE (name)
);
