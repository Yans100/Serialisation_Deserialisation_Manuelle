package com.company;


import com.company.test.Address;
import com.company.test.Person;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Chaîne JSON à désérialiser
    	//Chaîne original
        //String jsonString = "{ \"name\": \"Alice\", \"age\": 30, \"password\": \"admin1234\",\"hobbies\":[\"Art\",\"Soccer\",\"Musique\"], \"Address\": { \"street\": \"123 Main St\", \"city\": \"New York\", \"zipCode\": 10001 } }";
        
        String jsonString = "{ \"Nom\":\"Alice\",\"age\": 30,\"password\":\"admin1234\",\"hobbies\":[\"Art\",\"Soccer\",\"Musique\"]}";

        
        //Personne à sérialiser
        Address addressPerson = new Address("Rue Lemaire", "New York", 12332);
        List<String> hobbiesPerson = new ArrayList<String>();
        hobbiesPerson.add("Art");
        hobbiesPerson.add("Soccer");
        hobbiesPerson.add("Musique");
        
        Person personSerialisation = new Person("Robert", 23, "admin1234", hobbiesPerson, addressPerson);
        
        //Appeler la classe Json1035 qui s'occupe des appels vers les classes du projet servant à la sérialisation et désérialisation
        Json1035 json1035 = new Json1035();
        
        String personJson = json1035.serialize(personSerialisation);

        System.out.println("Personne sérialisé :");
        System.out.println(personJson);
        
        System.out.println("Json à désérialiser : " + jsonString);
        Person person = json1035.deserialize(jsonString, Person.class);
        
        // Afficher le résultat de la désérialisation
        if (person != null) {
            System.out.println("Désérialisation de l'objet Person :");
            System.out.println("Name: " + person.name);
            System.out.println("Age: " + person.age);
            System.out.println("Password: " + person.password); // Devrait être null car annoté avec @Ignored

            
            if (person.hobbies != null) {
            	System.out.println("Hobbies: ");
            	for(Object hobby : person.hobbies) {
            		System.out.println(hobby);
            	}
                
            } else {
                System.out.println("Hobbies: null");
            }
/*
            if (person.address != null) {
                System.out.println("Address: " + person.address.street + ", " + person.address.city + ", " + person.address.zipCode);
            } else {
                System.out.println("Address: null");
            }*/
        } else {
            System.out.println("La désérialisation a échoué.");


        }
    }
}


