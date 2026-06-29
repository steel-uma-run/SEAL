DO $$
DECLARE
    season_id UUID := gen_random_uuid();
    event_id UUID := gen_random_uuid();
BEGIN
    INSERT INTO seasons (id, year, semester) VALUES (season_id, 2026, 'SPRING');
    INSERT INTO events (id, season_id, start_time, end_time, name, description, status) VALUES (event_id, season_id, 'now', 'tomorrow', 'PEAK EVENT', '', 'FINALIZED');
END $$;
