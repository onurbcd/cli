DROP TABLE IF EXISTS public.bill;

CREATE TABLE public.bill (
    id UUID NOT NULL,
    name VARCHAR(255),
    active BOOLEAN NOT NULL,
    created_date TIMESTAMP WITHOUT TIME ZONE,
    last_modified_date TIMESTAMP WITHOUT TIME ZONE,
    reference_day_id INTEGER NOT NULL,
    document_date_id INTEGER,
    due_date_id INTEGER NOT NULL,
    value DECIMAL(19, 4) NOT NULL,
    payment_date_id INTEGER,
    bill_document_id UUID,
    receipt_id UUID,
    observation VARCHAR(250),
    installment SMALLINT,
    bill_type_id UUID NOT NULL,
    document_type VARCHAR(7) NOT NULL,
    payment_type VARCHAR(20),
    budget_id UUID NOT NULL,
    source_id UUID,
    reference_type VARCHAR(5) NOT NULL,
    CONSTRAINT pk_bill PRIMARY KEY (id),
    CONSTRAINT fk_bill_document_id_1 FOREIGN KEY (bill_document_id)
        REFERENCES public.document (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_bill_bill_type_id FOREIGN KEY (bill_type_id)
        REFERENCES public.bill_type (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_bill_day_id_1 FOREIGN KEY (document_date_id)
        REFERENCES public.day (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_bill_day_id_2 FOREIGN KEY (due_date_id)
        REFERENCES public.day (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_bill_day_id_3 FOREIGN KEY (payment_date_id)
        REFERENCES public.day (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_bill_document_id_2 FOREIGN KEY (receipt_id)
        REFERENCES public.document (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_bill_day_id_4 FOREIGN KEY (reference_day_id)
        REFERENCES public.day (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_bill_budget_id FOREIGN KEY (budget_id)
        REFERENCES public.budget (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_bill_source_id FOREIGN KEY (source_id)
        REFERENCES public.source (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);
