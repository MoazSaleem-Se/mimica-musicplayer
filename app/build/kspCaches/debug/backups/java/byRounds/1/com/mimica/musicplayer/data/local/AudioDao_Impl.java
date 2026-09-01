package com.mimica.musicplayer.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AudioDao_Impl implements AudioDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AudioEntity> __insertionAdapterOfAudioEntity;

  private final EntityDeletionOrUpdateAdapter<AudioEntity> __deletionAdapterOfAudioEntity;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  private final SharedSQLiteStatement __preparedStmtOfIncrementStats;

  public AudioDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAudioEntity = new EntityInsertionAdapter<AudioEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `audio` (`id`,`title`,`artist`,`album`,`duration`,`filePath`,`albumArtUri`,`albumId`,`plays`,`lastPlayed`,`totalTime`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AudioEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        statement.bindString(3, entity.getArtist());
        statement.bindString(4, entity.getAlbum());
        statement.bindLong(5, entity.getDuration());
        statement.bindString(6, entity.getFilePath());
        if (entity.getAlbumArtUri() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getAlbumArtUri());
        }
        statement.bindLong(8, entity.getAlbumId());
        statement.bindLong(9, entity.getPlays());
        statement.bindLong(10, entity.getLastPlayed());
        statement.bindLong(11, entity.getTotalTime());
      }
    };
    this.__deletionAdapterOfAudioEntity = new EntityDeletionOrUpdateAdapter<AudioEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `audio` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AudioEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM audio";
        return _query;
      }
    };
    this.__preparedStmtOfIncrementStats = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE audio SET plays = plays + 1, lastPlayed = ?, totalTime = totalTime + ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertAll(final List<AudioEntity> audioList,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfAudioEntity.insert(audioList);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insert(final AudioEntity audio, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfAudioEntity.insert(audio);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final AudioEntity audio, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfAudioEntity.handle(audio);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAll.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object incrementStats(final long songId, final long timestamp, final long time,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfIncrementStats.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, timestamp);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, time);
        _argIndex = 3;
        _stmt.bindLong(_argIndex, songId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfIncrementStats.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<AudioEntity>> getAllAudio() {
    final String _sql = "SELECT * FROM audio ORDER BY title ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"audio"}, new Callable<List<AudioEntity>>() {
      @Override
      @NonNull
      public List<AudioEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfArtist = CursorUtil.getColumnIndexOrThrow(_cursor, "artist");
          final int _cursorIndexOfAlbum = CursorUtil.getColumnIndexOrThrow(_cursor, "album");
          final int _cursorIndexOfDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "duration");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfAlbumArtUri = CursorUtil.getColumnIndexOrThrow(_cursor, "albumArtUri");
          final int _cursorIndexOfAlbumId = CursorUtil.getColumnIndexOrThrow(_cursor, "albumId");
          final int _cursorIndexOfPlays = CursorUtil.getColumnIndexOrThrow(_cursor, "plays");
          final int _cursorIndexOfLastPlayed = CursorUtil.getColumnIndexOrThrow(_cursor, "lastPlayed");
          final int _cursorIndexOfTotalTime = CursorUtil.getColumnIndexOrThrow(_cursor, "totalTime");
          final List<AudioEntity> _result = new ArrayList<AudioEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AudioEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpArtist;
            _tmpArtist = _cursor.getString(_cursorIndexOfArtist);
            final String _tmpAlbum;
            _tmpAlbum = _cursor.getString(_cursorIndexOfAlbum);
            final long _tmpDuration;
            _tmpDuration = _cursor.getLong(_cursorIndexOfDuration);
            final String _tmpFilePath;
            _tmpFilePath = _cursor.getString(_cursorIndexOfFilePath);
            final String _tmpAlbumArtUri;
            if (_cursor.isNull(_cursorIndexOfAlbumArtUri)) {
              _tmpAlbumArtUri = null;
            } else {
              _tmpAlbumArtUri = _cursor.getString(_cursorIndexOfAlbumArtUri);
            }
            final long _tmpAlbumId;
            _tmpAlbumId = _cursor.getLong(_cursorIndexOfAlbumId);
            final int _tmpPlays;
            _tmpPlays = _cursor.getInt(_cursorIndexOfPlays);
            final long _tmpLastPlayed;
            _tmpLastPlayed = _cursor.getLong(_cursorIndexOfLastPlayed);
            final long _tmpTotalTime;
            _tmpTotalTime = _cursor.getLong(_cursorIndexOfTotalTime);
            _item = new AudioEntity(_tmpId,_tmpTitle,_tmpArtist,_tmpAlbum,_tmpDuration,_tmpFilePath,_tmpAlbumArtUri,_tmpAlbumId,_tmpPlays,_tmpLastPlayed,_tmpTotalTime);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getAllAudioList(final Continuation<? super List<AudioEntity>> $completion) {
    final String _sql = "SELECT * FROM audio ORDER BY title ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AudioEntity>>() {
      @Override
      @NonNull
      public List<AudioEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfArtist = CursorUtil.getColumnIndexOrThrow(_cursor, "artist");
          final int _cursorIndexOfAlbum = CursorUtil.getColumnIndexOrThrow(_cursor, "album");
          final int _cursorIndexOfDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "duration");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfAlbumArtUri = CursorUtil.getColumnIndexOrThrow(_cursor, "albumArtUri");
          final int _cursorIndexOfAlbumId = CursorUtil.getColumnIndexOrThrow(_cursor, "albumId");
          final int _cursorIndexOfPlays = CursorUtil.getColumnIndexOrThrow(_cursor, "plays");
          final int _cursorIndexOfLastPlayed = CursorUtil.getColumnIndexOrThrow(_cursor, "lastPlayed");
          final int _cursorIndexOfTotalTime = CursorUtil.getColumnIndexOrThrow(_cursor, "totalTime");
          final List<AudioEntity> _result = new ArrayList<AudioEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AudioEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpArtist;
            _tmpArtist = _cursor.getString(_cursorIndexOfArtist);
            final String _tmpAlbum;
            _tmpAlbum = _cursor.getString(_cursorIndexOfAlbum);
            final long _tmpDuration;
            _tmpDuration = _cursor.getLong(_cursorIndexOfDuration);
            final String _tmpFilePath;
            _tmpFilePath = _cursor.getString(_cursorIndexOfFilePath);
            final String _tmpAlbumArtUri;
            if (_cursor.isNull(_cursorIndexOfAlbumArtUri)) {
              _tmpAlbumArtUri = null;
            } else {
              _tmpAlbumArtUri = _cursor.getString(_cursorIndexOfAlbumArtUri);
            }
            final long _tmpAlbumId;
            _tmpAlbumId = _cursor.getLong(_cursorIndexOfAlbumId);
            final int _tmpPlays;
            _tmpPlays = _cursor.getInt(_cursorIndexOfPlays);
            final long _tmpLastPlayed;
            _tmpLastPlayed = _cursor.getLong(_cursorIndexOfLastPlayed);
            final long _tmpTotalTime;
            _tmpTotalTime = _cursor.getLong(_cursorIndexOfTotalTime);
            _item = new AudioEntity(_tmpId,_tmpTitle,_tmpArtist,_tmpAlbum,_tmpDuration,_tmpFilePath,_tmpAlbumArtUri,_tmpAlbumId,_tmpPlays,_tmpLastPlayed,_tmpTotalTime);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAudioById(final long id, final Continuation<? super AudioEntity> $completion) {
    final String _sql = "SELECT * FROM audio WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<AudioEntity>() {
      @Override
      @Nullable
      public AudioEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfArtist = CursorUtil.getColumnIndexOrThrow(_cursor, "artist");
          final int _cursorIndexOfAlbum = CursorUtil.getColumnIndexOrThrow(_cursor, "album");
          final int _cursorIndexOfDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "duration");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfAlbumArtUri = CursorUtil.getColumnIndexOrThrow(_cursor, "albumArtUri");
          final int _cursorIndexOfAlbumId = CursorUtil.getColumnIndexOrThrow(_cursor, "albumId");
          final int _cursorIndexOfPlays = CursorUtil.getColumnIndexOrThrow(_cursor, "plays");
          final int _cursorIndexOfLastPlayed = CursorUtil.getColumnIndexOrThrow(_cursor, "lastPlayed");
          final int _cursorIndexOfTotalTime = CursorUtil.getColumnIndexOrThrow(_cursor, "totalTime");
          final AudioEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpArtist;
            _tmpArtist = _cursor.getString(_cursorIndexOfArtist);
            final String _tmpAlbum;
            _tmpAlbum = _cursor.getString(_cursorIndexOfAlbum);
            final long _tmpDuration;
            _tmpDuration = _cursor.getLong(_cursorIndexOfDuration);
            final String _tmpFilePath;
            _tmpFilePath = _cursor.getString(_cursorIndexOfFilePath);
            final String _tmpAlbumArtUri;
            if (_cursor.isNull(_cursorIndexOfAlbumArtUri)) {
              _tmpAlbumArtUri = null;
            } else {
              _tmpAlbumArtUri = _cursor.getString(_cursorIndexOfAlbumArtUri);
            }
            final long _tmpAlbumId;
            _tmpAlbumId = _cursor.getLong(_cursorIndexOfAlbumId);
            final int _tmpPlays;
            _tmpPlays = _cursor.getInt(_cursorIndexOfPlays);
            final long _tmpLastPlayed;
            _tmpLastPlayed = _cursor.getLong(_cursorIndexOfLastPlayed);
            final long _tmpTotalTime;
            _tmpTotalTime = _cursor.getLong(_cursorIndexOfTotalTime);
            _result = new AudioEntity(_tmpId,_tmpTitle,_tmpArtist,_tmpAlbum,_tmpDuration,_tmpFilePath,_tmpAlbumArtUri,_tmpAlbumId,_tmpPlays,_tmpLastPlayed,_tmpTotalTime);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<AudioEntity> getFavoriteSong(final long sinceTimestamp) {
    final String _sql = "SELECT * FROM audio WHERE plays > 0 AND lastPlayed >= ? ORDER BY plays DESC, totalTime DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sinceTimestamp);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"audio"}, new Callable<AudioEntity>() {
      @Override
      @Nullable
      public AudioEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfArtist = CursorUtil.getColumnIndexOrThrow(_cursor, "artist");
          final int _cursorIndexOfAlbum = CursorUtil.getColumnIndexOrThrow(_cursor, "album");
          final int _cursorIndexOfDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "duration");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfAlbumArtUri = CursorUtil.getColumnIndexOrThrow(_cursor, "albumArtUri");
          final int _cursorIndexOfAlbumId = CursorUtil.getColumnIndexOrThrow(_cursor, "albumId");
          final int _cursorIndexOfPlays = CursorUtil.getColumnIndexOrThrow(_cursor, "plays");
          final int _cursorIndexOfLastPlayed = CursorUtil.getColumnIndexOrThrow(_cursor, "lastPlayed");
          final int _cursorIndexOfTotalTime = CursorUtil.getColumnIndexOrThrow(_cursor, "totalTime");
          final AudioEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpArtist;
            _tmpArtist = _cursor.getString(_cursorIndexOfArtist);
            final String _tmpAlbum;
            _tmpAlbum = _cursor.getString(_cursorIndexOfAlbum);
            final long _tmpDuration;
            _tmpDuration = _cursor.getLong(_cursorIndexOfDuration);
            final String _tmpFilePath;
            _tmpFilePath = _cursor.getString(_cursorIndexOfFilePath);
            final String _tmpAlbumArtUri;
            if (_cursor.isNull(_cursorIndexOfAlbumArtUri)) {
              _tmpAlbumArtUri = null;
            } else {
              _tmpAlbumArtUri = _cursor.getString(_cursorIndexOfAlbumArtUri);
            }
            final long _tmpAlbumId;
            _tmpAlbumId = _cursor.getLong(_cursorIndexOfAlbumId);
            final int _tmpPlays;
            _tmpPlays = _cursor.getInt(_cursorIndexOfPlays);
            final long _tmpLastPlayed;
            _tmpLastPlayed = _cursor.getLong(_cursorIndexOfLastPlayed);
            final long _tmpTotalTime;
            _tmpTotalTime = _cursor.getLong(_cursorIndexOfTotalTime);
            _result = new AudioEntity(_tmpId,_tmpTitle,_tmpArtist,_tmpAlbum,_tmpDuration,_tmpFilePath,_tmpAlbumArtUri,_tmpAlbumId,_tmpPlays,_tmpLastPlayed,_tmpTotalTime);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<ArtistStats> getFavoriteArtist(final long sinceTimestamp) {
    final String _sql = "SELECT artist, COUNT(id) as songCount, SUM(plays) as totalPlays, SUM(totalTime) as totalTime, 0.0 as percentage FROM audio WHERE plays > 0 AND lastPlayed >= ? GROUP BY artist ORDER BY totalPlays DESC, totalTime DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sinceTimestamp);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"audio"}, new Callable<ArtistStats>() {
      @Override
      @Nullable
      public ArtistStats call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfArtist = 0;
          final int _cursorIndexOfSongCount = 1;
          final int _cursorIndexOfTotalPlays = 2;
          final int _cursorIndexOfTotalTime = 3;
          final int _cursorIndexOfPercentage = 4;
          final ArtistStats _result;
          if (_cursor.moveToFirst()) {
            final String _tmpArtist;
            _tmpArtist = _cursor.getString(_cursorIndexOfArtist);
            final int _tmpSongCount;
            _tmpSongCount = _cursor.getInt(_cursorIndexOfSongCount);
            final int _tmpTotalPlays;
            _tmpTotalPlays = _cursor.getInt(_cursorIndexOfTotalPlays);
            final long _tmpTotalTime;
            _tmpTotalTime = _cursor.getLong(_cursorIndexOfTotalTime);
            final float _tmpPercentage;
            _tmpPercentage = _cursor.getFloat(_cursorIndexOfPercentage);
            _result = new ArtistStats(_tmpArtist,_tmpSongCount,_tmpTotalPlays,_tmpTotalTime,_tmpPercentage);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<ArtistStats>> getArtistBreakdown(final long sinceTimestamp) {
    final String _sql = "SELECT artist, COUNT(id) as songCount, SUM(plays) as totalPlays, SUM(totalTime) as totalTime, 0.0 as percentage FROM audio WHERE plays > 0 AND lastPlayed >= ? GROUP BY artist ORDER BY totalPlays DESC, totalTime DESC LIMIT 5";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sinceTimestamp);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"audio"}, new Callable<List<ArtistStats>>() {
      @Override
      @NonNull
      public List<ArtistStats> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfArtist = 0;
          final int _cursorIndexOfSongCount = 1;
          final int _cursorIndexOfTotalPlays = 2;
          final int _cursorIndexOfTotalTime = 3;
          final int _cursorIndexOfPercentage = 4;
          final List<ArtistStats> _result = new ArrayList<ArtistStats>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ArtistStats _item;
            final String _tmpArtist;
            _tmpArtist = _cursor.getString(_cursorIndexOfArtist);
            final int _tmpSongCount;
            _tmpSongCount = _cursor.getInt(_cursorIndexOfSongCount);
            final int _tmpTotalPlays;
            _tmpTotalPlays = _cursor.getInt(_cursorIndexOfTotalPlays);
            final long _tmpTotalTime;
            _tmpTotalTime = _cursor.getLong(_cursorIndexOfTotalTime);
            final float _tmpPercentage;
            _tmpPercentage = _cursor.getFloat(_cursorIndexOfPercentage);
            _item = new ArtistStats(_tmpArtist,_tmpSongCount,_tmpTotalPlays,_tmpTotalTime,_tmpPercentage);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<StatsSummary> getStatsSummary(final long sinceTimestamp) {
    final String _sql = "SELECT COALESCE(SUM(totalTime), 0) as totalTimeListened, COALESCE(SUM(plays), 0) as totalPlays, COUNT(DISTINCT id) as uniqueSongs, COUNT(DISTINCT artist) as uniqueArtists FROM audio WHERE plays > 0 AND lastPlayed >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sinceTimestamp);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"audio"}, new Callable<StatsSummary>() {
      @Override
      @NonNull
      public StatsSummary call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfTotalTimeListened = 0;
          final int _cursorIndexOfTotalPlays = 1;
          final int _cursorIndexOfUniqueSongs = 2;
          final int _cursorIndexOfUniqueArtists = 3;
          final StatsSummary _result;
          if (_cursor.moveToFirst()) {
            final long _tmpTotalTimeListened;
            _tmpTotalTimeListened = _cursor.getLong(_cursorIndexOfTotalTimeListened);
            final int _tmpTotalPlays;
            _tmpTotalPlays = _cursor.getInt(_cursorIndexOfTotalPlays);
            final int _tmpUniqueSongs;
            _tmpUniqueSongs = _cursor.getInt(_cursorIndexOfUniqueSongs);
            final int _tmpUniqueArtists;
            _tmpUniqueArtists = _cursor.getInt(_cursorIndexOfUniqueArtists);
            _result = new StatsSummary(_tmpTotalTimeListened,_tmpTotalPlays,_tmpUniqueSongs,_tmpUniqueArtists);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<AudioEntity>> getPlayedAudio(final long sinceTimestamp) {
    final String _sql = "SELECT * FROM audio WHERE plays > 0 AND lastPlayed >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sinceTimestamp);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"audio"}, new Callable<List<AudioEntity>>() {
      @Override
      @NonNull
      public List<AudioEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfArtist = CursorUtil.getColumnIndexOrThrow(_cursor, "artist");
          final int _cursorIndexOfAlbum = CursorUtil.getColumnIndexOrThrow(_cursor, "album");
          final int _cursorIndexOfDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "duration");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfAlbumArtUri = CursorUtil.getColumnIndexOrThrow(_cursor, "albumArtUri");
          final int _cursorIndexOfAlbumId = CursorUtil.getColumnIndexOrThrow(_cursor, "albumId");
          final int _cursorIndexOfPlays = CursorUtil.getColumnIndexOrThrow(_cursor, "plays");
          final int _cursorIndexOfLastPlayed = CursorUtil.getColumnIndexOrThrow(_cursor, "lastPlayed");
          final int _cursorIndexOfTotalTime = CursorUtil.getColumnIndexOrThrow(_cursor, "totalTime");
          final List<AudioEntity> _result = new ArrayList<AudioEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AudioEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpArtist;
            _tmpArtist = _cursor.getString(_cursorIndexOfArtist);
            final String _tmpAlbum;
            _tmpAlbum = _cursor.getString(_cursorIndexOfAlbum);
            final long _tmpDuration;
            _tmpDuration = _cursor.getLong(_cursorIndexOfDuration);
            final String _tmpFilePath;
            _tmpFilePath = _cursor.getString(_cursorIndexOfFilePath);
            final String _tmpAlbumArtUri;
            if (_cursor.isNull(_cursorIndexOfAlbumArtUri)) {
              _tmpAlbumArtUri = null;
            } else {
              _tmpAlbumArtUri = _cursor.getString(_cursorIndexOfAlbumArtUri);
            }
            final long _tmpAlbumId;
            _tmpAlbumId = _cursor.getLong(_cursorIndexOfAlbumId);
            final int _tmpPlays;
            _tmpPlays = _cursor.getInt(_cursorIndexOfPlays);
            final long _tmpLastPlayed;
            _tmpLastPlayed = _cursor.getLong(_cursorIndexOfLastPlayed);
            final long _tmpTotalTime;
            _tmpTotalTime = _cursor.getLong(_cursorIndexOfTotalTime);
            _item = new AudioEntity(_tmpId,_tmpTitle,_tmpArtist,_tmpAlbum,_tmpDuration,_tmpFilePath,_tmpAlbumArtUri,_tmpAlbumId,_tmpPlays,_tmpLastPlayed,_tmpTotalTime);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
