# mobilesco-back

Backend del ERP Mobilesco construido con Spring Boot.

## Comandos utiles

```bash
mvn -DskipTests compile
mvn test
```

## Cambios importantes recientes

### Catalogo administrable de tipos de insumo

- Se agrego el modulo `tipoinsumo`.
- El catalogo ahora se persiste en base de datos en la tabla `tipo_insumo_catalogo`.
- El seeder carga tipos base iniciales si no existen.

### API nueva

Base path:

```txt
/api/v1/tipos-insumo
```

Endpoints principales:

- `GET /api/v1/tipos-insumo`
- `GET /api/v1/tipos-insumo?soloActivos=true`
- `GET /api/v1/tipos-insumo/preview-codigo?nombre=Metalico`
- `POST /api/v1/tipos-insumo`
- `PUT /api/v1/tipos-insumo/{id}`
- `PATCH /api/v1/tipos-insumo/{id}/estado`

### Regla del codigo

- El codigo sugerido usa 1 letra.
- Si ya existe, intenta con 2 letras.
- Si sigue ocupado, intenta con 3 letras.
- El maximo permitido para autogeneracion es 3 caracteres.

### Proveedores

- `Proveedor` ya no usa un enum fijo para `tipoInsumo`.
- Ahora se relaciona con el catalogo de tipos por codigo.
- El response del modulo devuelve `tipoInsumo` y `tipoInsumoNombre`.

### Compatibilidad MySQL

- Flyway crea las instalaciones nuevas con `utf8mb4_unicode_ci`.
- La adopcion de una base existente valida las collations criticas y detiene el arranque si requieren una migracion explicita.

## Nota de compatibilidad

- Al editar un tipo de insumo se conserva el codigo actual para no romper relaciones existentes.

## Migraciones de base de datos

- Flyway es la unica autoridad de cambios de esquema.
- Hibernate usa `ddl-auto=validate` en todos los ambientes.
- Las instalaciones nuevas ejecutan las migraciones automaticamente.
- Las instalaciones existentes requieren backup, preflight y un baseline controlado.
- No edites una migracion aplicada ni uses `flyway repair` como rollback.

Consulta [la guia operativa](../docs/database-migrations.md) antes de adoptar Flyway o desplegar una migracion.
