@echo off
REM =====================================================
REM MSA Integration Project - Data Migration Script
REM Target DB: 223.130.163.203:30306
REM =====================================================

set DB_HOST=223.130.163.203
set DB_PORT=30306
set DB_USER=root
set /p DB_PASS=Enter database password:
set SOURCE_DB=swyp_test
set TARGET_DB=wedding_db

echo.
echo =====================================================
echo Step 1: Import source database dump
echo =====================================================
echo Importing dump file to %SOURCE_DB%...
mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASS% %SOURCE_DB% < "%~dp0..\dump-swyp_test-202512061114.sql"

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Failed to import source database
    pause
    exit /b 1
)
echo Source database imported successfully!

echo.
echo =====================================================
echo Step 2: Create target tables (if not exist)
echo =====================================================
echo Creating recommendation tables in %TARGET_DB%...
mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASS% %TARGET_DB% -e "
CREATE TABLE IF NOT EXISTS dress_recommendations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    query_hash VARCHAR(64) NOT NULL UNIQUE,
    arm_length VARCHAR(20) NOT NULL,
    leg_length VARCHAR(20) NOT NULL,
    neck_length VARCHAR(20) NOT NULL,
    face_shape VARCHAR(20) NOT NULL,
    body_type VARCHAR(100),
    recommendation JSON NOT NULL,
    access_count INT DEFAULT 0,
    last_accessed DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_dress_rec_params (arm_length, leg_length, neck_length, face_shape)
);

CREATE TABLE IF NOT EXISTS venue_recommendations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    query_hash VARCHAR(64) NOT NULL UNIQUE,
    guest_count VARCHAR(20) NOT NULL,
    budget VARCHAR(20) NOT NULL,
    region VARCHAR(20) NOT NULL,
    style_preference VARCHAR(20) NOT NULL,
    season VARCHAR(20) NOT NULL,
    recommendation JSON NOT NULL,
    access_count INT DEFAULT 0,
    last_accessed DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_venue_rec_params (guest_count, budget, region, style_preference, season)
);
"

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Failed to create recommendation tables
    pause
    exit /b 1
)
echo Recommendation tables created successfully!

echo.
echo =====================================================
echo Step 3: Run migration script
echo =====================================================
echo Running migration from %SOURCE_DB% to %TARGET_DB%...
mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASS% %TARGET_DB% < "%~dp0migration_script.sql"

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Migration failed
    pause
    exit /b 1
)

echo.
echo =====================================================
echo Migration completed successfully!
echo =====================================================
pause
