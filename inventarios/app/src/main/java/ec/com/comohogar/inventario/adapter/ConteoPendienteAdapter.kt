package ec.com.comohogar.inventario.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import ec.com.comohogar.inventario.R
import ec.com.comohogar.inventario.modelo.ConteoPendiente


class ConteoPendienteAdapter(val context: Context, var listItemsTxt: List<ConteoPendiente>?) : BaseAdapter() {

    val mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val vh: ItemRowHolder
        if (convertView == null) {
            view = mInflater.inflate(R.layout.item_conteo_pendiente, parent, false)
            vh = ItemRowHolder(view)
            view?.tag = vh
        } else {
            view = convertView
            vh = view.tag as ItemRowHolder
        }

        val params = view.layoutParams
        params.height = 40
        view.layoutParams = params

        vh.zona?.text = listItemsTxt?.get(position)?.zona.toString()
        vh.barra?.text = listItemsTxt?.get(position)?.barra.toString()
        vh.cantidad?.text = listItemsTxt?.get(position)?.cantidad.toString()

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

        init {
            this.zona = row?.findViewById(R.id.txtZona) as TextView
            this.cantidad = row?.findViewById(R.id.txtCantidad) as TextView
            this.barra = row?.findViewById(R.id.txtBarra) as TextView
        }
    }
}