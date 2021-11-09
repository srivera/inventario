package ec.com.comohogar.inventario.persistencia.dao

import androidx.room.*
import ec.com.comohogar.inventario.persistencia.entities.Conteo

@Dao
interface ConteoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertarConteo(conteo: Conteo)

    @Update
    fun actualizarConteo(conteo: Conteo)

    @Delete
    fun eliminarConteo(conteo: Conteo)

    @Query("SELECT * FROM Conteo WHERE id == :id")
    fun getConteoById(id: Int): List<Conteo>

    @Query("SELECT * FROM Conteo")
    fun getConteos(): List<Conteo>
}