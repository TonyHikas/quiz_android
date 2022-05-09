package ru.tonyhikas.quizlearn;


public class ResponseResult{

    public String data = "";
    public int statusCode = 200;
    public boolean hasError = false;
    public String errorText = "";

    public ResponseResult(){}

    public ResponseResult(String data, int statusCode, boolean hasError, String errorText){
        this.data = data;
        this.statusCode = statusCode;
        this.hasError = hasError;
        this.errorText = errorText;
    }
    public ResponseResult(String data){
        this(data, 200, false, "");
    }
    public ResponseResult(String data, int statusCode){
        this(data, statusCode, false, "");
    }
}
