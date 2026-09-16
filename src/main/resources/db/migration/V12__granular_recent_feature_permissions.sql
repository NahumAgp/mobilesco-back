INSERT INTO permissions (code, nombre, modulo, vista, descripcion, ruta, tipo, activo)
SELECT 'VIEW_ASSISTED_PROCUREMENT',
       'Ver abastecimiento asistido',
       'Compras',
       'Abastecimiento asistido',
       'Permite abrir y consultar Abastecimiento asistido.',
       '/compras/abastecimiento',
       'VIEW',
       TRUE
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'VIEW_ASSISTED_PROCUREMENT');

INSERT INTO permissions (code, nombre, modulo, vista, descripcion, ruta, tipo, activo)
SELECT 'ACTION_ASSISTED_PROCUREMENT_DRAFTS',
       'Generar borradores desde abastecimiento asistido',
       'Compras',
       'Abastecimiento asistido',
       'Generar borradores desde abastecimiento asistido en Abastecimiento asistido.',
       '/compras/abastecimiento',
       'ACTION',
       TRUE
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ACTION_ASSISTED_PROCUREMENT_DRAFTS');

INSERT INTO permissions (code, nombre, modulo, vista, descripcion, ruta, tipo, activo)
SELECT 'VIEW_INPUT_SETS',
       'Ver conjuntos de insumos',
       'Almacén',
       'Conjuntos de insumos',
       'Permite abrir y consultar Conjuntos de insumos.',
       '/insumos',
       'VIEW',
       TRUE
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'VIEW_INPUT_SETS');

INSERT INTO permissions (code, nombre, modulo, vista, descripcion, ruta, tipo, activo)
SELECT 'ACTION_INPUT_SETS_CREATE',
       'Crear conjuntos de insumos',
       'Almacén',
       'Conjuntos de insumos',
       'Crear conjuntos de insumos en Conjuntos de insumos.',
       '/insumos',
       'ACTION',
       TRUE
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ACTION_INPUT_SETS_CREATE');

INSERT INTO permissions (code, nombre, modulo, vista, descripcion, ruta, tipo, activo)
SELECT 'ACTION_INPUT_SETS_EDIT',
       'Editar conjuntos de insumos',
       'Almacén',
       'Conjuntos de insumos',
       'Editar conjuntos de insumos en Conjuntos de insumos.',
       '/insumos',
       'ACTION',
       TRUE
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ACTION_INPUT_SETS_EDIT');

UPDATE permissions
   SET nombre = 'Ver abastecimiento asistido',
       modulo = 'Compras',
       vista = 'Abastecimiento asistido',
       descripcion = 'Permite abrir y consultar Abastecimiento asistido.',
       ruta = '/compras/abastecimiento',
       tipo = 'VIEW',
       activo = TRUE
 WHERE code = 'VIEW_ASSISTED_PROCUREMENT';

UPDATE permissions
   SET nombre = 'Generar borradores desde abastecimiento asistido',
       modulo = 'Compras',
       vista = 'Abastecimiento asistido',
       descripcion = 'Generar borradores desde abastecimiento asistido en Abastecimiento asistido.',
       ruta = '/compras/abastecimiento',
       tipo = 'ACTION',
       activo = TRUE
 WHERE code = 'ACTION_ASSISTED_PROCUREMENT_DRAFTS';

UPDATE permissions
   SET nombre = 'Ver conjuntos de insumos',
       modulo = 'Almacén',
       vista = 'Conjuntos de insumos',
       descripcion = 'Permite abrir y consultar Conjuntos de insumos.',
       ruta = '/insumos',
       tipo = 'VIEW',
       activo = TRUE
 WHERE code = 'VIEW_INPUT_SETS';

UPDATE permissions
   SET nombre = 'Crear conjuntos de insumos',
       modulo = 'Almacén',
       vista = 'Conjuntos de insumos',
       descripcion = 'Crear conjuntos de insumos en Conjuntos de insumos.',
       ruta = '/insumos',
       tipo = 'ACTION',
       activo = TRUE
 WHERE code = 'ACTION_INPUT_SETS_CREATE';

UPDATE permissions
   SET nombre = 'Editar conjuntos de insumos',
       modulo = 'Almacén',
       vista = 'Conjuntos de insumos',
       descripcion = 'Editar conjuntos de insumos en Conjuntos de insumos.',
       ruta = '/insumos',
       tipo = 'ACTION',
       activo = TRUE
 WHERE code = 'ACTION_INPUT_SETS_EDIT';

INSERT INTO role_permissions (role_id, permission_id)
SELECT DISTINCT rp.role_id, target_permission.id
  FROM role_permissions rp
  JOIN permissions source_permission
    ON source_permission.id = rp.permission_id
   AND source_permission.code = 'VIEW_PURCHASES'
  JOIN permissions target_permission
    ON target_permission.code = 'VIEW_ASSISTED_PROCUREMENT'
 WHERE NOT EXISTS (
       SELECT 1 FROM role_permissions existing_assignment
        WHERE existing_assignment.role_id = rp.role_id
          AND existing_assignment.permission_id = target_permission.id
 );

