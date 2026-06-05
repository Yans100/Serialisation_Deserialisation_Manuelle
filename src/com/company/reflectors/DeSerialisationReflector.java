package com.company.reflectors;

import com.company.fields.FieldRef;
import com.company.fields.SimpleFieldRef;
import com.company.fields.ObjectFieldRef;
import com.company.fields.CollectionFieldRef;
import com.company.annotations.Ignored;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;

public class DeSerialisationReflector {

    private List<FieldRef> simpleFields = new ArrayList<>();
    private List<FieldRef> objectFields = new ArrayList<>();
    private List<FieldRef> collectionFields = new ArrayList<>();

    public DeSerialisationReflector(Class<?> deSerialReflector) {

        // Remplir en fonction des types de champs (Fields)
        for (Field field : deSerialReflector.getDeclaredFields()) {
            field.setAccessible(true);

            // Ignorer les champs qui sont annotés @Ignored
            if (field.isAnnotationPresent(Ignored.class)) {
                continue;
            }

            //Traitement spécifique pour chaque type de champ
            if (field.getType().isPrimitive() || field.getType().equals(String.class)) {
                // Pour champs primitifs (int, float, double, bool...) ou string
                simpleFields.add(new SimpleFieldRef(field));

            } else if (field.getType().isArray() || List.class.isAssignableFrom(field.getType())) {
                // Pour collections ou tableaux
                collectionFields.add(new CollectionFieldRef(field));

            } else {
                // Pour objets complexes
                objectFields.add(new ObjectFieldRef(field));
            }
        }
    }

    // Méthode pour désérialiser le JSON en objet
    public <T> T fromJson(StringTokenizer json, Class<T> toType) {
        try {
            T instance = toType.getDeclaredConstructor().newInstance();
            
            //Passer au travers du Tokenizer Json, un element à la fois
            while(json.hasMoreTokens()) {
            	String currentFieldName = json.nextToken(" ;,:{}");
            	
            	//Quand arrive au dernier Field, skip le " ]" restant
            	if(currentFieldName.length() <= 2) {
            		continue;
            	}
            	
            	String currentName = currentFieldName.substring(1, currentFieldName.length() - 1);
            	
            	//Trouver le FieldRef associé selon le type (Simple, collection, object)
            	Optional<FieldRef> ref = simpleFields.stream().filter(f -> f.getFieldName().equals(currentName)).findFirst();

            	if(ref.isPresent()) {
            		// Désérialisation des champs simples
            		SimpleFieldRef fieldRef = (SimpleFieldRef) ref.get();
            		String fieldContent = json.nextToken();

        			fieldRef.fromJson(fieldContent, instance, fieldRef.getFieldName());
            		continue;
            	}
            	
            	ref = collectionFields.stream().filter(f -> f.getFieldName().equals(currentName)).findFirst();
            	if(ref.isPresent()) {
            		// Désérialisation des champs collections
            		CollectionFieldRef fieldRef = (CollectionFieldRef) ref.get();
            		//Get la collection d'éléments et remove les premiers caractère [ 
        			String fieldContent = json.nextToken("]");
        			fieldContent = fieldContent.substring(2);
        			
        			fieldRef.fromJson(fieldContent, instance, fieldRef.getFieldName());
        			continue;
            	}
            	
            	
            	ref = objectFields.stream().filter(f -> f.getFieldName().equals(currentName)).findFirst();
            	if(ref.isPresent()) {
            		// Désérialisation des champs objets complexes
            		ObjectFieldRef fieldRef = (ObjectFieldRef) ref.get();
            		//Get la collection d'éléments et remove le premier { (vérifier si besoin d'ajuster et de remove le dernier caractère aussi)
        			String fieldContent = json.nextToken("}");
        			fieldContent.substring(1);
        			
        			fieldRef.fromJson(fieldContent, instance, fieldRef.getFieldName());
        			continue;
            	}

            }
            

            return instance;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}