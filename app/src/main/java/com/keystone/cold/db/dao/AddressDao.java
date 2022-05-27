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
import androidx.room.Query;
import androidx.room.Update;

import com.keystone.cold.db.entity.AddressEntity;

import java.util.List;

@Dao
public interface AddressDao {
    @Query("SELECT * FROM addresses WHERE coinId = :coinId AND belongTo =:belongTo")
    LiveData<List<AddressEntity>> loadAddressForCoin(String coinId, String belongTo);

    @Query("SELECT * FROM addresses WHERE coinId = :coinId AND belongTo =:belongTo")
    List<AddressEntity> loadAddressSync(String coinId, String belongTo);

    @Query("SELECT * FROM addresses WHERE path = :path AND belongTo = :belongTo")
    AddressEntity loadAddress(String path, String belongTo);

    @Insert
    void insertAddress(AddressEntity address);

    @Insert
    void insertAll(List<AddressEntity> addresses);

    @Update
    int update(AddressEntity addr);

    @Query("DELETE FROM addresses WHERE coinId = :coinId AND belongTo = :belongTo")
    int deleteAddressByCoin(String coinId, String belongTo);

    @Query("DELETE FROM addresses WHERE belongTo = 'hidden'")
    int deleteHidden();
}
