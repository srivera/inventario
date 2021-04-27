package ec.com.comohogar.inventario.persistencia.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Conteo (
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    var zona: String,
    var barra: String,
    var cantidad: Int,
    var estado: String,
    var cinId: Long?,
    var binId: Long?,
    var numConteo: Int?,
    var usuId: Long?,
    var fecha: Long?)
