package top.zhangpy.mychat.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import top.zhangpy.mychat.data.local.entity.UserProfile;

@Dao
public interface UserProfileDao {

    @Query("SELECT * FROM user_profiles WHERE user_id = :userId")
    public UserProfile getUserProfileById(Integer userId);

    @Update
    public void updateUserProfile(UserProfile userProfile);

    @Delete
    public void deleteUserProfile(UserProfile userProfile);

    @Insert
    public void insertUserProfile(UserProfile userProfile);
}
