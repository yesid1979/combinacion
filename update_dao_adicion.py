import sys
import os

file_path = 'src/main/java/com/combinacion/dao/ContratoDAO.java'
with open(file_path, 'r', encoding='utf-8') as f:
    text = f.read()

# 1. Update INSERT sql
text = text.replace(
    'apoyo_supervision, fecha_idoneidad, fecha_estructurador) VALUES',
    'apoyo_supervision, fecha_idoneidad, fecha_estructurador, adicion_si_no, numero_cuotas_adicion, valor_total_adicion_letras, valor_total_adicion, valor_contrato_mas_adicion_letras, valor_contrato_mas_adicion, enlace_secop) VALUES'
)
text = text.replace(
    '(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
    '(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)'
)

insert_block_old = '''            ps.setDate(45, c.getFechaIdoneidad());
            ps.setDate(46, c.getFechaEstructurador());'''

insert_block_new = '''            ps.setDate(45, c.getFechaIdoneidad());
            ps.setDate(46, c.getFechaEstructurador());
            ps.setString(47, c.getAdicionSiNo());
            ps.setInt(48, c.getNumeroCuotasAdicion());
            ps.setString(49, c.getValorTotalAdicionLetras());
            ps.setBigDecimal(50, c.getValorTotalAdicion());
            ps.setString(51, c.getValorContratoMasAdicionLetras());
            ps.setBigDecimal(52, c.getValorContratoMasAdicion());
            ps.setString(53, c.getEnlaceSecop());'''

text = text.replace(insert_block_old, insert_block_new)

# 2. Update UPDATE sql
text = text.replace(
    'fecha_idoneidad=?, fecha_estructurador=? ' +
    'WHERE id=?',
    'fecha_idoneidad=?, fecha_estructurador=?, adicion_si_no=?, numero_cuotas_adicion=?, valor_total_adicion_letras=?, valor_total_adicion=?, valor_contrato_mas_adicion_letras=?, valor_contrato_mas_adicion=?, enlace_secop=? ' +
    'WHERE id=?'
)

actualizar_block_old = '''            ps.setDate(44, c.getFechaIdoneidad());
            ps.setDate(45, c.getFechaEstructurador());
            ps.setInt(46, c.getId());'''

actualizar_block_new = '''            ps.setDate(44, c.getFechaIdoneidad());
            ps.setDate(45, c.getFechaEstructurador());
            ps.setString(46, c.getAdicionSiNo());
            ps.setInt(47, c.getNumeroCuotasAdicion());
            ps.setString(48, c.getValorTotalAdicionLetras());
            ps.setBigDecimal(49, c.getValorTotalAdicion());
            ps.setString(50, c.getValorContratoMasAdicionLetras());
            ps.setBigDecimal(51, c.getValorContratoMasAdicion());
            ps.setString(52, c.getEnlaceSecop());
            ps.setInt(53, c.getId());'''
text = text.replace(actualizar_block_old, actualizar_block_new)

# 3. Update getters (select block)
getters_old = '''                    c.setFechaEstructurador(rs.getDate("fecha_estructurador"));
                    return c;'''
getters_new = '''                    c.setFechaEstructurador(rs.getDate("fecha_estructurador"));
                    c.setAdicionSiNo(rs.getString("adicion_si_no"));
                    c.setNumeroCuotasAdicion(rs.getInt("numero_cuotas_adicion"));
                    c.setValorTotalAdicionLetras(rs.getString("valor_total_adicion_letras"));
                    c.setValorTotalAdicion(rs.getBigDecimal("valor_total_adicion"));
                    c.setValorContratoMasAdicionLetras(rs.getString("valor_contrato_mas_adicion_letras"));
                    c.setValorContratoMasAdicion(rs.getBigDecimal("valor_contrato_mas_adicion"));
                    c.setEnlaceSecop(rs.getString("enlace_secop"));
                    return c;'''
text = text.replace(getters_old, getters_new)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(text)
print('Success updating DAO for ADICION')
