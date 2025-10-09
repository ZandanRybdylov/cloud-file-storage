package com.zandan.app.filestorage.service;

import com.zandan.app.filestorage.security.MyUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class PathService {

    public String getFullPath(String path) {
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userDetails.getUsername() + "/" + path;
    }
}
