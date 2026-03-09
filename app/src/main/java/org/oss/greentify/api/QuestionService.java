package org.oss.greentify.api;

import retrofit2.Call;
import retrofit2.http.GET;

public interface QuestionService {
    @GET("api.php?amount=50&difficulty=easy&type=boolean")
    Call<ApiResponse> getQuestions();
}