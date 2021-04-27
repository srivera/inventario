package ec.com.comohogar.inventario.persistencia

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ec.com.comohogar.inventario.persistencia.dao.ConteoDao
import ec.com.comohogar.inventario.persistencia.dao.ReconteoBodegaDao
import ec.com.comohogar.inventario.persistencia.dao.ReconteoLocalDao
import ec.com.comohogar.inventario.persistencia.entities.Conteo
import ec.com.comohogar.inventario.persistencia.entities.ReconteoBodega
import ec.com.comohogar.inventario.persistencia.entities.ReconteoLocal

@Database(entities = [Conteo::class, ReconteoBodega::class, ReconteoLocal::class], version = 2)
abstract class InventarioDatabase : RoomDatabase() {
    abstract fun conteoDao(): ConteoDao

    abstract fun reconteoBodegaDao(): ReconteoBodegaDao

    abstract fun reconteoLocalDao(): ReconteoLocalDao

    companion object {
        var INSTANCE: InventarioDatabase? = null

        fun getInventarioDataBase(context: Context): InventarioDatabase? {
            if (INSTANCE == null){
                synchronized(InventarioDatabase::class){
                    INSTANCE = Room.databaseBuilder(context.applicationContext, InventarioDatabase::class.java, "inventario")
                        .addMigrations(MIGRATION_1_2)
                        .build()
                }
            }
            return INSTANCE
        }

        fun destroyDataBase(){
            INSTANCE = null
        }


        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Conteo ADD COLUMN fecha INTEGER DEFAULT 0")
                database.execSQL("ALTER TABLE ReconteoLocal ADD COLUMN fecha INTEGER DEFAULT 0")
                database.execSQL("ALTER TABLE ReconteoBodega ADD COLUMN fecha INTEGER DEFAULT 0")
            }
        }

    }



}