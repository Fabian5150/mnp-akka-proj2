package com.expression;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class FormatterReciever extends AbstractBehavior<FormatterCont.Message> {


    public static Behavior<FormatterCont.Message> create() {
        return Behaviors.setup(FormatterReciever::new);
    }


    private FormatterReciever(ActorContext<FormatterCont.Message> context) {
        super(context);
    }

    @Override
    public Receive<FormatterCont.Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(FormatterCont.Calc.class, this::onExampleMessage)
                .build();
    }

    private Behavior<FormatterCont.Message> onExampleMessage(FormatterCont.Calc msg) {
        getContext().getLog().info("Got: {}",msg.val());
        return this;
    }
}
