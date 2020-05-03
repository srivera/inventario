package ec.com.comohogar.inventario.persistencia.dao

import androidx.room.*
import ec.com.comohogar.inventario.persistencia.entities.ReconteoBodega

@Dao
interface ReconteoBodegaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertarReconteoBodega(reconteoBodega: ReconteoBodega)

    @Update
    fun actualizarConteo(reconteoBodega: ReconteoBodega)

    @Delete
    fun eliminarConteo(reconteoBodega: ReconteoBodega)

    @Query("DELETE FROM ReconteoBodega where estado = 'INS'")
    fun eliminarInsertado()

    @Query("DELETE FROM ReconteoBodega")
    fun eliminarTodo()

    @Query("SELECT * FROM ReconteoBodega WHERE id == :id")
    fun getReconteoBodegaById(id: Int): List<ReconteoBodega>

    @Query("SELECT * FROM ReconteoBodega")
    fun getReconteosBodega(): List<ReconteoBodega>

    @Query("SELECT * FROM ReconteoBodega WHERE estado = 'INS'")
    fun getReconteosBodegaInsertado(): List<ReconteoBodega>

    @Query("SELECT count(*) FROM ReconteoBodega")
    fun count(): Int

    @Query("SELECT count(*) FROM ReconteoBodega WHERE estado = 'PEN'")
    fun countPendiente(): Int

    @Query("SELECT count(*) FROM ReconteoBodega WHERE estado = 'ENV'")
    fun countEnviado(): Int

    @Query("SELECT count(*) FROM ReconteoBodega WHERE estado = 'INS'")
    fun countInsertado(): Int

    @Query("SELECT * FROM ReconteoBodega WHERE estado = 'PEN'")
    fun getReconteoBodegaPendiente(): List<ReconteoBodega>

    @Query("SELECT * FROM ReconteoBodega WHERE estado = 'PEN' and barra = :barra")
    fun getReconteoPendienteByBarra(barra: String?): List<ReconteoBodega>

    @Query("SELECT * FROM ReconteoBodega WHERE estado = 'PEN' and rcoUbicacion = :zona")
    fun getReconteoPendienteByZona(zona: String?): List<ReconteoBodega>

    @Query("SELECT * FROM ReconteoBodega WHERE estado = 'PEN' and barra = :barra and rcoUbicacion = :zona")
    fun getReconteoPendienteByBarraAndZona(barra: String?, zona: String?): List<ReconteoBodega>
}