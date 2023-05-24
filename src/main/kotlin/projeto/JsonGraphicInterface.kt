package projeto

import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.event.*
import javax.swing.*

fun main() {
    Editor().open()
}

fun initialJson(): JSONObject{
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

    return json
}

class Editor {
    private val commandList = mutableListOf<JSONObject>()

    val frame = JFrame("JSON Graphic Interface Project").apply {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        layout = GridLayout(0, 2)
        size = Dimension(600, 600)

        val left = JPanel()
        left.layout = GridLayout()
        val scrollPane = JScrollPane(objectPanel()).apply {
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        }
        left.add(scrollPane)
        add(left)

        val right = JPanel()
        right.layout = GridLayout()
        val srcArea = JTextArea()
        srcArea.tabSize = 2
        srcArea.isEditable = false
        srcArea.text = ""
        val scrollText = JScrollPane(srcArea).apply {
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        }
        val rightPanel = JPanel()
        rightPanel.layout = BorderLayout()
        val button = JButton("Undo")
        button.addActionListener { undo() }

        rightPanel.add(button, BorderLayout.PAGE_START)
        rightPanel.add(scrollText, BorderLayout.CENTER)

        right.add(rightPanel)
        add(right)
    }

    fun open() {
        generateJsonPanels(initialJson())
        updateJson()
        frame.isVisible = true
    }

    fun updateJson() {
        val right = frame.contentPane.getComponent(1) as JPanel
        val panel = right.getComponent(0) as JPanel
        val button = panel.getComponent(0) as JButton
        val scrollPane = panel.getComponent(1) as JScrollPane
        val rightPanel = scrollPane.viewport.view as JTextArea

        val jsonObject = generateJsonObject()
        rightPanel.text = createJson(jsonObject)

        val lastObject = commandList.lastOrNull()
        if (lastObject != jsonObject) {
            commandList.add(jsonObject)
            button.isEnabled = commandList.size > 1
        }
    }

    private fun undo() {
        commandList.removeLast()
        val lastObject = commandList.last()

        val right = frame.contentPane.getComponent(1) as JPanel
        val panel = right.getComponent(0) as JPanel
        val button = panel.getComponent(0) as JButton
        val scrollPane = panel.getComponent(1) as JScrollPane
        val rightPanel = scrollPane.viewport.view as JTextArea

        generateJsonPanels(lastObject)
        rightPanel.text = createJson(lastObject)

        button.isEnabled = commandList.size > 1
    }

    private fun textFieldTypeDifferentiator(text: String): Any?{
        if (text == "null" || text == "") {
            return null
        } else if(text == "true" || text == "false") {
            return text.toBoolean()
        } else if (text.toDoubleOrNull() != null){
            if (text.toIntOrNull() != null){
                return text.toInt()
            } else {
                return text.toDouble()
            }
        }
        return text
    }

    private fun generateJsonObject(): JSONObject {
        val final = JSONObject()

        val left = frame.contentPane.getComponent(0) as JPanel
        val scrollPane = left.getComponent(0) as JScrollPane
        val leftPanel = scrollPane.viewport.view as JPanel

        for (number in 0 until leftPanel.componentCount) {
            val component = leftPanel.getComponent(number) as JPanel
            val label = component.getComponent(0) as JLabel
            if (component.getComponent(1) is JTextField) {
                val textField = component.getComponent(1) as JTextField
                final.addProperty(label.text, textFieldTypeDifferentiator(textField.text))
            }
            if (component.getComponent(1) is JPanel) {
                val panel = component.getComponent(1) as JPanel
                val array = generateJsonArray(panel)
                final.addProperty(label.text, array)
            }
        }
        return final
    }

    private fun generateJsonArray(panel: JPanel): JSONArray{
        val final = JSONArray()

        val panelScrollPane = panel.getComponent(0) as JScrollPane
        val arrayPanel = panelScrollPane.viewport.view as JPanel

        for (number in 0 until arrayPanel.componentCount) {
            val panel = arrayPanel.getComponent(number) as JPanel
            if (panel.getComponent(1) is JTextField) {
                val textField = panel.getComponent(1) as JTextField
                final.add(textFieldTypeDifferentiator(textField.text)!!)
            }
            if (panel.getComponent(1) is JPanel) {
                val panel = panel.getComponent(1) as JPanel
                val panelScrollPane = panel.getComponent(0) as JScrollPane
                val arrayPanel = panelScrollPane.viewport.view as JPanel

                val result = JSONObject()
                for (number in 0 until arrayPanel.componentCount) {
                    val panel = arrayPanel.getComponent(number) as JPanel
                    val label = panel.getComponent(0) as JLabel
                    val textField = panel.getComponent(1) as JTextField
                    result.addProperty(label.text, textFieldTypeDifferentiator(textField.text))
                }
                final.add(result)
            }
        }
        return final
    }

