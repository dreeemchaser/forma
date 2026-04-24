package com.forma.api.service;

import com.forma.api.dto.CreatePostRequest;
import com.forma.api.dto.PostAnalysisResponse;

public interface PostAnalysisInterface {
    PostAnalysisResponse analysePost(CreatePostRequest request);
}
