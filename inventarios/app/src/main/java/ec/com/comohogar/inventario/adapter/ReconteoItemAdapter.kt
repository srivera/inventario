package ec.com.comohogar.inventario.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import ec.com.comohogar.inventario.R
import ec.com.comohogar.inventario.modelo.ReconteoItemSeccion


class ReconteoItemAdapter(val context: Context, var listItemsTxt: List<ReconteoItemSeccion>?) : BaseAdapter() {

    val mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val vh: ItemRowHolder
        if (convertView == null) {
            view = mInflater.inflate(R.layout.item_reconteo_usuario, parent, false)
            vh = ItemRowHolder(view)
            view?.tag = vh
        } else {
            view = convertView
            vh = view.tag as ItemRowHolder
        }

        val params = view.layoutParams
        params.height = 80
        view.layoutParams = params

        vh.codigo?.text = listItemsTxt?.get(position)?.codigoI.toString()
        vh.descripcion?.text = listItemsTxt?.get(position)?.descripcion.toString()
        vh.cantidad?.text = listItemsTxt?.get(position)?.cantidad.toString()
        vh.ubicacion?.text = listItemsTxt?.get(position)?.ubicacion.toString()
        vh.kardex?.text = listItemsTxt?.get(position)?.stock.toString()

        //if(listItemsTxt?.get(position)?.corregido.equals("S")){
        //    vh.layoutFila?.setBackgroundColor(Color.parseColor("#c6f6e3"))
        //}else{
        //    vh.layoutFila?.setBackgroundColor(Color.parseColor("#ffffff"))
        //}
        return view
    }

    override fun getItem(position: Int): Any? {

        return null

    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return listItemsTxt?.size!!
    }

    private class ItemRowHolder(row: View?) {

        var codigo: TextView? = null
        var descripcion: TextView? = null
        var cantidad: TextView? = null
        var ubicacion: TextView? = null
        var kardex: TextView? = null
        var layoutFila: LinearLayout? = null

        init {
            this.codigo = row?.findViewById(R.id.txtCodigo) as TextView
            this.descripcion = row?.findViewById(R.id.txtDescripcion) as TextView
            this.cantidad = row?.findViewById(R.id.txtCantidad) as TextView
            this.ubicacion = row?.findViewById(R.id.txtUbicacion) as TextView
            this.kardex = row?.findViewById(R.id.txtKardex) as TextView
            this.layoutFila = row?.findViewById(R.id.layoutFila) as LinearLayout
        }
    }
}