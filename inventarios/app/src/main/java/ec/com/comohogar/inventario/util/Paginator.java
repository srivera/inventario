package ec.com.comohogar.inventario.util;

import java.util.ArrayList;
import java.util.List;

import ec.com.comohogar.inventario.modelo.ReconteoItemSeccion;

public class Paginator {

    public static int TOTAL_NUM_ITEMS = 100;
    public static final int ITEMS_PER_PAGE = 10;

    public Paginator() {
    }

    public int getTotalPages() {
        int remainingItems=TOTAL_NUM_ITEMS % ITEMS_PER_PAGE;
        if(remainingItems>0)
        {
            return TOTAL_NUM_ITEMS / ITEMS_PER_PAGE;
        }
        return (TOTAL_NUM_ITEMS / ITEMS_PER_PAGE)-1;

    }


    public ArrayList<ReconteoItemSeccion> getCurrentConteos(int currentPage, int total, List<ReconteoItemSeccion> conteos) {
        TOTAL_NUM_ITEMS = total;
        int startItem = currentPage * ITEMS_PER_PAGE;
        int lastItem = startItem + ITEMS_PER_PAGE;


        ArrayList<ReconteoItemSeccion> currentConteos = new ArrayList<>();
        try {
            for (int i = 0; i < total; i++) {

                if (i >= startItem && i < lastItem) {
                    currentConteos.add(conteos.get(i));
                }
            }
            return currentConteos;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
