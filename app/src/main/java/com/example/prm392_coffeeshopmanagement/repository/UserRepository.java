package com.example.prm392_coffeeshopmanagement.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.prm392_coffeeshopmanagement.dao.AppDatabase;
import com.example.prm392_coffeeshopmanagement.dao.OrderDao;
import com.example.prm392_coffeeshopmanagement.dao.ScheduleDao;
import com.example.prm392_coffeeshopmanagement.dao.UserDao;
import com.example.prm392_coffeeshopmanagement.dao.UserRoleDao;
import com.example.prm392_coffeeshopmanagement.entity.User;
import com.example.prm392_coffeeshopmanagement.entity.UserRole;
import com.example.prm392_coffeeshopmanagement.utils.EditUserProfile;
import com.example.prm392_coffeeshopmanagement.utils.UserWithRole;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class UserRepository {
    private UserDao userDao;
    private OrderDao orderDao;
    private ScheduleDao scheduleDao;
    private UserRoleDao userRoleDao;
    private ExecutorService executorService;

    public UserRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        userDao = db.userDao();
        scheduleDao = db.scheduleDao();
        orderDao = db.orderDao();
        userRoleDao = db.userRoleDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public UserWithRole login(String username, String password) {
        UserWithRole user = userDao.getUser(username, password);
        if (user != null && user.getActive() != 1) {
            return null;
        }
        return user;
    }

    public UserWithRole getUserWithRoleById(int userId) {
        return userDao.getUserWithRoleById(userId);
    }


    public LiveData<List<User>> getEmployees() {
        return userDao.getEmployees();
    }

    public void deleteEmployee(int employeeId , Consumer<Boolean> callback) {
        executorService.execute(() -> {

            int scheduleCount = scheduleDao.countSchedulesByUserId(employeeId);
            int orderCount = orderDao.countOrdersByUserId(employeeId);

            boolean isDeleted = false;
            if (scheduleCount == 0 && orderCount == 0) {
                userDao.deleteEmployeeFromUserRole(employeeId);
                userDao.deleteEmployeeById(employeeId);
                isDeleted = true;
            }
            callback.accept(isDeleted);
        });
    }

    public void addEmployee(User employee) {
        userDao.insertUser(employee);
        User insertedUser = userDao.getUserByUsername(employee.getUserName());
        int userId = insertedUser.getUserId();
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(3);
        userRoleDao.insertUserRole(userRole);
    }

    public boolean isUsernameExists(String username) {
        return userDao.getUserByUsername(username) != null;
    }

    public boolean isEmailExists(String email) {
        return userDao.getUserByEmail(email) != null;
    }

    public boolean isPhoneExists(String phone) {
        return userDao.getUserByPhone(phone) != null;
    }

    public void updateEmployee(User employee) {
        executorService.execute(() -> {
            userDao.updateUser(employee);
        });
    }

    public void updateUserProfile(String fullName, String phone, String avatar, int userId) {
        // Tuỳ vào yêu cầu, bạn có thể chạy trên luồng nền (AsyncTask, Executor, v.v.)
        userDao.updateUserProfile(fullName, phone, avatar, userId);
    }

    public EditUserProfile getUserWithRole(String username) {
        return userDao.getUserWithRole(username);
    }

    public User getUserByUserName(String userName) {
        return userDao.getUserByUsername(userName);
    }

    public void updatePassword(String username, String newPassword) {
        userDao.updatePassword(username, newPassword);
    }
}
