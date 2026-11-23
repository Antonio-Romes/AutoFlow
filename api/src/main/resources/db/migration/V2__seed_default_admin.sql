-- Conta inicial de desenvolvimento. Atualize a senha antes de disponibilizar o ambiente.
INSERT INTO users (email, password, name, active, created_at, updated_at)
VALUES (
    'admin@autoflow.local',
    '$2b$12$5mfmzkLvawOgVqP0cz6bpuWNsPrh6/n8em24vwlC57/M0IJ0TWo8W',
    'Administrador AutoFlow',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;

INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN'
FROM users
WHERE email = 'admin@autoflow.local'
ON CONFLICT (user_id, role) DO NOTHING;
