CREATE TABLE IF NOT EXISTS public.day (
    id INTEGER NOT NULL,
    calendar_date date NOT NULL,
    calendar_year SMALLINT NOT NULL,
    calendar_quarter SMALLINT NOT NULL,
    calendar_month SMALLINT NOT NULL,
    calendar_month_name VARCHAR(9) NOT NULL,
    calendar_week_in_year SMALLINT NOT NULL,
    calendar_week_in_month SMALLINT NOT NULL,
    calendar_day_in_year SMALLINT NOT NULL,
    calendar_day_in_week SMALLINT NOT NULL,
    calendar_day_in_month SMALLINT NOT NULL,
    calendar_weekday_name VARCHAR(9) NOT NULL,
    CONSTRAINT pk_day PRIMARY KEY (id),
    CONSTRAINT uc_day_calendar_date UNIQUE (calendar_date)
);
