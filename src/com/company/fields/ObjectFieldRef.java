package com.company.fields;
import com.company.reflectors.DeSerialisationReflector;
import com.company.reflectors.SerialisationReflector;
import com.company.annotations.Ignored;
import java.lang.reflect.Field;
import java.util.StringTokenizer;


public class ObjectFieldRef extends FieldRef {
    private DeSerialisationReflector dreflector;
    private SerialisationReflector reflector;
    private Field field;

    // Constructeur
    public ObjectFieldRef(Field field) {
        super(field);
        this.field = field;

        // Vérifier si le champ est annoté avec @Ignore
        if (field.isAnnotationPresent(Ignored.class)) {
            // Si @Ignore est présent, on ignore ce champ pour la sérialisation
            this.reflector = null;
            this.dreflector = null;
            return;
        }

        // Vérifier que le champ est un objet (pas collection ni tableau ni primitifs ni string)
        Class<?> fieldType = field.getType();
        if (!fieldType.isPrimitive()
                && !fieldType.equals(String.class)
                && !fieldType.isArray()
                && !Iterable.class.isAssignableFrom(fieldType)) {
            this.reflector = new SerialisationReflector(fieldType);
            this.dreflector = new DeSerialisationReflector(fieldType);
        } else {
            // Si ce n'est pas un objet, pas de reflector
            this.reflector = null;
            this.dreflector = null;
        }
    }

    // Méthode pour récupérer la valeur du champ
    protected Object getValue(Object instance) {
        try {
            this.field.setAccessible(true);
            return this.field.get(instance);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Retourne le nom du champ ou son alias s'il est défini
    public String getName() {
        String valueName = this.name;
        if (this.aliasAnnotation != null && !this.aliasAnnotation.name().isEmpty()) {
            valueName = this.aliasAnnotation.name();
        }
        return valueName;
    }

    // Méthode pour sérialiser un objet en JSON
    public String toJson(Object instance) {
        Object fieldInstance = this.getValue(instance);

        // Si l'objet est null, on retourne une chaîne représentant null
        if (fieldInstance == null) {
            return "null";
        }

        // Si le champ est ignoré, on retourne une chaîne vide
        if (field.isAnnotationPresent(Ignored.class)) {
            return "";
        }

        // Utiliser un StringBuilder pour construire la représentation JSON
        StringBuilder jsonBuilder = new StringBuilder();

        // Vérification du type du champ
        if (fieldInstance.getClass().isPrimitive() || fieldInstance instanceof String) {
            // Sérialiser directement les primitives ou les chaînes
            jsonBuilder.append("\"").append(fieldInstance).append("\"");
        } else if (reflector != null) {
            // Sinon utiliser le reflector pour sérialiser l'objet
            jsonBuilder.append("{").append(reflector.toJson(fieldInstance)).append("}");
        }

        return jsonBuilder.toString();
    }

    public void fromJson(String json, Object instance, String fieldName) {
        StringTokenizer tokenizer = new StringTokenizer(json, " ;,:{}");

        try {
            // Vérifier si le champ est annoté avec @Ignored
            if (field.isAnnotationPresent(Ignored.class)) {
                return; // Ne rien faire si le champ est ignoré
            }

            // Si le JSON est null, ne rien assigner
            if (json == null || json.trim().equalsIgnoreCase("null")) {
                this.field.setAccessible(true);
                this.field.set(instance, null);
                return;
            }

            // Vérifier si le champ dans l'objet est primitif ou un string
            if (getElementType(field).isPrimitive() || getElementType(field).equals(String.class)) {
                // Si c'est un type primitif ou une chaîne, on peut directement désérialiser
                int elementCount = tokenizer.countTokens();
                if (elementCount > 0) {
                    String token = tokenizer.nextToken();
                    // Désérialiser directement en fonction du type
                    if (getElementType(field).isPrimitive()) {
                        this.field.setAccessible(true);
                        this.field.set(instance, parsePrimitive(token, field.getType()));
                    } else if (getElementType(field).equals(String.class)) {
                        this.field.setAccessible(true);
                        this.field.set(instance, token.replace("\"", "")); // Retirer les guillemets autour de la chaîne
                    }
                }
                return;  // Sortir après avoir désérialisé les primitifs ou string contenue dans l'objet
            }

            // Si le champ est un objet, utiliser le dreflector pour désérialiser
            if (dreflector != null) {
                Object deserializedObject = dreflector.fromJson(tokenizer, field.getType());  // Passer le type explicitement
                this.field.setAccessible(true);
                this.field.set(instance, deserializedObject);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la désérialisation pour: " + fieldName, e);
        }
    }

    // Méthode pour récupérer le type de l'élément (primitif ou string dans ce contexte)
    private Class<?> getElementType(Field field) {
        return field.getType();
    }

    // Méthode pour désérialiser les types primitifs automatiquement 
    private Object parsePrimitive(String json, Class<?> fieldType) {
        if (fieldType.equals(int.class)) {
            return Integer.parseInt(json);
        } else if (fieldType.equals(long.class)) {
            return Long.parseLong(json);
        } else if (fieldType.equals(double.class)) {
            return Double.parseDouble(json);
        } else if (fieldType.equals(float.class)) {
            return Float.parseFloat(json);
        } else if (fieldType.equals(boolean.class)) {
            return Boolean.parseBoolean(json);
        } else if (fieldType.equals(char.class)) {
            return json.charAt(0);
        } else if (fieldType.equals(byte.class)) {
            return Byte.parseByte(json);
        } else if (fieldType.equals(short.class)) {
            return Short.parseShort(json);
        }
        return null;
    }
}