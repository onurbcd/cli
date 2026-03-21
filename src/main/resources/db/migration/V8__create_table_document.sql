CREATE TABLE IF NOT EXISTS public.document (
    id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    path VARCHAR(250) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    size BIGINT NOT NULL,
    hash VARCHAR(250) NOT NULL,
    CONSTRAINT pk_document PRIMARY KEY (id)
);
