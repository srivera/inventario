package ec.com.comohogar.inventario.persistencia

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ec.com.comohogar.inventario.persistencia.dao.ConteoDao
import ec.com.comohogar.inventario.persistencia.entities.Conteo

@Database(entities = [Conteo::class], version = 1)
abstract class InventarioDatabase : RoomDatabase() {
    abstract fun conteoDao(): ConteoDao

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