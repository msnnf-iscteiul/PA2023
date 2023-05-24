import projeto.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ProjetoTest {
    @Test
    fun testSearchPropertyNameValues(){
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

        val json = JSONObject()
        json.addProperty("uc", "PA")
        json.addProperty("ects", 6.0)
        json.addProperty("data-exame", null)
        json.addProperty("inscritos", inscritosArray)

        val expected = listOf(101101, 101102, 26503)
        Assertions.assertEquals(expected, searchPropertyNameValues("numero", json))
    }

    @Test
    fun testSearchPropertyNameObjects(){
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

        val json = JSONObject()
        json.addProperty("uc", "PA")
        json.addProperty("ects", 6.0)
        json.addProperty("data-exame", null)
        json.addProperty("inscritos", inscritosArray)

        val expected = listOf(aluno1, aluno2, aluno3)
        Assertions.assertEquals(expected, searchPropertyNameObjects(listOf("numero", "nome"), json))
    }

    @Test
    fun testVerifyStructure(){
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

        val json = JSONObject()
        json.addProperty("uc", "PA")
        json.addProperty("ects", 6.0)
        json.addProperty("data-exame", null)
        json.addProperty("inscritos", inscritosArray)

        Assertions.assertTrue(verifyStructure("numero", json))
        Assertions.assertTrue(verifyStructure("inscritos", json))
    }

    @Test
    fun testCreateJson(){
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

        val expected ="{\n\"uc\" : \"PA\",\n\"ects\" : 6.0,\n\"data-exame\" : null,\n\"inscritos\" : [\n{\n\"numero\" : 101101,\n\"nome\" : \"Dave Farley\",\n\"internacional\" : true\n},\n{\n\"numero\" : 101102,\n\"nome\" : \"Martin Fowler\",\n\"internacional\" : true\n},\n{\n\"numero\" : 26503,\n\"nome\" : \"André Santos\",\n\"internacional\" : false\n}\n]\n}"
        Assertions.assertEquals(expected, createJson(json))
    }
}