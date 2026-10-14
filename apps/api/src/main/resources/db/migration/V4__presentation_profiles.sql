-- Presentation categories describe the owner's stated interests; no achievements are seeded.
INSERT INTO professional_profiles(id,slug,enabled,display_order,created_at,updated_at) VALUES
('b6b56754-7f3c-4d66-9e33-7ce24777b001','support-infrastructure',true,0,now(),now()),
('b6b56754-7f3c-4d66-9e33-7ce24777b002','development',true,1,now(),now()),
('b6b56754-7f3c-4d66-9e33-7ce24777b003','information-security',true,2,now(),now());
INSERT INTO professional_profile_translations(professional_profile_id,locale,title) VALUES
('b6b56754-7f3c-4d66-9e33-7ce24777b001','pt-BR','Suporte e Infraestrutura'),
('b6b56754-7f3c-4d66-9e33-7ce24777b001','en','IT Support and Infrastructure'),
('b6b56754-7f3c-4d66-9e33-7ce24777b002','pt-BR','Desenvolvimento'),
('b6b56754-7f3c-4d66-9e33-7ce24777b002','en','Development'),
('b6b56754-7f3c-4d66-9e33-7ce24777b003','pt-BR','Segurança da Informação'),
('b6b56754-7f3c-4d66-9e33-7ce24777b003','en','Information Security');
