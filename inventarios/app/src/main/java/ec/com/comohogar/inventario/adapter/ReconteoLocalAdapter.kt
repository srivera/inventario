package ec.com.comohogar.inventario.adapter

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import ec.com.comohogar.inventario.R
import ec.com.comohogar.inventario.persistencia.entities.ReconteoLocal
import ec.com.comohogar.inventario.util.Constantes


class ReconteoLocalAdapter(val context: Context, var listItemsTxt: List<ReconteoLocal>?) : BaseAdapter() {


    val mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val vh: ItemRowHolder
        if (convertView == null) {
            view = mInflater.inflate(R.layout.item_reconteo_local, parent, false)
            vh = ItemRowHolder(view)
            view?.tag = vh
        } else {
            view = convertView
            vh = view.tag as ItemRowHolder
        }

        val params = view.layoutParams
        params.height = 36
        view.layoutParams = params

        vh.codigo?.text = listItemsTxt?.get(position)?.codigoItem.toString()
        vh.barra?.text = listItemsTxt?.get(position)?.barra.toString()
        vh.descripcion?.text = listItemsTxt?.get(position)?.descripcionItem?.substring(0, 10)
        vh.stock?.text = listItemsTxt?.get(position)?.stock.toString()

        if(!listItemsTxt?.get(position)?.estado.equals(Constantes.ESTADO_INSERTADO)){
            vh.layoutFila?.setBackgroundColor(Color.parseColor("#c6f6e3"))
            Log.i("item", vh.codigo?.text.toString() + listItemsTxt?.get(position)?.estado)
        }else{
            vh.layoutFila?.setBackgroundColor(Color.parseColor("#ffffff"))
        }

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
        var stock: TextView? = null
        var barra: TextView? = null
        var layoutFila: LinearLayout? = null

        init {
            this.descripcion = row?.findViewById(R.id.txtDescripcion) as TextView
            this.codigo = row?.findViewById(R.id.txtItem) as TextView
            this.stock = row?.findViewById(R.id.txtStock) as TextView
            this.barra = row?.findViewById(R.id.txtBarra) as TextView
            this.layoutFila = row?.findViewById(R.id.layoutFila) as LinearLayout
        }
    }
}