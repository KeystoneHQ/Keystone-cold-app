/*
 * Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.keystone.cold.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.keystone.cold.AppExecutors;
import com.keystone.cold.db.dao.AccountDao;
import com.keystone.cold.db.dao.AddressDao;
import com.keystone.cold.db.dao.CoinDao;
import com.keystone.cold.db.dao.ETHTxDao;
import com.keystone.cold.db.dao.TxDao;
import com.keystone.cold.db.dao.WhiteListDao;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.db.entity.ETHMsgDBEntity;
import com.keystone.cold.db.entity.Web3TxEntity;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.db.entity.WhiteListEntity;

@Database(entities = {CoinEntity.class, AddressEntity.class,
        TxEntity.class, WhiteListEntity.class, AccountEntity.class, Web3TxEntity.class, ETHMsgDBEntity.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "keystone-db";
    private static AppDatabase sInstance;

    public abstract CoinDao coinDao();

    public abstract AddressDao addressDao();

    public abstract TxDao txDao();

    public abstract ETHTxDao ethTxDao();

    public abstract WhiteListDao whiteListDao();

    public abstract AccountDao accountDao();

    private final MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Create new table ethtxs and ethmsgs
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS ethtxs (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            " txId TEXT NOT NULL, signedHex TEXT, 'from' TEXT, timeStamp INTEGER NOT NULL, " +
                            "belongTo TEXT, txType INTEGER NOT NULL, addition TEXT)");
            database.execSQL("CREATE INDEX index_ethtxs_id ON ethtxs (id)");
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS ethmsgs (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            " msgId TEXT NOT NULL, signature TEXT, timeStamp INTEGER NOT NULL)");
            database.execSQL("CREATE INDEX index_ethmsgs_id ON ethmsgs (id)");
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE accounts ADD addition TEXT DEFAULT '{}'");
            database.execSQL("ALTER TABLE addresses ADD addition TEXT DEFAULT '{}'");
            database.execSQL("ALTER TABLE coins ADD addition TEXT DEFAULT '{}'");
            database.execSQL("ALTER TABLE ethmsgs ADD addition TEXT DEFAULT '{}'");
            database.execSQL("ALTER TABLE txs ADD addition TEXT DEFAULT '{}'");
        }
    };

    public static AppDatabase getInstance(final Context context, final AppExecutors executors) {
        if (sInstance == null) {
            synchronized (AppDatabase.class) {
                if (sInstance == null) {
                    sInstance = buildDatabase(context.getApplicationContext(), executors);
                    sInstance.updateDatabaseCreated(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    private static AppDatabase buildDatabase(final Context appContext,
                                             final AppExecutors executors) {

        return Room.databaseBuilder(appContext, AppDatabase.class, DATABASE_NAME)
                .addMigrations(MIGRATION_1_2)
                .addMigrations(MIGRATION_2_3)
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        executors.diskIO().execute(() -> {
                            // Generate the data for pre-population
                            AppDatabase database = AppDatabase.getInstance(appContext, executors);
                            // notify that the database was created and it's ready to be used
                            database.setDatabaseCreated();
                        });
                    }
                })
                .fallbackToDestructiveMigration()
                .build();
    }

    private void updateDatabaseCreated(final Context context) {
        if (context.getDatabasePath(DATABASE_NAME).exists()) {
            setDatabaseCreated();
        }
    }

    private void setDatabaseCreated() {
        mIsDatabaseCreated.postValue(true);
    }

    public LiveData<Boolean> getDatabaseCreated() {
        return mIsDatabaseCreated;
    }
}
