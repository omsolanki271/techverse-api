-- TechVerse Schema Update Script
-- Database: blog_app_api

USE blog_app_api;

-- Users table updates
ALTER TABLE users ADD COLUMN created_at DATETIME NULL;
ALTER TABLE users ADD COLUMN updated_at DATETIME NULL;
ALTER TABLE users ADD COLUMN google_id VARCHAR(255) NULL;

-- Posts table updates (share counter & draft status)
ALTER TABLE posts ADD COLUMN share_count INT DEFAULT 0;
ALTER TABLE posts ADD COLUMN status VARCHAR(20) DEFAULT 'PUBLISHED';

-- Ensure all legacy posts are marked as PUBLISHED
UPDATE posts SET status = 'PUBLISHED' WHERE status IS NULL OR status = '';
UPDATE posts SET share_count = 0 WHERE share_count IS NULL;

-- Post Likes Table
CREATE TABLE IF NOT EXISTS post_likes (
    like_id INT AUTO_INCREMENT PRIMARY KEY,
    post_id INT NOT NULL,
    user_id INT NOT NULL,
    created_at DATETIME,
    UNIQUE KEY uk_post_user_like (post_id, user_id),
    CONSTRAINT fk_post_like_post FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
    CONSTRAINT fk_post_like_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Post Bookmarks Table
CREATE TABLE IF NOT EXISTS post_bookmarks (
    bookmark_id INT AUTO_INCREMENT PRIMARY KEY,
    post_id INT NOT NULL,
    user_id INT NOT NULL,
    created_at DATETIME,
    UNIQUE KEY uk_post_user_bookmark (post_id, user_id),
    CONSTRAINT fk_post_bookmark_post FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
    CONSTRAINT fk_post_bookmark_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- User Follows Table
CREATE TABLE IF NOT EXISTS user_follows (
    follow_id INT AUTO_INCREMENT PRIMARY KEY,
    follower_id INT NOT NULL,
    following_id INT NOT NULL,
    created_at DATETIME,
    UNIQUE KEY uk_follower_following (follower_id, following_id),
    CONSTRAINT fk_follow_follower FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_follow_following FOREIGN KEY (following_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Author Subscriptions Table
CREATE TABLE IF NOT EXISTS author_subscriptions (
    subscription_id INT AUTO_INCREMENT PRIMARY KEY,
    subscriber_id INT NOT NULL,
    author_id INT NOT NULL,
    created_at DATETIME,
    UNIQUE KEY uk_subscriber_author (subscriber_id, author_id),
    CONSTRAINT fk_sub_subscriber FOREIGN KEY (subscriber_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_sub_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
);
