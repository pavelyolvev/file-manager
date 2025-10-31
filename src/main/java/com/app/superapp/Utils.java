package com.app.superapp;

public class Utils {
    String name;
    String execName;
    public Utils (String name, String execName){
        this.name = name;
        this.execName = execName;
    }
    public String getName(){
        return name;
    }
    public String getExecName(){
        return execName;
    }
    @Override
    public String toString(){
        return name;
    }

}
