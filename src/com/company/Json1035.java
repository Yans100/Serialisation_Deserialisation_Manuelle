package com.company;
import java.util.StringTokenizer;

import com.company.reflectors.DeSerialisationReflector;
import com.company.reflectors.SerialisationReflector;

public class Json1035 {
	
	 private SerialisationReflector serialisationReflector;

    public String serialize(Object o){
        // Initialiser le reflector pour analyser une classe ciblé
        serialisationReflector = new SerialisationReflector(o);

        // Sérialiser l'objet en utilisant SerialisationReflector
        return serialisationReflector.toJson(o);
    }

    //dit en classe devrait retour T
    //original en commentaire
    //public <T> Class<T> deserialize(String json, Class<T> toType){
    public <T>  T deserialize(String json, Class<T> toType){

    	// Utiliser un StringTokenizer pour analyser le JSON
        StringTokenizer tokenizer = new StringTokenizer(json, " ;,:{}");
                //https://www.geeksforgeeks.org/stringtokenizer-class-in-java/

        // Créer une instance de DeSerialisationReflector
        DeSerialisationReflector reflector = new DeSerialisationReflector(toType);
        // Désérialiser le JSON en objet de type T
        T deserialised = reflector.fromJson(tokenizer, toType);
        
        return deserialised;

    }
}
