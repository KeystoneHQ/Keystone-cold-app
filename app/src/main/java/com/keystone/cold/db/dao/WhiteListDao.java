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
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.keystone.cold.db.entity.WhiteListEntity;

import java.util.List;

@Dao
public interface WhiteListDao {

    @Query("select * from white_list")
    LiveData<List<WhiteListEntity>> load();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WhiteListEntity entity);

    @Delete
    void delete(WhiteListEntity entity);

    @Query("select * from white_list where addr = :addr and belongTo = :belongTo")
    WhiteListEntity queryAddress(String addr, String belongTo);

    @Query("DELETE FROM white_list WHERE belongTo = 'hidden'")
    int deleteHidden();
}
