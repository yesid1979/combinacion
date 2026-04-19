import sys
import os
import re

file_path = 'src/main/java/com/combinacion/dao/ContratoDAO.java'
with open(file_path, 'r', encoding='utf-8') as f:
    text = f.read()

# 1. Update INSERT sql
text = text.replace(
    'valor_cuota_letras, valor_cuota_numero, num_cuotas_letras',
    'valor_cuota_letras, valor_cuota_numero, valor_cuota_antes_iva_letras, valor_cuota_antes_iva, valor_cuota_iva_letras, valor_cuota_iva, num_cuotas_letras'
)
text = text.replace(
    '(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
    '(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)'
)

# Replace sets in insertar
# Instead of Regex, let's just replace the whole block exactly because it's deterministic
insert_block_old = '''            ps.setString(24, c.getValorCuotaLetras());
            ps.setBigDecimal(25, c.getValorCuotaNumero());
            ps.setString(26, c.getNumCuotasLetras());
            ps.setInt(27, c.getNumCuotasNumero());
            ps.setString(28, c.getValorMediaCuotaLetras());
            ps.setBigDecimal(29, c.getValorMediaCuotaNumero());
            ps.setString(30, c.getActividadesEntregables());
            ps.setString(31, c.getLiquidacionAcuerdo());
            ps.setString(32, c.getLiquidacionArticulo());
            ps.setString(33, c.getLiquidacionDecreto());
            ps.setString(34, c.getCircularHonorarios());
            if (c.getContratistaId() > 0)
                ps.setInt(35, c.getContratistaId());
            else
                ps.setNull(35, java.sql.Types.INTEGER);
            if (c.getSupervisorId() > 0)
                ps.setInt(36, c.getSupervisorId());
            else
                ps.setNull(36, java.sql.Types.INTEGER);
            if (c.getOrdenadorId() > 0)
                ps.setInt(37, c.getOrdenadorId());
            else
                ps.setNull(37, java.sql.Types.INTEGER);
            if (c.getPresupuestoId() > 0)
                ps.setInt(38, c.getPresupuestoId());
            else
                ps.setNull(38, java.sql.Types.INTEGER);
            if (c.getEstructuradorId() > 0)
                ps.setInt(39, c.getEstructuradorId());
            else
                ps.setNull(39, java.sql.Types.INTEGER);
            ps.setString(40, c.getApoyoSupervision());
            ps.setDate(41, c.getFechaIdoneidad());
            ps.setDate(42, c.getFechaEstructurador());'''

insert_block_new = '''            ps.setString(24, c.getValorCuotaLetras());
            ps.setBigDecimal(25, c.getValorCuotaNumero());
            ps.setString(26, c.getValorCuotaAntesIvaLetras());
            ps.setBigDecimal(27, c.getValorCuotaAntesIva());
            ps.setString(28, c.getValorCuotaIvaLetras());
            ps.setBigDecimal(29, c.getValorCuotaIva());
            ps.setString(30, c.getNumCuotasLetras());
            ps.setInt(31, c.getNumCuotasNumero());
            ps.setString(32, c.getValorMediaCuotaLetras());
            ps.setBigDecimal(33, c.getValorMediaCuotaNumero());
            ps.setString(34, c.getActividadesEntregables());
            ps.setString(35, c.getLiquidacionAcuerdo());
            ps.setString(36, c.getLiquidacionArticulo());
            ps.setString(37, c.getLiquidacionDecreto());
            ps.setString(38, c.getCircularHonorarios());
            if (c.getContratistaId() > 0)
                ps.setInt(39, c.getContratistaId());
            else
                ps.setNull(39, java.sql.Types.INTEGER);
            if (c.getSupervisorId() > 0)
                ps.setInt(40, c.getSupervisorId());
            else
                ps.setNull(40, java.sql.Types.INTEGER);
            if (c.getOrdenadorId() > 0)
                ps.setInt(41, c.getOrdenadorId());
            else
                ps.setNull(41, java.sql.Types.INTEGER);
            if (c.getPresupuestoId() > 0)
                ps.setInt(42, c.getPresupuestoId());
            else
                ps.setNull(42, java.sql.Types.INTEGER);
            if (c.getEstructuradorId() > 0)
                ps.setInt(43, c.getEstructuradorId());
            else
                ps.setNull(43, java.sql.Types.INTEGER);
            ps.setString(44, c.getApoyoSupervision());
            ps.setDate(45, c.getFechaIdoneidad());
            ps.setDate(46, c.getFechaEstructurador());'''

text = text.replace(insert_block_old, insert_block_new)

# 2. Update UPDATE sql
text = text.replace(
    'valor_cuota_letras=?, valor_cuota_numero=?, num_cuotas_letras',
    'valor_cuota_letras=?, valor_cuota_numero=?, valor_cuota_antes_iva_letras=?, valor_cuota_antes_iva=?, valor_cuota_iva_letras=?, valor_cuota_iva=?, num_cuotas_letras'
)

