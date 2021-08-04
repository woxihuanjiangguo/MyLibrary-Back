package fudan.se.lab3.domain.people;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Code {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String code;

    private String email;

    public Code (){
    }

    public Code(String code,String email){
        this.code = "" + code;
        this.email = email;
    }

    public String getCode(){
        return this.code;
    }

}
