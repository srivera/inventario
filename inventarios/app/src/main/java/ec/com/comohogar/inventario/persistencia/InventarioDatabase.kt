package ec.com.comohogar.inventario.persistencia

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ec.com.comohogar.inventario.persistencia.dao.ConteoDao
import ec.com.comohogar.inventario.persistencia.dao.ReconteoBodegaDao
import ec.com.comohogar.inventario.persistencia.entities.Conteo
import ec.com.comohogar.inventario.persistencia.entities.ReconteoBodega

@Database(entities = [Conteo::class, ReconteoBodega::class], version = 2)
abstract class InventarioDatabase : RoomDatabase() {
    abstract fun conteoDao(): ConteoDao

    abstract fun reconteoBodegaDao(): ReconteoBodegaDao

    companion object {
        var INSTANCE: InventarioDatabase? = null

        fun getInventarioDataBase(context: Context): InventarioDatabase? {
            if (INSTANCE == null){
                synchronized(InventarioDatabase::class){
                    INSTANCE = Room.databaseBuilder(context.applicationContext, InventarioDatabase::class.java, "myDB").build()
                }
            }
            return INSTANCE
        }

        fun destroyDataBase(){
            INSTANCE = null
        }
    }
}