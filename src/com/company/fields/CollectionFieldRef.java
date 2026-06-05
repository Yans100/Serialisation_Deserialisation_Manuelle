package com.company.fields;

import com.company.annotations.Ignored;
import com.company.reflectors.DeSerialisationReflector;
import com.company.reflectors.SerialisationReflector;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


public class CollectionFieldRef extends FieldRef {

    private SerialisationReflector reflector;
    private DeSerialisationReflector dreflector;
    private Field field;

    // Constructeur qui vérifie le type d'élément dans la collection (Liste ou tableau)
    public CollectionFieldRef(Field field) {
        super(field);
        this.field = field;

        // Vérifier si le champ est annoté avec @Ignore
        if (field.isAnnotationPresent(Ignored.class)) {
            // Si @Ignore est présent on ignore ce champ pour la sérialisation
            this.reflector = null;
            this.dreflector = null;
            return;
        }

        // Définir le reflector pour les types collections (Liste, Array, Objets; pas primitifs ou string)
        Class<?> elementType = getElementType(field);
        if (elementType != null && !elementType.isPrimitive() && !elementType.equals(String.class)) {
            this.reflector = new SerialisationReflector(field);
            this.dreflector = new DeSerialisationReflector(elementType);
        }
    }

    // Récupère valeur du champ field de l'instance passée en paramètre
    protected Object getValue(Object instance) {
        try {
            return this.field.get(instance);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Retourne nom du champ (ou Alias)
    public String getName() {
        String valueName = this.name;
        if (this.aliasAnnotation != null && !this.aliasAnnotation.name().isEmpty()) {
            valueName = this.aliasAnnotation.name();
        }
        return valueName;
    }

    // Méthode pour sérialiser le type collection en JSON
    public String toJson(Object instance) {
        Object fieldInstance = this.getValue(instance);

        // Si la collection est vide ou null, on retourne un tableau JSON vide
        if (fieldInstance == null) {
            return "[]";
        }

        // Si le champ est annoté avec @Ignore, ne pas sérialiser
        if (field.isAnnotationPresent(Ignored.class)) {
            return "";  // Ne rien retourner si @Ignore est présent
        }

        // On utilise un StringBuilder pour construire le JSON
        StringBuilder jsonBuilder = new StringBuilder();

        // Si la collection est une liste (List)
        if (fieldInstance instanceof List<?>) {
            List<?> list = (List<?>) fieldInstance;
            for (Object item : list) {
                if (item == null) {
                    jsonBuilder.append("null,");
                } else if (item.getClass().isPrimitive() || item instanceof String) {
                    // Sérialiser la liste avec des types primitifs ou String directement sans reflector
                    jsonBuilder.append("\"").append(item).append("\",");
                } else if (reflector != null) {
                    // Utiliser le reflector pour sérialiser les objets contenus dans la liste
                    jsonBuilder.append(reflector.toJson(item)).append(",");
                }
            }

            // Si la collection est un tableau (Array)
        } else if (fieldInstance.getClass().isArray()) {
            Object[] array = (Object[]) fieldInstance;
            for (Object item : array) {
                if (item == null) {
                    jsonBuilder.append("null,");
                } else if (item.getClass().isPrimitive() || item instanceof String) {
                    // Sérialiser le tableau avec les types primitifs ou String directement sans reflector
                    jsonBuilder.append("\"").append(item).append("\",");
                } else if (reflector != null) {
                    // Utiliser le reflector pour sérialiser les objets contenus dans le tableau
                    jsonBuilder.append(reflector.toJson(item)).append(",");
                }
            }
        }

        // Retirer la dernière virgule et fermer la chaine JSON avant de la renvoyer
        if (jsonBuilder.length() > 1) {
            jsonBuilder.setLength(jsonBuilder.length() - 1);
        }

        return jsonBuilder.toString();
    }

    public void fromJson(String json, Object instance, String fieldName) {
        //Permet de parcourir la string json pour obtenir chaque element un à la fois
        StringTokenizer tokenizer = new StringTokenizer(json, ",");

        //Ajouter les types primitifs à l'array directement
        if (getElementType(field).isPrimitive() || getElementType(field).equals(String.class)) {
            int elementCount = tokenizer.countTokens();

            //Créer une liste ou une array avec les éléments à l'intérieur
            if(field.getType().isArray()) {
                Object jsonArray = Array.newInstance(this.getElementType(field), elementCount);

                //Ajouter les elements à jsonArray
                for(int i = 0; tokenizer.hasMoreTokens(); i++) {
                    String value = tokenizer.nextToken();
                    Array.set(jsonArray, i, value);
                }

                this.setValue(instance, jsonArray);
            }
            else if(List.class.isAssignableFrom(field.getType())){
                //Ajouter les types primitifs à la liste directement

                ArrayList<Object> jsonList = new ArrayList<Object>();
                //Ajouter les elements à jsonArray
                for(int i = 0; tokenizer.hasMoreTokens(); i++) {
                    jsonList.add(tokenizer.nextToken());
                }

                this.setValue(instance, jsonList);
            }
        }
        else {
            int elementCount = tokenizer.countTokens();

            // Si le champ est un tableau
            if (field.getType().isArray()) {
                // Créer un tableau du type approprié
                Object jsonArray = Array.newInstance(getElementType(field), elementCount);

                for (int i = 0; tokenizer.hasMoreTokens(); i++) {
                    String value = tokenizer.nextToken().trim();

                    // Désérialiser chaque élément avec le dreflector
                    Object deserializedObject = dreflector.fromJson(new StringTokenizer(value), getElementType(field));

                    // Ajouter au tableau
                    Array.set(jsonArray, i, deserializedObject);
                }

                // Définir le tableau comme valeur
                this.setValue(instance, jsonArray);
            }
            // Si le champ est une collection
            else if (List.class.isAssignableFrom(field.getType())) {
                List<Object> jsonList = new ArrayList<>();

                while (tokenizer.hasMoreTokens()) {
                    String value = tokenizer.nextToken().trim();

                    // Désérialiser chaque élément avec dreflector
                    Object deserializedObject = dreflector.fromJson(new StringTokenizer(value), getElementType(field));

                    // Ajouter à la liste
                    jsonList.add(deserializedObject);
                }

                // Définir la liste comme valeur
                this.setValue(instance, jsonList);
            }
            // Si le champ est un objet complexe
            else {
                // Désérialiser l'objet avec le dreflector
                String objectContent = tokenizer.nextToken("}");
                objectContent = objectContent.substring(1); // Retirer le premier '{'

                Object deserializedObject = dreflector.fromJson(new StringTokenizer(objectContent), field.getType());

                // Définir l'objet comme valeur
                this.setValue(instance, deserializedObject);
            }
        }
    }

    // Méthode pour identifier le type des éléments dans la liste ou le tableau
    private Class<?> getElementType(Field field) {
        if (field.getType().isArray()) {
            return field.getType().getComponentType();
        }
        if (List.class.isAssignableFrom(field.getType())) {
            // Si le champ est une liste, vérifier le type générique
            Type genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType) {
                Type[] actualTypeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
                if (actualTypeArguments.length > 0) {
                    // Retourne le premier type générique
                    return (Class<?>) actualTypeArguments[0];
                }
            }
        	
        	//Retour de base si en haut ne fonctionne pas
            return Object.class;
        }
        return null;
    }
}
