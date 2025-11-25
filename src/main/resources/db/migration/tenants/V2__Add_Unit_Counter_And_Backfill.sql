-- 1. Adiciona as colunas (inicialmente permitindo NULL para facilitar o update)
ALTER TABLE tb_product 
ADD COLUMN unit_counter BIGINT NOT NULL DEFAULT 0;

ALTER TABLE tb_product_transaction 
ADD COLUMN unit_name VARCHAR(50);

-- 2. BACKFILL: Preenche o unit_name para transações existentes
-- Usa uma 'Window Function' para numerar as linhas existentes por produto, ordenadas por data
UPDATE tb_product_transaction pt
JOIN (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY product_id ORDER BY created_at ASC, id ASC) as seq
    FROM tb_product_transaction
) sub ON pt.id = sub.id
SET pt.unit_name = CONCAT('Unidade ', sub.seq);

-- 3. Sincroniza o contador do Produto
-- Atualiza o produto com a contagem real de transações que ele possui
UPDATE tb_product p
SET p.unit_counter = (
    SELECT COUNT(*) 
    FROM tb_product_transaction pt 
    WHERE pt.product_id = p.id
);