package ec.com.comohogar.inventario;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;

import ec.com.comohogar.inventario.persistencia.entities.ReconteoBodega;

public class SesionAplicacion extends Application {

    private List<ReconteoBodega> listaReconteoBodega = new ArrayList<>();

    public List<ReconteoBodega> getListaReconteoBodega() {
        return listaReconteoBodega;
    }

    public void setListaReconteoBodega(List<ReconteoBodega> listaReconteoBodega) {
        this.listaReconteoBodega = listaReconteoBodega;
    }
}
