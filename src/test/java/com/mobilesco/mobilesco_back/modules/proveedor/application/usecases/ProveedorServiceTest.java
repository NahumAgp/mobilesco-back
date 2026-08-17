package com.mobilesco.mobilesco_back.modules.proveedor.application.usecases;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mobilesco.mobilesco_back.modules.proveedor.domain.models.ProveedorModel;
import com.mobilesco.mobilesco_back.modules.proveedor.infrastructure.in.api.dtos.ProveedorResponseDTO;
import com.mobilesco.mobilesco_back.modules.proveedor.infrastructure.in.api.dtos.ProveedorUpdateDTO;
import com.mobilesco.mobilesco_back.modules.tipoinsumo.domain.models.TipoInsumoModel;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ValidationException;

class ProveedorServiceTest {

    private Object proveedorRepository;
    private Object compraRepository;
    private Object service;

    private RepositoryStub proveedorStub;
    private RepositoryStub compraStub;
    private RepositoryStub tipoInsumoStub;

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
        tipoInsumoStub = new RepositoryStub();

        proveedorRepository = crearProxy(proveedorRepositoryClass, proveedorStub);
        compraRepository = crearProxy(compraRepositoryClass, compraStub);
        Object tipoInsumoRepository = crearProxy(tipoInsumoRepositoryClass, tipoInsumoStub);

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

    @Test
    void actualizarCalificacionPersisteYMapeaElValor() throws Exception {
        ProveedorModel proveedor = new ProveedorModel();
        proveedor.setId(4L);
        proveedor.setRazonSocial("Proveedor evaluado");
        proveedorStub.objectResults.put("findById", Optional.of(proveedor));

        BigDecimal calificacion = new BigDecimal("87.45");
        ProveedorResponseDTO respuesta = invocarActualizarCalificacion(4L, calificacion);

        assertEquals(calificacion, proveedor.getCalificacionProveedor());
        assertEquals(calificacion, respuesta.getCalificacionProveedor());
        assertEquals(1, proveedorStub.invocations.getOrDefault("save", 0));
    }

    @Test
    void actualizarFichaConservaLaCalificacionExistente() throws Exception {
        ProveedorModel proveedor = new ProveedorModel();
        proveedor.setId(5L);
        proveedor.setRazonSocial("Proveedor existente");
        proveedor.setCalificacionProveedor(new BigDecimal("91.25"));
        proveedorStub.objectResults.put("findById", Optional.of(proveedor));
        proveedorStub.objectResults.put("findByRazonSocialIgnoreCase", Optional.of(proveedor));

        TipoInsumoModel tipo = new TipoInsumoModel();
        tipo.setCodigo("M");
        tipoInsumoStub.objectResults.put("findByCodigoIgnoreCase", Optional.of(tipo));

        ProveedorUpdateDTO dto = new ProveedorUpdateDTO();
        dto.setRazonSocial("Proveedor existente");
        dto.setRfc("PEX010101AA1");
        dto.setNombre("Ana");
        dto.setTipoInsumo("M");
        dto.setTelefono("3312345678");
        dto.setCorreo("ana@example.com");
        dto.setActivo(true);

        Method method = service.getClass().getMethod("actualizar", Long.class, ProveedorUpdateDTO.class);
        ProveedorResponseDTO respuesta = (ProveedorResponseDTO) method.invoke(service, 5L, dto);

        assertEquals(new BigDecimal("91.25"), proveedor.getCalificacionProveedor());
        assertEquals(new BigDecimal("91.25"), respuesta.getCalificacionProveedor());
    }

    @Test
    void actualizarFichaCambiaLaCalificacionCuandoElCampoFueIncluido() throws Exception {
        ProveedorModel proveedor = new ProveedorModel();
        proveedor.setId(6L);
        proveedor.setRazonSocial("Proveedor editable");
        proveedor.setCalificacionProveedor(new BigDecimal("70.00"));
        proveedorStub.objectResults.put("findById", Optional.of(proveedor));
        proveedorStub.objectResults.put("findByRazonSocialIgnoreCase", Optional.of(proveedor));

        TipoInsumoModel tipo = new TipoInsumoModel();
        tipo.setCodigo("M");
        tipoInsumoStub.objectResults.put("findByCodigoIgnoreCase", Optional.of(tipo));

        ProveedorUpdateDTO dto = new ProveedorUpdateDTO();
        dto.setRazonSocial("Proveedor editable");
        dto.setRfc("PED010101AA1");
        dto.setNombre("Ana");
        dto.setTipoInsumo("M");
        dto.setTelefono("3312345678");
        dto.setCorreo("ana@example.com");
        dto.setActivo(true);
        dto.setCalificacionProveedor(new BigDecimal("88.50"));

        Method method = service.getClass().getMethod("actualizar", Long.class, ProveedorUpdateDTO.class);
        ProveedorResponseDTO respuesta = (ProveedorResponseDTO) method.invoke(service, 6L, dto);

        assertEquals(new BigDecimal("88.50"), proveedor.getCalificacionProveedor());
        assertEquals(new BigDecimal("88.50"), respuesta.getCalificacionProveedor());
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

    private ProveedorResponseDTO invocarActualizarCalificacion(Long id, BigDecimal calificacion) throws Exception {
        Method method = service.getClass().getMethod("actualizarCalificacion", Long.class, BigDecimal.class);
        try {
            return (ProveedorResponseDTO) method.invoke(service, id, calificacion);
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
        private final Map<String, Object> objectResults = new HashMap<>();
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

            if (objectResults.containsKey(method.getName())) {
                return objectResults.get(method.getName());
            }

            if ("save".equals(method.getName())) {
                return args[0];
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
