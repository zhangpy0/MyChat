package top.zhangpy.mychat.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import top.zhangpy.mychat.data.local.entity.User;
import top.zhangpy.mychat.data.local.entity.UserProfile;

@Dao
public interface UserDao {

    @Insert
    public void insertUser(User user);

    @Update
    public void updateUser(User user);

    @Query("SELECT * FROM users WHERE email = :email")
    public User getUserByEmail(String email);

    @Query("SELECT * FROM users WHERE user_id = :userId")
    public User getUserById(Integer userId);

    @Delete
    public void deleteUser(User user);

    @Query("SELECT * FROM users")
    public List<User> getAllUsers();

    @Query("SELECT * FROM user_profiles WHERE user_id = :userId")
    public UserProfile getUserProfileById(Integer userId);

    @Update
    public void updateUserProfile(UserProfile userProfile);

    @Delete
    public void deleteUserProfile(UserProfile userProfile);

    @Insert
    public void insertUserProfile(UserProfile userProfile);

}
