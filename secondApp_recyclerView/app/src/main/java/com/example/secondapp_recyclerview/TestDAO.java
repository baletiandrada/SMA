package com.example.secondapp_recyclerview;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TestDAO {
    @Query("SELECT * FROM testentity")
    List<TestEntity> getAll();

    @Query("SELECT * FROM testentity WHERE id IN (:userIds)")
    List<TestEntity> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM testentity WHERE name LIKE :first LIMIT 1")
    TestEntity findByName(String first);

    @Query("SELECT * FROM testentity WHERE username = :username AND password = :password")
    TestEntity findByUsername(String username, String password);

    @Insert
    void insertAll(TestEntity... users);

    @Query("DELETE from testentity")
    void delete();

    @Query("UPDATE testentity SET email = :new_email, age = :new_age, username = :new_username, password = :new_password WHERE id= :id")
    void update(int id, String new_email, String new_age, String new_username, String new_password);
}
