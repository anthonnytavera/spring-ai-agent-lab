-- Esquema de ejemplo: un modelo mínimo de ventas (clientes, productos y pedidos).
-- Spring Boot ejecuta este archivo al arrancar, antes que data.sql.

CREATE TABLE customers (
    id      BIGINT PRIMARY KEY,
    name    VARCHAR(100) NOT NULL,
    country VARCHAR(50)  NOT NULL
);

CREATE TABLE products (
    id       BIGINT PRIMARY KEY,
    name     VARCHAR(100)  NOT NULL,
    category VARCHAR(50)   NOT NULL,
    price    DECIMAL(10,2) NOT NULL
);

-- Un pedido relaciona un cliente con un producto. El importe de un pedido es quantity * products.price.
CREATE TABLE orders (
    id          BIGINT PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    product_id  BIGINT NOT NULL REFERENCES products(id),
    quantity    INT    NOT NULL,
    order_date  DATE   NOT NULL
);

-- Usuario de solo lectura para las tools del agente. Es la defensa principal: aunque el modelo
-- genere SQL malicioso (o lo manipulen con un prompt injection), la base de datos solo le permite
-- SELECT sobre estas tres tablas. No puede escribir, borrar, crear usuarios ni leer archivos.
CREATE USER IF NOT EXISTS agent_ro PASSWORD 'agent_ro';
GRANT SELECT ON customers, products, orders TO agent_ro;
