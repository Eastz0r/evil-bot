package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.service.TimeService
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.types.ExtendedBot
import dev.inmo.tgbotapi.types.message.CommonMessageImpl
import dev.inmo.tgbotapi.types.message.abstracts.ContentMessage
import dev.inmo.tgbotapi.types.message.content.LocationContent
import org.springframework.stereotype.Component
import java.time.format.DateTimeFormatter

@Component
class CurrentTimeHandler(
    private val timeService: TimeService,
    private val requestsExecutor: RequestsExecutor,
    botInfo: ExtendedBot
) : CommandHandler(
    botInfo,
    command = arrayOf("time"),
    commandDescription = "Текущее время"
) {
    companion object {
        private val dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }

    override suspend fun handleCommand(message: CommonMessageImpl<*>, args: String?) {
        val replyMessage = message.replyTo as? ContentMessage<*>
        val locationContent = replyMessage?.content as? LocationContent

        var defaultLocationChosen = false

        val timeForLocation = if (locationContent !== null) {
            val (longitude, latitude) = locationContent.location
            timeService.getTimeForLocation(latitude, longitude)
        } else if (!args.isNullOrBlank()) {
            timeService.getTimeForLocation(args)
        } else {
            defaultLocationChosen = true
            timeService.getTimeForLocation("Москва")
        }

        if (timeForLocation === null) {
            requestsExecutor.reply(message, "Не знаю такого")
            return
        }

        val formattedLocationTime = timeForLocation.format(dateTimeFormatter)

        requestsExecutor.reply(
            message,
            if (defaultLocationChosen) {
                "Ты не уточнил, поэтому вот результат для дефолт-сити: $formattedLocationTime"
            } else {
                formattedLocationTime
            }
        )
    }
}
