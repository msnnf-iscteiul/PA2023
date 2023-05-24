# PA2023 JSON Project Editor and Graphic Interface 

![image](https://github.com/msnnf-iscteiul/PA2023/assets/56412586/65d8bfb1-c9ca-4e3e-a69e-64f01fa33bb3)
    
Data class JSONObject has addProperty(propertyName as String, value as Any).
Data class JSONArray has add(value as Any). 
You can create arrays of simple values or complex objects with properties. 

    val aluno1 = JSONObject()
    aluno1.addProperty("numero", 101101)
    aluno1.addProperty("nome", "Dave Farley")
    aluno1.addProperty("internacional", true)
    val aluno2 = JSONObject()
    aluno2.addProperty("numero", 101102)
    aluno2.addProperty("nome", "Martin Fowler")
    aluno2.addProperty("internacional", true)
    val aluno3 = JSONObject()
    aluno3.addProperty("numero", 26503)
    aluno3.addProperty("nome", "André Santos")
    aluno3.addProperty("internacional", false)

    val inscritosArray = JSONArray()
    inscritosArray.add(aluno1)
    inscritosArray.add(aluno2)
    inscritosArray.add(aluno3)

    val cursosArray = JSONArray()
    cursosArray.add("MEI")
    cursosArray.add("MIG")
    cursosArray.add("METI")

    val json = JSONObject()
    json.addProperty("uc", "PA")
    json.addProperty("ects", 6.0)
    json.addProperty("data-exame", null)
    json.addProperty("inscritos", inscritosArray)
    json.addProperty("cursos", cursosArray)
    
Both data classes are Visitable and have 4 functions SearchPropertyNameValues, SearchPropertyNameObjects, VerifyStructure and CreateJson.

    interface Visitable {
      fun acceptCreateJson(visitor: JsonVisitor)
      fun acceptSearchPropertyNameValues(propertyName: String, visitor: JsonVisitor)
      fun acceptSearchPropertyNameObjects(propertyNames: List<String>, visitor: JsonVisitor)
      fun acceptVerifyStructure(propertyName: String, visitor: JsonVisitor)
    }
    
    data class JSONObject(
        var fields: MutableList<Pair<String, Any>> = mutableListOf(),
    ) : Visitable {
        fun addProperty(name: String, value: Any?) {
            fields.add(Pair(name, value) as Pair<String, Any>)
    }
        override fun acceptCreateJson(visitor: JsonVisitor) = visitor.visitCreateJson(this)
        override fun acceptSearchPropertyNameValues(propertyName: String, visitor: JsonVisitor) = visitor.visitSearchPropertyNameValues(propertyName, this)
        override fun acceptSearchPropertyNameObjects(propertyNames: List<String>, visitor: JsonVisitor) = visitor.visitSearchPropertyNameObjects(propertyNames, this)
        override fun acceptVerifyStructure(propertyName: String, visitor: JsonVisitor) = visitor.visitVerifyStructure(propertyName, this)
    }

    data class JSONArray(
        var fields: MutableList<Any> = mutableListOf(),
    ): Visitable {
        fun add(value: Any) {
            fields.add(value)
    }
        override fun acceptCreateJson(visitor: JsonVisitor) = visitor.visitCreateJson(this)
        override fun acceptSearchPropertyNameValues(propertyName: String, visitor: JsonVisitor) = visitor.visitSearchPropertyNameValues(propertyName, this)
        override fun acceptSearchPropertyNameObjects(propertyNames: List<String>, visitor: JsonVisitor) = visitor.visitSearchPropertyNameObjects(propertyNames, this)
        override fun acceptVerifyStructure(propertyName: String, visitor: JsonVisitor) = visitor.visitVerifyStructure(propertyName, this)
    }
    
SearchPropertyNameValues accepts propertyName and JSONObject. 
Returns a list of values of the properties with that propertyName. 

    val expected = listOf(101101, 101102, 26503)
    Assertions.assertEquals(expected, searchPropertyNameValues("numero", json))
    
SearchPropertyNameObjects accepts list of propertyNames and JSONObject. 
Returns a list of JSONObjects that contain all the propertyNames. 

    val expected = listOf(aluno1, aluno2, aluno3)
    Assertions.assertEquals(expected, searchPropertyNameObjects(listOf("numero", "nome"), json))
    
VerifyStructure accepts propertyName and JSONObject. 
Returns a boolean, true if every property with that propertyName has same structure (all Int or all String). 

    Assertions.assertTrue(verifyStructure("numero", json))
    Assertions.assertTrue(verifyStructure("inscritos", json))
    
CreateJson aceepts JSONObject. 
Returns a string with JSONObject transformed into a JSON. 

    Assertions.assertEquals(expected, createJson(json))
    
    {
    "uc" : "PA",
    "ects" : 6.0,
    "data-exame" : null,
    "inscritos" : [
    {
    "numero" : 101101,
    "nome" : "Dave Farley",
    "internacional" : true
    },
    {
    "numero" : 101102,
    "nome" : "Martin Fowler",
    "internacional" : true
    },
    {
    "numero" : 26503,
    "nome" : "André Santos",
    "internacional" : false
    }
    ],
    "cursos" : [
    "MEI",
    "MIG",
    "METI"
    ]
    }
