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

    @Query("DELETE FROM Conteo")
    fun eliminar()

    @Query("SELECT * FROM Conteo WHERE id = :id")
    fun getConteoById(id: Int): List<Conteo>

    @Query("SELECT * FROM Conteo")
    fun getConteos(): List<Conteo>

    @Query("SELECT * FROM Conteo WHERE estado = 'PEN'")
    fun getConteoPendiente(): List<Conteo>

    @Query("SELECT * FROM Conteo WHERE estado = 'PCO'")
    fun getConteoPendienteCorreccion(): List<Conteo>

    @Query("SELECT count(*) FROM Conteo")
    fun count(): Int

    @Query("SELECT * FROM Conteo WHERE estado = 'PEN' or estado = 'PCO'")
    fun getConteoPendienteTodos(): List<Conteo>

    @Query("SELECT count(*) FROM Conteo WHERE estado = 'PEN'")
    fun countPendiente(): Int

    @Query("SELECT count(*) FROM Conteo WHERE estado = 'ENV'")
    fun countEnviado(): Int

    @Query("SELECT * FROM Conteo WHERE (estado = 'PEN' or estado = 'PCO') and barra = :barra")
    fun getConteoPendienteByBarra(barra: String?): List<Conteo>

    @Query("SELECT * FROM Conteo WHERE (estado = 'PEN' or estado = 'PCO') and zona = :zona")
    fun getConteoPendienteByZona(zona: String?): List<Conteo>

    @Query("SELECT * FROM Conteo WHERE (estado = 'PEN' or estado = 'PCO') and barra = :barra and zona = :zona")
    fun getConteoPendienteByBarraAndZona(barra: String?, zona: String?): List<Conteo>
}