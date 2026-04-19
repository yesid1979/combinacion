ALTER TABLE contratos 
ADD COLUMN adicion_si_no TEXT,
ADD COLUMN numero_cuotas_adicion INTEGER,
ADD COLUMN valor_total_adicion_letras TEXT,
ADD COLUMN valor_total_adicion NUMERIC,
ADD COLUMN valor_contrato_mas_adicion_letras TEXT,
ADD COLUMN valor_contrato_mas_adicion NUMERIC,
ADD COLUMN enlace_secop TEXT;
