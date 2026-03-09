package org.oss.greentify.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ApiResponse {
    @SerializedName("response_code")
    @Expose
    private String responseCode;
    @SerializedName("results")
    @Expose
    private QuestionRepository[] results;

    public String getResponseCode() {
        return responseCode;
    }

    public QuestionRepository[] getResults() {
        return results;
    }
}
