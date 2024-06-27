package com.expression;

import akka.actor.typed.ActorSystem;

import java.io.IOException;
public class AkkaStart {
  public static void main(String[] args) {
    final ActorSystem<PrintAndEvaluate.Message> messageMain = ActorSystem.create(PrintAndEvaluate.create(), "akkaMainSystem");

    messageMain.tell(new PrintAndEvaluate.Create());

    try {
      System.out.println(">>> Press ENTER to exit <<<");
      System.in.read();
    } catch (IOException ignored) {
    } finally {
      messageMain.terminate();
    }
  }
}

