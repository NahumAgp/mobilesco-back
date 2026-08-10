package com.mobilesco.mobilesco_back.modules.ordenproduccion.application.usecases;

import java.math.*;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.dto.common.PageResponseDTO;
import com.mobilesco.mobilesco_back.modules.cliente.domain.models.ClienteModel;
import com.mobilesco.mobilesco_back.modules.cliente.infrastructure.out.persistence.repositories.ClienteRepository;
import com.mobilesco.mobilesco_back.modules.cotizacion.domain.models.*;
import com.mobilesco.mobilesco_back.modules.cotizacion.infrastructure.out.persistence.repositories.CotizacionRepository;
import com.mobilesco.mobilesco_back.modules.insumo.application.usecases.StockMinimoNotificacionService;
import com.mobilesco.mobilesco_back.modules.insumo.domain.models.InsumoModel;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.out.persistence.repositories.InsumoRepository;
import com.mobilesco.mobilesco_back.modules.kardex.application.usecases.KardexService;
import com.mobilesco.mobilesco_back.modules.ordenproduccion.domain.models.*;
import com.mobilesco.mobilesco_back.modules.ordenproduccion.infrastructure.in.api.dtos.*;
import com.mobilesco.mobilesco_back.modules.ordenproduccion.infrastructure.in.api.dtos.OrdenProduccionAccionesDTO.*;
import com.mobilesco.mobilesco_back.modules.ordenproduccion.infrastructure.out.persistence.repositories.*;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.*;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.*;
import com.mobilesco.mobilesco_back.modules.salidainsumo.domain.models.*;
import com.mobilesco.mobilesco_back.modules.salidainsumo.infrastructure.out.persistence.repositories.*;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.*;
import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor
public class OrdenProduccionService {
    private static final BigDecimal CIEN = new BigDecimal("100");
    private final OrdenProduccionRepository ordenRepository;
    private final OrdenProduccionOperacionRepository operacionOrdenRepository;
    private final OrdenProduccionAvanceRepository avanceRepository;
    private final ProductoRepository productoRepository;
    private final ProductoInsumoRepository productoInsumoRepository;
    private final ProductoOperacionRepository productoOperacionRepository;
    private final ClienteRepository clienteRepository;
    private final CotizacionRepository cotizacionRepository;
    private final InsumoRepository insumoRepository;
    private final SalidaInsumoRepository salidaRepository;
    private final DetalleSalidaInsumoRepository detalleSalidaRepository;
    private final KardexService kardexService;
    private final StockMinimoNotificacionService stockMinimoNotificacionService;

