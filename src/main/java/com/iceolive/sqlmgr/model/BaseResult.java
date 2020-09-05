package com.iceolive.sqlmgr.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class BaseResult<T>  implements Serializable {
    private Integer code;
    private String message;
    private T data;
    private boolean success;

    public BaseResult() {
    }

    public boolean isSuccess(){
        return this.code.equals(200);
    }

    public BaseResult(Integer code,String message,T data) {
        this.code = code;
        this.data = data;
        this.message =  message;
    }




    public  BaseResult (Exception e) {
        this(500,e.getMessage(),null);
    }

    public BaseResult self(){
        return this;
    }
    public BaseResult success(){
        this.code = 200;
        this.message = "";
        return self();
    }
    public BaseResult success(T data){
        return success(null,data);
    }
    public BaseResult success(String message,T data){
        this.code = 200;
        this.message = message;
        this.data = data;
        return self();
    }
    public BaseResult failure(Integer code,String message){
        this.code = code;
        this.message = message;
        return self();
    }
    public BaseResult failure(String message){
        return failure(500,message);
    }
    public BaseResult failure(Exception e){
        return failure(500,e.getMessage());
    }
}