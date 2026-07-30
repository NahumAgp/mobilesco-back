# Perfil y optimizacion de consultas

## Alcance

Se perfilaron el arranque de datos base y las rutas de roles, permisos e insumos.
La medicion se hizo contando consultas emitidas por cada patron y verificando las
consultas de pagina con las estadisticas de Hibernate sobre H2 en modo MySQL.

## Antes

- El arranque consultaba cada permiso, rol y tipo de insumo por separado. Con
  `P` permisos, `R` roles y `D` roles que requieren permisos iniciales, el
  arranque nuevo requeria `P + R + D + 8` consultas de lectura, ademas de las
  escrituras necesarias.
- La pagina filtrada de insumos cargaba todo el catalogo, lo convertia a DTO,
  filtraba y finalmente aplicaba `subList`.
- Cada insumo convertido ejecutaba una consulta de ultimo costo, una de costo
  promedio y cuatro consultas de existencia. Una pagina de 10 elementos
  requeria hasta 62 consultas: pagina, conteo y `6 x 10`.
- La pagina de roles combinaba paginacion con carga de la coleccion de permisos,
  un patron que puede forzar paginacion en memoria en Hibernate.

## Despues

- El arranque carga permisos, roles y tipos una vez por catalogo y calcula en
  memoria solamente los registros faltantes. Pasa de `P + R + D + 8` lecturas
  a tres.
- Busqueda, estado, stock bajo, orden, conteo y limites de insumos se resuelven
  en la consulta paginada.
- Los costos y asociaciones de la pagina se cargan por lote. El numero de
  consultas queda acotado a un maximo de ocho para cualquier tamano de pagina:
  pagina, conteo, ultimo costo, costo promedio y cuatro conjuntos de uso.
- La unidad de medida se obtiene en la consulta de pagina; una prueba con
  estadisticas de Hibernate verifica un maximo de dos sentencias para pagina y
  conteo, sin crecimiento por fila.
- Los roles se paginan primero y sus permisos se recuperan en una segunda
  consulta limitada a los identificadores de esa pagina.

El formato de `PageResponseDTO` y los campos de los DTO de insumo, costo y rol
no cambiaron.
