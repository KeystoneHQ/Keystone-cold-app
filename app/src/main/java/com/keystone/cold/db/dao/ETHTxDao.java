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


import androidx.lifecycle.MutableLiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.keystone.cold.db.entity.GenericETHTxEntity;

import java.util.List;

@Dao
public interface ETHTxDao {
    @Query("SELECT * FROM txs ORDER BY timeStamp DESC")
    MutableLiveData<List<GenericETHTxEntity>> loadETHTxs();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(GenericETHTxEntity tx);

    @Query("SELECT * FROM txs WHERE txId = :id")
    GenericETHTxEntity loadSync(String id);

    @Query("DELETE FROM txs WHERE belongTo = 'hidden'")
    int deleteHidden();
}
