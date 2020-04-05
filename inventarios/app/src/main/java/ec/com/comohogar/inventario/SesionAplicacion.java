package ec.com.comohogar.inventario;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;

import ec.com.comohogar.inventario.modelo.Conteo;
import ec.com.comohogar.inventario.modelo.Empleado;
import ec.com.comohogar.inventario.modelo.Inventario;
import ec.com.comohogar.inventario.modelo.Lugar;
import ec.com.comohogar.inventario.persistencia.entities.ReconteoBodega;
import ec.com.comohogar.inventario.persistencia.entities.ReconteoLocal;

public class SesionAplicacion extends Application {

    private List<ReconteoBodega> listaReconteoBodega = new ArrayList<>();

    private List<ReconteoLocal> listaReconteoLocal= new ArrayList<>();

    private List<Lugar> listaLugares = new ArrayList<>();

    private Inventario inventario;

    private Conteo conteo;

    private Empleado empleado;

    private Long binId;

    private Long cinId;

    private Long usuId;

    private Integer numConteo;

    private Integer tipoInventario;

    private String tipo;

    private Boolean primeraVez = true;

    public List<ReconteoBodega> getListaReconteoBodega() {
        return listaReconteoBodega;
    }

    public void setListaReconteoBodega(List<ReconteoBodega> listaReconteoBodega) {
        this.listaReconteoBodega = listaReconteoBodega;
    }

    public List<Lugar> getListaLugares() {
        return listaLugares;
    }

    public void setListaLugares(List<Lugar> listaLugares) {
        this.listaLugares = listaLugares;
    }

    public Inventario getInventario() {
        return inventario;
    }

    public void setInventario(Inventario inventario) {
        this.inventario = inventario;
    }

    public Conteo getConteo() {
        return conteo;
    }

    public void setConteo(Conteo conteo) {
        this.conteo = conteo;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    public Long getBinId() {
        return binId;
    }

    public void setBinId(Long binId) {
        this.binId = binId;
    }

    public Long getCinId() {
        return cinId;
    }

    public void setCinId(Long cinId) {
        this.cinId = cinId;
    }

    public Long getUsuId() {
        return usuId;
    }

    public void setUsuId(Long usuId) {
        this.usuId = usuId;
    }

    public Integer getNumConteo() {
        return numConteo;
    }

    public void setNumConteo(Integer numConteo) {
        this.numConteo = numConteo;
    }

    public Integer getTipoInventario() {
        return tipoInventario;
    }

    public void setTipoInventario(Integer tipoInventario) {
        this.tipoInventario = tipoInventario;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public List<ReconteoLocal> getListaReconteoLocal() {
        return listaReconteoLocal;
    }

    public void setListaReconteoLocal(List<ReconteoLocal> listaReconteoLocal) {
        this.listaReconteoLocal = listaReconteoLocal;
    }

    public Boolean getPrimeraVez() {
        return primeraVez;
    }

    public void setPrimeraVez(Boolean primeraVez) {
        this.primeraVez = primeraVez;
    }
}
