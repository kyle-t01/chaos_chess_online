package com.chaoschessonline.chaoschessonline.util


import com.chaoschessonline.chaoschessonline.model.EventType
import com.chaoschessonline.chaoschessonline.model.Event
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage

/* singleton class that manages conversion between Event <-> JSON <-> TextMessage */
@Component
class JsonMapper {
    private val mapper = jacksonObjectMapper()

    // helper function to parse Text Message to Event
    fun readTextMessage(message: TextMessage): Event {
        val json = mapper.readTree(message.payload)
        // json = { type: "", data: {} }
        val typeStr:String = json.get("type").asText().uppercase()
        val type = EventType.valueOf(typeStr)
        val data = json.get("data").asText()
        return Event(type, data)
    }

    // convert Event to TextMessage
    fun convertToTextMessage(event: Event) : TextMessage {
        val json = mapper.writeValueAsString(event)
        return TextMessage(json)
    }
}