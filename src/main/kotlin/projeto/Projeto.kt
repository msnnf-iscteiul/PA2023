package projeto

import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

interface JsonVisitor {
    fun visitCreateJson(visitable: Visitable)
    fun visitSearchPropertyNameValues(propertyName: String, visitable: Visitable)
    fun visitSearchPropertyNameObjects(propertyNames: List<String>, visitable: Visitable)
    fun visitVerifyStructure(propertyName: String, visitable: Visitable)
}

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

class ProjectVisitor(var createJsonResult: String? = null,
                     var searchPropertyNameValueResult: MutableList<Any?>? = null,
                     var searchPropertyNameObjectsResult: MutableList<Any?>? = null,
                     var verifyStructureResult: MutableList<Any?>? = null,
): JsonVisitor {
    override fun visitCreateJson(visitable: Visitable) {
        var jsonText = ""
        if (visitable is JSONObject) {
            val fieldValues = visitable::class.dataClassFields.map { it.call(visitable) }
            val valuesList = fieldValues[0] as List<Pair<String,Any>>
            valuesList.map {
                if (it.second is JSONArray) {
                    val name = it.first
                    jsonText += "\"$name\" : [\n"
                    val array = it.second as JSONArray
                    array.acceptCreateJson(this)
                    if (it != valuesList.last()) {
                        jsonText += this.createJsonResult + "],\n"
                    } else {
                        jsonText += this.createJsonResult + "]\n"
                    }
                } else {
                    val name = it.first
                    if (it.second is String) {
                        val asString = it.second
                        val value = "\"$asString\""
                        jsonText += "\"$name\" : $value,\n"
                    } else {
                        val value = it.second
                        jsonText += "\"$name\" : $value,\n"
                    }
                }
            }
        } else if (visitable is JSONArray) {
            val fieldValues = visitable::class.dataClassFields.map { it.call(visitable) }
            val jsonArray = fieldValues[0] as List<*>
            val jsonIterator: Iterator<Any?> = jsonArray.iterator()
            while (jsonIterator.hasNext()) {
                val jsonLine = jsonIterator.next()
                if (jsonLine is JSONObject) {
                    jsonText += "{\n"
                    val jsonFieldIterator: Iterator<Pair<String, Any>> = jsonLine.fields.iterator()
                    while (jsonFieldIterator.hasNext()) {
                        val jsonFieldLine = jsonFieldIterator.next()
                        val name = jsonFieldLine.first
                        if (jsonFieldLine.second is String) {
                            val asString = jsonFieldLine.second
                            val value = "\"$asString\""
                            jsonText += "\"$name\" : $value"
                        } else {
                            val value = jsonFieldLine.second
                            jsonText += "\"$name\" : $value"
                        }
                        if (jsonFieldIterator.hasNext()) {
                            jsonText += ",\n"
                        } else {
                            jsonText += "\n"
                        }
                    }
                    if (jsonIterator.hasNext()) {
                        jsonText += "},\n"
                    } else {
                        jsonText += "}\n"
                    }
                } else if (jsonLine is JSONArray) {
                    jsonLine.acceptCreateJson(this)
                    jsonText += this.createJsonResult
                } else {
                    if (jsonIterator.hasNext()) {
                        jsonText += "\"$jsonLine\",\n"
                    } else {
                        jsonText += "\"$jsonLine\"\n"
                    }
                }
            }
        }
        createJsonResult = jsonText
    }

    override fun visitSearchPropertyNameValues(propertyName: String, visitable: Visitable) {
        val valueList = mutableListOf<Any?>()
        if (visitable is JSONObject) {
            val fieldValues = visitable::class.dataClassFields.map { it.call(visitable) }
            val valuesList = fieldValues[0] as List<Pair<String,Any>>
            valuesList.map {
                if (it.first == propertyName) {
                    valueList.add(it.second)
                }
                if (it.second is JSONArray) {
                    val array = it.second as JSONArray
                    array.acceptSearchPropertyNameValues(propertyName, this)
                    this.searchPropertyNameValueResult?.forEach {
                        valueList.add(it)
                    }
                }
            }
        } else if (visitable is JSONArray) {
            val fieldValues = visitable::class.dataClassFields.map { it.call(visitable) }
            val jsonArray = fieldValues[0] as List<*>
            jsonArray.forEach {
                if (it is JSONObject) {
                    it.fields.forEach{
                        if (it.first == propertyName) {
                            valueList.add(it.second)
                        }
                    }
                } else if (it is JSONArray) {
                    it.acceptSearchPropertyNameValues(propertyName, this)
                    this.searchPropertyNameValueResult?.forEach {
                        valueList.add(it)
                    }
                }
            }
        }
        searchPropertyNameValueResult = valueList
    }

    override fun visitSearchPropertyNameObjects(propertyNames: List<String>, visitable: Visitable) {
        val objectList = mutableListOf<Any?>()
        if (visitable is JSONObject) {
            val fieldValues = visitable::class.dataClassFields.map { it.call(visitable) }
            val valuesList = fieldValues[0] as List<Pair<String,Any>>
            val names = valuesList.map { it.first }
            if (names.containsAll(propertyNames)) {
                objectList.add(visitable)
            }
            valuesList.map {
                if (it.second is JSONArray) {
                    val array = it.second as JSONArray
                    array.acceptSearchPropertyNameObjects(propertyNames, this)
                    this.searchPropertyNameObjectsResult?.forEach {
                        objectList.add(it)
                    }
                }
            }
        } else if (visitable is JSONArray) {
            val fieldValues = visitable::class.dataClassFields.map { it.call(visitable) }
            val jsonArray = fieldValues[0] as List<*>
            jsonArray.forEach {
                if (it is JSONObject) {
                    val names = it.fields.map { it.first }
                    if (names.containsAll(propertyNames)) {
                        objectList.add(it)
                    }
                } else if (it is JSONArray) {
                    it.acceptSearchPropertyNameObjects(propertyNames, this)
                    this.searchPropertyNameObjectsResult?.forEach {
                        objectList.add(it)
                    }
                }
            }
        }
        searchPropertyNameObjectsResult = objectList
    }

    override fun visitVerifyStructure(propertyName: String, visitable: Visitable) {
        val valueList = mutableListOf<Any?>()
        if (visitable is JSONObject) {
            val fieldValues = visitable::class.dataClassFields.map { it.call(visitable) }
            val valuesList = fieldValues[0] as List<Pair<String,Any>>
            valuesList.map {
                if (it.first == propertyName) {
                    valueList.add(it.second)
                }
                if (it.second is JSONArray) {
                    val array = it.second as JSONArray
                    array.acceptVerifyStructure(propertyName, this)
                    this.verifyStructureResult?.forEach {
                        valueList.add(it)
                    }
                }
            }
        } else if (visitable is JSONArray) {
            val fieldValues = visitable::class.dataClassFields.map { it.call(visitable) }
            val jsonArray = fieldValues[0] as List<*>
            jsonArray.forEach {
                if (it is JSONObject) {
                    it.fields.forEach {
                        if (it.first == propertyName) {
                            valueList.add(it.second)
                        }
                    }
                } else if (it is JSONArray) {
                    it.acceptVerifyStructure(propertyName, this)
                    this.verifyStructureResult?.forEach {
                        valueList.add(it)
                    }
                }
            }
        }
        verifyStructureResult = valueList
    }
}

