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

    @Query("DELETE FROM ReconteoBodega")
    fun eliminar()

    @Query("SELECT * FROM ReconteoBodega WHERE id == :id")
    fun getReconteoBodegaById(id: Int): List<ReconteoBodega>

    @Query("SELECT * FROM ReconteoBodega")
    fun getReconteosBodega(): List<ReconteoBodega>

    @Query("SELECT count(*) FROM ReconteoBodega")
    fun count(): Int

    @Query("SELECT * FROM ReconteoBodega WHERE estado == 'PEN'")
    fun getReconteoBodegaPendiente(): List<ReconteoBodega>
}