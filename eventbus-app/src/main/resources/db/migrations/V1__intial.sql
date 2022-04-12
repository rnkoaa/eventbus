
-- [jooq ignore start]
PRAGMA foreign_keys = ON;
-- [jooq ignore stop]

CREATE TABLE IF NOT EXISTS category (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL,
    active BOOLEAN,
    created_at TEXT,
    updated_at TEXT,
    version INTEGER
);

-- [jooq ignore start]
  CREATE UNIQUE INDEX IF NOT EXISTS idx_category_name
      ON category (name);
-- [jooq ignore stop]

CREATE TABLE IF NOT EXISTS  product (
    id INTEGER PRIMARY KEY,
    title TEXT,
    sku TEXT,
    description TEXT,
    summary TEXT,
    website_url TEXT,
    price JSON,
    active BOOLEAN,
    created_at TEXT,
    updated_at TEXT,
    version INTEGER
);


-- [jooq ignore start]
  CREATE UNIQUE INDEX IF NOT EXISTS idx_product_sku
      ON product (sku);
-- [jooq ignore stop]


CREATE TABLE IF NOT EXISTS product_category (
    category_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    CONSTRAINT fk_pc_product_id FOREIGN KEY (product_id) REFERENCES product (id),
    CONSTRAINT fk_pc_category_id FOREIGN KEY (category_id) REFERENCES category (id)
);

CREATE TABLE IF NOT EXISTS product_images (
    id INTEGER PRIMARY KEY,
    product_id INTEGER NOT NULL,
    image_slug TEXT,
    caption TEXT,
    created_at TEXT,
    updated_at TEXT,
    version INTEGER,
    CONSTRAINT fk_pi_product_id FOREIGN KEY (product_id) REFERENCES product (id)
);


