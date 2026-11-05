-- La carga inicial insertó insumos directamente en SQL, sin pasar por el generador EAN-13.
-- Se usa la misma regla de InsumoService: prefijo 750, nueve dígitos del ID y checksum.
UPDATE insumo
SET codigo_barras = CONCAT(
    '750',
    LPAD(MOD(id, 1000000000), 9, '0'),
    MOD(10 - MOD(
        22
        + 3 * MOD(FLOOR(id / 100000000), 10)
        +     MOD(FLOOR(id / 10000000), 10)
        + 3 * MOD(FLOOR(id / 1000000), 10)
        +     MOD(FLOOR(id / 100000), 10)
        + 3 * MOD(FLOOR(id / 10000), 10)
        +     MOD(FLOOR(id / 1000), 10)
        + 3 * MOD(FLOOR(id / 100), 10)
        +     MOD(FLOOR(id / 10), 10)
        + 3 * MOD(id, 10),
    10), 10)
)
WHERE codigo_barras IS NULL OR codigo_barras = '';
