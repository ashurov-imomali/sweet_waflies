CREATE TABLE customers (
                           id SERIAL PRIMARY KEY,          -- Уникальный идентификатор клиента
                           first_name VARCHAR(50) NOT NULL,         -- Имя клиента
                           last_name VARCHAR(50) NOT NULL,          -- Фамилия клиента
                           email VARCHAR(100) UNIQUE,      -- Электронная почта клиента
                           phone_number VARCHAR(20),                -- Номер телефона клиента
                           address TEXT,                            -- Адрес клиента
                           city VARCHAR(50) DEFAULT 'Dushanbe',                        -- Город
                           country VARCHAR(50) DEFAULT 'Tajikistan',   -- Страна
                           registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- Дата регистрации клиента
);


CREATE TABLE products (
                          id SERIAL PRIMARY KEY,                -- Уникальный идентификатор продукта
                          name VARCHAR(150) NOT NULL,           -- Название продукта
                          description TEXT,                     -- Описание продукта
                          price DECIMAL(10, 2) NOT NULL CHECK (price >= 0), -- Цена продукта, не может быть отрицательной
                          stock_quantity INT DEFAULT 0 CHECK (stock_quantity >= 0), -- Количество на складе, не может быть отрицательным
                          created_at TIMESTAMP DEFAULT NOW(),   -- Дата и время создания записи
                          updated_at TIMESTAMP DEFAULT NOW()  -- Дата и время последнего обновления записи
);

-- Создание индекса для быстрого поиска по названию продукта
CREATE INDEX idx_products_name ON products(name);


CREATE TABLE employees (
                           id SERIAL PRIMARY KEY,                  -- Уникальный идентификатор сотрудника
                           first_name VARCHAR(100) NOT NULL,       -- Имя сотрудника
                           last_name VARCHAR(100) NOT NULL,        -- Фамилия сотрудника
                           email VARCHAR(255) UNIQUE NOT NULL,     -- Электронная почта (уникальная)
                           phone_number VARCHAR(20),               -- Телефонный номер
                           position VARCHAR(100),                  -- Должность сотрудника
                           hire_date DATE DEFAULT CURRENT_DATE,    -- Дата приема на работу
                           salary DECIMAL(10, 2) CHECK (salary >= 0), -- Зарплата, не может быть отрицательной
                           status VARCHAR(20) DEFAULT 'active',    -- Статус сотрудника (по умолчанию 'active')
                           department VARCHAR(100)                 -- Отдел или департамент
);


create table users_auth(
                           id serial primary key,
                           username text,
                           password text,
                           employee_id int references employees
);


CREATE TABLE orders (
                        id SERIAL PRIMARY KEY,                                      -- Уникальный идентификатор заказа
                        customer_id INT NOT NULL references customers,              -- Идентификатор клиента
                        employee_id INT references employees,                       -- Идентификатор сотрудника, обрабатывающего заказ
                        order_date TIMESTAMP DEFAULT NOW(),                         -- Дата и время заказа
                        status VARCHAR(20) DEFAULT 'pending',                       -- Статус заказа (например, 'pending', 'completed', 'canceled')
                        total_amount DECIMAL(10, 2)                                 -- Общая сумма заказа
);

CREATE TABLE order_items (
                             id SERIAL PRIMARY KEY,                                      -- Уникальный идентификатор позиции заказа
                             order_id INT NOT NULL references orders,                    -- Идентификатор заказа
                             product_id INT NOT NULL references products,                -- Идентификатор продукта
                             quantity INT NOT NULL CHECK (quantity > 0),                 -- Количество продукта, должно быть больше нуля
                             price DECIMAL(10, 2) NOT NULL CHECK (price >= 0)            -- Цена продукта на момент заказа
);