    private fun generateJsonPanels(json: JSONObject) {
        val left = frame.contentPane.getComponent(0) as JPanel
        val scrollPane = left.getComponent(0) as JScrollPane
        val leftPanel = scrollPane.viewport.view as JPanel

        leftPanel.components.forEach {
            leftPanel.remove(it)
        }

        json.fields.forEach {
            if (it.second is JSONArray) {
                val panel = leftPanel.add(addArray(it.first)) as JPanel
                val panelPane = panel.getComponent(1) as JPanel
                val panelScrollPane = panelPane.getComponent(0) as JScrollPane
                val arrayPanel = panelScrollPane.viewport.view as JPanel

                addJsonArray(arrayPanel, it.second as JSONArray)
            } else {
                leftPanel.add(addProperty(it.first, it.second?.toString() ?: "null"))
            }
        }
    }

    private fun addJsonArray(arrayPanel: JPanel, array: JSONArray) {
        array.fields.forEach{
            when (it) {
                is JSONObject -> {
                    val panel = arrayPanel.add(addObject()) as JPanel
                    addJsonObjects(panel, it)
                }
                is JSONArray -> {
                    val panel = arrayPanel.add(addArray(" ")) as JPanel
                    val panelPane = panel.getComponent(1) as JPanel
                    val panelScrollPane = panelPane.getComponent(0) as JScrollPane
                    val arrayPanel = panelScrollPane.viewport.view as JPanel

                    addJsonArray(arrayPanel, it)
                }
                else -> {
                    arrayPanel.add(addValue(it.toString()))
                }
            }
        }
    }

    private fun addJsonObjects(panel: JPanel, it: JSONObject) {
        val panelPane = panel.getComponent(1) as JPanel
        val panelScrollPane = panelPane.getComponent(0) as JScrollPane
        val objectPanel = panelScrollPane.viewport.view as JPanel

        it.fields.forEach{
            objectPanel.add(addProperty(it.first, it.second?.toString() ?: "null"))
        }
    }

    private fun objectPanel(): JPanel =
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
            alignmentY = Component.TOP_ALIGNMENT

            // menu
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        val menu = JPopupMenu("Message")
                        val addProperty = JButton("Add Property")
                        addProperty.addActionListener {
                            val name = JOptionPane.showInputDialog(null, "Name",
                                "Add Property", JOptionPane.QUESTION_MESSAGE);
                            if (name != null) {
                                add(addProperty(name, ""))
                            }
                            menu.isVisible = false
                            revalidate()
                            frame.repaint()
                        }
                        val addArray = JButton("Add Array")
                        addArray.addActionListener {
                            val name = JOptionPane.showInputDialog(null, "Name",
                                "Add Array", JOptionPane.QUESTION_MESSAGE);
                            if (name != null) {
                                add(addArray(name))
                            }
                            menu.isVisible = false
                            revalidate()
                            frame.repaint()
                        }
                        val del = JButton("Delete all")
                        del.addActionListener {
                            components.forEach {
                                remove(it)
                            }
                            menu.isVisible = false
                            revalidate()
                            frame.repaint()
                        }
                        menu.add(addProperty)
                        menu.add(addArray)
                        menu.add(del)
                        menu.show(this@apply, 10, 10)
                    }
                    updateJson()
                }
            })
        }

    private fun arrayPanel(): JPanel =
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
            alignmentY = Component.TOP_ALIGNMENT

            // array menu
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        val menu = JPopupMenu("Message")
                        val addValue = JButton("Add Value")
                        addValue.addActionListener {
                            val value = JOptionPane.showInputDialog(null, "Value",
                                "Add Value", JOptionPane.QUESTION_MESSAGE);
                            if (value != null) {
                                add(addValue(value))
                            }
                            menu.isVisible = false
                            revalidate()
                            frame.repaint()
                        }
                        val addObject = JButton("Add Object")
                        addObject.addActionListener {
                            add(addObject())
                            menu.isVisible = false
                            revalidate()
                            frame.repaint()
                        }
                        val del = JButton("Delete all")
                        del.addActionListener {
                            components.forEach {
                                remove(it)
                            }
                            menu.isVisible = false
                            revalidate()
                            frame.repaint()
                        }
                        menu.add(addValue)
                        menu.add(addObject)
                        menu.add(del)
                        menu.show(this@apply, 10, 10)
                    }
                    updateJson()
                }
            })
        }

    fun addObject(): JPanel =
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
            alignmentY = Component.TOP_ALIGNMENT

            add(JLabel("  "))

            val panel = JPanel()
            panel.layout = GridLayout()
            val scrollPane = JScrollPane(objectPanel())
            panel.add(scrollPane)
            add(panel)
        }

    fun addArray(name: String): JPanel =
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
            alignmentY = Component.TOP_ALIGNMENT

            add(JLabel(name))

            val panel = JPanel()
            panel.layout = GridLayout()
            val scrollPane = JScrollPane(arrayPanel())
            panel.add(scrollPane)
            add(panel)
        }

    fun addValue(value: String): JPanel =
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
            alignmentY = Component.TOP_ALIGNMENT

            add(JLabel("  "))

            val text = JTextField(value)
            text.addFocusListener(object : FocusAdapter() {
                override fun focusLost(e: FocusEvent) {
                    updateJson()
                }
            })
            add(text)
        }

    fun addProperty(key: String, value: String): JPanel =
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
            alignmentY = Component.TOP_ALIGNMENT

            add(JLabel(key))

            val text = JTextField(value)
            text.addFocusListener(object : FocusAdapter() {
                override fun focusLost(e: FocusEvent) {
                    updateJson()
                }
            })
            add(text)
        }
}






