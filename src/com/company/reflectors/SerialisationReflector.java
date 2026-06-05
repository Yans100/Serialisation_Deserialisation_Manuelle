package com.company.reflectors;

import com.company.fields.SimpleFieldRef;
import com.company.fields.ObjectFieldRef;
import com.company.fields.CollectionFieldRef;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class SerialisationReflector {

    private List<SimpleFieldRef> simpleFields = new ArrayList<>();
    private List<ObjectFieldRef> objectFields = new ArrayList<>();
    private List<CollectionFieldRef> collectionFields = new ArrayList<>();

    public SerialisationReflector(Object instance) {

        // Gérer une instance nulle (à modifier au besoin)
        if (instance == null) {
            throw new IllegalArgumentException("L'instance ne peut pas être nulle");
        }

        // Récupère le type de l'objet et retourne les champs de la classe en ignorant les restrictions d'accès
        Class<?> serialReflector = instance.getClass();
        for (Field field : serialReflector.getDeclaredFields()) {
            //field.setAccessible(true);

            //Si problème avec Ignored, mettre le If en dessous en commentaire
            // Ignorer les champs qui sont annotés @Ignored
            
            if (field.isAnnotationPresent(com.company.annotations.Ignored.class)) {
                continue;
            }
            
            
            // Traitement spécifique pour chaque type de champ

            if (field.getType().isPrimitive() || field.getType().equals(String.class)) {
                // Pour champs primitifs (int, float, double, bool...) ou string
            	
                //simpleFields.add(new SimpleFieldRef(field));
                
            	//Validation pour erreurs obtenues lors de tests
            	if(!field.getName().equals("ANNOTATION") && !field.getName().equals("ENUM") && !field.getName().equals("SYNTHETIC")) {

                    simpleFields.add(new SimpleFieldRef(field));
            	}

            } else if (field.getType().isArray() || List.class.isAssignableFrom(field.getType())) {
                // Pour collections ou tableaux
                collectionFields.add(new CollectionFieldRef(field));

            } else {
            	// Pour objets complexes
            	if(!field.getName().equals("cachedConstructor")) {
            		objectFields.add(new ObjectFieldRef(field));
            	}
                
            }
        }
    }

    public String toJson(Object instance) {
        StringBuilder jsonBuilder = new StringBuilder("{");

        // Sérialisation des champs simples
        simpleFields.stream()
                .map(fieldRef -> {
                    String fieldName = fieldRef.getFieldName();
                    String fieldValue = fieldRef.toJson(instance);
                    return "\"" + fieldName + "\": " + fieldValue;
                })
                .filter(json -> json != null)
                .forEach(json -> jsonBuilder.append(json).append(","));

        // Sérialisation des champs collections
        collectionFields.stream()
                .map(fieldRef -> {
                    String fieldName = fieldRef.getFieldName();
                    String fieldValue = fieldRef.toJson(instance);
                    return "\"" + fieldName + "\": [" + fieldValue + "]";
                })
                .filter(json -> json != null)
                .forEach(json -> jsonBuilder.append(json).append(","));

        // Sérialisation des champs objets complexes
        objectFields.stream()
                .map(fieldRef -> {
                    String fieldName = fieldRef.getFieldName();
                    String fieldValue = fieldRef.toJson(instance);
                    return "\"" + fieldName + "\": " + fieldValue;
                })
                .filter(json -> json != null)
                .forEach(json -> jsonBuilder.append(json).append(","));

        // Suppression de la virgule finale
        if (jsonBuilder.length() > 1) {
            jsonBuilder.setLength(jsonBuilder.length() - 1);
        }

        jsonBuilder.append("}");
        return jsonBuilder.toString();
    }
}