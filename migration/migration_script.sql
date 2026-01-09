-- =====================================================
-- MSA Integration Project - Data Migration Script
-- Source: swyp_test (Old Schema with tb_ prefix)
-- Target: wedding_db (New MSA Schema)
-- =====================================================

-- 설정
SET FOREIGN_KEY_CHECKS = 0;
SET SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO';

-- =====================================================
-- 1. Wedding Halls Migration (tb_wedding_hall -> wedding_halls)
-- =====================================================

INSERT INTO wedding_halls (
    id, name, address, hall_type, description, min_guarantee, max_capacity,
    meal_price, hall_rental_price, cover_image, phone, email, parking,
    avg_rating, review_count, created_at, updated_at
)
SELECT
    wh.id,
    wh.name,
    wh.address,
    CASE wh.venue_type
        WHEN 'HOTEL' THEN 'HOTEL'
        WHEN 'WEDDING_HALL' THEN 'CONVENTION'
        WHEN 'GARDEN' THEN 'OUTDOOR'
        WHEN 'HOUSE_STUDIO' THEN 'HOUSE'
        WHEN 'RESTAURANT' THEN 'RESTAURANT'
        WHEN 'OUTDOOR' THEN 'OUTDOOR'
        WHEN 'OTHER' THEN 'OTHER'
        ELSE 'OTHER'
    END as hall_type,
    NULL as description,
    NULL as min_guarantee,
    NULL as max_capacity,
    NULL as meal_price,
    NULL as hall_rental_price,
    wh.image_url as cover_image,
    wh.phone,
    wh.email,
    wh.parking,
    0.0 as avg_rating,
    0 as review_count,
    COALESCE(wh.reg_dt, NOW()) as created_at,
    NOW() as updated_at
FROM swyp_test.tb_wedding_hall wh
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    address = VALUES(address),
    updated_at = NOW();

-- =====================================================
-- 2. Halls Migration (tb_hall -> halls)
-- =====================================================

INSERT INTO halls (
    id, wedding_hall_id, name, floor, min_capacity, max_capacity,
    rental_price, meal_price, description, image_url, is_available,
    created_at, updated_at
)
SELECT
    h.id,
    h.wedding_hall_id,
    h.name,
    h.floor_no as floor,
    h.capacity_min as min_capacity,
    h.capacity_max as max_capacity,
    NULL as rental_price,
    NULL as meal_price,
    h.description,
    h.image_url,
    h.status as is_available,
    COALESCE(h.reg_dt, NOW()) as created_at,
    COALESCE(h.update_dt, NOW()) as updated_at
FROM swyp_test.tb_hall h
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    description = VALUES(description),
    updated_at = NOW();

-- =====================================================
-- 3. Dress Shops Migration (tb_dress_shop -> dress_shops)
-- =====================================================

INSERT INTO dress_shops (
    id, shop_name, description, address, phone, sns_url, image_url,
    specialty, features, avg_rating, created_at, updated_at
)
SELECT
    ds.id,
    ds.shop_name,
    ds.description,
    ds.address,
    ds.phone,
    ds.sns_url,
    ds.image_url,
    ds.specialty,
    ds.features,
    0.0 as avg_rating,
    COALESCE(ds.reg_dt, NOW()) as created_at,
    COALESCE(ds.update_dt, NOW()) as updated_at
FROM swyp_test.tb_dress_shop ds
ON DUPLICATE KEY UPDATE
    shop_name = VALUES(shop_name),
    description = VALUES(description),
    updated_at = NOW();

-- =====================================================
-- 4. Dresses Migration (tb_dress -> dresses)
-- =====================================================

INSERT INTO dresses (
    id, dress_shop_id, name, color, shape, price_range, length, season,
    designer, dress_type, neck_line, mood, fabric, image_url, features,
    created_at, updated_at
)
SELECT
    d.id,
    d.dress_shop_id,
    d.name,
    d.color,
    d.shape,
    d.price_range,
    CASE d.length
        WHEN 'MINI' THEN 'MINI'
        WHEN 'MIDI' THEN 'MIDI'
        WHEN 'LONG' THEN 'LONG'
        WHEN 'TRAIN' THEN 'TRAIN'
        ELSE NULL
    END as length,
    CASE d.season
        WHEN 'SPRING' THEN 'SPRING'
        WHEN 'SUMMER' THEN 'SUMMER'
        WHEN 'FALL' THEN 'FALL'
        WHEN 'WINTER' THEN 'WINTER'
        WHEN 'SPRING_FALL' THEN 'ALL'
        WHEN 'ALL_SEASON' THEN 'ALL'
        ELSE 'ALL'
    END as season,
    d.designer,
    CASE d.type
        WHEN 'A_LINE' THEN 'WEDDING_DRESS'
        WHEN 'MERMAID' THEN 'WEDDING_DRESS'
        WHEN 'BALL_LINE' THEN 'WEDDING_DRESS'
        ELSE 'WEDDING_DRESS'
    END as dress_type,
    CASE d.neck_line
        WHEN 'V_NECK' THEN 'V_NECK'
        WHEN 'OFF_SHOULDER' THEN 'OFF_SHOULDER'
        WHEN 'HALTER' THEN 'HALTER'
        WHEN 'SWEETHEART' THEN 'SWEETHEART'
        ELSE 'OTHER'
    END as neck_line,
    CASE d.mood
        WHEN 'ROMANTIC' THEN 'ROMANTIC'
        WHEN 'ELEGANT' THEN 'ELEGANT'
        WHEN 'SEXY_ELEGANT' THEN 'ELEGANT'
        WHEN 'MODERN' THEN 'MODERN'
        WHEN 'CLASSIC' THEN 'CLASSIC'
        WHEN 'VINTAGE' THEN 'VINTAGE'
        ELSE NULL
    END as mood,
    d.fabric,
    d.image_url,
    d.features,
    COALESCE(d.reg_dt, NOW()) as created_at,
    COALESCE(d.update_dt, NOW()) as updated_at