fun searchPropertyNameValues(propertyName: String, json: JSONObject): MutableList<Any?>? {
    val visitor = ProjectVisitor()
    json.acceptSearchPropertyNameValues(propertyName, visitor)
    return visitor.searchPropertyNameValueResult
}

fun searchPropertyNameObjects(propertyNames: List<String>, json: JSONObject): MutableList<Any?>? {
    val visitor = ProjectVisitor()
    json.acceptSearchPropertyNameObjects(propertyNames, visitor)
    return visitor.searchPropertyNameObjectsResult
}

fun verifyStructure(propertyName: String, json: JSONObject): Boolean {
    val visitor = ProjectVisitor()
    json.acceptVerifyStructure(propertyName, visitor)

    if (visitor.verifyStructureResult?.get(0) is JSONArray) {
        val array = visitor.verifyStructureResult?.get(0) as JSONArray
        val types = array.fields.map { it.javaClass }
        if (types.distinct().size == 1) {
            return true
        }
    } else {
        val types = visitor.verifyStructureResult?.map { it?.javaClass }
        if (types != null) {
            if (types.distinct().size == 1) {
                return true
            }
        }
    }
    return false
}

fun createJson(json: JSONObject): String {
    val visitor = ProjectVisitor();
    json.acceptCreateJson(visitor)
    return "{\n" + visitor.createJsonResult + "}"
}

val KClass<*>.dataClassFields: List<KProperty<*>>
    get() {
        require(isData) { "instance must be data class" }
        return primaryConstructor!!.parameters.map { p ->
            declaredMemberProperties.find { it.name == p.name }!!
        }
    }
