-- Datos de ejemplo. Sirven para formular preguntas con respuesta conocida y comprobar al agente, por ejemplo:
--   "¿Cuántos clientes hay en Peru?"  ->  2 (Ana Torres y Maria Lopez)
--   "¿Qué cliente gastó más?"          ->  Ana Torres (1251.00). Totales: Ana 1251.00, John 1200.00,
--                                          Maria 436.50, Luis 300.00 (gasto = quantity * price de cada pedido)
INSERT INTO customers (id, name, country) VALUES
    (1, 'Ana Torres',   'Peru'),
    (2, 'Luis Gomez',   'Chile'),
    (3, 'Maria Lopez',  'Peru'),
    (4, 'John Smith',   'USA');

INSERT INTO products (id, name, category, price) VALUES
    (1, 'Laptop Pro',      'Electronica', 1200.00),
    (2, 'Mouse Inalambrico','Electronica',   25.50),
    (3, 'Escritorio',      'Muebles',       300.00),
    (4, 'Silla Ergonomica','Muebles',       180.00);

INSERT INTO orders (id, customer_id, product_id, quantity, order_date) VALUES
    (1, 1, 1, 1, '2026-01-15'),
    (2, 1, 2, 2, '2026-01-15'),
    (3, 2, 3, 1, '2026-02-10'),
    (4, 3, 4, 2, '2026-03-05'),
    (5, 4, 1, 1, '2026-03-20'),
    (6, 3, 2, 3, '2026-04-02');
