CREATE TYPE user_role AS ENUM ('student', 'judge', 'mentor', 'coordinator', 'admin');
CREATE TYPE user_status AS ENUM ('pending', 'active', 'banned');
CREATE TYPE submission_status AS ENUM ('draft', 'submitted');

CREATE TABLE seasons (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    name TEXT NOT NULL UNIQUE,
    description TEXT NOT NULL,
    starts_at TIMESTAMPTZ NOT NULL,
    ends_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now(),
    
    PRIMARY KEY (id),
    -- Ràng buộc logic: Ngày kết thúc phải diễn ra sau ngày bắt đầu
    CONSTRAINT check_season_dates CHECK (ends_at > starts_at)
);

CREATE TABLE rounds (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    season_id UUID NOT NULL REFERENCES seasons(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    scoring_deadline TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now(),
    
    PRIMARY KEY (id)
);

CREATE TABLE tracks (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    season_id UUID NOT NULL REFERENCES seasons(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    description TEXT,
    
    PRIMARY KEY (id)
);

-- Bảng này cần tồn tại trước bảng submissions để hệ thống không báo lỗi khóa ngoại
CREATE TABLE IF NOT EXISTS teams (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE profiles (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    team_id UUID REFERENCES teams(id) ON DELETE CASCADE,
    email TEXT NOT NULL UNIQUE,
    full_name TEXT NOT NULL,
    roles user_role[] NOT NULL DEFAULT '{student}',
    status user_status NOT NULL DEFAULT 'pending',
    student_code TEXT,
    skills TEXT[],
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (id),
    -- Ràng buộc email: Chỉ chấp nhận các email có đuôi edu.vn (VD: admin@fpt.edu.vn)
    CONSTRAINT check_edu_email CHECK (email LIKE '%.edu.vn')
);

CREATE TABLE submissions (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    team_id UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    round_id UUID NOT NULL REFERENCES rounds(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    description TEXT,
    github_url TEXT NOT NULL,
    video_url TEXT NOT NULL,
    pdf_url TEXT NOT NULL,
    status submission_status DEFAULT 'submitted',
    submitted_at TIMESTAMPTZ DEFAULT now(),
    
    PRIMARY KEY (id),
    -- Ràng buộc: Mỗi đội (team) chỉ được có 1 bài nộp (submission) duy nhất trong 1 vòng (round)
    CONSTRAINT unique_team_round_submission UNIQUE (team_id, round_id)
);
