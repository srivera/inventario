package ec.com.comohogar.inventario.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import ec.com.comohogar.inventario.R
import ec.com.comohogar.inventario.modelo.ConteoPocketError


class ConteoErrorAdapter(val context: Context, var listItemsTxt: List<ConteoPocketError>?) : BaseAdapter() {

    val mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val vh: ItemRowHolder
        if (convertView == null) {
            view = mInflater.inflate(R.layout.item_conteo_error, parent, false)
            vh = ItemRowHolder(view)
            view?.tag = vh
        } else {
            view = convertView
            vh = view.tag as ItemRowHolder
        }

        val params = view.layoutParams
        params.height = 70
        view.layoutParams = params

        vh.zona?.text = listItemsTxt?.get(position)?.pocZona.toString()
        vh.barra?.text = listItemsTxt?.get(position)?.pocBarra.toString()
        vh.cantidad?.text = listItemsTxt?.get(position)?.pocCantidad.toString()
        vh.error?.text = listItemsTxt?.get(position)?.pocObservacion.toString()

        if(listItemsTxt?.get(position)?.corregido.equals("S")){
            vh.layoutFila?.setBackgroundColor(Color.parseColor("#c6f6e3"))
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

        var zona: TextView? = null
        var barra: TextView? = null
        var cantidad: TextView? = null
        var error: TextView? = null
        var layoutFila: LinearLayout? = null

        init {
            this.zona = row?.findViewById(R.id.txtZona) as TextView
            this.cantidad = row?.findViewById(R.id.txtCantidad) as TextView
            this.barra = row?.findViewById(R.id.txtBarra) as TextView
            this.error = row?.findViewById(R.id.txtError) as TextView
            this.layoutFila = row?.findViewById(R.id.layoutFila) as LinearLayout
        }
    }
}