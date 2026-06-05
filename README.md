
# Moteur JSON par réflexion — INF1035

Bibliothèque Java de sérialisation et désérialisation JSON implémentée from scratch à l'aide de la réflexion Java, sans dépendance externe (pas de Jackson, pas de Gson).

## Fonctionnalités

- Sérialisation d'objets Java en JSON (champs primitifs, String, collections, objets imbriqués)
- Désérialisation de JSON vers des objets Java typés via generics
- Annotation `@Alias` — renommer un champ dans le JSON
- Annotation `@Ignored` — exclure un champ de la sérialisation / désérialisation
- Support de `List`, tableaux et objets complexes imbriqués
- Dispatching automatique selon le type de champ : SimpleFieldRef, CollectionFieldRef, ObjectFieldRef

## Concepts démontrés

- Java Reflection API
- Annotations personnalisées (`@Retention`, `@Target`)
- Generics (``)
- Patron Template Method (classe abstraite `FieldRef`)

## Exemple d'utilisation

```java
// Sérialisation
Json1035 json = new Json1035();
String result = json.serialize(monObjet);

// Désérialisation
Person p = json.deserialize(jsonString, Person.class);
```

## Structure

```
com/company/
  Json1035.java                  — point d'entrée public
  Main.java                      — démonstration
  annotations/
    Alias.java                   — @Alias
    Ignored.java                 — @Ignored
  fields/
    FieldRef.java                — classe abstraite de base
    SimpleFieldRef.java          — primitifs et String
    CollectionFieldRef.java      — List et tableaux
    ObjectFieldRef.java          — objets complexes
  reflectors/
    SerialisationReflector.java  — sérialisation
    DeSerialisationReflector.java — désérialisation
  test/
    Person.java                  — classe de test
    Address.java                 — objet imbriqué de test
```

---

Projet universitaire en équipe — cours INF1035, UQTR.
