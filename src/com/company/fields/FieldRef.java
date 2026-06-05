package com.company.fields;

import com.company.annotations.Alias;
import com.company.annotations.Ignored;
import java.lang.reflect.Field;

public abstract class FieldRef {
    // Déclaration des variables d'instance
    protected String name;
    protected Alias aliasAnnotation;
    protected Ignored ignoredAnnotation;
    protected Field field;

    // Constructeur qui prends un objet Field en paramètre
    protected FieldRef(Field field) {
        this.field = field;
        this.name = field.getName();
        this.aliasAnnotation = field.getAnnotation(Alias.class);
        this.ignoredAnnotation = field.getAnnotation(Ignored.class);
        field.setAccessible(true);
    }

    // Méthode qui récupère la valeur du champ de l'objet instance
    protected Object getValue(Object instance) {
        try {
            return field.get(instance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Impossible d'accéder au champ: " + field.getName(), e);
        }
    }

    // Méthode qui permet de définir la valeur d'un champ de l'objet instance
    protected void setValue(Object instance, Object value) {
        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Impossible de modifier le champ: " + field.getName(), e);
        }
    }

    // Méthode qui envoie le nom du champ a utiliser avec son Alias 
    public String getFieldName() {
        return aliasAnnotation != null ? aliasAnnotation.name() : name;
    }

    // Méthode abstraire pour sérialisation qui va être implémenter dans les sous-classes de FieldRef
    public abstract String toJson(Object instance);

    // Méthode abstraire pour desérialisation qui va être implémenter dans les sous-classes de FieldRef
    public abstract void fromJson(String json, Object instance, String fieldName);
}