package ec.com.comohogar.inventario.persistencia.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class ReconteoBodega (
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val codigoItem: String,
    var descripcionItem: String,
    var barra: String,
    var referenciaItem: String,
    var cinId: Long,
    var itmId: Long,
    var usuIdAsignado: Int,
    var rcoUbicacion: String,
    var uinSecuencial: Int,
    var rcoId: Long)
