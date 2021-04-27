package ec.com.comohogar.inventario.persistencia.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class ReconteoLocal (
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    var pendiente: kotlin.String,
    var codigoItem: kotlin.String,
    val descripcionItem: String,
    val barra: String,
    val stock: Int,
    var cinId: kotlin.Long,
    val itmId: Long,
    var estado: String,
    var cantidad: Int,
    var fecha: Long?)
