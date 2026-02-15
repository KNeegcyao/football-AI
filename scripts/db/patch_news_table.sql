ALTER TABLE news ADD COLUMN category_id INT DEFAULT 0 COMMENT '分类ID' AFTER category;
desc news;