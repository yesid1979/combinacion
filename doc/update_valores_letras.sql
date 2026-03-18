ALTER TABLE contratos 
ADD COLUMN valor_antes_iva_letras TEXT,
ADD COLUMN valor_iva_letras TEXT,
ADD COLUMN valor_cuota_antes_iva_letras TEXT,
ADD COLUMN valor_cuota_antes_iva NUMERIC,
ADD COLUMN valor_cuota_iva_letras TEXT,
ADD COLUMN valor_cuota_iva NUMERIC;