actualizar_block_old = '''            ps.setString(23, c.getValorCuotaLetras());
            ps.setBigDecimal(24, c.getValorCuotaNumero());
            ps.setString(25, c.getNumCuotasLetras());
            ps.setInt(26, c.getNumCuotasNumero());
            ps.setString(27, c.getValorMediaCuotaLetras());
            ps.setBigDecimal(28, c.getValorMediaCuotaNumero());
            ps.setString(29, c.getActividadesEntregables());
            ps.setString(30, c.getLiquidacionAcuerdo());
            ps.setString(31, c.getLiquidacionArticulo());
            ps.setString(32, c.getLiquidacionDecreto());
            ps.setString(33, c.getCircularHonorarios());
            if (c.getContratistaId() > 0)
                ps.setInt(34, c.getContratistaId());
            else
                ps.setNull(34, java.sql.Types.INTEGER);
            if (c.getSupervisorId() > 0)
                ps.setInt(35, c.getSupervisorId());
            else
                ps.setNull(35, java.sql.Types.INTEGER);
            if (c.getOrdenadorId() > 0)
                ps.setInt(36, c.getOrdenadorId());
            else
                ps.setNull(36, java.sql.Types.INTEGER);
            if (c.getPresupuestoId() > 0)
                ps.setInt(37, c.getPresupuestoId());
            else
                ps.setNull(37, java.sql.Types.INTEGER);
            if (c.getEstructuradorId() > 0)
                ps.setInt(38, c.getEstructuradorId());
            else
                ps.setNull(38, java.sql.Types.INTEGER);
            ps.setString(39, c.getApoyoSupervision());
            ps.setDate(40, c.getFechaIdoneidad());
            ps.setDate(41, c.getFechaEstructurador());
            ps.setInt(42, c.getId());'''

actualizar_block_new = '''            ps.setString(23, c.getValorCuotaLetras());
            ps.setBigDecimal(24, c.getValorCuotaNumero());
            ps.setString(25, c.getValorCuotaAntesIvaLetras());
            ps.setBigDecimal(26, c.getValorCuotaAntesIva());
            ps.setString(27, c.getValorCuotaIvaLetras());
            ps.setBigDecimal(28, c.getValorCuotaIva());
            ps.setString(29, c.getNumCuotasLetras());
            ps.setInt(30, c.getNumCuotasNumero());
            ps.setString(31, c.getValorMediaCuotaLetras());
            ps.setBigDecimal(32, c.getValorMediaCuotaNumero());
            ps.setString(33, c.getActividadesEntregables());
            ps.setString(34, c.getLiquidacionAcuerdo());
            ps.setString(35, c.getLiquidacionArticulo());
            ps.setString(36, c.getLiquidacionDecreto());
            ps.setString(37, c.getCircularHonorarios());
            if (c.getContratistaId() > 0)
                ps.setInt(38, c.getContratistaId());
            else
                ps.setNull(38, java.sql.Types.INTEGER);
            if (c.getSupervisorId() > 0)
                ps.setInt(39, c.getSupervisorId());
            else
                ps.setNull(39, java.sql.Types.INTEGER);
            if (c.getOrdenadorId() > 0)
                ps.setInt(40, c.getOrdenadorId());
            else
                ps.setNull(40, java.sql.Types.INTEGER);
            if (c.getPresupuestoId() > 0)
                ps.setInt(41, c.getPresupuestoId());
            else
                ps.setNull(41, java.sql.Types.INTEGER);
            if (c.getEstructuradorId() > 0)
                ps.setInt(42, c.getEstructuradorId());
            else
                ps.setNull(42, java.sql.Types.INTEGER);
            ps.setString(43, c.getApoyoSupervision());
            ps.setDate(44, c.getFechaIdoneidad());
            ps.setDate(45, c.getFechaEstructurador());
            ps.setInt(46, c.getId());'''
text = text.replace(actualizar_block_old, actualizar_block_new)

# 3. Update getters (select block)
getters_old = '''                    c.setValorCuotaLetras(rs.getString("valor_cuota_letras"));
                    c.setValorCuotaNumero(rs.getBigDecimal("valor_cuota_numero"));'''
getters_new = '''                    c.setValorCuotaLetras(rs.getString("valor_cuota_letras"));
                    c.setValorCuotaNumero(rs.getBigDecimal("valor_cuota_numero"));
                    c.setValorCuotaAntesIvaLetras(rs.getString("valor_cuota_antes_iva_letras"));
                    c.setValorCuotaAntesIva(rs.getBigDecimal("valor_cuota_antes_iva"));
                    c.setValorCuotaIvaLetras(rs.getString("valor_cuota_iva_letras"));
                    c.setValorCuotaIva(rs.getBigDecimal("valor_cuota_iva"));'''
text = text.replace(getters_old, getters_new)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(text)
print('Success updating DAO')
