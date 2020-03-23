package ec.com.comohogar.inventario;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;

import ec.com.comohogar.inventario.modelo.Conteo;
import ec.com.comohogar.inventario.modelo.Empleado;
import ec.com.comohogar.inventario.modelo.Inventario;
import ec.com.comohogar.inventario.modelo.Lugar;
import ec.com.comohogar.inventario.persistencia.entities.ReconteoBodega;

public class SesionAplicacion extends Application {

    private List<ReconteoBodega> listaReconteoBodega = new ArrayList<>();

    private List<Lugar> listaLugares = new ArrayList<>();

    private Inventario inventario;

    private Conteo conteo;

    private Empleado empleado;

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
}