FROM swyp_test.tb_dress d
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    updated_at = NOW();

-- =====================================================
-- 5. Makeup Shops Migration (tb_makeup_shop -> makeup_shops)
-- =====================================================

INSERT INTO makeup_shops (
    id, name, address, service_type, base_price, cover_image, phone,
    sns_url, specialty, avg_rating, on_site_available, created_at, updated_at
)
SELECT
    ms.id,
    ms.shop_name as name,
    ms.address,
    'MAKEUP_AND_HAIR' as service_type,
    NULL as base_price,
    ms.image_url as cover_image,
    ms.phone,
    ms.sns_url,
    ms.specialty,
    0.0 as avg_rating,
    TRUE as on_site_available,
    COALESCE(ms.reg_dt, NOW()) as created_at,
    COALESCE(ms.update_dt, NOW()) as updated_at
FROM swyp_test.tb_makeup_shop ms
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    address = VALUES(address),
    updated_at = NOW();

-- =====================================================
-- 6. Bookmarks Migration (tb_likes -> bookmarks)
-- =====================================================

INSERT INTO bookmarks (
    user_id, service_type, target_type, target_id, created_at, updated_at
)
SELECT
    l.user_id,
    'WEDDING' as service_type,
    l.likes_type as target_type,
    l.target_id,
    COALESCE(l.update_dt, NOW()) as created_at,
    COALESCE(l.update_dt, NOW()) as updated_at
FROM swyp_test.tb_likes l
WHERE l.user_id IS NOT NULL
ON DUPLICATE KEY UPDATE
    updated_at = NOW();

-- =====================================================
-- 7. Recommendation Queries Migration
-- =====================================================

-- 7.1 Dress Recommendations (recommendation_queries -> dress_recommendations)
INSERT INTO dress_recommendations (
    query_hash, arm_length, leg_length, neck_length, face_shape, body_type,
    recommendation, access_count, last_accessed, created_at, updated_at
)
SELECT
    rq.query_hash,
    rq.arm_length,
    rq.leg_length,
    rq.neck_length,
    rq.face_shape,
    rq.body_type,
    rq.recommendation,
    COALESCE(rq.access_count, 0),
    rq.last_accessed,
    COALESCE(rq.created_at, NOW()) as created_at,
    NOW() as updated_at
FROM swyp_test.recommendation_queries rq
ON DUPLICATE KEY UPDATE
    access_count = VALUES(access_count),
    last_accessed = VALUES(last_accessed),
    updated_at = NOW();

-- 7.2 Venue Recommendations (venue_queries -> venue_recommendations)
INSERT INTO venue_recommendations (
    query_hash, guest_count, budget, region, style_preference, season,
    recommendation, access_count, last_accessed, created_at, updated_at
)
SELECT
    vq.query_hash,
    vq.guest_count,
    vq.budget,
    vq.region,
    vq.style_preference,
    vq.season,
    vq.recommendation,
    COALESCE(vq.access_count, 0),
    vq.last_accessed,
    COALESCE(vq.created_at, NOW()) as created_at,
    NOW() as updated_at
FROM swyp_test.venue_queries vq
ON DUPLICATE KEY UPDATE
    access_count = VALUES(access_count),
    last_accessed = VALUES(last_accessed),
    updated_at = NOW();

-- =====================================================
-- Reset Auto Increment Values
-- =====================================================

-- Get max IDs and set auto_increment
SELECT @max_wh := COALESCE(MAX(id), 0) + 1 FROM wedding_halls;
SET @sql = CONCAT('ALTER TABLE wedding_halls AUTO_INCREMENT = ', @max_wh);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT @max_h := COALESCE(MAX(id), 0) + 1 FROM halls;
SET @sql = CONCAT('ALTER TABLE halls AUTO_INCREMENT = ', @max_h);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT @max_ds := COALESCE(MAX(id), 0) + 1 FROM dress_shops;
SET @sql = CONCAT('ALTER TABLE dress_shops AUTO_INCREMENT = ', @max_ds);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT @max_d := COALESCE(MAX(id), 0) + 1 FROM dresses;
SET @sql = CONCAT('ALTER TABLE dresses AUTO_INCREMENT = ', @max_d);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT @max_ms := COALESCE(MAX(id), 0) + 1 FROM makeup_shops;
SET @sql = CONCAT('ALTER TABLE makeup_shops AUTO_INCREMENT = ', @max_ms);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- Verification Queries
-- =====================================================

SELECT 'Migration Summary' as info;
SELECT 'wedding_halls' as table_name, COUNT(*) as count FROM wedding_halls
UNION ALL
SELECT 'halls', COUNT(*) FROM halls
UNION ALL
SELECT 'dress_shops', COUNT(*) FROM dress_shops
UNION ALL
SELECT 'dresses', COUNT(*) FROM dresses
UNION ALL
SELECT 'makeup_shops', COUNT(*) FROM makeup_shops
UNION ALL
SELECT 'dress_recommendations', COUNT(*) FROM dress_recommendations
UNION ALL
SELECT 'venue_recommendations', COUNT(*) FROM venue_recommendations;
