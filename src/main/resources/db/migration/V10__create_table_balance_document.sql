CREATE TABLE IF NOT EXISTS public.balance_document (
    balance_id UUID NOT NULL,
    document_id UUID NOT NULL,
    CONSTRAINT pk_balance_document PRIMARY KEY (balance_id, document_id),
    CONSTRAINT uc_balance_document_id UNIQUE (document_id),
    CONSTRAINT fk_balance_document_balance_id FOREIGN KEY (balance_id) REFERENCES public.balance (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION,
    CONSTRAINT fk_balance_document_document_id FOREIGN KEY (document_id) REFERENCES public.document (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION
);
