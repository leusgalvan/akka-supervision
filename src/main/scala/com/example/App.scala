package com.example

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, DeathPactException, Props, SupervisorStrategy}

object Messages {
  case object Message
  case object HeartBeat
  case class SetStrategy(supervisorStrategy: SupervisorStrategy)
  case class AddDelegate(delegateProps: Props, actorName: String)
}

object Actors {
  class ErrorActor extends Actor {
    def receive: Receive = {
      case _ => throw new Error("Error")
    }
  }

  class ExceptionActor extends Actor {
    def receive: Receive = {
      case _ => throw new Exception("Exception")
    }
  }

  class DeathPactActor extends Actor {
    def receive: Receive = {
      case _ => throw DeathPactException(self)
    }
  }

  class TopLevelActor extends Actor with ActorLogging {

    import Messages._

    private var _supervisorStrategy = SupervisorStrategy.defaultStrategy
    private var _delegates = Set.empty[ActorRef]

    override def supervisorStrategy: SupervisorStrategy = _supervisorStrategy

    def receive: Receive = {
      case HeartBeat => log.info("{} alive!", self.path)

      case msg@Message => _delegates foreach {_ ! msg}

      case SetStrategy(supervisorStrategy) =>
        log.info("Setting strategy: {}", supervisorStrategy)
        _supervisorStrategy = supervisorStrategy


      case AddDelegate(delegateProps, actorName) =>
        log.info("Setting delegate: {}", actorName)
        _delegates += context.actorOf(delegateProps, actorName)
    }
  }
}

object App {
  import Messages._
  import Actors._

  def main(args: Array[String]): Unit = {
    val system = ActorSystem("strategies-system")
    val topLevelActor = system.actorOf(Props[TopLevelActor], "top-level-actor")

    topLevelActor ! AddDelegate(Props[DeathPactActor], "death-pact-actor")
    topLevelActor ! AddDelegate(Props[ExceptionActor], "exception-actor")
    topLevelActor ! AddDelegate(Props[ErrorActor], "error-actor")
    topLevelActor ! SetStrategy(SupervisorStrategy.defaultStrategy)
    topLevelActor ! HeartBeat
    topLevelActor ! Message
    topLevelActor ! HeartBeat
    topLevelActor ! Message
    topLevelActor ! HeartBeat

    val systemTTL = 3000
    Thread.sleep(systemTTL)
    system.terminate()
  }
}
