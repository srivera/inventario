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

    @Query("SELECT * FROM Conteo WHERE estado == 'PEN'")
    fun getConteoPendiente(): List<Conteo>

    @Query("SELECT count(*) FROM Conteo")
    fun count(): Int

    @Query("SELECT count(*) FROM Conteo WHERE estado = 'PEN'")
    fun countPendiente(): Int

    @Query("SELECT count(*) FROM Conteo WHERE estado = 'ENV'")
    fun countEnviado(): Int
}