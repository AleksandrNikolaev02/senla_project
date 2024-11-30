CREATE TABLE IF NOT EXISTS public.users
(
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    fname character varying(100) COLLATE pg_catalog."default",
    sname character varying(100) COLLATE pg_catalog."default",
    email character varying(100) COLLATE pg_catalog."default",
    password character varying(100) COLLATE pg_catalog."default",
    role character varying(50) COLLATE pg_catalog."default",
    CONSTRAINT users_pkey PRIMARY KEY (id)
)

    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.users
    OWNER to postgres;

GRANT ALL ON TABLE public.users TO postgres;

-- Table: public.courses

-- DROP TABLE IF EXISTS public.courses;

CREATE TABLE IF NOT EXISTS public.courses
(
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    title character varying COLLATE pg_catalog."default",
    description character varying COLLATE pg_catalog."default",
    teacher_id integer,
    CONSTRAINT cources_pkey PRIMARY KEY (id),
    CONSTRAINT fk_teacher FOREIGN KEY (teacher_id)
        REFERENCES public.users (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.courses
    OWNER to postgres;

GRANT ALL ON TABLE public.courses TO postgres;


-- Table: public.course_users

-- DROP TABLE IF EXISTS public.course_users;

CREATE TABLE IF NOT EXISTS public.course_users
(
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    course_id integer NOT NULL,
    user_id integer NOT NULL,
    CONSTRAINT course_users_pkey PRIMARY KEY (id),
    CONSTRAINT fk_course FOREIGN KEY (course_id)
        REFERENCES public.courses (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
        NOT VALID,
    CONSTRAINT fk_user FOREIGN KEY (user_id)
        REFERENCES public.users (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
        NOT VALID
)

    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.course_users
    OWNER to postgres;


-- Table: public.artifacts

-- DROP TABLE IF EXISTS public.artifacts;

CREATE TABLE IF NOT EXISTS public.artifacts
(
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    title character varying COLLATE pg_catalog."default",
    content character varying COLLATE pg_catalog."default",
    course_id integer,
    type character varying(50) COLLATE pg_catalog."default",
    CONSTRAINT artifacts_pkey PRIMARY KEY (id),
    CONSTRAINT fk_course FOREIGN KEY (course_id)
        REFERENCES public.courses (id) MATCH SIMPLE
        ON UPDATE RESTRICT
        ON DELETE CASCADE
)

    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.artifacts
    OWNER to postgres;

GRANT ALL ON TABLE public.artifacts TO postgres;

CREATE TABLE IF NOT EXISTS public.answers
(
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    artifact_id integer,
    user_id integer,
    sub_date timestamp without time zone,
    content character varying COLLATE pg_catalog."default",
    course_id integer,
    type character varying(30) COLLATE pg_catalog."default",
    status character varying(30) COLLATE pg_catalog."default",
    CONSTRAINT answers_pkey PRIMARY KEY (id),
    CONSTRAINT fk_artifact FOREIGN KEY (artifact_id)
        REFERENCES public.artifacts (id) MATCH SIMPLE
        ON UPDATE RESTRICT
        ON DELETE CASCADE,
    CONSTRAINT fk_course FOREIGN KEY (course_id)
        REFERENCES public.courses (id) MATCH SIMPLE
        ON UPDATE RESTRICT
        ON DELETE CASCADE,
    CONSTRAINT fk_user FOREIGN KEY (user_id)
        REFERENCES public.users (id) MATCH SIMPLE
        ON UPDATE RESTRICT
        ON DELETE CASCADE
)

    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.answers
    OWNER to postgres;

GRANT ALL ON TABLE public.answers TO postgres;



-- Table: public.grades

-- DROP TABLE IF EXISTS public.grades;

CREATE TABLE IF NOT EXISTS public.grades
(
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    grade smallint,
    user_id integer,
    course_id integer,
    artifact_id integer,
    CONSTRAINT grades_pkey PRIMARY KEY (id),
    CONSTRAINT fk_artifact FOREIGN KEY (artifact_id)
        REFERENCES public.artifacts (id) MATCH SIMPLE
        ON UPDATE RESTRICT
        ON DELETE CASCADE,
    CONSTRAINT fk_course FOREIGN KEY (course_id)
        REFERENCES public.courses (id) MATCH SIMPLE
        ON UPDATE RESTRICT
        ON DELETE CASCADE,
    CONSTRAINT fk_student FOREIGN KEY (user_id)
        REFERENCES public.users (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
)

    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.grades
    OWNER to postgres;

GRANT ALL ON TABLE public.grades TO postgres;


-- Table: public.messages

-- DROP TABLE IF EXISTS public.messages;

CREATE TABLE IF NOT EXISTS public.messages
(
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    sender_id integer,
    recipient_id integer,
    content character varying COLLATE pg_catalog."default",
    created_at timestamp without time zone,
    CONSTRAINT messages_pkey PRIMARY KEY (id),
    CONSTRAINT fk_recipient FOREIGN KEY (recipient_id)
        REFERENCES public.users (id) MATCH SIMPLE
        ON UPDATE RESTRICT
        ON DELETE RESTRICT,
    CONSTRAINT fk_sender FOREIGN KEY (sender_id)
        REFERENCES public.users (id) MATCH SIMPLE
        ON UPDATE RESTRICT
        ON DELETE RESTRICT
)

    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.messages
    OWNER to postgres;

GRANT ALL ON TABLE public.messages TO postgres;