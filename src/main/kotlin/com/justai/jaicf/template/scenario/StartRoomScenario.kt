package com.justai.jaicf.template.scenario

import com.justai.jaicf.context.ActionContext
import com.justai.jaicf.model.scenario.Scenario
import com.justai.jaicf.template.state.*

object StartRoomScenario : Scenario() {

    const val state = "/location/start_room"
    private const val window = "/location/window"
    private const val door = "/location/door"
    private const val things = "/location/things"

    private fun ActionContext.handleSmoke() {
        StartLocationSmoke.handleSmoke(this)
    }

    init {
        state(state) {
            action {
                handleSmoke()
            }

            state("call firefighters") {
                activators {
                    intent("CallFirefighters")
                }

                action {
                    reactions.say("Да, пожарных я уже вызвал, но когда они ещё приедут, а мне нужно что-то делать…")
                    checkpoints.CallFirefighters = true
                    checkpoints.KeepCalm = true
                    handleSmoke()
                    reactions.go(window)
                }
            }

            state("keep calm") {
                activators {
                    intent("KeepCalm")
                }

                action {
                    reactions.say("Вдох-выдох, вдох-выдох")
                    context.checkpoints.CallFirefighters = false
                    context.checkpoints.KeepCalm = true
                    handleSmoke()
                    reactions.go(window)
                }

            }

            fallback {
                handleSmoke()
                reactions.say("АААААААААа!!!!!!")
                context.checkpoints.CallFirefighters = false
                context.checkpoints.KeepCalm = false
                reactions.go(window)
            }
        }

        fun ActionContext.openWindow() {
            reactions.say("Кажется, стало только хуже, дыма резко стало больше, кажется, я даже вижу огонёк.")
            context.checkpoints.OpenWindow = true
            StartLocationSmoke.increaseVelocity(this)
            handleSmoke()
            reactions.go(door)
        }

        state(window) {
            action {
                reactions.say("Думаю открыть окно. Стоит?")
            }

            state("No") {
                activators {
                    intent("No")
                }
                action {
                    reactions.say("Отлично, понял.")
                    context.checkpoints.OpenWindow = false
                    handleSmoke()
                    reactions.go(door)
                }
            }

            state("Yes") {
                activators {
                    intent("Yes")
                }
                action {
                    openWindow()
                }
            }

            fallback {
                reactions.say("Открыл.")
                openWindow()
            }
        }

        state(door) {
            action {
                reactions.say("Надо выбираться отсюда, сейчас только дверь открою.")
            }

            state("Check doorknob") {
                activators {
                    intent("CheckDoorknob")
                }

                action {
                    reactions.say("Сейчас проверю")
                    context.checkpoints.CheckDoorknob = true
                    reactions.go(things)
                }
            }

            fallback {
                context.checkpoints.CheckDoorknob = false
                handleSmoke()
                reactions.go(things)
            }
        }

        state(things) {
            action {
                reactions.say("Стоп, надо собрать с собой учебники библиотечные и конспекты, новые джинсы, а вдруг сгорят? Так, где тут чемодан…")
            }

            state("No") {
                activators {
                    intent("No")
                }
                action {
                    context.checkpoints.GetExtraClothes = false
                    handleSmoke()
                    reactions.go(StartFloorScenario.state)
                }
            }

            fallback {
                context.checkpoints.GetExtraClothes = true
                handleSmoke()
                reactions.go(StartFloorScenario.state)
            }
        }
    }
}