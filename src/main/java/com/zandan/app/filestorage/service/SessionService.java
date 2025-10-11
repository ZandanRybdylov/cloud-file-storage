package com.zandan.app.filestorage.service;

import com.zandan.app.filestorage.model.User;
import jakarta.servlet.http.HttpServletRequest;

public interface SessionService {

    void setSessionAttribute(User user, HttpServletRequest request);
}
