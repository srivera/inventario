package ec.com.comohogar.inventario.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import ec.com.comohogar.inventario.R
import ec.com.comohogar.inventario.modelo.ReconteoAsignado
import java.sql.Timestamp
import java.text.DecimalFormat
import java.util.*


class ReconteoUsuarioAdapter(val context: Context, var listItemsTxt: List<ReconteoAsignado>?) : BaseAdapter() {

    val mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val vh: ItemRowHolder
        if (convertView == null) {
            view = mInflater.inflate(R.layout.item_reconteo_asignado, parent, false)
            vh = ItemRowHolder(view)
            view?.tag = vh
        } else {
            view = convertView
            vh = view.tag as ItemRowHolder
        }

        val params = view.layoutParams
        params.height = 190
        view.layoutParams = params

        vh.usuario?.text = listItemsTxt?.get(position)?.codigo.toString() + " - " + listItemsTxt?.get(position)?.nombre.toString() + " " + listItemsTxt?.get(position)?.apellido.toString()
        vh.unidades?.text = listItemsTxt?.get(position)?.unidades.toString()
        vh.numItems?.text = listItemsTxt?.get(position)?.totalContados.toString()
        vh.avance?.text = listItemsTxt?.get(position)?.porcentajeAvance.toString() + "%"

        val tiempoInicial = Timestamp(listItemsTxt?.get(position)?.fechaMin!!)
        val tiempoFinal = Timestamp(listItemsTxt?.get(position)?.fechaMax!!)
        val diff = (listItemsTxt?.get(position)?.fechaMax!!).minus(listItemsTxt?.get(position)?.fechaMin!!)
        val df = DecimalFormat("###,###,###.##");
        val diffHours = diff / (60 * 60 * 1000);

        vh.monto?.text = df.format(listItemsTxt?.get(position)?.costoContado).toString()
        vh.tiempo?.text = df.format(diffHours).toString()

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

        var monto: TextView? = null
        var usuario: TextView? = null
        var numItems: TextView? = null
        var unidades: TextView? = null
        var avance: TextView? = null
        var tiempo: TextView? = null
        var layoutFila: LinearLayout? = null

        init {
            this.tiempo = row?.findViewById(R.id.txtTiempo) as TextView
            this.monto = row?.findViewById(R.id.txtMonto) as TextView
            this.usuario = row?.findViewById(R.id.txtUsuario) as TextView
            this.numItems = row?.findViewById(R.id.txtNumItems) as TextView
            this.unidades = row?.findViewById(R.id.txtUnidades) as TextView
            this.avance = row?.findViewById(R.id.txtAvance) as TextView
            this.layoutFila = row?.findViewById(R.id.layoutFila) as LinearLayout
        }
    }
}