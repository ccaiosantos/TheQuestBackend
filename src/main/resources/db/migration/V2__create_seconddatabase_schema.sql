CREATE TABLE IF NOT EXISTS users (
                                     id          BIGSERIAL PRIMARY KEY,
                                     name        VARCHAR(255)        NOT NULL,
    email       VARCHAR(255)        NOT NULL UNIQUE,
    password    VARCHAR(255)        NOT NULL,
    avatar_url  VARCHAR(500),
    created_at  TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP           NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);

CREATE TABLE IF NOT EXISTS documents (
                                         id              BIGSERIAL PRIMARY KEY,
                                         user_id         BIGINT          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    file_name       VARCHAR(500)    NOT NULL,
    file_path       VARCHAR(1000)   NOT NULL,
    file_size_bytes BIGINT,
    page_count      INTEGER,
    extracted_text  TEXT,
    uploaded_at     TIMESTAMP       NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_documents_user_id ON documents (user_id);

CREATE TABLE IF NOT EXISTS video_contents (
                                              id               BIGSERIAL PRIMARY KEY,
                                              user_id          BIGINT          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    youtube_url      VARCHAR(500)    NOT NULL,
    video_id         VARCHAR(20)     NOT NULL,
    title            VARCHAR(500),
    channel_name     VARCHAR(255),
    duration_minutes INTEGER,
    thumbnail_url    VARCHAR(500),
    transcript       TEXT,
    imported_at      TIMESTAMP       NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_video_contents_user_id ON video_contents (user_id);
-- Garante que o mesmo usuário não importe a mesma URL duas vezes
CREATE UNIQUE INDEX IF NOT EXISTS idx_video_contents_url_user ON video_contents (youtube_url, user_id);


CREATE TABLE IF NOT EXISTS quizzes (
                                       id                BIGSERIAL PRIMARY KEY,
                                       user_id           BIGINT          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    document_id       BIGINT          REFERENCES documents (id) ON DELETE SET NULL,
    video_content_id  BIGINT          REFERENCES video_contents (id) ON DELETE SET NULL,
    exam_type         VARCHAR(50)     NOT NULL,   -- ENEM, CONCURSO, VESTIBULAR...
    banca             VARCHAR(100),               -- Cebraspe, FGV, etc.
    difficulty        VARCHAR(20),                -- EASY, MEDIUM, HARD
    format            VARCHAR(20),                -- OBJECTIVE, ESSAY
    question_count    INTEGER,
    created_at        TIMESTAMP       NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_quizzes_user_id ON quizzes (user_id);

-- -------------------------------------------------------

CREATE TABLE IF NOT EXISTS questions (
                                         id             BIGSERIAL PRIMARY KEY,
                                         quiz_id        BIGINT      NOT NULL REFERENCES quizzes (id) ON DELETE CASCADE,
    statement      TEXT        NOT NULL,
    subject        VARCHAR(255),
    exam_type      VARCHAR(50),
    banca          VARCHAR(100),
    explanation    TEXT,
    question_order INTEGER
    );

CREATE INDEX IF NOT EXISTS idx_questions_quiz_id ON questions (quiz_id);


CREATE TABLE IF NOT EXISTS alternatives (
                                            id          BIGSERIAL PRIMARY KEY,
                                            question_id BIGINT          NOT NULL REFERENCES questions (id) ON DELETE CASCADE,
    letter      VARCHAR(1)      NOT NULL,   -- A, B, C, D, E
    text        TEXT            NOT NULL,
    is_correct  BOOLEAN         NOT NULL DEFAULT FALSE
    );

CREATE INDEX IF NOT EXISTS idx_alternatives_question_id ON alternatives (question_id);


CREATE TABLE IF NOT EXISTS quiz_results (
                                            id               BIGSERIAL PRIMARY KEY,
                                            quiz_id          BIGINT          NOT NULL UNIQUE REFERENCES quizzes (id) ON DELETE CASCADE,
    correct_answers  INTEGER         NOT NULL DEFAULT 0,
    wrong_answers    INTEGER         NOT NULL DEFAULT 0,
    total_questions  INTEGER         NOT NULL DEFAULT 0,
    accuracy_percent NUMERIC(5, 2),             -- ex: 85.50
    duration_seconds INTEGER,
    completed_at     TIMESTAMP       NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_quiz_results_quiz_id ON quiz_results (quiz_id);