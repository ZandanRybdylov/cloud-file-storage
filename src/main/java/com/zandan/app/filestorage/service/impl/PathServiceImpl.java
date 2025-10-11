package com.zandan.app.filestorage.service.impl;
import com.zandan.app.filestorage.service.PathService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class PathServiceImpl implements PathService {

    @Override
    public String getFullPath(String path) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        if (path.startsWith(userDetails.getUsername() + "/")) {
            return path;
        } else {
            return userDetails.getUsername() + "/" + path;
        }
    }
}
