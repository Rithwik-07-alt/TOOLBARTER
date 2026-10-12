-- ==============================================================================
-- TOOLBARTER Schema DDL (Controlled and Documented)
-- Target DBMS: MySQL 8.0+ with Spatial Data Extensions
-- ==============================================================================

CREATE DATABASE IF NOT EXISTS toolbarter;
USE toolbarter;

-- 1. Users Table (Core account records with spatial home coordinates)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL,
    home_location POINT SRID 4326,
    trust_score INT NOT NULL DEFAULT 0,
    trust_level VARCHAR(30) NOT NULL DEFAULT 'NEW_NEIGHBOUR',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    SPATIAL INDEX idx_users_home_location (home_location)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. User Roles Table (Supports dual roles OWNER & BORROWER)
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_name VARCHAR(30) NOT NULL,
    PRIMARY KEY (user_id, role_name),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Items Table (Tool inventory with spatial location and pickup constraints)
CREATE TABLE IF NOT EXISTS items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    condition_grade VARCHAR(50) NOT NULL,
    condition_notes TEXT,
    one_time_use BOOLEAN NOT NULL DEFAULT FALSE,
    pickup_radius_km DECIMAL(6,2) NOT NULL DEFAULT 5.00,
    min_duration_hours INT NOT NULL DEFAULT 1,
    max_duration_hours INT NOT NULL DEFAULT 72,
    photo_url VARCHAR(500),
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL,
    location POINT SRID 4326,
    status VARCHAR(30) NOT NULL DEFAULT 'LISTED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_items_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE RESTRICT,
    SPATIAL INDEX idx_items_location (location),
    INDEX idx_items_category (category),
    INDEX idx_items_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Availability Slots Table (Owner-defined scheduled time-windows)
CREATE TABLE IF NOT EXISTS availability_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL,
    slot_start DATETIME NOT NULL,
    slot_end DATETIME NOT NULL,
    is_booked BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_slots_item FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE,
    INDEX idx_slots_item_time (item_id, slot_start, slot_end)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Borrow Requests Table (Scheduled borrow contracts with conflict locking)
CREATE TABLE IF NOT EXISTS borrow_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL,
    borrower_id BIGINT NOT NULL,
    slot_id BIGINT NOT NULL,
    start_ts DATETIME NOT NULL,
    end_ts DATETIME NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    points_awarded BOOLEAN NOT NULL DEFAULT FALSE,
    pickup_confirmed_at DATETIME NULL,
    return_confirmed_at DATETIME NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_requests_item FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE RESTRICT,
    CONSTRAINT fk_requests_borrower FOREIGN KEY (borrower_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_requests_slot FOREIGN KEY (slot_id) REFERENCES availability_slots(id) ON DELETE RESTRICT,
    INDEX idx_requests_item_status (item_id, status),
    INDEX idx_requests_borrower (borrower_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Reviews Table (Reciprocal trust reviews tied to completed returns)
CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    borrow_request_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    reviewee_id BIGINT NOT NULL,
    rating INT NOT NULL,
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_request FOREIGN KEY (borrow_request_id) REFERENCES borrow_requests(id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_reviewer FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_reviews_reviewee FOREIGN KEY (reviewee_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT uq_request_reviewer UNIQUE (borrow_request_id, reviewer_id),
    CHECK (rating >= 1 AND rating <= 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. User Favorites Table (Borrower bookmarked tools)
CREATE TABLE IF NOT EXISTS user_favorites (
    user_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, item_id),
    CONSTRAINT fk_favorites_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_favorites_item FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
