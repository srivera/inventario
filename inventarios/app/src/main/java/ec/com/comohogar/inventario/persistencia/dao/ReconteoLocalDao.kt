package ec.com.comohogar.inventario.persistencia.dao

import androidx.room.*
import ec.com.comohogar.inventario.persistencia.entities.ReconteoLocal

@Dao
interface ReconteoLocalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertarReconteoLocal(reconteoLocal: ReconteoLocal)

    @Update
    fun actualizarConteo(reconteoBodega: ReconteoLocal)

    @Delete
    fun eliminarConteo(reconteoBodega: ReconteoLocal)

    @Query("DELETE FROM ReconteoLocal where estado = 'INS'")
    fun eliminar()

    @Query("DELETE FROM ReconteoLocal")
    fun eliminarTodo()

    @Query("SELECT * FROM ReconteoLocal WHERE id = :id")
    fun getReconteoLocalById(id: Int): List<ReconteoLocal>

    @Query("SELECT * FROM ReconteoLocal")
    fun getReconteosLocal(): List<ReconteoLocal>

    @Query("SELECT count(*) FROM ReconteoLocal")
    fun count(): Int

    @Query("SELECT count(*) FROM ReconteoLocal WHERE estado = 'PEN'")
    fun countPendiente(): Int

    @Query("SELECT count(*) FROM ReconteoLocal WHERE estado = 'ENV'")
    fun countEnviado(): Int

    @Query("SELECT * FROM ReconteoLocal WHERE estado = 'PEN'")
    fun getReconteoLocalPendiente(): List<ReconteoLocal>

    @Query("SELECT * FROM ReconteoLocal WHERE estado != 'PEN' and barra = :barra")
    fun getReconteoLocalPendienteByBarra(barra: String?): List<ReconteoLocal>

}