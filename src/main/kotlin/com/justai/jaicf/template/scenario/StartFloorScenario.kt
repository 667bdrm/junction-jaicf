package com.justai.jaicf.template.scenario

import com.justai.jaicf.channel.jaicp.channels.TelephonyEvents
import com.justai.jaicf.context.ActionContext
import com.justai.jaicf.helpers.ssml.audio
import com.justai.jaicf.model.scenario.Scenario
import com.justai.jaicf.template.delayS
import com.justai.jaicf.template.fallbackOrSilence
import com.justai.jaicf.template.state.EmergencyPaths
import com.justai.jaicf.template.state.StartLocationSmoke
import com.justai.jaicf.template.state.checkpoints

object StartFloorScenario : Scenario() {

    const val state = "/location/start_floor"
    private const val choosePath = "/location/choosePath"
    private const val elevator = "/location/elevator"
    private const val redButton = "/location/redButton"
    private const val doorClosing = "/location/doorClosing"
    private const val doorOutside = "/location/doorOutside"

    private fun ActionContext.handleSmoke() {
        StartLocationSmoke.handleSmoke(this)
    }

    init {
        state(state) {
            action {
                reactions.go(doorOutside)
            }
        }

        state(doorOutside) {
            action {
                reactions.say("Вышел, дверь закрыть или оставить?")
            }

            state("Yes") {
                activators {
                    intent("Yes")
                    intent("CloseDoor")
                }

                action {
                    context.checkpoints.LeaveDoorClosed = true
                    handleSmoke()
                    reactions.go(doorClosing)
                }
            }

            fallbackOrSilence {
                context.checkpoints.LeaveDoorClosed = false
                handleSmoke()
                reactions.go(choosePath)
            }
        }

        state(doorClosing) {
            action {
                reactions.say("Хорошо. Только найду ключ. Где же он?")
            }

            state("No") {
                activators {
                    intent("No") // TODO add more intents
                }

                action {
                    context.checkpoints.DoorClosedByKey = false
                    handleSmoke()
                    reactions.go(choosePath)
                }
            }

            fallbackOrSilence {
                context.checkpoints.DoorClosedByKey = true
                handleSmoke()
                reactions.go(choosePath)
            }
        }

        state(choosePath) {
            action {
                reactions.say("Окей, я в коридоре, тут тоже дым. Непонятно, что и где горит, надо эвакуироваться. Лестница справа, лифты слева, куда бежать?")
            }

            state("Stair") {
                activators {
                    intent("Stairs")
                }

                action {
                    reactions.say("Да. Кажется, на инструктаже по безопасности нам говорили не пользоваться лифтом при пожаре, жаль, я всё прослушал. Бег+у.")
                    context.checkpoints.ChooseEmergencyPath = EmergencyPaths.Stair
                    handleSmoke()
                    reactions.go(redButton)
                }
            }

            fallbackOrSilence {
                reactions.say("Побежал к лифтам")
                context.checkpoints.ChooseEmergencyPath = EmergencyPaths.Elevator
                handleSmoke()
                reactions.go(elevator)
            }
        }

        state(elevator) {
            action {
                reactions.say("Окей, я добежал до лифтов и вызвал.")
                reactions.say("${delayS(5)}Похоже, лифты отключились, и я только зря потратил время. Побегу на лестницу.")
                context.checkpoints.KeepCalm = false
                handleSmoke()
                reactions.go(redButton)
            }
        }

        state(redButton) {
            action {
                reactions.say("Тут на стене какая-то красная кнопка под стеклом. Нажать или не тратить время?")
            }

            state("Push it!") {
                activators {
                    intent("PressAlarm")
                }
                action {
                    reactions.say(audio("https://248305.selcdn.ru/demo_bot_static/Keep_talking_сигнализация3с.wav"))
                    reactions.say("Включилась пожарная тревога")
                    context.checkpoints.ActivateFireAlarm = true
                    handleSmoke()
                    reactions.go(StairsLocationScenario.state)
                }
            }

            fallbackOrSilence {
                context.checkpoints.ActivateFireAlarm = false
                handleSmoke()
                reactions.go(StairsLocationScenario.state)
            }
        }
    }
}
