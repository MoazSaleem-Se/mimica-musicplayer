package com.mimica.musicplayer.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        AudioEntity::class,
        PlaylistEntity::class,
        PlaylistSongEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun audioDao(): AudioDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playlistSongDao(): PlaylistSongDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE audio ADD COLUMN plays INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE audio ADD COLUMN lastPlayed INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE audio ADD COLUMN totalTime INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE audio ADD COLUMN customArtworkUri TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE audio ADD COLUMN customArtistName TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE audio ADD COLUMN fileFormat TEXT NOT NULL DEFAULT 'MP3'")
                db.execSQL("ALTER TABLE playlists ADD COLUMN customArtworkUri TEXT DEFAULT NULL")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "music_player_database"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
