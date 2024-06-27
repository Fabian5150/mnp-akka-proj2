package com.expression;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class FormatterReciever extends AbstractBehavior<FormatterCont.Message> {

    private final ActorRef<PrintAndEvaluate.Message> mainSystem;
    public static Behavior<FormatterCont.Message> create(ActorRef<PrintAndEvaluate.Message> main) {
        return Behaviors.setup(context -> new FormatterReciever(context, main));
    }


    private FormatterReciever(ActorContext<FormatterCont.Message> context, ActorRef<PrintAndEvaluate.Message> main) {
        super(context);
        this.mainSystem=main;
    }

    @Override
    public Receive<FormatterCont.Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(FormatterCont.Calc.class, this::onExampleMessage)
                .build();
    }

    private Behavior<FormatterCont.Message> onExampleMessage(FormatterCont.Calc msg) {
        //getContext().getLog().info("Got: {}",msg.val());
        this.mainSystem.tell(new PrintAndEvaluate.FormatterResult(msg.val()));
        return this;
    }
}
