package com.example.yuanshuai.wrj.model;

/**
 * Created by yuanshuai on 2017/12/20.
 */

public class Output<T>
{
    private int code;
    private Code codeInfo;
    private T data;

    public int getCode() {
        return code;
    }

    public Code getCodeInfo() {
        return codeInfo;
    }

    public T getData() {
        return data;
    }

    public Output(Code code, T data)
    {
        this.codeInfo = code;
        this.code = code.getCode();
        this.data = data;
    }

    public enum Code
    {
        OK(0),
        NotLogin(100),
        UsernameExist(101),
        SmsCodeError(102),
        NotBelong(103),
        Error(200),
        ParameterError(201),
        InsufficientBalance(202),
        ;

        public int getCode() {
            return code;
        }

        private final int code;


        Code(int code)
        {
            this.code = code;
        }
    }
}
