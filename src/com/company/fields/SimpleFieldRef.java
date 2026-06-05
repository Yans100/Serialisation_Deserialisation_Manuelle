package com.company.fields;
import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.text.ParseException;

public class SimpleFieldRef  extends FieldRef{
	
    private Field field;

    public SimpleFieldRef(Field field){
    	super(field);
        this.field = field;
    }

    public String getName(){
    	String ValueName = this.name;
    	if (this.aliasAnnotation != null && !this.aliasAnnotation.name().isEmpty()) {
    		ValueName = this.aliasAnnotation.name();
    	}
        return ValueName;
    }


	public String toJson(Object instance) {
		/*if(this.ignoredAnnotation.Ignore()) {
			return "";
		}*/

		this.field.setAccessible(true);
		Object Value = this.getValue(instance);
		String Retour = "";
		if (this.field.getType().equals(String.class) || this.field.getType().equals(char.class))

		{
			return Retour + "\"" + Value.toString() + "\"";
		}
		else {
			return Retour + Value.toString();
		}
	}

	//Désérialisation de type primitif ou String
	public void fromJson(String json, Object instance, String fieldName) {
		//Vérification si la valeur est une string ou un char et retirer les "" ou '' de la string
		if((json.startsWith("\"") && json.endsWith("\"")) || (json.startsWith("'") && json.endsWith("'"))) {
			json = json.substring(1, json.length() - 1);
		}
		
		//Besoin de changer json string en type approprié
		Object value = "";
		if (this.field.getType().equals(String.class) || this.field.getType().equals(char.class)) {
			value = field.getType().cast(json);//.getClass()cast(fieldName);
		}
		else if (this.field.getType().equals(boolean.class)){
			value = Boolean.valueOf(json);
		} else {
			//Todo, valeur autre de int
			value = Integer.valueOf(json);
		}
		
		this.setValue(instance, value);
	}
}

