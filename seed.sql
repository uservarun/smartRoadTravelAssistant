-- RoadSathi SQL Seed Data Script
-- Paste and run this in your Supabase SQL Editor to populate test data for the frontend team.

-- 1. Insert Mock Roads (LineStrings) in Mathura, India
INSERT INTO roads (id, road_name, geometry, length_m, curvature, speed_limit, avg_speed, speed_variance, risk_index, risk_class)
VALUES 
(
  'e29a986d-318e-4a6f-998f-4de1f10cd123',
  'Mathura Bypass Road (Straight Segment)',
  ST_GeomFromText('LINESTRING(77.6700 27.4900, 77.6800 27.5000)', 4326),
  1500.0,
  0.0,
  80.0,
  60.0,
  2.5,
  0.0,
  1
),
(
  'a38b982d-289e-4b6f-999f-5ee2f20cd456',
  'Highway Curve (High Risk Area)',
  ST_GeomFromText('LINESTRING(77.6800 27.5000, 77.6830 27.5015, 77.6850 27.5050)', 4326),
  800.0,
  0.015, -- Sharp curvature
  50.0,
  35.0,
  12.8, -- Unstable traffic speed
  153.6, -- Risk Score = Curvature * Length * Variance
  0 -- Class 0 (High Risk Curve)
);

-- 2. Insert Mock Railway Gate Alert (at the intersection of the bypass and the curve)
INSERT INTO alerts (id, alert_type, status, coordinate, description, is_active, road_id, created_at, updated_at)
VALUES (
  'b28c8eae-1ce4-47c4-936d-4e0508505999',
  'RAILWAY_GATE',
  'OPEN', -- Can toggle to CLOSED via Postman to test re-routing
  ST_GeomFromText('POINT(77.6800 27.5000)', 4326),
  'National Highway crossing gate - Mathura Junction',
  TRUE,
  'e29a986d-318e-4a6f-998f-4de1f10cd123',
  NOW(),
  NOW()
);

-- 3. Insert Mock Potholes
INSERT INTO potholes (id, coordinate, image_url, severity, ai_status, road_id, detected_at)
VALUES 
(
  'c38d8eae-2ce4-47c4-936d-4e0508505888',
  ST_GeomFromText('POINT(77.6740 27.4940)', 4326),
  'https://images.unsplash.com/photo-1515162305285-0293e4767cc2',
  'HIGH',
  'VERIFIED',
  'e29a986d-318e-4a6f-998f-4de1f10cd123',
  NOW()
),
(
  'd48e8eae-3ce4-47c4-936d-4e0508505777',
  ST_GeomFromText('POINT(77.6820 27.5008)', 4326),
  NULL,
  'MEDIUM',
  'PENDING',
  'a38b982d-289e-4b6f-999f-5ee2f20cd456',
  NOW()
);
