```docker exec -it predictorama-db psql -U predictorama -d predictorama```

-- USERS
INSERT INTO users (id, username, email, system_role)
VALUES
('11111111-1111-1111-1111-111111111111', 'user1', 'user1@test.com', 'USER'),
('22222222-2222-2222-2222-222222222222', 'user2', 'user2@test.com', 'USER');

-- GROUP
INSERT INTO groups (id, owner_id, name)
VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 'Test Group');

-- GROUP MEMBERS
INSERT INTO group_members (id, user_id, group_id, status, member_role)
VALUES
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '11111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'ACTIVE', 'USER'),
('cccccccc-cccc-cccc-cccc-cccccccccccc', '22222222-2222-2222-2222-222222222222', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'ACTIVE', 'USER');

-- PREDICTIONS (same match, different users)
INSERT INTO predictions (id, user_id, match_id, group_id, predicted_winner)
VALUES
('dddddddd-dddd-dddd-dddd-dddddddddddd', '11111111-1111-1111-1111-111111111111', '3527c99c-30b9-480e-82ac-55ba5341ba35', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'HOME'),
('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', '22222222-2222-2222-2222-222222222222', '3527c99c-30b9-480e-82ac-55ba5341ba35', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'AWAY');

-- PREDICTION SCORES
INSERT INTO prediction_scores (id, prediction_id, score_type, home_score, away_score)
VALUES
('ffffffff-ffff-ffff-ffff-ffffffffffff', 'dddddddd-dddd-dddd-dddd-dddddddddddd', 'FULL_TIME', 2, 1),
('99999999-9999-9999-9999-999999999999', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'FULL_TIME', 0, 3);