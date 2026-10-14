package com.campusfind.lostfound.service;

import com.campusfind.lostfound.dao.UserDao;
import com.campusfind.lostfound.model.Role;
import com.campusfind.lostfound.model.User;
import com.campusfind.lostfound.util.PasswordUtil;

import java.util.ArrayList;
import java.util.List;

public class AuthService {

    private static final AuthService INSTANCE = new AuthService();
    private final UserDao userDao = new UserDao();
    private User currentUser;
    private final List<Runnable> authListeners = new ArrayList<>();

    private AuthService() {
        // Default to Rahul Sharma (Student) demo login on start for seamless interactive experience
        User defaultUser = userDao.findByEmail("rahul.sharma@college.edu");
        if (defaultUser != null) {
            this.currentUser = defaultUser;
        }
    }

    public static AuthService getInstance() {
        return INSTANCE;
    }

    public synchronized User getCurrentUser() {
        return currentUser;
    }

    public synchronized void setCurrentUser(User user) {
        this.currentUser = user;
        notifyAuthListeners();
    }

    public synchronized boolean isLoggedIn() {
        return currentUser != null;
    }

    public synchronized boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == Role.ADMIN;
    }

    public boolean login(String email, String password) {
        if (email == null || password == null) return false;
        User user = userDao.findByEmail(email.trim());
        if (user != null && PasswordUtil.checkPassword(password, user.getPasswordHash())) {
            setCurrentUser(user);
            return true;
        }
        return false;
    }

    public void quickSwitchUser(String email) {
        User user = userDao.findByEmail(email);
        if (user != null) {
            setCurrentUser(user);
        }
    }

    public User register(String fullName, String email, String campusId, String department, String phone, String password, Role role) throws Exception {
        if (userDao.existsByEmail(email)) {
            throw new IllegalArgumentException("College email is already registered: " + email);
        }
        if (userDao.existsByCampusId(campusId)) {
            throw new IllegalArgumentException("Campus ID is already registered: " + campusId);
        }

        String hash = PasswordUtil.hashPassword(password);
        User newUser = new User(fullName, email, campusId, department, phone, hash, role);
        User saved = userDao.save(newUser);
        setCurrentUser(saved);
        return saved;
    }

    public void logout() {
        setCurrentUser(null);
    }

    public void addAuthListener(Runnable listener) {
        authListeners.add(listener);
    }

    private void notifyAuthListeners() {
        for (Runnable listener : authListeners) {
            try {
                listener.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public List<User> getAllUsers() {
        return userDao.findAll();
    }
}
