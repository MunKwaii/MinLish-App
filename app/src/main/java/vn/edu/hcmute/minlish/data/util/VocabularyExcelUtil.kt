package vn.edu.hcmute.minlish.data.util

import android.content.Context
import android.net.Uri
import org.w3c.dom.Document
import org.w3c.dom.Element
import vn.edu.hcmute.minlish.data.local.entity.Word
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.xml.parsers.DocumentBuilderFactory

object VocabularyExcelUtil {

    // Đọc file .xlsx, lấy cột đầu tiên làm danh sách word để import
    fun readWordsFromExcel(
        context: Context,
        uri: Uri
    ): List<String> {
        val entries = mutableMapOf<String, String>()

        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Không thể mở file Excel")

        inputStream.use { input ->
            ZipInputStream(input).use { zip ->
                var entry = zip.nextEntry

                while (entry != null) {
                    if (!entry.isDirectory) {
                        when (entry.name) {
                            "xl/sharedStrings.xml",
                            "xl/worksheets/sheet1.xml" -> {
                                entries[entry.name] = zip.readBytes().toString(Charsets.UTF_8)
                            }
                        }
                    }

                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        }

        val sheetXml = entries["xl/worksheets/sheet1.xml"]
            ?: throw IllegalArgumentException("Không tìm thấy sheet đầu tiên trong file Excel")

        val sharedStrings = parseSharedStrings(entries["xl/sharedStrings.xml"])

        return parseFirstColumnWords(
            sheetXml = sheetXml,
            sharedStrings = sharedStrings
        ).distinct()
    }

    // Xuất danh sách word ra file .xlsx
    fun writeWordsToExcel(
        context: Context,
        uri: Uri,
        words: List<Word>
    ) {
        val outputStream = context.contentResolver.openOutputStream(uri)
            ?: throw IllegalArgumentException("Không thể tạo file Excel")

        outputStream.use { output ->
            ZipOutputStream(output).use { zip ->
                zip.writeEntry("[Content_Types].xml", contentTypesXml())
                zip.writeEntry("_rels/.rels", rootRelsXml())
                zip.writeEntry("xl/workbook.xml", workbookXml())
                zip.writeEntry("xl/_rels/workbook.xml.rels", workbookRelsXml())
                zip.writeEntry("xl/styles.xml", stylesXml())
                zip.writeEntry("xl/worksheets/sheet1.xml", sheetXml(words))
            }
        }
    }

    private fun parseSharedStrings(xml: String?): List<String> {
        if (xml.isNullOrBlank()) return emptyList()

        val document = parseXml(xml)
        val siNodes = document.getElementsByTagName("si")
        val result = mutableListOf<String>()

        for (i in 0 until siNodes.length) {
            val siElement = siNodes.item(i) as? Element ?: continue
            val textNodes = siElement.getElementsByTagName("t")

            val text = buildString {
                for (j in 0 until textNodes.length) {
                    append(textNodes.item(j).textContent)
                }
            }

            result.add(text)
        }

        return result
    }

    private fun parseFirstColumnWords(
        sheetXml: String,
        sharedStrings: List<String>
    ): List<String> {
        val document = parseXml(sheetXml)
        val rows = document.getElementsByTagName("row")
        val words = mutableListOf<String>()

        for (i in 0 until rows.length) {
            val row = rows.item(i) as? Element ?: continue
            val cells = row.getElementsByTagName("c")

            for (j in 0 until cells.length) {
                val cell = cells.item(j) as? Element ?: continue
                val cellRef = cell.getAttribute("r")

                if (!cellRef.startsWith("A", ignoreCase = true)) {
                    continue
                }

                val value = getCellValue(cell, sharedStrings).trim()

                if (value.isBlank()) {
                    break
                }

                if (words.isEmpty() && value.equals("word", ignoreCase = true)) {
                    break
                }

                words.add(value)
                break
            }
        }

        return words
    }

    private fun getCellValue(
        cell: Element,
        sharedStrings: List<String>
    ): String {
        val type = cell.getAttribute("t")

        return when (type) {
            "s" -> {
                val indexText = cell.getFirstTagText("v")
                val index = indexText.toIntOrNull()
                if (index != null) sharedStrings.getOrNull(index).orEmpty() else ""
            }

            "inlineStr" -> {
                cell.getFirstTagText("t")
            }

            else -> {
                cell.getFirstTagText("v")
            }
        }
    }

    private fun sheetXml(words: List<Word>): String {
        val headers = listOf(
            "word",
            "pronunciation",
            "meaning",
            "description",
            "example",
            "collocations",
            "relatedWords",
            "note"
        )

        val rows = StringBuilder()

        rows.append(rowXml(1, headers))

        words.forEachIndexed { index, word ->
            val rowIndex = index + 2

            rows.append(
                rowXml(
                    rowIndex = rowIndex,
                    values = listOf(
                        word.word,
                        word.pronunciation,
                        word.meaning,
                        word.description.orEmpty(),
                        word.example.orEmpty(),
                        word.collocations.orEmpty(),
                        word.relatedWords.orEmpty(),
                        word.note.orEmpty()
                    )
                )
            )
        }

        return """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
                <sheetData>
                    $rows
                </sheetData>
            </worksheet>
        """.trimIndent()
    }

    private fun rowXml(
        rowIndex: Int,
        values: List<String>
    ): String {
        val cells = values.mapIndexed { index, value ->
            val cellRef = "${columnName(index)}$rowIndex"
            val safeValue = escapeXml(value)

            """
                <c r="$cellRef" t="inlineStr">
                    <is>
                        <t>$safeValue</t>
                    </is>
                </c>
            """.trimIndent()
        }.joinToString("")

        return """
            <row r="$rowIndex">
                $cells
            </row>
        """.trimIndent()
    }

    private fun columnName(index: Int): String {
        return when (index) {
            0 -> "A"
            1 -> "B"
            2 -> "C"
            3 -> "D"
            4 -> "E"
            5 -> "F"
            6 -> "G"
            7 -> "H"
            else -> "A"
        }
    }

    private fun parseXml(xml: String): Document {
        val factory = DocumentBuilderFactory.newInstance()

        try {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false)
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
        } catch (_: Exception) {
        }

        return factory
            .newDocumentBuilder()
            .parse(xml.byteInputStream())
    }

    private fun Element.getFirstTagText(tagName: String): String {
        val nodes = getElementsByTagName(tagName)
        if (nodes.length == 0) return ""
        return nodes.item(0).textContent.orEmpty()
    }

    private fun ZipOutputStream.writeEntry(
        name: String,
        content: String
    ) {
        putNextEntry(ZipEntry(name))
        write(content.toByteArray(Charsets.UTF_8))
        closeEntry()
    }

    private fun escapeXml(value: String): String {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun contentTypesXml(): String {
        return """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                <Default Extension="xml" ContentType="application/xml"/>
                <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
                <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
                <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
            </Types>
        """.trimIndent()
    }

    private fun rootRelsXml(): String {
        return """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                <Relationship 
                    Id="rId1" 
                    Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" 
                    Target="xl/workbook.xml"/>
            </Relationships>
        """.trimIndent()
    }

    private fun workbookXml(): String {
        return """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
                      xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
                <sheets>
                    <sheet name="Vocabulary" sheetId="1" r:id="rId1"/>
                </sheets>
            </workbook>
        """.trimIndent()
    }

    private fun workbookRelsXml(): String {
        return """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                <Relationship 
                    Id="rId1" 
                    Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" 
                    Target="worksheets/sheet1.xml"/>
                <Relationship 
                    Id="rId2" 
                    Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" 
                    Target="styles.xml"/>
            </Relationships>
        """.trimIndent()
    }

    private fun stylesXml(): String {
        return """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
                <fonts count="1">
                    <font>
                        <sz val="11"/>
                        <name val="Calibri"/>
                    </font>
                </fonts>
                <fills count="1">
                    <fill>
                        <patternFill patternType="none"/>
                    </fill>
                </fills>
                <borders count="1">
                    <border/>
                </borders>
                <cellStyleXfs count="1">
                    <xf/>
                </cellStyleXfs>
                <cellXfs count="1">
                    <xf/>
                </cellXfs>
            </styleSheet>
        """.trimIndent()
    }
}