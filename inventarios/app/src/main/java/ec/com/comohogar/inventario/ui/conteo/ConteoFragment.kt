package ec.com.comohogar.inventario.ui.conteo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import ec.com.comohogar.inventario.R
import ec.com.comohogar.inventario.databinding.FragmentConteoBinding

class ConteoFragment : Fragment() {

    private lateinit var conteoViewModel: ConteoViewModel

    private var editZona: EditText? = null
    private var editBarra: EditText? = null
    private var editCantidad: EditText? = null

    private var textBarraAnterior: TextView? = null
    private var textCantidadAnterior: TextView? = null

    private var buttonGuardar: Button? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentConteoBinding

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_conteo, container, false
        )

        binding.setLifecycleOwner(this)

        conteoViewModel = ViewModelProviders.of(this).get(ConteoViewModel::class.java)

        binding.uiController = this


        binding.conteoViewModel = conteoViewModel

        val root = binding.getRoot()

        editZona = root.findViewById(R.id.editZona)
        editBarra = root.findViewById(R.id.editBarra)
        editCantidad = root.findViewById(R.id.editCantidad)

        textBarraAnterior = root.findViewById(R.id.textBarraAnterior)
        textCantidadAnterior = root.findViewById(R.id.textCantidadAnterior)

        buttonGuardar = root.findViewById(R.id.buttonGuardar)

        return root
    }

}