    @Transactional(readOnly=true)
    public PageResponseDTO<OrdenProduccionResponseDTO> listar(EstadoOrdenProduccion estado, OrigenOrdenProduccion origen,
            String busqueda, LocalDate desde, LocalDate hasta, Pageable pageable) {
        String texto=limpiar(busqueda);
        Page<OrdenProduccionResponseDTO> page=ordenRepository.buscar(estado, origen, texto, desde, hasta, pageable).map(this::mapear);
        return new PageResponseDTO<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    @Transactional(readOnly=true)
    public OrdenProduccionResponseDTO obtener(Long id){ return mapear(buscar(id)); }

    @Transactional
    public OrdenProduccionResponseDTO crear(OrdenProduccionRequestDTO dto, String usuario){
        validarFechas(dto.getFechaInicioProgramada(), dto.getFechaCompromiso());
        ClienteModel cliente=cliente(dto.getClienteId());
        OrdenProduccionModel orden=OrdenProduccionModel.builder().folio("TEMP-"+UUID.randomUUID())
            .origen(OrigenOrdenProduccion.MANUAL).estado(EstadoOrdenProduccion.BORRADOR).cliente(cliente)
            .fechaInicioProgramada(dto.getFechaInicioProgramada()).fechaCompromiso(dto.getFechaCompromiso())
            .observaciones(limpiar(dto.getObservaciones())).creadoPor(usuario).actualizadoPor(usuario).build();
        reemplazarPartidas(orden,dto.getPartidas());
        ordenRepository.saveAndFlush(orden);
        orden.setFolio("OP-"+LocalDate.now().getYear()+"-"+String.format("%05d",orden.getId()));
        return mapear(ordenRepository.save(orden));
    }

    @Transactional
    public OrdenProduccionResponseDTO actualizar(Long id, OrdenProduccionRequestDTO dto, String usuario){
        OrdenProduccionModel orden=buscar(id);
        exigirBorrador(orden);
        validarFechas(dto.getFechaInicioProgramada(),dto.getFechaCompromiso());
        if(orden.getOrigen()==OrigenOrdenProduccion.MANUAL) orden.setCliente(cliente(dto.getClienteId()));
        orden.setFechaInicioProgramada(dto.getFechaInicioProgramada());
        orden.setFechaCompromiso(dto.getFechaCompromiso()); orden.setObservaciones(limpiar(dto.getObservaciones())); orden.setActualizadoPor(usuario);
        if(orden.getOrigen()==OrigenOrdenProduccion.MANUAL) sincronizarPartidas(orden,dto.getPartidas());
        return mapear(ordenRepository.save(orden));
    }

    @Transactional
    public OrdenProduccionResponseDTO convertirCotizacion(Long cotizacionId, Conversion dto, String usuario){
        CotizacionModel cot=cotizacionRepository.findById(cotizacionId).orElseThrow(()->new ResourceNotFoundException("Cotización no encontrada"));
        if(cot.getEstado()!=EstadoCotizacion.ACEPTADA) throw new ValidationException("Sólo una cotización aceptada puede convertirse en orden de producción");
        if(ordenRepository.existsByCotizacionId(cotizacionId)) throw new ValidationException("La cotización ya tiene una orden de producción");
        validarFechas(dto.getFechaInicioProgramada(),dto.getFechaCompromiso());
        OrdenProduccionModel orden=OrdenProduccionModel.builder().folio("TEMP-"+UUID.randomUUID()).origen(OrigenOrdenProduccion.COTIZACION)
            .estado(EstadoOrdenProduccion.BORRADOR).cotizacion(cot).cliente(cot.getCliente())
            .fechaInicioProgramada(dto.getFechaInicioProgramada()).fechaCompromiso(dto.getFechaCompromiso())
            .observaciones(limpiar(dto.getObservaciones())).creadoPor(usuario).actualizadoPor(usuario).build();
        for(CotizacionDetalleModel item:cot.getDetalles()) agregarPartida(orden,item.getProducto(),BigDecimal.valueOf(item.getCantidad()));
        ordenRepository.saveAndFlush(orden); orden.setFolio("OP-"+LocalDate.now().getYear()+"-"+String.format("%05d",orden.getId()));
        cot.setEstado(EstadoCotizacion.COMPLETADA); cotizacionRepository.save(cot);
        return mapear(ordenRepository.save(orden));
    }

    @Transactional
    public OrdenProduccionResponseDTO liberar(Long id,String usuario){
        OrdenProduccionModel orden=buscar(id); exigirBorrador(orden);
        if(orden.getDetalles().isEmpty()) throw new ValidationException("La orden debe tener al menos un producto");
        Map<Long,OrdenProduccionInsumoModel> consolidados=new LinkedHashMap<>();
        orden.getInsumos().clear();
        for(OrdenProduccionDetalleModel detalle:orden.getDetalles()){
            ProductoModel producto=detalle.getProducto();
            if(!Boolean.TRUE.equals(producto.getActivo())) throw new ValidationException("El producto "+detalle.getSkuSnapshot()+" está inactivo");
            int unidades;
            try { unidades=detalle.getCantidadPlaneada().intValueExact(); } catch(ArithmeticException ex){ throw new ValidationException("La cantidad planeada de productos debe ser un número entero"); }
            List<ProductoInsumoModel> bom=productoInsumoRepository.findByProductoId(producto.getId());
            List<ProductoOperacionModel> ruta=productoOperacionRepository.findByProductoIdOrderByOrdenAsc(producto.getId()).stream().filter(p->Boolean.TRUE.equals(p.getActivo())).toList();
            if(bom.isEmpty()||bom.stream().anyMatch(x->x.getCantidad()==null||x.getCantidad()<=0)) throw new ValidationException("El producto "+producto.getSku()+" no tiene una BOM de insumos completa");
            if(ruta.isEmpty()||ruta.stream().anyMatch(x->x.getCantidad()==null||x.getCantidad()<=0)) throw new ValidationException("El producto "+producto.getSku()+" no tiene una ruta de operaciones completa");
            for(ProductoInsumoModel pi:bom){
                InsumoModel ins=pi.getInsumo();
                BigDecimal cantidad=bd(pi.getCantidad()).multiply(BigDecimal.valueOf(unidades))
                    .multiply(BigDecimal.ONE.add(bd(pi.getDesperdicioPorcentaje()).divide(CIEN,8,RoundingMode.HALF_UP))).setScale(4,RoundingMode.HALF_UP);
                consolidados.compute(ins.getId(),(key,actual)->{
                    if(actual==null) return OrdenProduccionInsumoModel.builder().orden(orden).insumo(ins).codigoSnapshot(ins.getCodigo())
                        .nombreSnapshot(ins.getNombre()).unidadSnapshot(ins.getUnidadMedida().getSimbolo()).cantidadRequerida(cantidad)
                        .cantidadSurtida(BigDecimal.ZERO).cantidadApartada(BigDecimal.ZERO).build();
                    actual.setCantidadRequerida(actual.getCantidadRequerida().add(cantidad)); return actual;
                });
            }
            detalle.getOperaciones().clear();
            for(ProductoOperacionModel po:ruta){
                int repeticiones=Math.multiplyExact(po.getCantidad(),unidades);
                var op=po.getOperacion();
                detalle.getOperaciones().add(OrdenProduccionOperacionModel.builder().detalle(detalle).operacion(op)
                    .codigoSnapshot(op.getCodigo()).nombreSnapshot(op.getNombre()).centroTrabajoSnapshot(op.getCentroTrabajo().getNombre())
                    .secuencia(po.getOrden()).repeticionesPlaneadas(repeticiones)
                    .tiempoPlaneado(bd(op.getTiempoOperacion()).multiply(BigDecimal.valueOf(repeticiones)).setScale(3,RoundingMode.HALF_UP))
                    .estado(EstadoOperacionProduccion.PENDIENTE).build());
            }
        }
        orden.getInsumos().addAll(consolidados.values());
        apartarDisponibles(orden);
        orden.setEstado(EstadoOrdenProduccion.LIBERADA); orden.setActualizadoPor(usuario);
        return mapear(ordenRepository.save(orden));
    }

    @Transactional
    public OrdenProduccionResponseDTO surtir(Long id,Surtido dto,String usuario){
        OrdenProduccionModel orden=buscar(id); exigirActiva(orden);
        if(orden.getEstado()==EstadoOrdenProduccion.BORRADOR) throw new ValidationException("La orden debe estar liberada antes de surtir materiales");
        Set<Long> ids=new HashSet<>();
        for(Insumo item:dto.getInsumos()) if(!ids.add(item.getInsumoId())) throw new ValidationException("No repitas el mismo insumo en el surtido");
        Map<Long,OrdenProduccionInsumoModel> necesidades=orden.getInsumos().stream().collect(Collectors.toMap(x->x.getInsumo().getId(),Function.identity()));
        SalidaInsumoModel salida=SalidaInsumoModel.builder().tipoSalida("PRODUCCION").ordenProduccion(orden.getFolio()).ordenProduccionModel(orden)
            .fechaSalida(LocalDateTime.now()).observaciones(limpiar(dto.getObservaciones())).cantidadTotal(0d).activo(true)
            .usuario(usuario).responsable(usuario).area("PRODUCCION").detalles(new ArrayList<>()).build();
        salidaRepository.save(salida); double total=0;
        for(Insumo item:dto.getInsumos()){
            OrdenProduccionInsumoModel necesidad=necesidades.get(item.getInsumoId());
            if(necesidad==null) throw new ValidationException("El insumo no pertenece a la orden");
            BigDecimal pendiente=necesidad.getCantidadRequerida().subtract(necesidad.getCantidadSurtida());
            if(item.getCantidad().compareTo(pendiente)>0) throw new ValidationException("La cantidad de "+necesidad.getNombreSnapshot()+" supera lo pendiente");
            InsumoModel ins=insumoRepository.findByIdForUpdate(item.getInsumoId()).orElseThrow(()->new ResourceNotFoundException("Insumo no encontrado"));
            double cantidad=item.getCantidad().doubleValue(), anterior=Optional.ofNullable(ins.getStockActual()).orElse(0d);
            BigDecimal apartadoGlobal=bd(ins.getStockApartado()), apartadoOrden=Optional.ofNullable(necesidad.getCantidadApartada()).orElse(BigDecimal.ZERO);
            BigDecimal apartadoOtras=apartadoGlobal.subtract(apartadoOrden).max(BigDecimal.ZERO);
            BigDecimal disponibleOrden=bd(anterior).subtract(apartadoOtras).max(BigDecimal.ZERO);
            if(disponibleOrden.add(new BigDecimal("0.0000001")).compareTo(item.getCantidad())<0)
                throw new ValidationException("Existencia disponible insuficiente para "+ins.getNombre()+". Disponible para esta orden: "+disponibleOrden+" "+ins.getUnidadMedida().getSimbolo());
            BigDecimal consumidoApartado=apartadoOrden.min(item.getCantidad());
            necesidad.setCantidadApartada(apartadoOrden.subtract(consumidoApartado).setScale(4,RoundingMode.HALF_UP));
            ins.setStockApartado(apartadoGlobal.subtract(consumidoApartado).max(BigDecimal.ZERO).doubleValue());
            double nuevo=anterior-cantidad; Double costo=Optional.ofNullable(kardexService.calcularCostoPromedio(ins.getId())).orElse(0d);
            ins.setStockActual(nuevo); insumoRepository.save(ins); stockMinimoNotificacionService.notificarSiCruzaMinimo(ins,anterior,nuevo);
            var detalle=DetalleSalidaInsumoModel.builder().salidaInsumo(salida).insumo(ins).cantidad(cantidad).stockAnterior(anterior).stockNuevo(nuevo)
                .costoUnitario(costo).costoTotal(cantidad*costo).observaciones(limpiar(dto.getObservaciones())).build();
            detalleSalidaRepository.save(detalle); salida.getDetalles().add(detalle);
            necesidad.setCantidadSurtida(necesidad.getCantidadSurtida().add(item.getCantidad()).setScale(4,RoundingMode.HALF_UP));
            kardexService.registrarSalidaProduccion(ins.getId(),cantidad,costo,orden.getId(),orden.getFolio(),dto.getObservaciones(),anterior,nuevo);
            total+=cantidad;
        }
        salida.setCantidadTotal(total); salidaRepository.save(salida); activar(orden,usuario);
        return mapear(ordenRepository.save(orden));
    }

    @Transactional
    public OrdenProduccionResponseDTO cambiarOperacion(Long ordenId,Long operacionId,CambioOperacion dto,String usuario){
        OrdenProduccionModel orden=buscar(ordenId); exigirActiva(orden);
        if(orden.getEstado()==EstadoOrdenProduccion.BORRADOR) throw new ValidationException("La orden debe estar liberada");
        OrdenProduccionOperacionModel item=operacionOrdenRepository.findById(operacionId).orElseThrow(()->new ResourceNotFoundException("Operación de producción no encontrada"));
        if(!item.getDetalle().getOrden().getId().equals(ordenId)) throw new ValidationException("La operación no pertenece a la orden");
        EstadoOperacionProduccion nuevo;
        try{ nuevo=EstadoOperacionProduccion.valueOf(dto.getEstado().toUpperCase(Locale.ROOT)); }catch(Exception ex){throw new ValidationException("Estado de operación inválido");}
        if(nuevo==EstadoOperacionProduccion.EN_PROCESO){
            if(item.getEstado()!=EstadoOperacionProduccion.PENDIENTE) throw new ValidationException("Sólo una operación pendiente puede iniciarse");
            boolean previasPendientes=item.getDetalle().getOperaciones().stream().anyMatch(x->x.getSecuencia()<item.getSecuencia()&&x.getEstado()!=EstadoOperacionProduccion.TERMINADA);
            if(previasPendientes) throw new ValidationException("Termina las operaciones anteriores antes de iniciar ésta");
            item.setFechaInicio(LocalDateTime.now()); activar(orden,usuario);
        } else if(nuevo==EstadoOperacionProduccion.TERMINADA){
            if(item.getEstado()!=EstadoOperacionProduccion.EN_PROCESO) throw new ValidationException("La operación debe estar en proceso antes de terminarse");
            item.setFechaFin(LocalDateTime.now());
        } else throw new ValidationException("No se puede regresar una operación a pendiente");
        item.setEstado(nuevo); operacionOrdenRepository.save(item); evaluarTerminacion(orden,usuario);
        return mapear(ordenRepository.save(orden));
    }

    @Transactional
    public OrdenProduccionResponseDTO registrarAvance(Long ordenId,Long partidaId,Avance dto,String usuario){
        OrdenProduccionModel orden=buscar(ordenId); exigirActiva(orden);
        if(orden.getEstado()==EstadoOrdenProduccion.BORRADOR) throw new ValidationException("La orden debe estar liberada");
        OrdenProduccionDetalleModel partida=orden.getDetalles().stream().filter(x->x.getId().equals(partidaId)).findFirst().orElseThrow(()->new ResourceNotFoundException("Partida no encontrada"));
        BigDecimal nuevo=partida.getCantidadTerminada().add(dto.getCantidad());
        if(nuevo.compareTo(partida.getCantidadPlaneada())>0) throw new ValidationException("El avance supera la cantidad planeada");
        partida.setCantidadTerminada(nuevo); var avance=OrdenProduccionAvanceModel.builder().detalle(partida).cantidad(dto.getCantidad())
            .observaciones(limpiar(dto.getObservaciones())).usuario(usuario).build();
        partida.getAvances().add(avance); avanceRepository.save(avance); activar(orden,usuario); evaluarTerminacion(orden,usuario);
        return mapear(ordenRepository.save(orden));
    }

    @Transactional
    public OrdenProduccionResponseDTO cancelar(Long id,Cancelacion dto,String usuario){
        OrdenProduccionModel orden=buscar(id); exigirActiva(orden); liberarApartados(orden); orden.setEstado(EstadoOrdenProduccion.CANCELADA);
        orden.setMotivoCancelacion(dto.getMotivo().trim()); orden.setActualizadoPor(usuario); return mapear(ordenRepository.save(orden));
    }

    private void reemplazarPartidas(OrdenProduccionModel orden,List<OrdenProduccionRequestDTO.Partida> partidas){
        Set<Long> ids=new HashSet<>();
        for(var item:partidas){ if(!ids.add(item.getProductoId())) throw new ValidationException("No se puede repetir el mismo producto");
            ProductoModel p=productoRepository.findById(item.getProductoId()).orElseThrow(()->new ResourceNotFoundException("Producto no encontrado: "+item.getProductoId()));
            if(!Boolean.TRUE.equals(p.getActivo())) throw new ValidationException("El producto "+p.getSku()+" está inactivo"); agregarPartida(orden,p,item.getCantidad()); }
    }
    private void sincronizarPartidas(OrdenProduccionModel orden,List<OrdenProduccionRequestDTO.Partida> partidas){
        Map<Long,OrdenProduccionDetalleModel> existentes=orden.getDetalles().stream()
            .collect(Collectors.toMap(x->x.getProducto().getId(),Function.identity()));
        Set<Long> ids=new HashSet<>(); List<OrdenProduccionDetalleModel> nuevas=new ArrayList<>();
        for(var item:partidas){
            if(!ids.add(item.getProductoId())) throw new ValidationException("No se puede repetir el mismo producto");
            ProductoModel p=productoRepository.findById(item.getProductoId()).orElseThrow(()->new ResourceNotFoundException("Producto no encontrado: "+item.getProductoId()));
            if(!Boolean.TRUE.equals(p.getActivo())) throw new ValidationException("El producto "+p.getSku()+" está inactivo");
            if(item.getCantidad()==null||item.getCantidad().signum()<=0||item.getCantidad().stripTrailingZeros().scale()>0) throw new ValidationException("La cantidad planeada debe ser un número entero mayor a cero");
            OrdenProduccionDetalleModel detalle=existentes.get(item.getProductoId());
            if(detalle==null) detalle=OrdenProduccionDetalleModel.builder().orden(orden).producto(p).cantidadTerminada(BigDecimal.ZERO.setScale(3)).build();
            detalle.setSkuSnapshot(p.getSku()); detalle.setNombreSnapshot(p.getNombre()); detalle.setCantidadPlaneada(item.getCantidad().setScale(3)); nuevas.add(detalle);
        }
        orden.getDetalles().clear(); orden.getDetalles().addAll(nuevas);
    }
    private void agregarPartida(OrdenProduccionModel orden,ProductoModel p,BigDecimal cantidad){
        if(cantidad==null||cantidad.signum()<=0||cantidad.stripTrailingZeros().scale()>0) throw new ValidationException("La cantidad planeada debe ser un número entero mayor a cero");
        orden.getDetalles().add(OrdenProduccionDetalleModel.builder().orden(orden).producto(p).skuSnapshot(p.getSku()).nombreSnapshot(p.getNombre())
            .cantidadPlaneada(cantidad.setScale(3)).cantidadTerminada(BigDecimal.ZERO.setScale(3)).build());
    }
    private ClienteModel cliente(Long id){ if(id==null)return null; ClienteModel c=clienteRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Cliente no encontrado")); if(!Boolean.TRUE.equals(c.getActivo()))throw new ValidationException("El cliente está inactivo"); return c; }
    private OrdenProduccionModel buscar(Long id){return ordenRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Orden de producción no encontrada"));}
    private void exigirBorrador(OrdenProduccionModel o){if(o.getEstado()!=EstadoOrdenProduccion.BORRADOR)throw new ValidationException("Sólo se puede modificar una orden en borrador");}
    private void exigirActiva(OrdenProduccionModel o){if(o.getEstado()==EstadoOrdenProduccion.CANCELADA||o.getEstado()==EstadoOrdenProduccion.TERMINADA)throw new ValidationException("La orden ya no admite cambios");}
    private void activar(OrdenProduccionModel o,String usuario){if(o.getEstado()==EstadoOrdenProduccion.LIBERADA)o.setEstado(EstadoOrdenProduccion.EN_PROCESO);o.setActualizadoPor(usuario);}
    private void evaluarTerminacion(OrdenProduccionModel o,String usuario){
        boolean cantidades=o.getDetalles().stream().allMatch(x->x.getCantidadTerminada().compareTo(x.getCantidadPlaneada())>=0);
        boolean operaciones=o.getDetalles().stream().flatMap(x->x.getOperaciones().stream()).allMatch(x->x.getEstado()==EstadoOperacionProduccion.TERMINADA);
        if(cantidades&&operaciones){liberarApartados(o);o.setEstado(EstadoOrdenProduccion.TERMINADA);o.setActualizadoPor(usuario);}
    }
    private void apartarDisponibles(OrdenProduccionModel orden){
        orden.getInsumos().stream().sorted(Comparator.comparing(x->x.getInsumo().getId())).forEach(necesidad->{
            InsumoModel insumo=insumoRepository.findByIdForUpdate(necesidad.getInsumo().getId())
                .orElseThrow(()->new ResourceNotFoundException("Insumo no encontrado"));
            BigDecimal existencia=bd(insumo.getStockActual()), apartadoGlobal=bd(insumo.getStockApartado());
            BigDecimal disponible=existencia.subtract(apartadoGlobal).max(BigDecimal.ZERO);
            BigDecimal pendiente=necesidad.getCantidadRequerida().subtract(necesidad.getCantidadSurtida());
            BigDecimal apartado=disponible.min(pendiente).setScale(4,RoundingMode.HALF_UP);
            necesidad.setInsumo(insumo); necesidad.setCantidadApartada(apartado);
            insumo.setStockApartado(apartadoGlobal.add(apartado).setScale(4,RoundingMode.HALF_UP).doubleValue());
            insumoRepository.save(insumo);
        });
    }
    private void liberarApartados(OrdenProduccionModel orden){
        orden.getInsumos().stream().filter(x->Optional.ofNullable(x.getCantidadApartada()).orElse(BigDecimal.ZERO).signum()>0)
            .sorted(Comparator.comparing(x->x.getInsumo().getId())).forEach(necesidad->{
                InsumoModel insumo=insumoRepository.findByIdForUpdate(necesidad.getInsumo().getId())
                    .orElseThrow(()->new ResourceNotFoundException("Insumo no encontrado"));
                BigDecimal apartado=necesidad.getCantidadApartada();
                insumo.setStockApartado(bd(insumo.getStockApartado()).subtract(apartado).max(BigDecimal.ZERO).doubleValue());
                necesidad.setCantidadApartada(BigDecimal.ZERO.setScale(4)); insumoRepository.save(insumo);
            });
    }
    private void validarFechas(LocalDate inicio,LocalDate compromiso){if(inicio!=null&&compromiso!=null&&compromiso.isBefore(inicio))throw new ValidationException("La fecha compromiso no puede ser anterior al inicio programado");}
    private BigDecimal bd(Double v){return BigDecimal.valueOf(v==null?0:v);}
    private String limpiar(String s){return s==null||s.isBlank()?null:s.trim();}
    private String nombreCliente(ClienteModel c){if(c==null)return null;if(c.getNombreComercial()!=null&&!c.getNombreComercial().isBlank())return c.getNombreComercial();if(c.getRazonSocial()!=null&&!c.getRazonSocial().isBlank())return c.getRazonSocial();return c.getNombre();}
    private OrdenProduccionResponseDTO mapear(OrdenProduccionModel o){
        var partidas=o.getDetalles().stream().map(d->OrdenProduccionResponseDTO.Partida.builder().id(d.getId()).productoId(d.getProducto().getId()).sku(d.getSkuSnapshot()).nombre(d.getNombreSnapshot())
            .cantidadPlaneada(d.getCantidadPlaneada()).cantidadTerminada(d.getCantidadTerminada()).porcentajeAvance(porcentaje(d.getCantidadTerminada(),d.getCantidadPlaneada())).build()).toList();
        var insumos=o.getInsumos().stream().map(i->{BigDecimal existencia=bd(i.getInsumo().getStockActual()),pendiente=i.getCantidadRequerida().subtract(i.getCantidadSurtida());
            BigDecimal apartado=Optional.ofNullable(i.getCantidadApartada()).orElse(BigDecimal.ZERO), apartadoGlobal=bd(i.getInsumo().getStockApartado());
            BigDecimal disponibleGeneral=existencia.subtract(apartadoGlobal).max(BigDecimal.ZERO), disponibleOrden=disponibleGeneral.add(apartado).min(existencia);
            return OrdenProduccionResponseDTO.Insumo.builder()
            .id(i.getId()).insumoId(i.getInsumo().getId()).codigo(i.getCodigoSnapshot()).nombre(i.getNombreSnapshot()).unidad(i.getUnidadSnapshot()).requerido(i.getCantidadRequerida()).surtido(i.getCantidadSurtida())
            .pendiente(pendiente).apartado(apartado).porApartar(pendiente.subtract(apartado).max(BigDecimal.ZERO)).existencia(existencia)
            .disponibleGeneral(disponibleGeneral).disponibleParaOrden(disponibleOrden).faltante(apartado.compareTo(pendiente)<0).build();}).toList();
        var ops=o.getDetalles().stream().flatMap(d->d.getOperaciones().stream().map(x->OrdenProduccionResponseDTO.Operacion.builder().id(x.getId()).partidaId(d.getId()).producto(d.getNombreSnapshot())
            .operacionId(x.getOperacion().getId()).codigo(x.getCodigoSnapshot()).nombre(x.getNombreSnapshot()).centroTrabajo(x.getCentroTrabajoSnapshot()).secuencia(x.getSecuencia())
            .repeticionesPlaneadas(x.getRepeticionesPlaneadas()).tiempoPlaneado(x.getTiempoPlaneado()).estado(x.getEstado().name()).fechaInicio(x.getFechaInicio()).fechaFin(x.getFechaFin()).build())).toList();
        var avances=o.getDetalles().stream().flatMap(d->d.getAvances().stream().map(a->OrdenProduccionResponseDTO.Avance.builder().id(a.getId()).partidaId(d.getId()).producto(d.getNombreSnapshot())
            .cantidad(a.getCantidad()).observaciones(a.getObservaciones()).usuario(a.getUsuario()).fechaRegistro(a.getFechaRegistro()).build())).sorted(Comparator.comparing(OrdenProduccionResponseDTO.Avance::getFechaRegistro).reversed()).toList();
        BigDecimal planeado=o.getDetalles().stream().map(OrdenProduccionDetalleModel::getCantidadPlaneada).reduce(BigDecimal.ZERO,BigDecimal::add), terminado=o.getDetalles().stream().map(OrdenProduccionDetalleModel::getCantidadTerminada).reduce(BigDecimal.ZERO,BigDecimal::add);
        return OrdenProduccionResponseDTO.builder().id(o.getId()).folio(o.getFolio()).origen(o.getOrigen().name()).estado(o.getEstado().name()).cotizacionId(o.getCotizacion()==null?null:o.getCotizacion().getId())
            .cotizacionFolio(o.getCotizacion()==null?null:o.getCotizacion().getFolio()).clienteId(o.getCliente()==null?null:o.getCliente().getId()).clienteNombre(nombreCliente(o.getCliente()))
            .fechaInicioProgramada(o.getFechaInicioProgramada()).fechaCompromiso(o.getFechaCompromiso()).observaciones(o.getObservaciones()).motivoCancelacion(o.getMotivoCancelacion())
            .creadoPor(o.getCreadoPor()).actualizadoPor(o.getActualizadoPor()).fechaRegistro(o.getFechaRegistro()).fechaActualizacion(o.getFechaActualizacion()).porcentajeAvance(porcentaje(terminado,planeado))
            .tieneFaltantes(insumos.stream().anyMatch(OrdenProduccionResponseDTO.Insumo::isFaltante)).partidas(partidas).insumos(insumos).operaciones(ops).avances(avances).build();
    }
    private BigDecimal porcentaje(BigDecimal valor,BigDecimal total){return total==null||total.signum()==0?BigDecimal.ZERO:valor.multiply(CIEN).divide(total,2,RoundingMode.HALF_UP);}
}
