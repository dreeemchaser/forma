package com.forma.api.service;

import com.forma.api.model.Post;
import org.springframework.stereotype.Service;

@Service
public interface ModeratorService {
    void analysePost(Post post);
}
