-- Sample seed data for Product API (COM-66)

-- Brands
INSERT INTO brands (id, name, slug, logo_url, description, is_active, created_at, updated_at) VALUES
('brand-001', 'Nike', 'nike', '/assets/nike_logo.png', 'Just Do It', true, NOW(), NOW()),
('brand-002', 'Adidas', 'adidas', '/assets/adidas_logo.png', 'Impossible Is Nothing', true, NOW(), NOW()),
('brand-003', 'Levi''s', 'levis', '/assets/levis_logo.png', 'Original Jeans', true, NOW(), NOW());

-- Categories
INSERT INTO categories (id, parent_id, name, slug, description, image_url, display_order, is_active, seo_title, created_at, updated_at) VALUES
('cat-001', NULL, 'Clothing', 'clothing', 'All clothing items', '/assets/shirt_category.jpg', 1, true, 'Shop Clothing', NOW(), NOW()),
('cat-002', NULL, 'Accessories', 'accessories', 'Fashion accessories', '/assets/accessories_category.jpg', 2, true, 'Shop Accessories', NOW(), NOW()),
('cat-011', 'cat-001', 'T-Shirts', 'tshirts', 'Casual and graphic tees', '/assets/shirt_category.jpg', 1, true, 'Shop T-Shirts', NOW(), NOW()),
('cat-012', 'cat-001', 'Denim', 'denim', 'Jeans and denim wear', '/assets/denim_category.jpg', 2, true, 'Shop Denim', NOW(), NOW()),
('cat-013', 'cat-001', 'Dresses', 'dresses', 'Women''s dresses', '/assets/dresses_category.jpg', 3, true, 'Shop Dresses', NOW(), NOW());

-- Products
INSERT INTO products (id, name, slug, sku, brand_id, category_id, description_short, description_long, base_price, compare_at_price, status, created_at, updated_at) VALUES
('prod-001', 'Nike Classic Tee', 'nike-classic-tee', 'NK-TEE-001', 'brand-001', 'cat-011',
 'Comfortable everyday tee', '<p>Premium cotton Nike Classic Tee.</p>', 29.99, 39.99, 'ACTIVE', NOW(), NOW()),
('prod-002', 'Adidas Slim Fit Tee', 'adidas-slim-fit-tee', 'AD-TEE-001', 'brand-002', 'cat-011',
 'Slim fit performance tee', '<p>Adidas slim fit tee made with recycled materials.</p>', 34.99, 44.99, 'ACTIVE', NOW(), NOW()),
('prod-003', 'Levi''s 501 Original Jeans', 'levis-501-original', 'LV-DEN-001', 'brand-003', 'cat-012',
 'The original straight-fit jean', '<p>The iconic Levi''s 501 in classic indigo.</p>', 79.99, 99.99, 'ACTIVE', NOW(), NOW());

-- Product Images
INSERT INTO product_images (id, product_id, url, thumbnail_url, alt_text, is_primary, display_order, created_at) VALUES
('img-001', 'prod-001', '/assets/featured_product_shirt.jpg', '/assets/featured_product_shirt.jpg', 'Nike Classic Tee front', true, 1, NOW()),
('img-002', 'prod-002', '/assets/featured_product_shirt.jpg', '/assets/featured_product_shirt.jpg', 'Adidas Slim Tee front', true, 1, NOW()),
('img-003', 'prod-003', '/assets/featured_product_denim.jpg', '/assets/featured_product_denim.jpg', 'Levi''s 501 front', true, 1, NOW());

-- Product Variants
INSERT INTO product_variants (id, product_id, sku, price, compare_at_price, quantity, is_default, created_at, updated_at) VALUES
('var-001', 'prod-001', 'NK-TEE-001-S', 29.99, 39.99, 50, true, NOW(), NOW()),
('var-002', 'prod-001', 'NK-TEE-001-M', 29.99, 39.99, 80, false, NOW(), NOW()),
('var-003', 'prod-001', 'NK-TEE-001-L', 29.99, 39.99, 60, false, NOW(), NOW()),
('var-004', 'prod-002', 'AD-TEE-001-S', 34.99, 44.99, 40, true, NOW(), NOW()),
('var-005', 'prod-002', 'AD-TEE-001-M', 34.99, 44.99, 70, false, NOW(), NOW()),
('var-006', 'prod-003', 'LV-DEN-001-32', 79.99, 99.99, 30, true, NOW(), NOW()),
('var-007', 'prod-003', 'LV-DEN-001-34', 79.99, 99.99, 25, false, NOW(), NOW());

-- Product Variant Attributes
INSERT INTO product_variant_attributes (id, variant_id, attribute_name, attribute_value) VALUES
('pva-001', 'var-001', 'size', 'S'),
('pva-002', 'var-002', 'size', 'M'),
('pva-003', 'var-003', 'size', 'L'),
('pva-004', 'var-004', 'size', 'S'),
('pva-005', 'var-005', 'size', 'M'),
('pva-006', 'var-006', 'waist', '32'),
('pva-007', 'var-007', 'waist', '34');

-- Product Attributes
INSERT INTO product_attributes (id, product_id, attribute_name, attribute_value) VALUES
('attr-001', 'prod-001', 'material', '100% Cotton'),
('attr-002', 'prod-001', 'fit', 'Regular'),
('attr-003', 'prod-002', 'material', 'Recycled Polyester'),
('attr-004', 'prod-002', 'fit', 'Slim'),
('attr-005', 'prod-003', 'material', 'Denim'),
('attr-006', 'prod-003', 'fit', 'Straight');

-- Reviews
INSERT INTO reviews (id, product_id, user_id, user_name, rating, title, comment, verified_purchase, helpful_count, not_helpful_count, created_at, updated_at) VALUES
('rev-001', 'prod-001', 'user-001', 'John D.', 5, 'Great quality!', 'Very comfortable and true to size.', true, 12, 1, NOW(), NOW()),
('rev-002', 'prod-001', 'user-002', 'Sarah M.', 4, 'Nice tee', 'Good quality, washes well.', true, 8, 0, NOW(), NOW()),
('rev-003', 'prod-003', 'user-003', 'Mike R.', 5, 'Perfect fit', 'Classic Levi''s quality. Will buy more.', true, 20, 2, NOW(), NOW());
