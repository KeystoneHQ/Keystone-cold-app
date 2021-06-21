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

package com.keystone.cold.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.keystone.cold.db.entity.CoinEntity;

import java.util.List;

@Dao
public interface CoinDao {
    @Query("SELECT * FROM coins")
    LiveData<List<CoinEntity>> loadAllCoins();

    @Query("SELECT * FROM coins")
    List<CoinEntity> loadAllCoinsSync();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CoinEntity> coins);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CoinEntity coin);

    @Query("SELECT * FROM coins WHERE id = :id")
    LiveData<CoinEntity> loadCoin(long id);

    @Query("SELECT * FROM coins WHERE coinId = :coinId AND belongTo = :belongTo")
    CoinEntity loadCoinSync(String coinId, String belongTo);

    @Query("SELECT * FROM coins WHERE coinId = :coinId AND belongTo = :belongTo")
    LiveData<CoinEntity> loadCoin(String coinId, String belongTo);

    @Update
    int update(CoinEntity coinEntity);

    @Query("DELETE FROM coins WHERE belongTo = 'hidden'")
    int deleteHidden();
}
