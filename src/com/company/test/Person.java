package com.company.test;

import java.util.List;

import com.company.annotations.Alias;
import com.company.annotations.Ignored;

public class Person {
    @Alias(name = "Nom")
    public String name;
    public int age;
    @Ignored(Ignore = true)
    public String password;

    public List<String> hobbies;

    //Retiré Address temporairement, car la sérialisation / désérialisation d'objet complexe ne fonctionne pas pour l'instant
    //public Address address;

    public Person() {
    }

    public Person(String name, int age, String password, List<String> hobbies, Address address) {
        this.name = name;
        this.age = age;
        this.password = password;
        this.hobbies = hobbies;
        //this.address = address;
    }
}