INSERT INTO role_permissions (role_id, permission_id)
SELECT DISTINCT rp.role_id, target_permission.id
  FROM role_permissions rp
  JOIN permissions source_permission
    ON source_permission.id = rp.permission_id
   AND source_permission.code = 'ACTION_PURCHASES_CREATE'
  JOIN permissions target_permission
    ON target_permission.code = 'ACTION_ASSISTED_PROCUREMENT_DRAFTS'
 WHERE NOT EXISTS (
       SELECT 1 FROM role_permissions existing_assignment
        WHERE existing_assignment.role_id = rp.role_id
          AND existing_assignment.permission_id = target_permission.id
 );

INSERT INTO role_permissions (role_id, permission_id)
SELECT DISTINCT rp.role_id, target_permission.id
  FROM role_permissions rp
  JOIN permissions source_permission
    ON source_permission.id = rp.permission_id
   AND source_permission.code = 'VIEW_INVENTORY'
  JOIN permissions target_permission
    ON target_permission.code = 'VIEW_INPUT_SETS'
 WHERE NOT EXISTS (
       SELECT 1 FROM role_permissions existing_assignment
        WHERE existing_assignment.role_id = rp.role_id
          AND existing_assignment.permission_id = target_permission.id
 );

INSERT INTO role_permissions (role_id, permission_id)
SELECT DISTINCT rp.role_id, target_permission.id
  FROM role_permissions rp
  JOIN permissions source_permission
    ON source_permission.id = rp.permission_id
   AND source_permission.code = 'ACTION_INVENTORY_CREATE'
  JOIN permissions target_permission
    ON target_permission.code = 'ACTION_INPUT_SETS_CREATE'
 WHERE NOT EXISTS (
       SELECT 1 FROM role_permissions existing_assignment
        WHERE existing_assignment.role_id = rp.role_id
          AND existing_assignment.permission_id = target_permission.id
 );

INSERT INTO role_permissions (role_id, permission_id)
SELECT DISTINCT rp.role_id, target_permission.id
  FROM role_permissions rp
  JOIN permissions source_permission
    ON source_permission.id = rp.permission_id
   AND source_permission.code = 'ACTION_INVENTORY_EDIT'
  JOIN permissions target_permission
    ON target_permission.code = 'ACTION_INPUT_SETS_EDIT'
 WHERE NOT EXISTS (
       SELECT 1 FROM role_permissions existing_assignment
        WHERE existing_assignment.role_id = rp.role_id
          AND existing_assignment.permission_id = target_permission.id
 );

INSERT INTO user_permissions (user_id, permission_id)
SELECT DISTINCT up.user_id, target_permission.id
  FROM user_permissions up
  JOIN permissions source_permission
    ON source_permission.id = up.permission_id
   AND source_permission.code = 'VIEW_PURCHASES'
  JOIN permissions target_permission
    ON target_permission.code = 'VIEW_ASSISTED_PROCUREMENT'
 WHERE NOT EXISTS (
       SELECT 1 FROM user_permissions existing_assignment
        WHERE existing_assignment.user_id = up.user_id
          AND existing_assignment.permission_id = target_permission.id
 );

INSERT INTO user_permissions (user_id, permission_id)
SELECT DISTINCT up.user_id, target_permission.id
  FROM user_permissions up
  JOIN permissions source_permission
    ON source_permission.id = up.permission_id
   AND source_permission.code = 'ACTION_PURCHASES_CREATE'
  JOIN permissions target_permission
    ON target_permission.code = 'ACTION_ASSISTED_PROCUREMENT_DRAFTS'
 WHERE NOT EXISTS (
       SELECT 1 FROM user_permissions existing_assignment
        WHERE existing_assignment.user_id = up.user_id
          AND existing_assignment.permission_id = target_permission.id
 );

INSERT INTO user_permissions (user_id, permission_id)
SELECT DISTINCT up.user_id, target_permission.id
  FROM user_permissions up
  JOIN permissions source_permission
    ON source_permission.id = up.permission_id
   AND source_permission.code = 'VIEW_INVENTORY'
  JOIN permissions target_permission
    ON target_permission.code = 'VIEW_INPUT_SETS'
 WHERE NOT EXISTS (
       SELECT 1 FROM user_permissions existing_assignment
        WHERE existing_assignment.user_id = up.user_id
          AND existing_assignment.permission_id = target_permission.id
 );

INSERT INTO user_permissions (user_id, permission_id)
SELECT DISTINCT up.user_id, target_permission.id
  FROM user_permissions up
  JOIN permissions source_permission
    ON source_permission.id = up.permission_id
   AND source_permission.code = 'ACTION_INVENTORY_CREATE'
  JOIN permissions target_permission
    ON target_permission.code = 'ACTION_INPUT_SETS_CREATE'
 WHERE NOT EXISTS (
       SELECT 1 FROM user_permissions existing_assignment
        WHERE existing_assignment.user_id = up.user_id
          AND existing_assignment.permission_id = target_permission.id
 );

INSERT INTO user_permissions (user_id, permission_id)
SELECT DISTINCT up.user_id, target_permission.id
  FROM user_permissions up
  JOIN permissions source_permission
    ON source_permission.id = up.permission_id
   AND source_permission.code = 'ACTION_INVENTORY_EDIT'
  JOIN permissions target_permission
    ON target_permission.code = 'ACTION_INPUT_SETS_EDIT'
 WHERE NOT EXISTS (
       SELECT 1 FROM user_permissions existing_assignment
        WHERE existing_assignment.user_id = up.user_id
          AND existing_assignment.permission_id = target_permission.id
 );
