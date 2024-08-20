package com.rbcsystem.demo.models;

public class Client extends User {
  
    private int importance;
    
    //non-param constructor
    public Client(){
    }

    //param constructor
    public Client(int id,String nm, String em, String pw, String dep, String pos, int imp){
        super(id, nm, em, pw, dep, pos);
        this.importance = imp;
        
    }

    //setter and getter
    public int getImportance(){
        return importance;
    }

    public void setImportance(int imp){
        this.importance = imp;
    }
}

