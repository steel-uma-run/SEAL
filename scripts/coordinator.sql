DO $$
DECLARE
    id UUID := gen_random_uuid();
BEGIN
    INSERT INTO users (id, email, full_name, password_hash, role) VALUES (id, 'nhc@fpt.edu.vn', 'Nguyễn Hùng Cường', '$2b$10$3zXsxgVGMU9rHRvPxmWP2.63m0IaArYDMU19bKQrzDi.aFFlusA0O', 'COORDINATOR');
    INSERT INTO coordinators (id) VALUES (id);
END $$;
