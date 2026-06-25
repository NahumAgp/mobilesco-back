package com.mobilesco.mobilesco_back.modules.proveedor.application.usecases;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ValidationException;

class ProveedorServiceTest {

    private Object proveedorRepository;
    private Object compraRepository;
    private Object service;

    private RepositoryStub proveedorStub;
    private RepositoryStub compraStub;

    @BeforeEach
    void setUp() throws Exception {
        Class<?> proveedorRepositoryClass = Class.forName(
                "com.mobilesco.mobilesco_back.modules.proveedor.infrastructure.out.persistence.repositories.ProveedorRepository");
        Class<?> tipoInsumoRepositoryClass = Class.forName(
                "com.mobilesco.mobilesco_back.modules.tipoinsumo.infrastructure.out.persistence.repositories.TipoInsumoRepository");
        Class<?> compraRepositoryClass = Class.forName(
                "com.mobilesco.mobilesco_back.modules.compra.infrastructure.out.persistence.repositories.CompraRepository");
        Class<?> serviceClass = Class.forName(
                "com.mobilesco.mobilesco_back.modules.proveedor.application.usecases.ProveedorService");

        proveedorStub = new RepositoryStub();
        compraStub = new RepositoryStub();

        proveedorRepository = crearProxy(proveedorRepositoryClass, proveedorStub);
        compraRepository = crearProxy(compraRepositoryClass, compraStub);
        Object tipoInsumoRepository = crearProxy(tipoInsumoRepositoryClass, new RepositoryStub());

        Constructor<?> constructor = serviceClass.getConstructor(
                proveedorRepositoryClass,
                tipoInsumoRepositoryClass,
                compraRepositoryClass);
        service = constructor.newInstance(proveedorRepository, tipoInsumoRepository, compraRepository);
    }

    @Test
    void eliminarPermiteBorrarProveedorSinCompras() throws Exception {
        proveedorStub.booleanResults.put("existsById", true);
        compraStub.booleanResults.put("existsByProveedorId", false);

        assertDoesNotThrow(() -> invocarEliminar(1L));

        assertEquals(1, proveedorStub.deleteByIdCalls);
    }

    @Test
    void eliminarBloqueaProveedorConComprasAsociadas() throws Exception {
        proveedorStub.booleanResults.put("existsById", true);
        compraStub.booleanResults.put("existsByProveedorId", true);

        assertThrows(ValidationException.class, () -> invocarEliminar(2L));

        assertEquals(0, proveedorStub.deleteByIdCalls);
    }

    @Test
    void eliminarLanzaNotFoundSiElProveedorNoExiste() throws Exception {
        proveedorStub.booleanResults.put("existsById", false);

        assertThrows(NotFoundException.class, () -> invocarEliminar(3L));

        assertEquals(0, compraStub.invocations.getOrDefault("existsByProveedorId", 0));
        assertEquals(0, proveedorStub.deleteByIdCalls);
    }

    private void invocarEliminar(Long id) throws Exception {
        Method method = service.getClass().getMethod("eliminar", Long.class);
        try {
            method.invoke(service, id);
        } catch (InvocationTargetException ex) {
            Throwable causa = ex.getCause();
            if (causa instanceof Exception exception) {
                throw exception;
            }
            throw new RuntimeException(causa);
        }
    }

    private static Object crearProxy(Class<?> interfaz, RepositoryStub stub) {
        return Proxy.newProxyInstance(
                interfaz.getClassLoader(),
                new Class<?>[] { interfaz },
                stub);
    }

    private static class RepositoryStub implements InvocationHandler {
        private final Map<String, Boolean> booleanResults = new HashMap<>();
        private final Map<String, Integer> invocations = new HashMap<>();
        private int deleteByIdCalls = 0;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            invocations.merge(method.getName(), 1, Integer::sum);

            if ("deleteById".equals(method.getName())) {
                deleteByIdCalls++;
                return null;
            }

            if (booleanResults.containsKey(method.getName())) {
                return booleanResults.get(method.getName());
            }

            Class<?> returnType = method.getReturnType();
            if (returnType.equals(boolean.class) || returnType.equals(Boolean.class)) {
                return false;
            }
            if (returnType.equals(void.class)) {
                return null;
            }
            if (returnType.isPrimitive()) {
                if (returnType.equals(int.class) || returnType.equals(short.class) || returnType.equals(byte.class)) {
                    return 0;
                }
                if (returnType.equals(long.class)) {
                    return 0L;
                }
                if (returnType.equals(double.class)) {
                    return 0d;
                }
                if (returnType.equals(float.class)) {
                    return 0f;
                }
                if (returnType.equals(char.class)) {
                    return '\0';
                }
            }
            return null;
        }
    }
}
