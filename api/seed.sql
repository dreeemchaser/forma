-- Forma Seed Data
-- Password for all users is: password123
-- BCrypt hash of "password123"

-- Clean up existing data (order matters due to foreign keys)
DELETE FROM comment;
DELETE FROM post_like;
DELETE FROM posts;
DELETE FROM users;

-- Users
INSERT INTO users (id, username, password, role, creation_date) VALUES
  ('a1000000-0000-0000-0000-000000000001', 'alice',    '$2b$10$tJLG78AVxiFTuaBPmMUBFOjKLNLAkQuWHMffa6ER8NOYEgKrv4EcS', 'REGULAR_USER',  NOW()),
  ('a1000000-0000-0000-0000-000000000002', 'bob',      '$2b$10$tJLG78AVxiFTuaBPmMUBFOjKLNLAkQuWHMffa6ER8NOYEgKrv4EcS', 'REGULAR_USER',  NOW()),
  ('a1000000-0000-0000-0000-000000000003', 'charlie',  '$2b$10$tJLG78AVxiFTuaBPmMUBFOjKLNLAkQuWHMffa6ER8NOYEgKrv4EcS', 'REGULAR_USER',  NOW()),
  ('a1000000-0000-0000-0000-000000000004', 'diana',    '$2b$10$tJLG78AVxiFTuaBPmMUBFOjKLNLAkQuWHMffa6ER8NOYEgKrv4EcS', 'REGULAR_USER',  NOW()),
  ('a1000000-0000-0000-0000-000000000005', 'moderator','$2b$10$tJLG78AVxiFTuaBPmMUBFOjKLNLAkQuWHMffa6ER8NOYEgKrv4EcS', 'MODERATOR',     NOW());

-- Posts (clean)
INSERT INTO posts (id, user_id, title, body, ai_flagged, ai_score, ai_reasoning, flagged_misleading, updated_at) VALUES
  (
    'b1000000-0000-0000-0000-000000000001',
    'a1000000-0000-0000-0000-000000000001',
    'Welcome to Forma!',
    'So excited to be part of this new platform. Looking forward to connecting with everyone here!',
    false, 0.05,
    'Clean and friendly post. No toxicity, misinformation, manipulative language, or spam detected.',
    false, NOW() - INTERVAL '5 hours'
  ),
  (
    'b1000000-0000-0000-0000-000000000002',
    'a1000000-0000-0000-0000-000000000002',
    'My morning coffee routine',
    'Started my day with a double espresso and a quick walk. Small habits really do make a difference over time.',
    false, 0.02,
    'Positive lifestyle content. No issues detected.',
    false, NOW() - INTERVAL '4 hours'
  ),
  (
    'b1000000-0000-0000-0000-000000000003',
    'a1000000-0000-0000-0000-000000000003',
    'Thoughts on remote work in 2026',
    'Three years into fully remote work and I still think the async communication model is underrated. Teams that master it outperform everyone.',
    false, 0.08,
    'Thoughtful opinion piece. No problematic content detected.',
    false, NOW() - INTERVAL '3 hours'
  ),
  (
    'b1000000-0000-0000-0000-000000000004',
    'a1000000-0000-0000-0000-000000000004',
    'Book recommendation: Atomic Habits',
    'Just finished Atomic Habits for the second time. The concept of 1% improvements compounding over time is genuinely life-changing.',
    false, 0.03,
    'Positive book recommendation. No issues detected.',
    false, NOW() - INTERVAL '2 hours'
  ),
  (
    'b1000000-0000-0000-0000-000000000005',
    'a1000000-0000-0000-0000-000000000002',
    'The best programming languages to learn in 2026',
    'Rust, TypeScript, and Python are my top picks. Each solves a different problem space and together they cover almost everything you need.',
    false, 0.06,
    'Informative tech opinion. No problematic content.',
    false, NOW() - INTERVAL '1 hour'
  ),

-- Posts (AI flagged)
  (
    'b1000000-0000-0000-0000-000000000006',
    'a1000000-0000-0000-0000-000000000003',
    'VACCINES CAUSE AUTISM - DOCTORS WON''T TELL YOU THIS!!!',
    'I have done my own research and the evidence is clear. Big Pharma is hiding the truth. Share this before it gets deleted!!',
    true, 0.94,
    'High misinformation score. Post repeats the debunked vaccine-autism claim which contradicts established scientific and medical consensus. Manipulative language used ("they won''t tell you", "share before deleted") to drive engagement through fear. Spam signals present (all caps title, excessive punctuation).',
    false, NOW() - INTERVAL '30 minutes'
  ),
  (
    'b1000000-0000-0000-0000-000000000007',
    'a1000000-0000-0000-0000-000000000004',
    'Everyone who disagrees with me is an idiot',
    'Seriously I am so sick of stupid people. If you don''t get it by now you are beyond help. Some people just shouldn''t be allowed to have opinions.',
    true, 0.82,
    'High toxicity score. Post contains personal attacks and derogatory language targeting people with differing views. Dismissive and hostile tone throughout.',
    false, NOW() - INTERVAL '15 minutes'
  );

-- Likes
INSERT INTO post_like (id, post_id, user_id) VALUES
  (gen_random_uuid(), 'b1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000002'),
  (gen_random_uuid(), 'b1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000003'),
  (gen_random_uuid(), 'b1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000004'),
  (gen_random_uuid(), 'b1000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000001'),
  (gen_random_uuid(), 'b1000000-0000-0000-0000-000000000003', 'a1000000-0000-0000-0000-000000000001'),
  (gen_random_uuid(), 'b1000000-0000-0000-0000-000000000003', 'a1000000-0000-0000-0000-000000000002'),
  (gen_random_uuid(), 'b1000000-0000-0000-0000-000000000004', 'a1000000-0000-0000-0000-000000000003'),
  (gen_random_uuid(), 'b1000000-0000-0000-0000-000000000005', 'a1000000-0000-0000-0000-000000000001'),
  (gen_random_uuid(), 'b1000000-0000-0000-0000-000000000005', 'a1000000-0000-0000-0000-000000000004');

-- Comments
INSERT INTO comment (id, post_id, user_id, body, created_at) VALUES
  (gen_random_uuid(), 'b1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000002', 'Welcome! Great to have you here.', NOW() - INTERVAL '4 hours 30 minutes'),
  (gen_random_uuid(), 'b1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000003', 'Same, really enjoying the vibe so far!', NOW() - INTERVAL '4 hours'),
  (gen_random_uuid(), 'b1000000-0000-0000-0000-000000000003', 'a1000000-0000-0000-0000-000000000001', 'Fully agree on async. Notion + Slack threads changed everything for our team.', NOW() - INTERVAL '2 hours 30 minutes'),
  (gen_random_uuid(), 'b1000000-0000-0000-0000-000000000005', 'a1000000-0000-0000-0000-000000000003', 'Would add Go to that list for backend services.', NOW() - INTERVAL '45 minutes');
